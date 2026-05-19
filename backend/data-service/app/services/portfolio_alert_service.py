"""포트폴리오 가치 변동 알림 서비스

- 08:50 KST: 장 시작 전 포트폴리오 가치 스냅샷 저장
- ETF 가격 갱신 후: 스냅샷 대비 변동률 체크
- 5% / 10% 이상 변동 시: RabbitMQ 이벤트 발행 (user-service → FCM 발송)
"""
import json
import logging
from decimal import Decimal, InvalidOperation

import redis.asyncio as aioredis

from app.config import get_settings

logger = logging.getLogger(__name__)

SNAPSHOT_KEY_PREFIX = "portfolio:snapshot:"
ALERT_FIRED_KEY_PREFIX = "portfolio:alert:fired:"
SNAPSHOT_TTL_SECONDS = 86400  # 24시간
THRESHOLDS = [10.0, 5.0]  # 높은 threshold 우선 체크


def _make_redis() -> aioredis.Redis:
    settings = get_settings()
    return aioredis.Redis(
        host=settings.redis_host,
        port=settings.redis_port,
        password=settings.redis_password or None,
        db=settings.redis_db,
        decode_responses=True,
    )


async def snapshot_portfolio_values():
    """장 시작 전(08:50) 기준 포트폴리오 가치 스냅샷 저장

    is_alert_enabled = true 인 포트폴리오를 대상으로
    현재 Redis ETF 캐시 가격 기준 포트폴리오 총 가치를 계산 후 저장.
    ETF 구성 정보도 함께 저장하여 check_portfolio_alerts() 에서 재조회 불필요.
    """
    from app.database import AsyncSessionLocal
    from sqlalchemy import text

    r = _make_redis()
    try:
        async with AsyncSessionLocal() as db:
            result = await db.execute(text("""
                SELECT
                    p.id        AS portfolio_id,
                    p.user_id,
                    p.name      AS portfolio_name,
                    e.stock_code AS ticker,
                    COALESCE(pe.etf_count, 1) AS etf_count
                FROM portfolio p
                JOIN portfolio_etf pe ON pe.portfolio_id = p.id
                JOIN etf e ON e.id = pe.etf_id AND e.is_active = true
            """))
            rows = result.fetchall()

        if not rows:
            logger.info("[포트폴리오 알림] 포트폴리오 없음 — 스냅샷 스킵")
            return

        # 포트폴리오별 ETF 목록 그룹화
        portfolio_map: dict[int, dict] = {}
        for row in rows:
            pid = row.portfolio_id
            if pid not in portfolio_map:
                portfolio_map[pid] = {
                    "user_id": row.user_id,
                    "name": row.portfolio_name,
                    "etfs": [],
                }
            portfolio_map[pid]["etfs"].append({
                "ticker": row.ticker,
                "count": str(row.etf_count),
            })

        saved = 0
        for pid, info in portfolio_map.items():
            total = Decimal("0")
            for etf in info["etfs"]:
                price_str = await r.hget(f"EtfCurrentInfo:{etf['ticker']}", "currentPrice")
                if price_str:
                    try:
                        total += Decimal(price_str) * Decimal(etf["count"])
                    except InvalidOperation:
                        pass

            if total <= 0:
                logger.debug(f"[포트폴리오 알림] portfolioId={pid} 캐시 가격 없음, 스냅샷 스킵")
                continue

            snapshot_key = f"{SNAPSHOT_KEY_PREFIX}{pid}"
            fired_key = f"{ALERT_FIRED_KEY_PREFIX}{pid}"

            # 스냅샷 저장 (ETF 구성 포함 — check 시 재조회 불필요)
            await r.hset(snapshot_key, mapping={
                "value": str(total),
                "userId": str(info["user_id"]),
                "name": info["name"],
                "etfs": json.dumps(info["etfs"]),
            })
            await r.expire(snapshot_key, SNAPSHOT_TTL_SECONDS)

            # 오늘의 알림 발송 기록 초기화
            await r.delete(fired_key)

            saved += 1

        logger.info(f"[포트폴리오 알림] 스냅샷 저장 완료: {saved}/{len(portfolio_map)}개")

    except Exception as e:
        logger.error(f"[포트폴리오 알림] 스냅샷 저장 실패: {e}")
    finally:
        await r.aclose()


async def check_portfolio_alerts():
    """ETF 가격 갱신 후 포트폴리오 변동률 체크 및 알림 발행

    run_etf_stock_cache_sync 완료 후 호출.
    스냅샷 대비 현재 가치 변동률이 5% 또는 10% 이상이면 RabbitMQ 이벤트 발행.
    하루에 동일 threshold 알림은 한 번만 발행 (10% 발행 시 5% 중복 발행 안 함).
    """
    from app.services.event_publisher import event_publisher

    r = _make_redis()
    try:
        snapshot_keys = await r.keys(f"{SNAPSHOT_KEY_PREFIX}*")
        if not snapshot_keys:
            return

        fired_count = 0
        for key in snapshot_keys:
            pid_str = key[len(SNAPSHOT_KEY_PREFIX):]
            try:
                portfolio_id = int(pid_str)
            except ValueError:
                continue

            snapshot = await r.hgetall(key)
            if not snapshot:
                continue

            snapshot_value_str = snapshot.get("value")
            user_id_str = snapshot.get("userId")
            portfolio_name = snapshot.get("name", "포트폴리오")
            etfs_json = snapshot.get("etfs")

            if not all([snapshot_value_str, user_id_str, etfs_json]):
                continue

            try:
                snapshot_value = Decimal(snapshot_value_str)
                user_id = int(user_id_str)
                etfs = json.loads(etfs_json)
            except (InvalidOperation, ValueError, json.JSONDecodeError):
                continue

            if snapshot_value <= 0:
                continue

            # 현재 포트폴리오 가치 계산
            current_total = Decimal("0")
            for etf in etfs:
                price_str = await r.hget(f"EtfCurrentInfo:{etf['ticker']}", "currentPrice")
                if price_str:
                    try:
                        current_total += Decimal(price_str) * Decimal(etf["count"])
                    except InvalidOperation:
                        pass

            if current_total <= 0:
                continue

            change_pct = float((current_total - snapshot_value) / snapshot_value * 100)
            abs_change = abs(change_pct)

            logger.info(f"[포트폴리오 알림] portfolioId={portfolio_id} snapshot={snapshot_value} current={current_total} change={change_pct:.4f}%")

            fired_key = f"{ALERT_FIRED_KEY_PREFIX}{portfolio_id}"

            # 10% → 5% 순서로 체크 (높은 threshold 우선, 하나만 발행)
            for threshold in THRESHOLDS:
                if abs_change < threshold:
                    continue

                threshold_str = str(int(threshold))
                already_fired = await r.sismember(fired_key, threshold_str)
                if already_fired:
                    break  # 이미 이 threshold 발행됨

                direction = "상승" if change_pct > 0 else "하락"
                published = await event_publisher.publish_portfolio_alert(
                    portfolio_id=portfolio_id,
                    user_id=user_id,
                    portfolio_name=portfolio_name,
                    change_rate=round(change_pct, 2),
                    threshold=int(threshold),
                    direction=direction,
                )

                if published:
                    # 발송 기록 저장 (당일 중복 방지)
                    await r.sadd(fired_key, threshold_str)
                    await r.expire(fired_key, SNAPSHOT_TTL_SECONDS)
                    fired_count += 1

                break  # 한 번에 하나의 threshold만 발행

        if fired_count > 0:
            logger.info(f"[포트폴리오 알림] {fired_count}건 알림 이벤트 발행")

    except Exception as e:
        logger.error(f"[포트폴리오 알림] 변동률 체크 실패: {e}")
    finally:
        await r.aclose()
