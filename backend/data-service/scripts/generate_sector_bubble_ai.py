"""
ETF 섹터 버블 AI 분석 생성 스크립트

ETF 클러스터 뷰에서 섹터 버블 클릭 시 표시되는 AI 분석 결과를 생성합니다.
결과는 etf_sector_ai_history 테이블에 저장됩니다.

Usage:
    python -m scripts.generate_sector_bubble_ai [--force] [--etf-id ETF_ID] [--model MODEL]

Options:
    --force     기존 분석이 있어도 재생성
    --etf-id    특정 ETF만 처리
    --model     사용할 모델 (기본: claude-3-haiku-20240307)
                - claude-3-haiku-20240307 (저렴, 추천)
                - claude-3-5-sonnet-20241022 (고품질)

Anthropic API 비용 (1M tokens 기준):
    - claude-3-haiku: $0.25 input / $1.25 output (저렴)
    - claude-3-5-sonnet: $3 input / $15 output (고품질)
"""

import asyncio
import argparse
import json
import logging
from datetime import datetime
from decimal import Decimal
from typing import Optional

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.database import SessionLocal
from app.services.llm_service import LLMService
from app.config import get_settings

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)
settings = get_settings()

PROMPT_NAME = "sector_bubble_analysis"

# 모델 목록
AVAILABLE_MODELS = {
    "haiku": "claude-3-haiku-20240307",
    "sonnet": "claude-3-5-sonnet-20241022",
}

DEFAULT_MODEL = "claude-3-haiku-20240307"


class SectorBubbleAIGenerator:
    """섹터 버블 AI 분석 생성기"""

    def __init__(self, db: Session, model: str = DEFAULT_MODEL):
        self.db = db
        self.llm = LLMService(db)
        self.model = model
        self.generated_count = 0
        self.error_count = 0

    async def generate_all(self, force: bool = False, etf_id: Optional[int] = None):
        """모든 섹터 버블에 대해 AI 분석 생성"""

        # 1. 분석 필요한 섹터 클러스터 조회
        clusters = self._get_clusters_to_analyze(force, etf_id)
        total = len(clusters)

        if total == 0:
            logger.info("분석할 섹터 버블이 없습니다.")
            return

        logger.info(f"[시작] {total}개 섹터 버블 AI 분석 생성 (모델: {self.model})")

        # 2. 프롬프트 조회
        prompt = self.llm.get_prompt(PROMPT_NAME)
        if not prompt:
            logger.error(f"프롬프트를 찾을 수 없습니다: {PROMPT_NAME}")
            return

        # 3. 각 클러스터별 분석 생성
        for i, cluster in enumerate(clusters):
            try:
                await self._generate_single(cluster, prompt)
                self.generated_count += 1

                if (i + 1) % 10 == 0:
                    logger.info(f"[진행] {i + 1}/{total} 완료")
                    self.db.commit()

                # Rate limit 방지 (0.5초 간격)
                await asyncio.sleep(0.5)

            except Exception as e:
                self.error_count += 1
                logger.error(f"[오류] ETF={cluster['etf_id']}, 섹터={cluster['group_code']}: {e}")
                self.db.rollback()  # 트랜잭션 롤백
                continue

        self.db.commit()
        logger.info(f"[완료] 생성: {self.generated_count}개, 오류: {self.error_count}개")

    def _get_clusters_to_analyze(self, force: bool, etf_id: Optional[int]) -> list:
        """분석 대상 섹터 클러스터 조회"""

        query = """
            SELECT DISTINCT
                esc.etf_id,
                esc.group_code,
                esc.group_name,
                esc.weight_pct,
                esc.stock_count,
                esc.base_date,
                e.name as etf_name,
                e.category as etf_category
            FROM etf_sector_cluster esc
            JOIN etf e ON e.id = esc.etf_id
            WHERE esc.cluster_type = 'GROUP_CODE'
              AND esc.group_code IS NOT NULL
        """

        params = {}

        if etf_id:
            query += " AND esc.etf_id = :etf_id"
            params["etf_id"] = etf_id

        if not force:
            # 이미 분석이 있는 (etf_id, group_code, base_date) 조합 제외
            query += """
                AND NOT EXISTS (
                    SELECT 1 FROM etf_sector_ai_history h
                    WHERE h.etf_id = esc.etf_id
                      AND h.group_code = esc.group_code
                      AND h.base_date = esc.base_date
                )
            """

        query += " ORDER BY esc.etf_id, esc.weight_pct DESC"

        result = self.db.execute(text(query), params)
        return [dict(row._mapping) for row in result.fetchall()]

    async def _generate_single(self, cluster: dict, prompt):
        """단일 섹터 버블 AI 분석 생성"""

        # 1. 상위 종목 조회
        top_stocks = self._get_top_stocks(
            cluster["etf_id"],
            cluster["group_code"],
            cluster["base_date"]
        )

        # 2. 집중도 계산
        weights = [float(s["weight"]) for s in top_stocks]
        top2_concentration = sum(weights[:2]) if len(weights) >= 2 else sum(weights)

        # 3. LLM 입력 컨텍스트 구성
        context = {
            "etf": {
                "name": cluster["etf_name"],
                "category": cluster["etf_category"] or "일반"
            },
            "sector": {
                "group_code": cluster["group_code"],
                "group_name": cluster["group_name"],
                "weight_pct": float(cluster["weight_pct"]),
                "stock_count": cluster["stock_count"]
            },
            "composition": {
                "top_stocks": top_stocks[:5],
                "top2_concentration": round(top2_concentration, 1)
            }
        }

        # 4. Anthropic API 직접 호출
        result = await self._call_anthropic(
            prompt.prompt_template,
            json.dumps(context, ensure_ascii=False)
        )

        if not result:
            raise Exception("LLM 응답 없음")

        # 5. 결과 저장
        self._save_result(cluster, top_stocks, result)

    async def _call_anthropic(self, system_prompt: str, user_message: str) -> Optional[dict]:
        """Anthropic API 직접 호출"""
        import httpx

        json_instruction = "\n\nRespond in JSON format only. Do not include any text outside of the JSON object."

        payload = {
            "model": self.model,
            "max_tokens": 1024,
            "system": system_prompt + json_instruction,
            "messages": [
                {"role": "user", "content": user_message}
            ]
        }

        url = "https://api.anthropic.com/v1/messages"
        headers = {
            "Content-Type": "application/json",
            "x-api-key": settings.anthropic_api_key,
            "anthropic-version": "2023-06-01"
        }

        async with httpx.AsyncClient(timeout=60) as client:
            response = await client.post(url, json=payload, headers=headers)
            response.raise_for_status()
            data = response.json()

            content = data.get("content", [])
            if content and len(content) > 0:
                text = content[0].get("text", "")

                # JSON 파싱
                if "```json" in text:
                    start = text.find("```json") + 7
                    end = text.find("```", start)
                    text = text[start:end].strip()
                elif "```" in text:
                    start = text.find("```") + 3
                    end = text.find("```", start)
                    text = text[start:end].strip()

                return json.loads(text)

        return None

    def _get_top_stocks(self, etf_id: int, group_code: str, base_date) -> list:
        """섹터 내 상위 종목 조회"""

        query = """
            SELECT
                ci.company_name as name,
                esc.weight_pct as weight
            FROM etf_stock_composition esc
            JOIN stock s ON s.id = esc.stock_id
            JOIN company_info ci ON ci.id = s.company_id
            WHERE esc.etf_id = :etf_id
              AND ci.industry_group = :group_code
              AND esc.base_date = :base_date
            ORDER BY esc.weight_pct DESC
            LIMIT 10
        """

        result = self.db.execute(text(query), {
            "etf_id": etf_id,
            "group_code": group_code,
            "base_date": base_date
        })

        return [
            {"name": row.name, "weight": float(row.weight) if row.weight else 0}
            for row in result.fetchall()
        ]

    def _save_result(self, cluster: dict, top_stocks: list, result: dict):
        """AI 분석 결과 저장"""

        # Decimal 변환
        weight_pct = cluster["weight_pct"]
        if isinstance(weight_pct, Decimal):
            weight_pct = float(weight_pct)

        insert_query = """
            INSERT INTO etf_sector_ai_history (
                etf_id, group_code, group_name,
                weight_pct, stock_count, top_stocks,
                ai_analysis,
                prompt_id, base_date, created_at
            ) VALUES (
                :etf_id, :group_code, :group_name,
                :weight_pct, :stock_count, CAST(:top_stocks AS jsonb),
                :ai_analysis,
                (SELECT id FROM ai_prompt WHERE name = :prompt_name AND is_active = true LIMIT 1),
                :base_date, :created_at
            )
        """

        self.db.execute(text(insert_query), {
            "etf_id": cluster["etf_id"],
            "group_code": cluster["group_code"],
            "group_name": cluster["group_name"],
            "weight_pct": weight_pct,
            "stock_count": cluster["stock_count"],
            "top_stocks": json.dumps(top_stocks[:5], ensure_ascii=False),
            "ai_analysis": result.get("analysis", ""),
            "prompt_name": PROMPT_NAME,
            "base_date": cluster["base_date"],
            "created_at": datetime.now()
        })


async def main():
    parser = argparse.ArgumentParser(description="섹터 버블 AI 분석 생성")
    parser.add_argument("--force", action="store_true", help="기존 분석이 있어도 재생성")
    parser.add_argument("--etf-id", type=int, help="특정 ETF만 처리")
    parser.add_argument("--model", type=str, default=DEFAULT_MODEL,
                        help=f"사용할 모델 (기본: {DEFAULT_MODEL})")
    args = parser.parse_args()

    # API 키 확인
    if not settings.anthropic_api_key:
        logger.error("Anthropic API 키가 설정되지 않았습니다. .env 파일에 ANTHROPIC_API_KEY를 설정하세요.")
        return

    db = SessionLocal()
    try:
        generator = SectorBubbleAIGenerator(db, model=args.model)
        await generator.generate_all(force=args.force, etf_id=args.etf_id)
    finally:
        db.close()


if __name__ == "__main__":
    asyncio.run(main())
