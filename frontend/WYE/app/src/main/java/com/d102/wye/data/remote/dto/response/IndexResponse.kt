package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class IndexPageResponse(
    @SerializedName("content")       val content:       List<IndexItemResponse>,
    @SerializedName("pageNumber")    val pageNumber:    Int,
    @SerializedName("pageSize")      val pageSize:      Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages")    val totalPages:    Int,
    @SerializedName("last")          val last:          Boolean,
)

data class IndexItemResponse(
    @SerializedName("close")       val close:       Double,
    @SerializedName("marketType")  val marketType:  String,
    @SerializedName("tradingDate") val tradingDate: String,
)
