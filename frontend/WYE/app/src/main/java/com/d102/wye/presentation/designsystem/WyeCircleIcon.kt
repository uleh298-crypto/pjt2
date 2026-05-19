package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.PrimaryGreen

@Composable
fun WyeCircleIcon(
    modifier: Modifier = Modifier,
    tag: String,
    iconRes: Int? = null,
    count: Int = 1,
    size: Dp = 40.dp,
    backgroundColor: Color = PrimaryGreen,
    contentColor: Color = Color.White,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (iconRes == null) {
            Text(
                text = tag.take(count),
                color = contentColor,
                style = textStyle
            )
        } else {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = contentColor // 💡 아이콘 색상도 contentColor에 맞춤
            )
        }
    }
}