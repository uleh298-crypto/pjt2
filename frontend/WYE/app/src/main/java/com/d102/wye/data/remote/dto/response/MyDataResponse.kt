package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class MyDataHoldingResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("counts") val counts: Int,
)
