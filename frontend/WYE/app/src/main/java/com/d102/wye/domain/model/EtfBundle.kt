package com.d102.wye.domain.model

enum class BundleType {
    STABLE_INCOME,
    HIGH_GROWTH,
    BALANCED,
    AGGRESSIVE,
    LONG_TERM,
    UNKNOWN;

    companion object {
        fun from(value: String): BundleType = entries.find {
            it.name.equals(value, ignoreCase = true)
        } ?: UNKNOWN
    }
}

data class EtfBundle(
    val id: Int,
    val name: String,
    val summary: String,
    val bundleType: BundleType,
    val tags: List<String>
)

data class EtfBundleDetail(
    val id: Int,
    val name: String,
    val description: String,
    val bundleType: BundleType,
    val etfItems: List<BundleEtfItem>
)

data class BundleEtfItem(
    val ticker: String,
    val name: String,
)