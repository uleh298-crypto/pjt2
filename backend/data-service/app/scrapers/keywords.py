"""뉴스 카테고리 정의

네이버 증권 종목뉴스 크롤링에서 사용하는 카테고리 매핑
"""

# 뉴스 카테고리 코드 -> 이름
NEWS_CATEGORIES = {
    "NEWS_SEMI": "반도체",
    "NEWS_IT": "IT/소프트웨어",
    "NEWS_BIO": "바이오/제약",
    "NEWS_AUTO": "자동차",
    "NEWS_CHEM": "화학/소재",
    "NEWS_ENERGY": "에너지",
    "NEWS_FINANCE": "금융",
    "NEWS_CONSTRUCT": "건설/인프라",
    "NEWS_CONSUMER": "소비재/유통",
    "NEWS_TELECOM": "통신",
    "NEWS_TRANSPORT": "운송/물류",
    "NEWS_INDUSTRY": "산업재",
    "NEWS_ETC": "기타",
}

# 산업 그룹 -> 뉴스 카테고리 매핑
INDUSTRY_TO_CATEGORY = {
    "IT_SEMI": "NEWS_SEMI",
    "IT_SW": "NEWS_IT",
    "IT_HW": "NEWS_IT",
    "BIO": "NEWS_BIO",
    "AUTO": "NEWS_AUTO",
    "CHEM": "NEWS_CHEM",
    "ENERGY": "NEWS_ENERGY",
    "FINANCE": "NEWS_FINANCE",
    "CONSTRUCT": "NEWS_CONSTRUCT",
    "CONSUMER": "NEWS_CONSUMER",
    "TELECOM": "NEWS_TELECOM",
    "TRANSPORT": "NEWS_TRANSPORT",
    "INDUSTRY": "NEWS_INDUSTRY",
}
