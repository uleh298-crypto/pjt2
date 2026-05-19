"""뉴스 AI 분석 서비스

뉴스 기사 분석:
1. AI 요약 (3줄 bullet points)
2. 키워드 추출
3. 관련 ETF 추천
"""
import json
import logging
from typing import Optional, List, Dict, Any
from dataclasses import dataclass
from decimal import Decimal

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.models.news import NewsArticle
from app.models.news_etf import NewsETFInfluence
from app.services.llm_service import LLMService
from app.services.event_publisher import event_publisher

logger = logging.getLogger(__name__)


@dataclass
class NewsAnalysisResult:
    """뉴스 분석 결과"""
    summary: List[str]  # 3줄 요약
    keywords: List[str]  # 키워드 목록
    sentiment: str  # POSITIVE, NEGATIVE, NEUTRAL
    industries: List[Dict[str, str]]  # [{"code": "IT_SEMI", "impact": "POSITIVE"}]


@dataclass
class ETFRecommendation:
    """ETF 추천 결과"""
    etf_id: int
    stock_code: str
    name: str
    influence_score: float
    influence_type: str  # POSITIVE, NEGATIVE, NEUTRAL
    reason: str


class NewsAnalyzer:
    """뉴스 AI 분석기"""

    def __init__(self, db: Session):
        self.db = db
        # 뉴스 분석은 Haiku(저렴한 모델) 사용
        self.llm = LLMService(db, use_light_model=True)

    async def close(self):
        await self.llm.close()

    async def analyze_article(self, article: NewsArticle) -> Optional[NewsAnalysisResult]:
        """
        뉴스 기사 AI 분석

        Args:
            article: 분석할 뉴스 기사

        Returns:
            NewsAnalysisResult or None
        """
        if not self.llm.is_configured():
            logger.warning("OpenAI API 키가 설정되지 않았습니다.")
            return None

        # 본문이 너무 길면 앞부분만 사용
        content = article.content or ""
        if len(content) > 3000:
            content = content[:3000] + "..."

        user_message = f"""## 뉴스 제목
{article.title}

## 뉴스 본문
{content}

## 언론사
{article.source or '알 수 없음'}
"""

        result = await self.llm.analyze_with_prompt("news_analysis", user_message)

        if not result:
            return None

        try:
            return NewsAnalysisResult(
                summary=result.get("summary", []),
                keywords=result.get("keywords", []),
                sentiment=result.get("sentiment", "NEUTRAL"),
                industries=result.get("industries", [])
            )
        except Exception as e:
            logger.error(f"분석 결과 파싱 실패: {e}")
            return None

    def recommend_etfs(
        self,
        news_id: int,
        analysis: NewsAnalysisResult,
        limit: int = 5
    ) -> List[ETFRecommendation]:
        """
        뉴스 분석 결과 기반 ETF 추천

        1. AI가 산업 영향력 있다고 판단한 경우에만 진행
        2. news_stock_mapping에서 뉴스에 매핑된 종목 조회
        3. 해당 종목이 AI가 판단한 산업에 속하는지 확인
        4. 해당 종목이 실제 포함된 ETF만 추천
        """
        # AI가 영향력 있다고 판단한 경우에만 진행
        if not analysis.industries:
            return []

        # AI가 판단한 산업 코드 목록
        industry_codes = [ind.get("code") for ind in analysis.industries if ind.get("code")]
        if not industry_codes:
            return []

        # 첫 번째 산업의 영향 타입 사용
        impact = analysis.industries[0].get("impact", "NEUTRAL")

        # news_stock_mapping → company_info(산업 검증) → stock → etf_stock_composition → etf
        # 뉴스 종목이 AI가 판단한 산업에 속하고, 실제 ETF에 포함된 경우만 조회
        result = self.db.execute(text("""
            SELECT DISTINCT
                e.id,
                e.stock_code,
                e.name,
                esc.weight_pct,
                ci.company_name
            FROM news_stock_mapping nsm
            JOIN company_info ci ON nsm.company_id = ci.id
            JOIN stock s ON s.company_id = ci.id
            JOIN etf_stock_composition esc ON esc.stock_id = s.id
            JOIN etf e ON esc.etf_id = e.id
            WHERE nsm.news_id = :news_id
              AND ci.industry_group = ANY(:industry_codes)
              AND e.is_active = true
            ORDER BY esc.weight_pct DESC
            LIMIT :limit
        """), {"news_id": news_id, "industry_codes": industry_codes, "limit": limit * 2})

        recommendations = []
        for row in result:
            # 이미 추천된 ETF 제외
            if any(r.etf_id == row[0] for r in recommendations):
                continue

            # 영향력 점수 계산 (비중 기반)
            weight = float(row[3]) if row[3] else 0
            score = min(weight / 100, 1.0)  # 0~1 범위로 정규화

            recommendations.append(ETFRecommendation(
                etf_id=row[0],
                stock_code=row[1],
                name=row[2],
                influence_score=score,
                influence_type=impact,
                reason=f"{row[4]} 보유 ({weight:.1f}%)"
            ))

        # 점수 높은 순 정렬 후 상위 N개 반환
        recommendations.sort(key=lambda x: x.influence_score, reverse=True)
        return recommendations[:limit]

    async def process_article(
        self,
        article: NewsArticle,
        save_to_db: bool = True
    ) -> Optional[Dict[str, Any]]:
        """
        뉴스 기사 분석 및 ETF 추천 전체 프로세스

        Args:
            article: 분석할 뉴스
            save_to_db: DB에 결과 저장 여부

        Returns:
            {
                "analysis": NewsAnalysisResult,
                "etf_recommendations": List[ETFRecommendation]
            }
        """
        # 1. AI 분석
        analysis = await self.analyze_article(article)
        if not analysis:
            logger.warning(f"뉴스 분석 실패: {article.id}")
            return None

        # 2. ETF 추천 (뉴스에 매핑된 종목 기반)
        etf_recs = self.recommend_etfs(article.id, analysis)

        # 3. DB 저장
        if save_to_db:
            # 뉴스 기사 업데이트 (요약, 키워드)
            article.content_summary = {"bullets": analysis.summary}
            article.keywords = analysis.keywords
            self.db.add(article)

            # ETF 영향력 저장
            for rec in etf_recs:
                influence = NewsETFInfluence(
                    news_id=article.id,
                    etf_id=rec.etf_id,
                    influence_score=Decimal(str(rec.influence_score)),
                    influence_type=rec.influence_type,
                    timeline_title=article.title[:100] if article.title else "",
                    timeline_summary=analysis.summary[0] if analysis.summary else "",
                    analysis_reason=rec.reason
                )
                self.db.add(influence)

            self.db.commit()
            logger.info(f"뉴스 분석 저장 완료: {article.id} - ETF {len(etf_recs)}개 추천")

            # 4. 이벤트 발행 (ETF 추천이 있는 경우)
            if etf_recs:
                await self._publish_news_event(article, analysis, etf_recs)

        return {
            "analysis": analysis,
            "etf_recommendations": etf_recs
        }

    async def _publish_news_event(
        self,
        article: NewsArticle,
        analysis: NewsAnalysisResult,
        etf_recs: List[ETFRecommendation]
    ):
        """뉴스 분석 완료 이벤트 발행 (RabbitMQ)"""
        try:
            etf_ids = [rec.etf_id for rec in etf_recs]
            summary = analysis.summary[0] if analysis.summary else ""
            influence_type = etf_recs[0].influence_type if etf_recs else "NEUTRAL"

            await event_publisher.publish_news_alert(
                news_id=article.id,
                news_title=article.title or "",
                news_summary=summary,
                etf_ids=etf_ids,
                influence_type=influence_type
            )
        except Exception as e:
            # 이벤트 발행 실패해도 분석 결과는 유지
            logger.error(f"뉴스 이벤트 발행 실패: newsId={article.id}, error={e}")


async def analyze_news(db: Session, news_id: int) -> Optional[Dict[str, Any]]:
    """단일 뉴스 분석 (API용)"""
    article = db.query(NewsArticle).filter(NewsArticle.id == news_id).first()
    if not article:
        return None

    analyzer = NewsAnalyzer(db)
    try:
        return await analyzer.process_article(article)
    finally:
        await analyzer.close()


async def analyze_unprocessed_news(db: Session, limit: int = 50) -> int:
    """
    미분석 뉴스 일괄 처리 (Atomic 선점 방식)

    Race Condition 방지:
    - UPDATE + RETURNING으로 원자적 선점
    - FOR UPDATE SKIP LOCKED로 다른 스케줄러가 선점한 건 스킵
    - 락은 ms 단위로 짧게, AI 분석 중에는 락 없음
    """
    analyzer = NewsAnalyzer(db)
    processed = 0

    try:
        for _ in range(limit):
            # 1. Atomic하게 1건 선점: UPDATE + RETURNING
            #    - FOR UPDATE SKIP LOCKED: 다른 스케줄러가 처리중인 건 스킵
            #    - content_summary를 PROCESSING으로 마킹하여 중복 처리 방지
            result = db.execute(text("""
                UPDATE news_article
                SET content_summary = '{"status": "PROCESSING"}'::jsonb
                WHERE id = (
                    SELECT id FROM news_article
                    WHERE content_summary IS NULL
                      AND content IS NOT NULL
                      AND is_active = true
                    ORDER BY published_at DESC
                    LIMIT 1
                    FOR UPDATE SKIP LOCKED
                )
                RETURNING id, title, content, source
            """))
            row = result.fetchone()

            if not row:
                # 더 이상 처리할 뉴스 없음
                if processed == 0:
                    logger.info("분석할 뉴스가 없습니다.")
                break

            # 2. 즉시 커밋하여 락 해제 (ms 단위)
            db.commit()

            article_id, title, content, source = row
            logger.info(f"선점 완료: news_id={article_id}, title={title[:30] if title else 'N/A'}...")

            # 3. AI 분석 수행 (락 없음, 시간 오래 걸려도 OK)
            try:
                # 분석용 임시 객체 생성
                article = db.query(NewsArticle).filter(NewsArticle.id == article_id).first()
                if not article:
                    logger.warning(f"뉴스 조회 실패: {article_id}")
                    continue

                # AI 분석 (analyze_article만 호출, DB 저장은 별도로)
                analysis = await analyzer.analyze_article(article)

                if not analysis:
                    # 분석 실패 시 상태 롤백
                    db.execute(text("""
                        UPDATE news_article
                        SET content_summary = NULL
                        WHERE id = :id
                    """), {"id": article_id})
                    db.commit()
                    logger.warning(f"뉴스 분석 실패, 상태 롤백: {article_id}")
                    continue

                # 4. ETF 추천 (뉴스에 매핑된 종목 기반)
                etf_recs = analyzer.recommend_etfs(article_id, analysis)

                # 5. 최종 결과 저장 (COMPLETED)
                db.execute(text("""
                    UPDATE news_article
                    SET content_summary = CAST(:summary AS jsonb),
                        keywords = CAST(:keywords AS jsonb)
                    WHERE id = :id
                """), {
                    "id": article_id,
                    "summary": json.dumps({"bullets": analysis.summary}, ensure_ascii=False),
                    "keywords": json.dumps(analysis.keywords, ensure_ascii=False)
                })

                # ETF 영향력 저장 (중복 방지: ON CONFLICT DO NOTHING)
                for rec in etf_recs:
                    db.execute(text("""
                        INSERT INTO news_etf_influence
                            (news_id, etf_id, influence_score, influence_type,
                             timeline_title, timeline_summary, analysis_reason)
                        VALUES
                            (:news_id, :etf_id, :score, :type, :title, :summary, :reason)
                        ON CONFLICT (news_id, etf_id) DO NOTHING
                    """), {
                        "news_id": article_id,
                        "etf_id": rec.etf_id,
                        "score": rec.influence_score,
                        "type": rec.influence_type,
                        "title": (article.title or "")[:100],
                        "summary": analysis.summary[0] if analysis.summary else "",
                        "reason": rec.reason
                    })

                db.commit()
                processed += 1
                logger.info(f"[{processed}] 분석 완료: {title[:30] if title else 'N/A'}... (ETF {len(etf_recs)}개)")

                # 6. 이벤트 발행
                if etf_recs:
                    await analyzer._publish_news_event(article, analysis, etf_recs)

            except Exception as e:
                logger.error(f"뉴스 처리 중 오류 (news_id={article_id}): {e}")
                # 오류 발생 시 상태 롤백
                db.rollback()
                db.execute(text("""
                    UPDATE news_article
                    SET content_summary = NULL
                    WHERE id = :id
                """), {"id": article_id})
                db.commit()
                continue

    finally:
        await analyzer.close()

    logger.info(f"뉴스 분석 완료: 총 {processed}건 처리")
    return processed
