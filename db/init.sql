-- =============================================
-- What's Your ETF ERD (DDL)
-- Docker PostgreSQL 초기화용 / PostgreSQL
-- ERD.sql 기준으로 동기화 (2024)
-- =============================================

-- =============================================
-- 1. 사용자/인증
-- =============================================

CREATE TABLE "user" (
    "id" BIGSERIAL PRIMARY KEY,
    "email" VARCHAR(100) NOT NULL UNIQUE,
    "password" VARCHAR(255),                      -- 비밀번호 (nullable, 소셜만 사용 시 NULL)
    "nickname" VARCHAR(50) UNIQUE,                -- 닉네임 (신규 가입 시 이메일로 설정)
    "profile_image" VARCHAR(500),                 -- 프로필 이미지 URL (카카오 프로필 등)
    "is_active" BOOLEAN DEFAULT TRUE,
    "last_login_at" TIMESTAMP,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 소셜 계정 연동
CREATE TABLE "user_social_account" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "provider" VARCHAR(20) NOT NULL,              -- KAKAO
    "provider_user_id" VARCHAR(100) NOT NULL,     -- 소셜 서비스에서의 사용자 ID
    "email" VARCHAR(100),                         -- 소셜 계정 이메일
    "is_primary" BOOLEAN DEFAULT FALSE,           -- 주 로그인 수단 여부
    "linked_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_social_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_provider_user" UNIQUE ("provider", "provider_user_id")
);

-- 리프레시 토큰
CREATE TABLE "refresh_token" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "token" VARCHAR(500) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_revoked" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_refresh_token_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

-- 비밀번호 재설정 토큰
CREATE TABLE "password_reset_token" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "token" VARCHAR(255) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_used" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_reset_token_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

-- 이메일 인증 토큰
CREATE TABLE "email_verification_token" (
    "id" BIGSERIAL PRIMARY KEY,
    "email" VARCHAR(100) NOT NULL,
    "token" VARCHAR(255) NOT NULL UNIQUE,
    "expires_at" TIMESTAMP NOT NULL,
    "is_verified" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 로그인 이력
CREATE TABLE "login_history" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "provider" VARCHAR(20) NOT NULL,              -- KAKAO / EMAIL
    "login_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "ip_address" VARCHAR(45),
    "device_info" VARCHAR(200),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_login_history_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

-- =============================================
-- 2. 공통 카테고리
-- =============================================

-- 카테고리 (뉴스, 포트폴리오 등 공통 사용)
CREATE TABLE "category" (
    "code" VARCHAR(30) PRIMARY KEY,              -- NEWS_MACRO, NEWS_SEMI, PORTFOLIO_DIVIDEND 등
    "type" VARCHAR(20) NOT NULL,                 -- NEWS / PORTFOLIO / ETF
    "name" VARCHAR(50) NOT NULL,                 -- "금리/거시경제", "배당형"
    "description" VARCHAR(200),
    "display_order" INTEGER DEFAULT 0,
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 기본 카테고리 데이터 (14개 투자테마 기반)
INSERT INTO category (code, type, name, display_order) VALUES
-- 뉴스 카테고리 (투자테마 13개 + 시장/경제)
('NEWS_SEMI', 'NEWS', '반도체', 1),
('NEWS_IT', 'NEWS', 'IT/전자', 2),
('NEWS_BIO', 'NEWS', '바이오/의약', 3),
('NEWS_AUTO', 'NEWS', '자동차', 4),
('NEWS_CHEM', 'NEWS', '화학/소재', 5),
('NEWS_ENERGY', 'NEWS', '에너지', 6),
('NEWS_FINANCE', 'NEWS', '금융', 7),
('NEWS_CONSTRUCT', 'NEWS', '건설/부동산', 8),
('NEWS_CONSUMER', 'NEWS', '소비재', 9),
('NEWS_TELECOM', 'NEWS', '통신/미디어', 10),
('NEWS_TRANSPORT', 'NEWS', '운송/물류', 11),
('NEWS_INDUSTRY', 'NEWS', '산업재', 12),
('NEWS_ETC', 'NEWS', '기타', 13),
('NEWS_MARKET', 'NEWS', '시장/경제', 14),
-- 포트폴리오 카테고리
('PORTFOLIO_DIVIDEND', 'PORTFOLIO', '배당형', 1),
('PORTFOLIO_GROWTH', 'PORTFOLIO', '성장형', 2),
('PORTFOLIO_STABLE', 'PORTFOLIO', '안정형', 3),
('PORTFOLIO_THEME', 'PORTFOLIO', '테마형', 4);

-- =============================================
-- 3. 뉴스
-- =============================================

CREATE TABLE "news_article" (
    "id" BIGSERIAL PRIMARY KEY,
    "title" VARCHAR(500) NOT NULL,
    "content" TEXT,                               -- 뉴스 본문 전체
    "content_summary" JSONB,                      -- AI 핵심 요약 {"bullets": ["...", "...", "..."]}
    "source" VARCHAR(100),                        -- 언론사명
    "source_url" VARCHAR(1000) NOT NULL UNIQUE,   -- 원본 URL
    "thumbnail_url" VARCHAR(1000),
    "category_code" VARCHAR(30),                  -- category FK (NEWS_MACRO, NEWS_SEMI 등)
    "keywords" JSONB,                             -- 키워드 태그 ["금리동결", "나스닥", "빅테크"]
    "published_at" TIMESTAMP,
    "view_count" INTEGER DEFAULT 0,
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_news_category" FOREIGN KEY ("category_code") REFERENCES "category"("code") ON DELETE SET NULL
);

CREATE INDEX "idx_news_published_at" ON "news_article"("published_at" DESC);
CREATE INDEX "idx_news_category" ON "news_article"("category_code");

-- =============================================
-- 4. AI 피드백
-- =============================================

CREATE TABLE "ai_prompt" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL,                  -- 'portfolio_feedback', 'etf_analysis'
    "version" VARCHAR(20) NOT NULL,               -- 'v1.0', 'v1.1'
    "prompt_template" TEXT NOT NULL,              -- 프롬프트 내용
    "description" VARCHAR(200),                   -- 변경 사항 메모
    "is_active" BOOLEAN DEFAULT FALSE,            -- 현재 활성 버전
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "uk_prompt_version" UNIQUE ("name", "version")
);

CREATE INDEX "idx_prompt_active" ON "ai_prompt"("name", "is_active") WHERE "is_active" = TRUE;

CREATE TABLE "portfolio_ai_feedback" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "prompt_id" BIGINT,                           -- 사용된 프롬프트 FK
    -- 진단 결과 헤드라인
    "headline" VARCHAR(100),                      -- "공격적인 수익 추구!"
    "sub_headline" VARCHAR(200),                  -- "기술주 중심의 로켓 포트폴리오"
    "keywords" JSONB,                             -- ["기술주집중", "고변동성", "성장중심"]
    -- 상세 분석
    "analysis" TEXT,                              -- 종합 분석 결과 (요약 상세)
    "llm_model" VARCHAR(50),                      -- 사용된 LLM 모델
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_ai_feedback_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_ai_feedback_prompt" FOREIGN KEY ("prompt_id") REFERENCES "ai_prompt"("id")
);

CREATE INDEX "idx_ai_feedback_user" ON "portfolio_ai_feedback"("user_id");
CREATE INDEX "idx_ai_feedback_created" ON "portfolio_ai_feedback"("created_at" DESC);

-- =============================================
-- 5. 알림
-- =============================================

-- 알림 유형 코드 테이블
CREATE TABLE "alert_type" (
    "code" VARCHAR(50) PRIMARY KEY,              -- ETF_LISTING, ETF_DELISTING, PORTFOLIO_RETURN_5PCT 등
    "name" VARCHAR(100) NOT NULL,                -- "ETF 신규 상장"
    "category" VARCHAR(30) NOT NULL,             -- ETF / PORTFOLIO / NEWS / SYSTEM
    "setting_group" VARCHAR(30) NOT NULL,        -- 사용자 설정 그룹 (화면에 보이는 단위)
    "description" VARCHAR(200),                  -- 알림 설명
    "is_active" BOOLEAN DEFAULT TRUE,
    "display_order" INTEGER DEFAULT 0,           -- 노출 순서
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "alert_type" IS '알림 유형 코드 테이블 (확장 가능한 코드성 테이블)';
COMMENT ON COLUMN "alert_type"."setting_group" IS '사용자 설정 그룹: APP_NOTIFICATION, ETF_LISTING, ETF_DELISTING, PORTFOLIO_REBALANCING, PORTFOLIO_RETURN, NEWS_NOTIFICATION';

-- 알림 메시지 템플릿 (버전 관리)
CREATE TABLE "alert_message_template" (
    "id" BIGSERIAL PRIMARY KEY,
    "alert_type_code" VARCHAR(50) NOT NULL,      -- alert_type FK
    "version" VARCHAR(20) NOT NULL,              -- 'v1.0', 'v1.1'
    "title_template" VARCHAR(200) NOT NULL,      -- "신규 ETF 상장 알림"
    "message_template" TEXT NOT NULL,            -- "{etf_name} ETF가 {date}에 상장 예정입니다."
    "variables" JSONB,                           -- ["etf_name", "date"] - 사용 가능한 변수 목록
    "description" VARCHAR(200),                  -- 변경 사항 메모
    "is_active" BOOLEAN DEFAULT FALSE,           -- 현재 활성 버전
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_alert_template_type" FOREIGN KEY ("alert_type_code") REFERENCES "alert_type"("code") ON DELETE CASCADE,
    CONSTRAINT "uk_alert_template_version" UNIQUE ("alert_type_code", "version")
);

CREATE INDEX "idx_alert_template_active" ON "alert_message_template"("alert_type_code", "is_active") WHERE "is_active" = TRUE;

COMMENT ON TABLE "alert_message_template" IS '알림 메시지 템플릿 (버전 관리, ai_prompt와 유사)';

-- 사용자별 알림 설정 (alert_type 코드 참조)
CREATE TABLE "user_notification_setting" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "alert_type_code" VARCHAR(50) NOT NULL,           -- alert_type FK
    "is_enabled" BOOLEAN DEFAULT TRUE,                -- 알림 활성화 여부
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_notification_setting_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_notification_setting_type" FOREIGN KEY ("alert_type_code") REFERENCES "alert_type"("code") ON DELETE CASCADE,
    CONSTRAINT "uk_user_alert_type" UNIQUE ("user_id", "alert_type_code")
);

CREATE INDEX "idx_notification_setting_user" ON "user_notification_setting"("user_id");

COMMENT ON TABLE "user_notification_setting" IS '사용자별 알림 설정 (유형별 ON/OFF)';

CREATE TABLE "fcm_token" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "token" VARCHAR(500) NOT NULL,
    "device_type" VARCHAR(20) NOT NULL,           -- ANDROID / IOS / WEB
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_fcm_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "uq_fcm_user_device" UNIQUE ("user_id", "device_type")
);

CREATE INDEX "idx_fcm_user" ON "fcm_token"("user_id");
CREATE INDEX "idx_fcm_token" ON "fcm_token"("token");

-- 사용자 알림 (통합 알림 테이블)
CREATE TABLE "user_alert" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "alert_type_code" VARCHAR(50) NOT NULL,       -- alert_type FK
    -- 참조 대상 (다형성)
    "reference_type" VARCHAR(30),                 -- ETF / PORTFOLIO / NEWS / DISCLOSURE (NULL이면 시스템 알림)
    "reference_id" BIGINT,                        -- 참조 대상 ID (etf.id / portfolio.id / news_article.id 등)
    -- 알림 내용
    "title" VARCHAR(200) NOT NULL,
    "message" TEXT,
    -- 상태
    "is_read" BOOLEAN DEFAULT FALSE,
    "read_at" TIMESTAMP,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_user_alert_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_user_alert_type" FOREIGN KEY ("alert_type_code") REFERENCES "alert_type"("code") ON DELETE CASCADE
);

CREATE INDEX "idx_user_alert_user" ON "user_alert"("user_id");
CREATE INDEX "idx_user_alert_type" ON "user_alert"("alert_type_code");
CREATE INDEX "idx_user_alert_created" ON "user_alert"("created_at" DESC);
CREATE INDEX "idx_user_alert_unread" ON "user_alert"("user_id", "is_read") WHERE "is_read" = FALSE;
CREATE INDEX "idx_user_alert_ref" ON "user_alert"("reference_type", "reference_id");

COMMENT ON TABLE "user_alert" IS '사용자 알림 (ETF/포트폴리오/뉴스/시스템 통합)';

-- =============================================
-- 6. 데이터 소스
-- =============================================

CREATE TABLE "data_source" (
    "id" BIGSERIAL PRIMARY KEY,
    "source_name" VARCHAR(30),
    "url" VARCHAR(200),
    "is_active" BOOLEAN DEFAULT TRUE
);

-- =============================================
-- 7. 산업분류 / 회사정보 / 주식
-- =============================================

CREATE TABLE "industry_classification" (
    "code" VARCHAR(10) PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "level" INTEGER NOT NULL,                     -- 1=대, 2=중, 3=소, 4=세분류
    "parent_code" VARCHAR(10),
    "group_code" VARCHAR(10),
    "group_name" VARCHAR(50),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_industry_parent" FOREIGN KEY ("parent_code")
        REFERENCES "industry_classification"("code") ON DELETE SET NULL
);

CREATE INDEX "idx_industry_parent" ON "industry_classification"("parent_code");
CREATE INDEX "idx_industry_level" ON "industry_classification"("level");
CREATE INDEX "idx_industry_group" ON "industry_classification"("group_code");

COMMENT ON TABLE "industry_classification" IS '산업분류 코드 (셀프참조 트리, 대/중/소/세분류)';

CREATE TABLE "company_info" (
    "id" BIGSERIAL PRIMARY KEY,
    "industry_code" VARCHAR(10),                  -- industry_classification FK (소분류)
    "company_name" VARCHAR(100) NOT NULL,
    "industry_group" VARCHAR(50),                 -- 투자테마 그룹 (IT_SEMI, BIO 등)
    "ceo_name" VARCHAR(100),
    "homepage" VARCHAR(200),
    "region" VARCHAR(50),
    "description" TEXT,
    "corporation_number" VARCHAR(20),              -- 사업자등록번호
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_company_industry" FOREIGN KEY ("industry_code")
        REFERENCES "industry_classification"("code") ON DELETE SET NULL
);

CREATE INDEX "idx_company_industry_code" ON "company_info"("industry_code");
CREATE INDEX "idx_company_industry_group" ON "company_info"("industry_group");

COMMENT ON TABLE "company_info" IS '상장 회사 정보';

CREATE TABLE "stock" (
    "id" BIGSERIAL PRIMARY KEY,
    "company_id" BIGINT,                          -- company_info FK
    "ticker" VARCHAR(20) NOT NULL,
    "close" DECIMAL(14,2),
    "listing_date" DATE,
    "face_value" INTEGER,
    "listed_shares" BIGINT,
    "market_type" VARCHAR(20),                    -- KOSPI / KOSDAQ / NYSE / NASDAQ 등
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_stock_company" FOREIGN KEY ("company_id") REFERENCES "company_info"("id") ON DELETE SET NULL
);

CREATE INDEX "idx_stock_ticker" ON "stock"("ticker");
CREATE INDEX "idx_stock_company" ON "stock"("company_id");

CREATE TABLE "company_data_source" (
    "id" BIGSERIAL PRIMARY KEY,
    "company_info_id" BIGINT,                     -- company_info FK
    "data_source_id" BIGINT NOT NULL,             -- data_source FK
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_company_data_source_company" FOREIGN KEY ("company_info_id") REFERENCES "company_info"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_company_data_source_source" FOREIGN KEY ("data_source_id") REFERENCES "data_source"("id") ON DELETE CASCADE
);

-- =============================================
-- 8. ETF
-- =============================================

CREATE TABLE "etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "stock_code" VARCHAR(20) UNIQUE NOT NULL,
    "name" VARCHAR(200) NOT NULL,
    -- 분류
    "category" VARCHAR(50),                       -- 국내주식형/해외주식형/채권형/원자재형/통화형
    "strategy_type" VARCHAR(30),                  -- 시장 대표/테마형/배당형/채권형/기타
    "sector" VARCHAR(50),                         -- 반도체/2차전지/AI/배당 등
    "asset_class" VARCHAR(30),                    -- EQUITY/BOND/COMMODITY/MIXED
    "asset_manager" VARCHAR(50),                  -- KODEX/TIGER/KBSTAR 등
    -- 속성 플래그
    "is_leveraged" BOOLEAN DEFAULT FALSE,
    "is_inverse" BOOLEAN DEFAULT FALSE,
    "is_hedged" BOOLEAN,
    -- 비용/규모
    "expense_ratio" DECIMAL(6,4),
    "nav" DECIMAL(14,2),
    "aum" BIGINT,
    -- 배당
    "dividend_yield" DECIMAL(6,3),
    "dividend_freq" VARCHAR(10),                  -- MONTHLY/QUARTERLY/SEMI_ANNUAL/ANNUAL/NONE
    -- 밸류에이션
    "avg_per" DECIMAL(8,2),
    "avg_pbr" DECIMAL(8,2),
    "avg_roe" DECIMAL(8,2),
    -- 위험 지표
    "risk_grade" VARCHAR(20),                     -- HIGH_RISK/MODERATE/STABLE
    "volatility_1y" DECIMAL(8,4),                 -- 1년 변동성 (%, 연율화)
    -- 생애주기
    "listing_date" DATE,
    "delisted_date" DATE,
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX "idx_etf_category" ON "etf"("category", "is_active");
CREATE INDEX "idx_etf_strategy" ON "etf"("strategy_type", "is_active");
CREATE INDEX "idx_etf_risk" ON "etf"("risk_grade", "is_active");
CREATE INDEX "idx_etf_dividend" ON "etf"("dividend_freq", "is_active");
CREATE INDEX "idx_etf_manager" ON "etf"("asset_manager", "is_active");

-- ETF 공시 정보
CREATE TABLE "etf_disclosure" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT,                              -- etf FK (상장 전 공시는 NULL)
    "etf_code" VARCHAR(20) NOT NULL,              -- ETF 종목코드 (상장 전에도 필요)
    "etf_name" VARCHAR(200) NOT NULL,
    "disclosure_type" VARCHAR(50) NOT NULL,       -- delisting / liquidation / caution / surveillance
    "disclosure_title" TEXT NOT NULL,
    "disclosure_content" TEXT,
    "disclosure_date" DATE NOT NULL,
    "effective_date" DATE,
    "source_url" TEXT,
    "is_notified" BOOLEAN DEFAULT FALSE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_disclosure_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE SET NULL
);

CREATE INDEX "idx_disclosure_etf_id" ON "etf_disclosure"("etf_id");
CREATE INDEX "idx_disclosure_etf_code" ON "etf_disclosure"("etf_code");
CREATE INDEX "idx_disclosure_type" ON "etf_disclosure"("disclosure_type");
CREATE INDEX "idx_disclosure_date" ON "etf_disclosure"("disclosure_date" DESC);

COMMENT ON TABLE "etf_disclosure" IS 'ETF 공시 정보 (KRX KIND 크롤링)';

-- ETF 주식 구성종목
CREATE TABLE "etf_stock_composition" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "stock_id" BIGINT,                            -- stock FK
    "weight_pct" DECIMAL(6,3),
    "base_date" DATE NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_stock_composition_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_stock_composition_stock" FOREIGN KEY ("stock_id") REFERENCES "stock"("id") ON DELETE SET NULL
);

CREATE INDEX "idx_etf_stock_composition_etf" ON "etf_stock_composition"("etf_id", "base_date" DESC);
CREATE INDEX "idx_etf_stock_composition_stock" ON "etf_stock_composition"("stock_id");

-- ETF 비주식 구성종목 (선물, 채권, 현금 등)
CREATE TABLE "etf_other_composition" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "asset_type" VARCHAR(20),                     -- FUTURES / BOND / CASH / COMMODITY
    "asset_name" VARCHAR(50),                     -- "KOSPI200 선물", "국고채 3년"
    "identifier_type" VARCHAR(20),                -- ISIN / TICKER / CUSTOM
    "identifier_value" VARCHAR(30),               -- 식별값
    "weight" DECIMAL(6,3),
    "market_value" BIGINT,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_other_composition_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

-- ETF 일별 시세
CREATE TABLE "etf_prices" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "trade_date" DATE NOT NULL,
    "close" DECIMAL(14,2),
    "nav" DECIMAL(14,2),
    "volume" BIGINT,
    "change_rate" DECIMAL(8,4),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("etf_id", "trade_date"),
    CONSTRAINT "fk_etf_prices_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_etf_prices_etf_date" ON "etf_prices"("etf_id", "trade_date" DESC);

-- ETF 섹터 클러스터 (버블 시각화용, 현재 스냅샷)
CREATE TABLE "etf_sector_cluster" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "cluster_type" VARCHAR(20) NOT NULL,        -- GROUP_CODE / INDUSTRY / SUB_SECTOR / ASSET_TYPE
    "industry_code" VARCHAR(10),
    "industry_name" VARCHAR(100),
    "group_code" VARCHAR(20),
    "group_name" VARCHAR(50),
    "sub_sector" VARCHAR(100),
    "weight_pct" DECIMAL(6,3) NOT NULL,
    "stock_count" INTEGER,
    -- 시각화 좌표 (UMAP)
    "pos_x" DECIMAL(10,6),
    "pos_y" DECIMAL(10,6),
    "radius" DECIMAL(10,6),
    "distance_to_center" DECIMAL(10,6),
    "base_date" DATE NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_sector_cluster_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_sector_cluster_etf" ON "etf_sector_cluster"("etf_id");
CREATE INDEX "idx_sector_cluster_date" ON "etf_sector_cluster"("etf_id", "base_date" DESC);

-- ETF 섹터 버블 AI 분석 이력
CREATE TABLE "etf_sector_ai_history" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "group_code" VARCHAR(20) NOT NULL,          -- IT_SEMI, FINANCE 등
    "group_name" VARCHAR(50),
    -- 분석 시점 스냅샷
    "weight_pct" DECIMAL(6,3),                  -- 분석 시점 비중
    "stock_count" INTEGER,                      -- 분석 시점 종목 수
    "top_stocks" JSONB,                         -- 상위 종목 [{"name":"삼성전자","weight":25.0}, ...]
    -- AI 분석 결과
    "ai_analysis" TEXT NOT NULL,                -- 분석 결과 텍스트
    "prompt_id" BIGINT,                         -- 사용된 프롬프트 FK
    -- 시점
    "base_date" DATE NOT NULL,                  -- ETF 데이터 기준일
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_sector_ai_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_sector_ai_prompt" FOREIGN KEY ("prompt_id") REFERENCES "ai_prompt"("id")
);

CREATE INDEX "idx_sector_ai_etf_date" ON "etf_sector_ai_history"("etf_id", "base_date" DESC);
CREATE INDEX "idx_sector_ai_lookup" ON "etf_sector_ai_history"("etf_id", "group_code", "base_date" DESC);

-- ETF 주식 클러스터 매핑 (클러스터 태그 → 회사 목록 조회용)
CREATE TABLE "etf_stock_cluster_mapping" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "composition_id" BIGINT NOT NULL,            -- etf_stock_composition FK
    "sector_code" VARCHAR(20) NOT NULL,          -- industry_classification FK (Level 4)
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("etf_id", "composition_id"),
    CONSTRAINT "fk_stock_cluster_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_stock_cluster_comp" FOREIGN KEY ("composition_id") REFERENCES "etf_stock_composition"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_stock_cluster_sector" FOREIGN KEY ("sector_code") REFERENCES "industry_classification"("code")
);

-- ETF 비주식 클러스터 매핑
CREATE TABLE "etf_other_cluster_mapping" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "composition_id" BIGINT NOT NULL,            -- etf_other_composition FK
    "sector_code" VARCHAR(20) NOT NULL,          -- industry_classification FK
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("etf_id", "composition_id"),
    CONSTRAINT "fk_other_cluster_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_other_cluster_comp" FOREIGN KEY ("composition_id") REFERENCES "etf_other_composition"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_other_cluster_sector" FOREIGN KEY ("sector_code") REFERENCES "industry_classification"("code")
);

-- =============================================
-- 9. 뉴스-종목 매핑 (네이버 증권 종목뉴스 기반)
-- =============================================

CREATE TABLE "news_stock_mapping" (
    "id" BIGSERIAL PRIMARY KEY,
    "news_id" BIGINT NOT NULL,
    "company_id" BIGINT NOT NULL,                 -- 관련 종목 (company_info FK)
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_news_stock_news" FOREIGN KEY ("news_id") REFERENCES "news_article"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_news_stock_company" FOREIGN KEY ("company_id") REFERENCES "company_info"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_news_stock" UNIQUE ("news_id", "company_id")
);

CREATE INDEX "idx_news_stock_news" ON "news_stock_mapping"("news_id");
CREATE INDEX "idx_news_stock_company" ON "news_stock_mapping"("company_id");

COMMENT ON TABLE "news_stock_mapping" IS '뉴스-종목 매핑 (네이버 증권 종목뉴스 크롤링 결과)';

-- =============================================
-- 10. 사용자 ETF
-- =============================================

CREATE TABLE "user_favorite_etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_favorite_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_favorite_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_user_favorite_etf" UNIQUE ("user_id", "etf_id")
);

CREATE INDEX "idx_favorite_user" ON "user_favorite_etf"("user_id");

CREATE TABLE "user_holding_etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "quantity" INTEGER NOT NULL,
    "avg_price" DECIMAL(15,2),
    "synced_at" TIMESTAMP,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_holding_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_holding_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE,
    CONSTRAINT "uk_user_holding_etf" UNIQUE ("user_id", "etf_id")
);

CREATE INDEX "idx_holding_user" ON "user_holding_etf"("user_id");

-- =============================================
-- 11. 꾸러미 (시스템 제공 포트폴리오)
-- =============================================

CREATE TABLE "preset_portfolios" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "short_description" VARCHAR(200),
    "description" TEXT,
    "category_code" VARCHAR(30),                  -- category FK (PORTFOLIO_DIVIDEND 등)
    "display_order" INTEGER DEFAULT 0,
    "is_active" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_preset_category" FOREIGN KEY ("category_code") REFERENCES "category"("code") ON DELETE SET NULL
);

CREATE TABLE "preset_portfolio_etfs" (
    "id" BIGSERIAL PRIMARY KEY,
    "preset_portfolio_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("preset_portfolio_id", "etf_id"),
    CONSTRAINT "fk_preset_portfolio" FOREIGN KEY ("preset_portfolio_id") REFERENCES "preset_portfolios"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_preset_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

-- =============================================
-- 12. 사용자 포트폴리오
-- =============================================

CREATE TABLE "portfolio" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "name" VARCHAR(100) NOT NULL,
    "description" TEXT,
    "invest_amount" DECIMAL(15,2),
    "snapshot_etfs" TEXT,
    "snapshot_metrics" TEXT,
    "is_alert_enabled" BOOLEAN DEFAULT FALSE,
    "current_return" DECIMAL(8,4),
    "prev_close_value" DECIMAL(15,2),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_portfolio_user" FOREIGN KEY ("user_id") REFERENCES "user"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_portfolio_user" ON "portfolio"("user_id");

CREATE TABLE "portfolio_etf" (
    "id" BIGSERIAL PRIMARY KEY,
    "portfolio_id" BIGINT NOT NULL,
    "etf_id" BIGINT NOT NULL,
    "weight_pct" DECIMAL(6,3) NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("portfolio_id", "etf_id"),
    CONSTRAINT "fk_portfolio_etf_portfolio" FOREIGN KEY ("portfolio_id") REFERENCES "portfolio"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_portfolio_etf_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id") ON DELETE CASCADE
);

-- =============================================
-- 13. 초기 데이터 (코드 테이블)
-- =============================================

-- 알림 유형 코드 초기 데이터
INSERT INTO "alert_type" ("code", "name", "category", "setting_group", "description", "display_order") VALUES
-- 앱 알림
('SYSTEM_ANNOUNCEMENT', '시스템 공지', 'SYSTEM', 'APP_NOTIFICATION', '서비스 공지사항', 1),
-- ETF 상장 알림
('ETF_LISTING', 'ETF 신규 상장', 'ETF', 'ETF_LISTING', '새로운 ETF가 상장되었습니다', 10),
-- ETF 상장폐지 알림
('ETF_DELISTING_SCHEDULED', 'ETF 상장폐지 예정', 'ETF', 'ETF_DELISTING', 'ETF 상장폐지가 예정되어 있습니다', 20),
('ETF_DELISTING_COMPLETED', 'ETF 상장폐지 완료', 'ETF', 'ETF_DELISTING', 'ETF 상장폐지가 완료되었습니다', 21),
-- 포트폴리오 리밸런싱 알림
('PORTFOLIO_REBALANCING_SCHEDULED', '포트폴리오 리밸런싱 예정', 'PORTFOLIO', 'PORTFOLIO_REBALANCING', '포함된 ETF 리밸런싱이 예정되어 있습니다', 30),
('PORTFOLIO_REBALANCING_COMPLETED', '포트폴리오 리밸런싱 완료', 'PORTFOLIO', 'PORTFOLIO_REBALANCING', '포함된 ETF 리밸런싱이 완료되었습니다', 31),
-- 포트폴리오 수익률 알림
('PORTFOLIO_RETURN_5PCT', '포트폴리오 수익률 5%', 'PORTFOLIO', 'PORTFOLIO_RETURN', '포트폴리오 수익률이 5%에 도달했습니다', 40),
('PORTFOLIO_RETURN_10PCT', '포트폴리오 수익률 10%', 'PORTFOLIO', 'PORTFOLIO_RETURN', '포트폴리오 수익률이 10%에 도달했습니다', 41),
('PORTFOLIO_LOSS_5PCT', '포트폴리오 손실률 -5%', 'PORTFOLIO', 'PORTFOLIO_RETURN', '포트폴리오 손실률이 -5%에 도달했습니다', 42),
('PORTFOLIO_LOSS_10PCT', '포트폴리오 손실률 -10%', 'PORTFOLIO', 'PORTFOLIO_RETURN', '포트폴리오 손실률이 -10%에 도달했습니다', 43),
-- 뉴스 수신 알림
('NEWS_ETF_RELATED', '관심 ETF 뉴스', 'NEWS', 'NEWS_NOTIFICATION', '관심 ETF와 관련된 뉴스가 있습니다', 50);

-- 알림 메시지 템플릿 초기 데이터
INSERT INTO "alert_message_template" ("alert_type_code", "version", "title_template", "message_template", "variables", "is_active") VALUES
-- ETF 관련
('ETF_LISTING', 'v1.0', '신규 ETF 상장 알림', '{etf_name} ETF가 {date}에 상장 예정입니다.', '["etf_name", "date"]', TRUE),
('ETF_DELISTING_SCHEDULED', 'v1.0', 'ETF 상장폐지 예정', '관심 ETF ''{etf_name}''가 {date}에 상장폐지 예정입니다.', '["etf_name", "date"]', TRUE),
('ETF_DELISTING_COMPLETED', 'v1.0', 'ETF 상장폐지 완료', '관심 ETF ''{etf_name}''가 상장폐지되었습니다.', '["etf_name"]', TRUE),
-- 포트폴리오 관련
('PORTFOLIO_REBALANCING_SCHEDULED', 'v1.0', '포트폴리오 리밸런싱 예정', '''{portfolio_name}'' 포함 ETF ''{etf_name}'' 리밸런싱이 {date}에 예정되어 있습니다.', '["portfolio_name", "etf_name", "date"]', TRUE),
('PORTFOLIO_REBALANCING_COMPLETED', 'v1.0', '포트폴리오 리밸런싱 완료', '''{portfolio_name}'' 포함 ETF ''{etf_name}'' 리밸런싱이 완료되었습니다.', '["portfolio_name", "etf_name"]', TRUE),
('PORTFOLIO_RETURN_5PCT', 'v1.0', '포트폴리오 수익률 알림', '''{portfolio_name}'' 수익률이 +5%를 달성했습니다!', '["portfolio_name"]', TRUE),
('PORTFOLIO_RETURN_10PCT', 'v1.0', '포트폴리오 수익률 알림', '''{portfolio_name}'' 수익률이 +10%를 달성했습니다!', '["portfolio_name"]', TRUE),
('PORTFOLIO_LOSS_5PCT', 'v1.0', '포트폴리오 손실률 알림', '''{portfolio_name}'' 손실률이 -5%에 도달했습니다.', '["portfolio_name"]', TRUE),
('PORTFOLIO_LOSS_10PCT', 'v1.0', '포트폴리오 손실률 알림', '''{portfolio_name}'' 손실률이 -10%에 도달했습니다.', '["portfolio_name"]', TRUE),
-- 뉴스 관련
('NEWS_ETF_RELATED', 'v1.0', '관심 ETF 뉴스', '관심 ETF ''{etf_name}'' 관련 뉴스: {news_title}', '["etf_name", "news_title"]', TRUE),
-- 시스템
('SYSTEM_ANNOUNCEMENT', 'v1.0', '{title}', '{message}', '["title", "message"]', TRUE);
