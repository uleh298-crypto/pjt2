-- =============================================
-- 누락된 industry_code 추가 (company_classification.json 기준)
-- =============================================

INSERT INTO "industry_classification" ("code", "name", "level", "parent_code", "group_code", "group_name") VALUES
-- 자동차
('AUTO_PART', '자동차 부품', 4, 'C30300', 'AUTO', '자동차'),

-- 바이오
('BIO_PHAR', '제약/바이오', 4, 'C21200', 'BIO', '바이오/의약'),

-- 화학 (JSON 네이밍)
('CHEM_COS', '화장품/뷰티', 4, 'C20400', 'CHEM', '화학/소재'),
('CHEM_PET', '석유화학', 4, 'C20100', 'CHEM', '화학/소재'),

-- 소비재 (JSON 네이밍)
('CONS_FAS', '패션/의류', 4, 'C14100', 'CONSUMER', '소비재'),
('CONS_SPO', '스포츠/레저', 4, 'R91100', 'CONSUMER', '소비재'),
('CONS_TRV', '여행/관광', 4, 'I55100', 'CONSUMER', '소비재'),

-- 건설
('CON_INFRA', '건설 인프라', 4, 'F41200', 'CONSTRUCT', '건설'),
('CON_REIT', '리츠/부동산', 4, 'L68100', 'CONSTRUCT', '건설'),

-- 디스플레이
('DISP_LED', 'LED/조명', 4, 'C28400', 'IT_ELEC', '전자/IT'),

-- 식품
('FOOD_GEN', '일반 식품', 4, 'C10700', 'FOOD', '식품/음료'),

-- IT/전자
('IT_PHONE', '스마트폰/휴대폰', 4, 'C26400', 'IT_ELEC', '전자/IT'),
('IT_SEC', 'IT보안/정보보안', 4, 'J58200', 'IT_SW', '소프트웨어'),

-- 기계
('MACH_3D', '3D 프린팅', 4, 'C29200', 'MACHINERY', '기계'),

-- 유통
('RET_CVS', '편의점/소매', 4, 'G47200', 'RETAIL', '유통/소매'),

-- 반도체
('SEMI_CXL', 'CXL (Compute Express Link)', 4, 'C26100', 'IT_SEMI', '반도체'),

-- 조선
('SHIP_GEN', '일반 조선', 4, 'C31100', 'SHIPBUILD', '조선'),

-- 철강
('STL_NF', '비철금속', 4, 'C24200', 'STEEL', '철강/금속'),

-- 소프트웨어
('SW_SI', 'SI/시스템통합', 4, 'J62000', 'IT_SW', '소프트웨어'),

-- 운송
('TRANS_SEA', '해운/해상운송', 4, 'H50100', 'TRANSPORT', '운송')

ON CONFLICT (code) DO NOTHING;
