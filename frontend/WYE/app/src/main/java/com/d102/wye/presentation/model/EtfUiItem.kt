package com.d102.wye.presentation.model

import android.graphics.Color
import androidx.annotation.DrawableRes
import com.d102.wye.domain.model.Etf

data class EtfUiItem(
    val etf: Etf,                           // domain 타입 참조
    val isLiked: Boolean,
    val riskTagColor: Color,
    @DrawableRes val sectorIcon: Int
)