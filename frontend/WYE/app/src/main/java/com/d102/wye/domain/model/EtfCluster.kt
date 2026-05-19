package com.d102.wye.domain.model

// GET /api/v1/etfs/{ticker}/clusters 응답 최상위
data class EtfClusterData(
    val englishName: String,
    val sectors: List<EtfCluster>,
    val influentialStocks: List<InfluentialStock>,
)

// 섹터 클러스터 항목
data class EtfCluster(
    val name: String,
    val percentage: Double,
    val stocks: List<EtfClusterStock>,
    val aiAnalysis: String?,
    val assetType: String?,
)

data class EtfClusterStock(
    val ticker: String,
    val name: String,
    val percentage: Double,
)
