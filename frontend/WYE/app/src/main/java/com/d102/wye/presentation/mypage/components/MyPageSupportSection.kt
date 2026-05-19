package com.d102.wye.presentation.mypage.components

import androidx.compose.foundation.lazy.LazyListScope
import com.d102.wye.presentation.designsystem.WyeListItem

fun LazyListScope.myPageSupportSection(
    onFaqClick: () -> Unit,
    onTermsClick: () -> Unit
) {
    item { MyPageSectionTitle(title = "지원센터") }

    item {
        WyeListItem(
            title = "자주 묻는 질문",
            showArrow = true,
            showDivider = false,
            onClick = onFaqClick
        )
    }

    item {
        WyeListItem(
            title = "이용약관",
            showArrow = true,
            showDivider = false,
            onClick = onTermsClick
        )
    }
}
