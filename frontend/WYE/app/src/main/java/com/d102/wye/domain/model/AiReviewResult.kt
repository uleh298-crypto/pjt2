package com.d102.wye.domain.model

// AI 진단 결과 모델
data class AiReviewResult(
    val mainTitle: String,    // ex: "공격적인 수익 추구!"
    val subTitle: String,     // ex: "기술주 중심의 로켓 포트폴리오 🚀"
    val tags: List<String>,   // ex: ["#기술주집중", "#고변동성", "#성장중심"]
    val feedback: String      // ex: "현재 포트폴리오는 특정 섹터에 집중되어 있어..."
)