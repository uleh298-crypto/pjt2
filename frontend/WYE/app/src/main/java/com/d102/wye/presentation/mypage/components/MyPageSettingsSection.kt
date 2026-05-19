package com.d102.wye.presentation.mypage.components

import androidx.compose.foundation.lazy.LazyListScope
import com.d102.wye.presentation.designsystem.WyeListItem

fun LazyListScope.myPageSettingsSection(
    onAlertSettingClick: () -> Unit,
) {
    item { MyPageSectionTitle(title = "설정") }

    item {
        WyeListItem(
            title = "알림 설정",
            showArrow = true,
            showDivider = false,
            onClick = onAlertSettingClick
        )
    }
}
