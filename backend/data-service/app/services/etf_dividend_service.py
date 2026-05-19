"""ETF 분배금 동기화 서비스"""
import asyncio
import logging
from typing import List, Optional

from decimal import Decimal
from sqlalchemy import select, delete, func, update
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.etf import ETF, EtfDividend
from app.scrapers.mirae_dividend_scraper import fetch_tiger_etf_data_batch, ticker_to_ksd_fund
from app.scrapers.samsung_dividend_scraper import fetch_kodex_etf_data_batch

logger = logging.getLogger(__name__)


async def _get_tiger_etfs(db: AsyncSession) -> List[dict]:
    """DB에서 Tiger ETF 목록 조회"""
    result = await db.execute(
        select(ETF.id, ETF.stock_code, ETF.isin, ETF.name, ETF.nav)
        .where(ETF.is_active == True)
        .where(ETF.name.ilike("TIGER%"))
    )
    return [
        {"id": row.id, "ticker": row.stock_code, "isin": row.isin, "name": row.name, "nav": float(row.nav) if row.nav else None}
        for row in result.all()
    ]


async def _get_kodex_etfs(db: AsyncSession) -> List[dict]:
    """DB에서 KODEX ETF 목록 조회"""
    result = await db.execute(
        select(ETF.id, ETF.stock_code, ETF.isin, ETF.name, ETF.nav)
        .where(ETF.is_active == True)
        .where(ETF.name.ilike("KODEX%"))
    )
    return [
        {"id": row.id, "ticker": row.stock_code, "isin": row.isin, "name": row.name, "nav": float(row.nav) if row.nav else None}
        for row in result.all()
    ]


async def _upsert_dividends(db: AsyncSession, etf_id: int, dividends: List[dict]) -> int:
    """분배금 데이터를 upsert (payment_date 기준 중복 무시)"""
    if not dividends:
        return 0

    rows = [
        {
            "etf_id": etf_id,
            "payment_date": d["payment_date"],
            "amount_per_unit": d["amount_per_unit"],
        }
        for d in dividends
    ]

    stmt = (
        insert(EtfDividend)
        .values(rows)
        .on_conflict_do_nothing(constraint="uq_etf_dividend_etf_date")
    )
    await db.execute(stmt)
    return len(rows)


async def _update_dividend_yield(db: AsyncSession, etf_id: int, nav: float) -> None:
    """최근 1년 분배금 합계 / NAV × 100 → ETF.dividend_yield 업데이트"""
    from datetime import date, timedelta
    one_year_ago = date.today() - timedelta(days=365)

    result = await db.execute(
        select(func.sum(EtfDividend.amount_per_unit))
        .where(EtfDividend.etf_id == etf_id)
        .where(EtfDividend.payment_date >= one_year_ago)
    )
    annual_dividend = result.scalar() or Decimal("0")

    if nav and nav > 0:
        dividend_yield = round(float(annual_dividend) / nav * 100, 3)
        await db.execute(
            update(ETF).where(ETF.id == etf_id).values(dividend_yield=dividend_yield)
        )
        logger.debug(f"[etf_id={etf_id}] dividend_yield={dividend_yield}% (연배당={annual_dividend}, NAV={nav})")


async def sync_etf_dividends(db: AsyncSession, ticker: Optional[str] = None) -> dict:
    """
    Tiger ETF 분배금 이력을 미래에셋 사이트에서 크롤링해 DB에 저장합니다.

    Args:
        db: AsyncSession
        ticker: 특정 종목만 동기화할 경우 지정. None이면 전체 Tiger ETF 대상.

    Returns:
        {"total_etfs": int, "total_rows": int, "errors": int}
    """
    if ticker:
        result = await db.execute(
            select(ETF.id, ETF.stock_code, ETF.isin, ETF.name, ETF.nav)
            .where(ETF.stock_code == ticker)
            .where(ETF.is_active == True)
        )
        row = result.first()
        if not row:
            logger.warning(f"ETF 없음: {ticker}")
            return {"total_etfs": 0, "total_rows": 0, "errors": 0}
        etf_list = [{"id": row.id, "ticker": row.stock_code, "isin": row.isin, "name": row.name, "nav": float(row.nav) if row.nav else None}]
    else:
        etf_list = await _get_tiger_etfs(db)

    if not etf_list:
        logger.info("동기화 대상 Tiger ETF 없음")
        return {"total_etfs": 0, "total_rows": 0, "errors": 0}

    logger.info(f"Tiger ETF 분배금 동기화 시작: {len(etf_list)}개")

    # DB isin 있으면 사용, 없으면 ticker_to_ksd_fund 로 계산
    etf_input = [
        {
            "ticker": e["ticker"],
            "isin": e.get("isin") or ticker_to_ksd_fund(e["ticker"]),
            "name": e["name"],
        }
        for e in etf_list
    ]
    etf_data_map = await fetch_tiger_etf_data_batch(etf_input)

    total_rows = 0
    errors = 0

    for etf in etf_list:
        ticker_key = etf["ticker"]
        etf_data = etf_data_map.get(ticker_key, {})
        dividends = etf_data.get("dividends", [])
        expense_ratio = etf_data.get("expense_ratio")

        try:
            if dividends:
                inserted = await _upsert_dividends(db, etf["id"], dividends)
                total_rows += inserted
                if etf.get("nav"):
                    await _update_dividend_yield(db, etf["id"], etf["nav"])
            else:
                logger.debug(f"[{ticker_key}] 분배금 데이터 없음")

            await db.execute(
                update(ETF).where(ETF.id == etf["id"]).values(expense_ratio=expense_ratio if expense_ratio is not None else 0)
            )
            logger.debug(f"[{ticker_key}] 총보수 {expense_ratio if expense_ratio is not None else 0}% 저장")
        except Exception as e:
            logger.error(f"[{ticker_key}] DB 저장 실패: {e}")
            errors += 1

    await db.commit()

    logger.info(
        f"Tiger ETF 분배금 동기화 완료: ETF {len(etf_list)}개, "
        f"저장 {total_rows}건, 오류 {errors}건"
    )
    return {"total_etfs": len(etf_list), "total_rows": total_rows, "errors": errors}


async def sync_kodex_etf_dividends(db: AsyncSession, ticker: Optional[str] = None) -> dict:
    """
    KODEX ETF 분배금 이력을 삼성자산운용 API에서 수집해 DB에 저장합니다.

    Args:
        db: AsyncSession
        ticker: 특정 종목만 동기화할 경우 지정. None이면 전체 KODEX ETF 대상.
    """
    if ticker:
        result = await db.execute(
            select(ETF.id, ETF.stock_code, ETF.isin, ETF.name, ETF.nav)
            .where(ETF.stock_code == ticker)
            .where(ETF.is_active == True)
        )
        row = result.first()
        if not row:
            logger.warning(f"ETF 없음: {ticker}")
            return {"total_etfs": 0, "total_rows": 0, "errors": 0}
        etf_list = [{"id": row.id, "ticker": row.stock_code, "name": row.name, "nav": float(row.nav) if row.nav else None}]
    else:
        etf_list = await _get_kodex_etfs(db)

    if not etf_list:
        logger.info("동기화 대상 KODEX ETF 없음")
        return {"total_etfs": 0, "total_rows": 0, "errors": 0}

    logger.info(f"KODEX ETF 분배금 동기화 시작: {len(etf_list)}개")

    etf_input = [{"ticker": e["ticker"], "name": e["name"]} for e in etf_list]
    etf_data_map = await fetch_kodex_etf_data_batch(etf_input)

    total_rows = 0
    errors = 0

    for etf in etf_list:
        ticker_key = etf["ticker"]
        etf_data = etf_data_map.get(ticker_key, {})
        dividends = etf_data.get("dividends", [])

        try:
            if dividends:
                inserted = await _upsert_dividends(db, etf["id"], dividends)
                total_rows += inserted
                if etf.get("nav"):
                    await _update_dividend_yield(db, etf["id"], etf["nav"])
            else:
                logger.debug(f"[{ticker_key}] 분배금 데이터 없음")
        except Exception as e:
            logger.error(f"[{ticker_key}] DB 저장 실패: {e}")
            errors += 1

    await db.commit()

    logger.info(
        f"KODEX ETF 분배금 동기화 완료: ETF {len(etf_list)}개, "
        f"저장 {total_rows}건, 오류 {errors}건"
    )
    return {"total_etfs": len(etf_list), "total_rows": total_rows, "errors": errors}
