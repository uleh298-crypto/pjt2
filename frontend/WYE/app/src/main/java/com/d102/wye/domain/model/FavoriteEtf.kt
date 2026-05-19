package com.d102.wye.domain.model

data class FavoriteEtfList(
    val favorites: List<FavoriteEtf>,
    val totalCount: Int
)

data class FavoriteEtf(
    val ticker: String,
    val name: String,
    val riskType: String?,
    val assetManager: String,
    val currentPrice: Long,
    val changeRate: Double,
    val favoritedAt: String
)

enum class FavoriteEtfSort(val queryValue: String) {
    RECENT("RECENT"),
    CHANGE_RATE_DESC("CHANGE_RATE_DESC"),
    CHANGE_RATE_ASC("CHANGE_RATE_ASC"),
    NAME_ASC("NAME_ASC")
}
