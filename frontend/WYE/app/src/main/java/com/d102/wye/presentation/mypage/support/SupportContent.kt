package com.d102.wye.presentation.mypage.support

data class FaqCategory(
    val key: String,
    val label: String
)

data class FaqItem(
    val id: String,
    val categoryKey: String,
    val question: String,
    val answer: String
)

data class TermsSection(
    val title: String,
    val body: String
)

internal val faqCategories = listOf(
    FaqCategory(key = "all", label = "전체"),
    FaqCategory(key = "service", label = "서비스 이용"),
    FaqCategory(key = "account", label = "계정/보안"),
    FaqCategory(key = "etf", label = "ETF 가이드"),
    FaqCategory(key = "portfolio", label = "포트폴리오"),
)

internal val faqItems = listOf(
    FaqItem(
        id = "real_time_trade",
        categoryKey = "service",
        question = "ETF 실시간 매수/매도 방법이 궁금해요.",
        answer = "What's Your ETF 앱에서는 국내 및 해외 주요 ETF를 실시간으로 탐색하고 비교할 수 있습니다. 다만 실제 매매는 증권사 계좌 연동 이후 지원될 예정이며, 현재는 ETF 정보 조회와 시뮬레이션 중심으로 서비스를 제공합니다. 탐색 화면에서 ETF를 고른 뒤 상세 정보와 과거 흐름을 먼저 확인해 보세요."
    ),
    FaqItem(
        id = "premium_cancel",
        categoryKey = "service",
        question = "정기 구독 멤버십 해지는 어떻게 하나요?",
        answer = "현재 WYE는 프로젝트 시연 단계로 운영되고 있어 별도 유료 멤버십이나 정기 구독 결제 기능은 제공하지 않습니다. 추후 멤버십이 도입되면 앱 내 구독 관리 화면과 스토어 결제 관리 페이지를 통해 해지할 수 있도록 안내할 예정입니다."
    ),
    FaqItem(
        id = "tax",
        categoryKey = "etf",
        question = "해외 ETF 배당금 세금은 얼마인가요?",
        answer = "해외 자산에 투자하는 ETF는 기초 자산, 상장 시장, 과세 방식에 따라 세금이 달라질 수 있습니다. WYE에서는 배당 주기와 배당률, 운용 방식 정보를 함께 제공해 사용자가 비교할 수 있게 돕고 있으며, 실제 세율과 신고 방식은 증권사 안내 및 최신 세법을 함께 확인하는 것을 권장합니다."
    ),
    FaqItem(
        id = "portfolio_build",
        categoryKey = "portfolio",
        question = "개인 맞춤형 포트폴리오는 어떻게 구성되나요?",
        answer = "포트폴리오는 투자 기간, 목표 금액, 위험 성향, ETF 비중 조절 값을 바탕으로 직접 구성하는 방식입니다. WYE는 ETF 간 상관관계, 섹터 분포, 수익률과 변동성 지표를 함께 보여주어 사용자가 스스로 조합을 판단할 수 있도록 설계되어 있습니다."
    ),
    FaqItem(
        id = "password_lost",
        categoryKey = "account",
        question = "비밀번호를 잊어버렸어요.",
        answer = "로그인 화면의 비밀번호 재설정 메뉴에서 이메일 인증을 진행하면 새 비밀번호를 설정할 수 있습니다. 현재 화면 구현 단계에서는 인증 절차가 mock 상태이지만, 이후 API 연동 시 인증 코드 확인과 비밀번호 변경이 실제로 처리될 예정입니다."
    ),
    FaqItem(
        id = "guidebook_offline",
        categoryKey = "etf",
        question = "ETF 가이드북을 오프라인으로 볼 수 있나요?",
        answer = "앱 내 ETF 가이드와 설명 콘텐츠는 현재 온라인 기준으로 제공됩니다. 다만 자주 보는 ETF 정보와 관심 목록은 저장 기능을 통해 빠르게 다시 확인할 수 있도록 확장할 계획이며, 오프라인 문서 제공 여부는 추후 정책에 따라 결정됩니다."
    ),
    FaqItem(
        id = "news_alert",
        categoryKey = "portfolio",
        question = "보유 ETF 관련 뉴스 알림은 어떻게 받나요?",
        answer = "마이페이지의 알림 설정에서 앱 알림, ETF 상장·상장폐지 알림, 포트폴리오 수익률 및 뉴스 수신 알림을 켜면 됩니다. 이후 백엔드 연동이 완료되면 보유 또는 관심 ETF와 관련된 뉴스가 수집될 때 사용자 설정에 따라 알림을 발송할 예정입니다."
    ),
)

internal val termsSections = listOf(
    TermsSection(
        title = "제1조 (목적)",
        body = "본 약관은 What's Your ETF(이하 \"회사\")가 제공하는 ETF 탐색, 포트폴리오 시뮬레이션, 뉴스 분석 및 회원 관련 서비스의 이용 조건과 절차, 회사와 회원의 권리·의무 및 책임 사항을 규정함을 목적으로 합니다."
    ),
    TermsSection(
        title = "제2조 (용어의 정의)",
        body = "1. \"서비스\"란 회원이 모바일 앱에서 이용할 수 있는 ETF 정보 조회, 관심 ETF 관리, 포트폴리오 구성, 시뮬레이션, 뉴스 연계 분석, 알림 기능을 의미합니다.\n\n2. \"회원\"이란 본 약관에 동의하고 회사가 정한 절차에 따라 가입한 자를 말합니다.\n\n3. \"포트폴리오\"란 회원이 목표 금액, 투자 성향, ETF 비중 등을 기준으로 구성한 가상의 투자 조합을 말합니다.\n\n4. \"관심 ETF\"란 회원이 별도로 저장한 ETF 목록을 의미하며, 향후 알림 및 맞춤 추천 기능의 기준 데이터로 활용될 수 있습니다."
    ),
    TermsSection(
        title = "제3조 (약관의 게시와 개정)",
        body = "회사는 회원이 쉽게 확인할 수 있도록 서비스 내 설정 또는 지원센터 화면에 본 약관을 게시합니다. 회사는 서비스 구조 변경, 법령 개정, 기능 추가에 따라 약관을 변경할 수 있으며, 중요한 변경이 있는 경우 적용일 이전에 앱 공지 또는 화면 안내를 통해 사전 고지합니다."
    ),
    TermsSection(
        title = "제4조 (서비스의 제공 및 변경)",
        body = "회사는 다음 각 호의 서비스를 제공합니다.\n\n• ETF 목록 및 상세 정보 조회\n• ETF 간 관계 탐색 및 관심 ETF 저장\n• 목표 기반 포트폴리오 구성 및 과거 데이터 기반 시뮬레이션\n• 뉴스 기반 ETF 영향도 분석 정보 제공\n• 회원 정보 관리, 비밀번호 재설정, 알림 설정 기능\n\n회사는 운영상 또는 기술상 필요에 따라 서비스의 일부를 변경하거나 중단할 수 있으며, 그 경우 사전에 가능한 범위에서 안내합니다."
    ),
    TermsSection(
        title = "제5조 (회원의 의무)",
        body = "회원은 관계 법령, 본 약관 및 서비스 내 안내 사항을 준수해야 합니다. 회원은 타인의 계정을 도용하거나 비정상적인 방법으로 서비스를 이용해서는 안 되며, 시뮬레이션 결과나 제공 정보를 투자 수익 보장의 근거로만 사용해서는 안 됩니다. 회원은 계정 정보와 인증 수단을 스스로 관리할 책임이 있습니다."
    ),
    TermsSection(
        title = "제6조 (투자 정보에 대한 면책)",
        body = "회사가 제공하는 ETF 정보, 뉴스 요약, 시뮬레이션 결과, 포트폴리오 분석 내용은 투자 판단을 돕기 위한 참고 자료이며 특정 금융상품의 매수·매도 추천이나 수익 보장을 의미하지 않습니다. 실제 투자 판단과 그 결과에 대한 책임은 회원 본인에게 있으며, 회사는 시장 상황, 외부 데이터 오류, 지연 등으로 발생한 손실에 대해 법령상 허용되는 범위 내에서 책임을 제한할 수 있습니다."
    ),
    TermsSection(
        title = "제7조 (개인정보 및 알림)",
        body = "회사는 회원 식별, 로그인 유지, 비밀번호 재설정, 관심 ETF 및 포트폴리오 저장, 사용자 맞춤 알림 제공을 위해 필요한 최소한의 정보를 처리할 수 있습니다. 회원은 알림 설정 화면에서 앱 공지, ETF 상장·상장폐지, 포트폴리오 변동, 뉴스 알림 수신 여부를 직접 변경할 수 있습니다."
    ),
    TermsSection(
        title = "제8조 (서비스 이용 제한 및 종료)",
        body = "회사는 회원이 본 약관 또는 관계 법령을 위반하는 경우 서비스 이용을 제한할 수 있습니다. 회원은 언제든지 서비스 이용을 중단하거나 로그아웃할 수 있으며, 계정 삭제 기능이 제공되는 경우 해당 절차에 따라 탈퇴를 요청할 수 있습니다."
    ),
    TermsSection(
        title = "부칙",
        body = "본 약관은 2024년 5월 20일부터 적용합니다.\n\n현재 약관은 프로젝트 시연용 초안으로, 정식 서비스 전 법률 검토 결과에 따라 변경될 수 있습니다."
    ),
)
