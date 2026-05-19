# KRX 공시 스크래퍼 테스트 결과

## 테스트 일시
2026-03-08

## 테스트 대상
- 파일: `backend/data-service/app/scrapers/krx_scraper.py`
- 기능: KRX KIND 공시 크롤링 (ETF 상장폐지/정리매매/투자유의)

---

## 구현 상태

### 완료된 기능
- KRX KIND API 호출 로직
- 공시 유형 분류 (DELISTING, LIQUIDATION, CAUTION, SURVEILLANCE)
- ETF 관련 공시 필터링
- DB 저장 (etf_disclosure 테이블)
- 중복 체크 (종목코드 + 공시제목 + 공시일)
- 알림 대기 공시 조회 (`get_pending_notifications`)
- 알림 발송 완료 처리 (`mark_as_notified`)

### 스케줄
- 매일 09:00 실행 (CronTrigger)
- 최근 7일간 공시 조회

---

## 테스트 결과

### API 접속 테스트
```
KRX KIND 메인 페이지: 200 OK (2149자)
KRX KIND API 엔드포인트: 200 OK (0자 - 빈 응답)
```

### 문제점
KRX KIND에서 자동화된 요청을 차단하고 있음:
- HTTP 요청 자체는 성공 (200 OK)
- 응답 본문이 비어있음 (0자)
- JavaScript 렌더링 또는 특별한 세션 처리 필요로 추정

### 시도한 방법
1. GET 요청 + 다양한 키워드 ("ETF", "ETF 상장폐지", "상장지수", "투자유의")
2. 세션 쿠키 획득 후 요청
3. POST 요청
4. Referer 헤더 추가

모든 방법에서 동일하게 빈 응답 반환

---

## 대안

### 1. Selenium/Playwright (브라우저 자동화)
- 장점: JavaScript 렌더링 가능, 실제 브라우저처럼 동작
- 단점: 리소스 사용량 높음, 속도 느림

### 2. KRX 공식 API
- URL: https://data.krx.co.kr
- 장점: 공식 지원, 안정적
- 단점: API 키 신청 필요, 승인 대기 시간

### 3. DART API (금융감독원 전자공시)
- URL: https://opendart.fss.or.kr
- 장점: 무료 API 키 발급, ETF 공시 포함
- 단점: KRX KIND와 데이터 범위 다를 수 있음

---

## 결론

- **코드 구현**: 완료
- **실제 동작**: KRX 봇 차단으로 인해 현재 불가
- **권장 조치**: KRX 공식 API 또는 DART API로 전환 검토

---

## 참고: 코드 위치

```
backend/data-service/
├── app/
│   ├── scrapers/
│   │   └── krx_scraper.py          # KRX 공시 스크래퍼
│   ├── models/
│   │   └── etf_disclosure.py       # 공시 모델
│   └── schedulers/
│       └── scheduler.py            # 스케줄러 (매일 09:00)
```
