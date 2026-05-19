package com.d102.wye.presentation.mypage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.d102.wye.presentation.theme.BackGroundLightGreen
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun MyPageProfileHeader(
    nickname: String,
    profileImage: String?,
    onProfileImageChangeClick: () -> Unit,
    onProfileImageDeleteClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(BackGroundLightGreen),
                contentAlignment = Alignment.Center
            ) {
                if (profileImage.isNullOrBlank()) {
                    Text(
                        text = "WYE",
                        style = MaterialTheme.typography.titleSmall,
                        color = PrimaryGreen
                    )
                } else {
                    AsyncImage(
                        model = profileImage,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { isMenuExpanded = true }
                )
            }

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                offset = DpOffset(x = 90.dp, y = (-12).dp),
                containerColor = SurfaceWhite
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "이미지 변경",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                        onProfileImageChangeClick()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "이미지 삭제",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (profileImage.isNullOrBlank()) TextSecondary else EtfRise
                        )
                    },
                    enabled = !profileImage.isNullOrBlank(),
                    onClick = {
                        isMenuExpanded = false
                        onProfileImageDeleteClick()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = nickname,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
