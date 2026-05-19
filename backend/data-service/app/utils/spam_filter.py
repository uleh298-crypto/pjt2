"""
Spam Filter - 명백한 스팸 뉴스 필터링

All-LLM 방식에서 GPT-4o 분석 전 명백한 스팸만 제거
복잡한 룰베이스 대신 단순 키워드 필터링
"""
import re
from typing import List, Tuple
from dataclasses import dataclass


@dataclass
class FilterResult:
    """필터링 결과"""
    is_spam: bool
    reason: str = None
    matched_keyword: str = None


# 스팸 키워드 (제목에 포함되면 스킵)
SPAM_KEYWORDS = [
    # 비경제 카테고리
    "연예", "스포츠", "날씨", "운세", "로또", "축구", "야구", "농구",
    "배구", "골프", "올림픽", "월드컵", "아이돌", "드라마", "예능",

    # 광고성/이벤트
    "무료체험", "할인쿠폰", "이벤트당첨", "경품", "추첨",

    # 생활/잡다
    "맛집", "레시피", "요리", "다이어트", "뷰티", "패션",
]

# 스팸 패턴 (정규식)
SPAM_PATTERNS = [
    r"^\[광고\]",          # [광고]로 시작
    r"^\[PR\]",            # [PR]로 시작
    r"^\[스포츠\]",        # [스포츠]로 시작
    r"^\[연예\]",          # [연예]로 시작
    r"\d+% ?할인",         # 50% 할인
    r"무료 ?배송",         # 무료 배송
]

# 경제/투자 관련 키워드 (있으면 스팸 아님 - 우선순위 높음)
WHITELIST_KEYWORDS = [
    # 시장/지수
    "코스피", "코스닥", "나스닥", "다우", "S&P", "니케이",

    # 금융
    "금리", "기준금리", "연준", "Fed", "FOMC", "통화정책", "인플레이션",
    "환율", "달러", "원화", "엔화", "유로",

    # 산업
    "반도체", "2차전지", "배터리", "바이오", "AI", "인공지능",
    "전기차", "자율주행", "태양광", "풍력", "원자력",

    # 투자
    "ETF", "펀드", "주식", "채권", "배당", "실적", "어닝",
    "공모주", "IPO", "상장", "매출", "영업이익", "순이익",

    # 회사/기관
    "삼성", "SK", "LG", "현대", "기아", "네이버", "카카오",
    "한국은행", "금융위", "금감원",
]


def is_spam(title: str, description: str = None) -> FilterResult:
    """
    뉴스가 스팸인지 판단

    Args:
        title: 뉴스 제목
        description: RSS description (선택)

    Returns:
        FilterResult: 스팸 여부 및 이유
    """
    if not title:
        return FilterResult(is_spam=True, reason="빈 제목")

    text = title.lower()
    if description:
        text += " " + description.lower()

    # 1. 화이트리스트 체크 (있으면 무조건 통과)
    for keyword in WHITELIST_KEYWORDS:
        if keyword.lower() in text:
            return FilterResult(is_spam=False)

    # 2. 스팸 패턴 체크
    for pattern in SPAM_PATTERNS:
        if re.search(pattern, title, re.IGNORECASE):
            return FilterResult(
                is_spam=True,
                reason="스팸 패턴 매칭",
                matched_keyword=pattern
            )

    # 3. 스팸 키워드 체크
    for keyword in SPAM_KEYWORDS:
        if keyword in text:
            return FilterResult(
                is_spam=True,
                reason="스팸 키워드 매칭",
                matched_keyword=keyword
            )

    # 4. 기본: 스팸 아님
    return FilterResult(is_spam=False)


def filter_news_list(news_list: List[dict]) -> Tuple[List[dict], List[dict]]:
    """
    뉴스 리스트에서 스팸 필터링

    Args:
        news_list: [{"title": "...", "description": "..."}, ...]

    Returns:
        (통과된 뉴스, 스팸 뉴스)
    """
    passed = []
    spam = []

    for news in news_list:
        title = news.get("title", "")
        description = news.get("description", "")

        result = is_spam(title, description)

        if result.is_spam:
            news["spam_reason"] = result.reason
            news["spam_keyword"] = result.matched_keyword
            spam.append(news)
        else:
            passed.append(news)

    return passed, spam


# 테스트용
if __name__ == "__main__":
    test_cases = [
        # 통과해야 함
        ("삼성전자, AI 반도체 대규모 투자 발표", False),
        ("SK하이닉스 HBM3E 양산 시작", False),
        ("미국 연준 금리 동결 발표", False),
        ("코스피 3000 돌파, 외국인 순매수", False),
        ("KODEX 반도체 ETF 순자산 1조 돌파", False),
        ("2차전지 수요 급증 전망", False),
        ("현대차, 전기차 판매 신기록", False),

        # 스팸이어야 함
        ("손흥민 연봉 협상 타결", True),
        ("[스포츠] 월드컵 예선 결과", True),
        ("오늘의 날씨, 전국 맑음", True),
        ("이번 주 로또 당첨번호 발표", True),
        ("[광고] 특가 이벤트 진행중", True),
        ("연예인 열애설 터졌다", True),
        ("맛집 추천 TOP 10", True),
    ]

    print("=" * 60)
    print("Spam Filter 테스트")
    print("=" * 60)

    passed = 0
    failed = 0

    for title, expected_spam in test_cases:
        result = is_spam(title)
        actual_spam = result.is_spam

        if actual_spam == expected_spam:
            status = "✓ PASS"
            passed += 1
        else:
            status = "✗ FAIL"
            failed += 1

        expected_str = "스팸" if expected_spam else "통과"
        actual_str = "스팸" if actual_spam else "통과"

        print(f"{status} [{expected_str}→{actual_str}] {title[:30]}...")
        if result.is_spam:
            print(f"       → {result.reason}: {result.matched_keyword}")

    print("=" * 60)
    print(f"결과: {passed}/{passed+failed} 통과")
