# -*- coding: utf-8 -*-
"""
KRX ETF 전체 목록 (Naver Finance API) vs industry_classification Level 4 비교
누락된 테마/섹터를 찾아내는 스크립트
"""
import re
import requests
from collections import Counter

# ============================================
# 1. Naver Finance에서 전체 ETF 목록 가져오기
# ============================================
print("=" * 60)
print("Naver Finance ETF 전체 목록 수집 중...")
print("=" * 60)

url = "https://finance.naver.com/api/sise/etfItemList.nhn?etfType=0&targetColumn=market_sum&sortOrder=desc"
resp = requests.get(url, headers={"User-Agent": "Mozilla/5.0"})
data = resp.json()
items = data.get("result", {}).get("etfItemList", [])

etf_names = [(item["itemcode"], item["itemname"]) for item in items]
print(f"전체 ETF 수: {len(etf_names)}")

# ============================================
# 2. 국내 주식형 ETF만 필터링
# ============================================
bond_keywords = [
    '국채', '회사채', '채권', '머니마켓', 'CD금리', '통안채',
    'KOFR', 'SOFR', '금리', '단기자금', 'STRIP', '크레딧',
    '하이일드', '종합채권', '국공채', '통화안정', '전자단기',
    '단기채', '장기채', '중기채', '국고채', '특수채', '전단채',
    'KIS국고', 'Enhanced',
]

exclude_keywords = [
    '레버리지', '인버스', '2X', '곱버스',
    'TDF', 'TRF', '멀티에셋', '혼합',
    '머니마켓',
]

def is_stock_etf(name):
    """주식형 ETF 판별 (국내+해외 포함, 채권/레버리지/혼합만 제외)"""
    name_upper = name.upper()
    for kw in bond_keywords + exclude_keywords:
        if kw in name or kw.upper() in name_upper:
            return False
    return True

korea_etfs = [(t, n) for t, n in etf_names if is_stock_etf(n)]
print(f"주식형 ETF (채권/레버리지/혼합 제외, 해외주식 포함): {len(korea_etfs)}개")

# ============================================
# 3. 테마 매칭
# ============================================
theme_map = {
    # 반도체
    '반도체': '반도체', 'HBM': '반도체', '팹리스': '반도체', '파운드리': '반도체',
    # 2차전지
    '2차전지': '2차전지', '배터리': '2차전지', '리튬': '2차전지',
    '양극재': '2차전지', '음극재': '2차전지', '전고체': '2차전지',
    # 디스플레이
    '디스플레이': '디스플레이', 'OLED': '디스플레이',
    # 바이오
    '바이오': '바이오', '헬스케어': '바이오', '제약': '바이오', '의료기기': '바이오',
    '의료': '바이오', '게놈': '바이오', '유전자': '바이오',
    # 자동차
    '자동차': '자동차', '전기차': '자동차', '자율주행': '자동차',
    'UAM': '자동차', '모빌리티': '자동차',
    # SW/IT/AI
    '소프트웨어': 'SW/IT', '인공지능': 'SW/IT', '클라우드': 'SW/IT',
    '빅데이터': 'SW/IT', '사이버보안': 'SW/IT', '데이터센터': 'SW/IT',
    '양자컴퓨팅': 'SW/IT', '양자': 'SW/IT', '핀테크': 'SW/IT',
    '블록체인': 'SW/IT', '디지털': 'SW/IT', '플랫폼': 'SW/IT',
    '메타버스': 'SW/IT',
    # 통신
    '통신': '통신', '5G': '통신', '6G': '통신',
    # 미디어/콘텐츠
    '미디어': '미디어/콘텐츠', '콘텐츠': '미디어/콘텐츠',
    '게임': '미디어/콘텐츠', '엔터': '미디어/콘텐츠', 'KPOP': '미디어/콘텐츠',
    # 로봇
    '로봇': '로봇', '로보틱스': '로봇', '피지컬': '로봇',
    # 에너지/전력
    '원자력': '에너지/전력', '태양광': '에너지/전력', '풍력': '에너지/전력',
    '수소': '에너지/전력', '전력': '에너지/전력', 'ESS': '에너지/전력',
    '신재생': '에너지/전력', '에너지': '에너지/전력', '원전': '에너지/전력',
    # 화학/소재
    '화학': '화학/소재', '화장품': '화학/소재', '뷰티': '화학/소재', '소재': '화학/소재',
    # 철강
    '철강': '철강/금속',
    # 건설/부동산
    '건설': '건설/부동산', '리츠': '건설/부동산', '부동산': '건설/부동산',
    '인프라': '건설/부동산',
    # 금융
    '은행': '금융', '증권': '금융', '보험': '금융', '금융': '금융',
    # 유통/소비
    '소비재': '유통/소비', '소비': '유통/소비', '필수소비': '유통/소비',
    # 식품
    '식품': '식품',
    # 소비재/라이프
    '여행': '소비재/라이프', '레저': '소비재/라이프', '카지노': '소비재/라이프',
    # 운송/물류
    '운송': '운송/물류', '물류': '운송/물류', '해운': '운송/물류', '항공': '운송/물류',
    # 조선
    '조선': '조선/해양',
    # 방산/우주항공
    '방산': '방산/우주항공', '우주': '방산/우주항공', '방위': '방산/우주항공',
    # 기계/산업재
    '기계': '기계/산업재', '산업재': '기계/산업재', '중공업': '기계/산업재',
    # 원자재
    '골드': '원자재', '실버': '원자재', '구리': '원자재',
    '원유': '원자재', 'WTI': '원자재', '팔라듐': '원자재', '곡물': '원자재',
    '천연가스': '원자재', '원자재': '원자재', '금현물': '원자재', '금선물': '원자재',
    '은선물': '원자재', '금액티브': '원자재', '국제금': '원자재',
    # ESG
    'ESG': 'ESG', '친환경': 'ESG', '탄소': 'ESG', '그린': 'ESG',
    '기후변화': 'ESG',
    # 대기업그룹
    '삼성그룹': '대기업그룹', '현대차그룹': '대기업그룹', 'LG그룹': '대기업그룹',
    'SK그룹': '대기업그룹', '한화그룹': '대기업그룹',
    # 배당/가치
    '배당': '배당/가치', '밸류': '배당/가치', '커버드콜': '배당/가치',
    # 시장지수 (국내)
    '코스피': '시장지수(국내)', '코스닥': '시장지수(국내)', 'KRX300': '시장지수(국내)',
    # 시장지수 (해외)
    'S&P': '시장지수(해외)', 'NASDAQ': '시장지수(해외)', '나스닥': '시장지수(해외)',
    '다우': '시장지수(해외)', 'Dow': '시장지수(해외)',
    'MSCI': '시장지수(해외)', 'FTSE': '시장지수(해외)',
    'Russell': '시장지수(해외)', 'DAX': '시장지수(해외)',
    '항셍': '시장지수(해외)', '니케이': '시장지수(해외)', 'Nifty': '시장지수(해외)',
    '유로스탁스': '시장지수(해외)', 'CSI': '시장지수(해외)',
    '필라델피아': '시장지수(해외)',
    # 해외 국가/지역
    '미국': '해외주식', '일본': '해외주식', '중국': '해외주식',
    '인도': '해외주식', '베트남': '해외주식', '유럽': '해외주식',
    '글로벌': '해외주식', '선진국': '해외주식', '신흥국': '해외주식',
    '독일': '해외주식', '대만': '해외주식', '홍콩': '해외주식',
    '브라질': '해외주식', '영국': '해외주식', '아세안': '해외주식',
    '차이나': '해외주식', '라틴': '해외주식', '심천': '해외주식',
    '아시아': '해외주식',
    # 해외 기업 특정
    '테슬라': '해외개별주', '엔비디아': '해외개별주', '팔란티어': '해외개별주',
    '버크셔': '해외개별주', 'iShares': '해외개별주',
    # 통화
    '달러': '통화', '엔화': '통화', '위안': '통화',
    # 대형/중소형/성장
    '대형': '사이즈/스타일', '중소형': '사이즈/스타일', '성장': '사이즈/스타일',
    '모멘텀': '사이즈/스타일', '퀄리티': '사이즈/스타일',
    # 기타
    '지주회사': '기타', 'BBIG': '기타', '수출': '기타',
    'IPO': '기타',
}

def match_special(name):
    """특수 패턴 매칭"""
    themes = set()
    if re.search(r'\bAI\b', name):
        themes.add('SW/IT')
    if re.search(r'\bIT\b', name):
        themes.add('SW/IT')
    if re.search(r'200(?!\d)', name):
        themes.add('시장지수')
    if re.search(r'150(?!\d)', name):
        themes.add('시장지수')
    return themes

matched = {}
unmatched = []

for ticker, name in korea_etfs:
    found_themes = set()
    for keyword, theme in theme_map.items():
        if keyword in name:
            found_themes.add(theme)
    found_themes |= match_special(name)
    if found_themes:
        matched[name] = found_themes
    else:
        unmatched.append((ticker, name))

# ============================================
# 4. 결과 출력
# ============================================
theme_counts = Counter()
for themes in matched.values():
    for t in themes:
        theme_counts[t] += 1

print("\n" + "=" * 60)
print("테마별 ETF 매칭 현황")
print("=" * 60)
for theme, count in theme_counts.most_common():
    print(f"  {theme:20s}: {count}개")

print(f"\n매칭된 ETF: {len(matched)}개")
print(f"미매칭 ETF: {len(unmatched)}개")

if unmatched:
    print("\n" + "=" * 60)
    print(f"미매칭 ETF 목록 ({len(unmatched)}개)")
    print("=" * 60)
    for ticker, name in unmatched:
        print(f"  [{ticker}] {name}")

    # 키워드 빈도
    print("\n" + "=" * 60)
    print("미매칭 ETF 키워드 빈도 (새 테마 후보)")
    print("=" * 60)

    brands = ['KODEX', 'TIGER', 'KBSTAR', 'ACE', 'PLUS', 'ARIRANG', 'HANARO',
              'SOL', 'KOSEF', 'TIMEFOLIO', 'WOORI', 'BNK', 'FOCUS', 'MASTER',
              'RISE', 'VITA', 'TREX', 'HERO', 'ABLE', 'SMART', 'UNICORN',
              'KoAct', 'WON', '마이티', 'KIWOOM']

    word_counter = Counter()
    for ticker, name in unmatched:
        clean = name
        for brand in brands:
            clean = clean.replace(brand, '')
        words = re.findall(r'[가-힣]{2,}|[A-Za-z]{2,}', clean)
        for w in words:
            if w not in brands and len(w) >= 2:
                word_counter[w] += 1

    for word, count in word_counter.most_common(30):
        print(f"  {word:20s}: {count}회")

# 전체 목록 저장
with open("docs/sql/krx_etf_list.txt", "w", encoding="utf-8") as f:
    f.write(f"# 전체 ETF: {len(etf_names)}개 / 국내주식형: {len(korea_etfs)}개\n")
    f.write(f"# 매칭: {len(matched)}개 / 미매칭: {len(unmatched)}개\n\n")
    f.write("== 국내 주식형 ETF ==\n")
    for t, n in korea_etfs:
        tag = "OK" if n in matched else "MISS"
        f.write(f"[{tag}] {t}\t{n}\n")

print(f"\n결과 저장: docs/sql/krx_etf_list.txt")
