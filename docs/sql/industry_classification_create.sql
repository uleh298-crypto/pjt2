-- =============================================
-- 표준산업분류코드 테이블 생성 (PostgreSQL)
-- industry_classification_seed.sql 실행 전에 이 파일 먼저 실행
-- =============================================
--
-- group_code 유효 값 (21개 + 기타 3개):
-- 주요: IT_SEMI, IT_ELEC, IT_SW, ENERGY, AUTO, BIO, CHEM, STEEL,
--       MACHINERY, CONSTRUCT, FINANCE, INSURANCE, RETAIL, FOOD,
--       CONSUMER, TELECOM, TRANSPORT, SHIPBUILD, DEFENSE, HOLDING, EVENT
-- 기타: AGRI, MINING, ETC
-- =============================================

CREATE TABLE "industry_classification" (
    "code" VARCHAR(10) PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "level" INTEGER NOT NULL,                    -- 1:대분류, 2:중분류, 3:소분류, 4:세분류
    "parent_code" VARCHAR(10),                   -- 상위 분류 코드 (셀프 참조)
    "group_code" VARCHAR(10),                    -- ETF 클러스터용 그룹 코드
    "group_name" VARCHAR(50),                    -- 그룹 한글명
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "fk_industry_parent" FOREIGN KEY ("parent_code")
        REFERENCES "industry_classification"("code") ON DELETE SET NULL
);

CREATE INDEX "idx_industry_parent" ON "industry_classification"("parent_code");
CREATE INDEX "idx_industry_level" ON "industry_classification"("level");
CREATE INDEX "idx_industry_group" ON "industry_classification"("group_code");
