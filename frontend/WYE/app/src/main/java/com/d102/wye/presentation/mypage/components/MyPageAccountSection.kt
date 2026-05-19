package com.d102.wye.presentation.mypage.components

import androidx.compose.foundation.lazy.LazyListScope
import com.d102.wye.presentation.designsystem.WyeListItem

fun LazyListScope.myPageAccountSection(
    onNicknameChangeClick: () -> Unit,
    onPasswordChangeClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    item { MyPageSectionTitle(title = "사용자 계정 관리") }

    item {
        WyeListItem(
            title = "닉네임 변경",
            showArrow = true,
            showDivider = false,
            onClick = onNicknameChangeClick
        )
    }

    item {
        WyeListItem(
            title = "비밀번호 변경",
            showArrow = true,
            showDivider = false,
            onClick = onPasswordChangeClick
        )
    }

    item {
        WyeListItem(
            title = "로그아웃",
            showArrow = true,
            showDivider = false,
            onClick = onLogoutClick
        )
    }
}
