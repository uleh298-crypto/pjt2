# 안드로이드 API 명세

> 안드로이드 화면 기준 실제 필요한 API만 정리

---

## 문서 목록

| 번호 | 파일 | 화면 | API 개수 |
|------|------|------|----------|
| 01 | [인증.md](01_인증.md) | LoginScreen | 2개 |
| 02 | [뉴스.md](02_뉴스.md) | NewsListScreen, NewsDetailScreen, PortfolioDetailScreen | 4개 |
| 03 | [ETF_클러스터.md](03_ETF_클러스터.md) | EtfDetailScreen, ClusterTab | 1개 |
| 04 | [AI_포트폴리오_진단.md](04_AI_포트폴리오_진단.md) | AiDiagnosisDialog | 1개 |
| 05 | [알림.md](05_알림.md) | NotificationScreen, NotificationSettingsScreen | 5개 |
| 07 | [종목.md](07_종목.md) | StockDetailScreen | 2개 |

---

## API 엔드포인트 요약

### 인증
| Method | Endpoint | 설명 | 백엔드 | 안드로이드 |
|--------|----------|------|:------:|:----------:|
| POST | `/api/v1/auth/oauth/kakao` | 카카오 로그인 | O | X |
| POST | `/api/v1/auth/fcm/token` | FCM 토큰 등록 | O | X |

### 뉴스
| Method | Endpoint | 설명 | 백엔드 | 안드로이드 |
|--------|----------|------|:------:|:----------:|
| GET | `/api/v1/news` | 뉴스 목록 | O | X |
| GET | `/api/v1/news/{newsId}` | 뉴스 상세 (relatedEtfs 포함) | O | X |
| GET | `/api/v1/news/search` | 뉴스 검색 | O | X |
| GET | `/api/v1/news/portfolio/{portfolioId}` | 포트폴리오 관련 뉴스 | O | X |

### ETF 클러스터
| Method | Endpoint | 설명 | 백엔드 | 안드로이드 |
|--------|----------|------|:------:|:----------:|
| GET | `/api/v1/etf/{ticker}` | ETF 상세 + 클러스터 | O | O* |

*안드로이드 API 경로 수정 필요

### AI
| Method | Endpoint | 설명 | 백엔드 | 안드로이드 |
|--------|----------|------|:------:|:----------:|
| POST | `/api/v1/ai/portfolio/review` | 포트폴리오 진단 | O | X |

### 종목
| Method | Endpoint | 설명 | 백엔드 | 안드로이드 |
|--------|----------|------|:------:|:----------:|
| GET | `/api/v1/stocks/{ticker}/tags` | 종목 태그 | O | X |
| GET | `/api/v1/stocks/{ticker}/related` | 관련 종목 | O | X |

### 알림
| Method | Endpoint | 설명 | 백엔드 | 안드로이드 |
|--------|----------|------|:------:|:----------:|
| GET | `/api/v1/alerts` | 알림 목록 | O | X |
| GET | `/api/v1/alerts/unread/count` | 읽지 않은 알림 수 | O | X |
| PUT | `/api/v1/alerts/{alertId}/read` | 알림 읽음 처리 | O | X |
| GET | `/api/v1/alerts/settings` | 알림 설정 조회 | O | X |
| PUT | `/api/v1/alerts/settings` | 알림 설정 수정 | O | X |

---

## 구현 현황 요약

### 백엔드 (user-service)
- [x] 카카오 로그인 API
- [x] FCM 토큰 등록 API
- [x] 뉴스 목록/상세/검색 API (relatedEtfs 포함)
- [x] ETF 상세 API (클러스터 + 영향력 종목)
- [x] AI 포트폴리오 진단 API
- [x] 종목 태그/관련 종목 API

### 안드로이드 (WYE)
- [ ] 카카오 로그인 연동
- [ ] FCM 토큰 등록 (로그인 직후)
- [ ] 뉴스 API 서비스/DTO
- [x] ETF API 서비스/DTO (경로 수정 필요)
- [ ] AI API 서비스/DTO
- [ ] 종목 API 서비스/DTO

---

## 우선순위

1. ~~**[백엔드]** ETF 상세 API 구현~~ (완료)
2. ~~**[백엔드]** 뉴스 상세에 relatedEtfs 필드 추가~~ (완료)
3. **[안드로이드]** AuthApiService에 kakaoLogin() 추가
4. **[안드로이드]** NewsApiService 생성
5. **[안드로이드]** AiApiService 생성
6. **[안드로이드]** EtfApiService 경로 수정 (`/api/v1/etf/`)
