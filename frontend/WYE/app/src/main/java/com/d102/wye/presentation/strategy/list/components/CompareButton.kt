package com.d102.wye.presentation.strategy.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.d102.wye.R
import com.d102.wye.presentation.theme.PrimaryGreen
import kotlinx.coroutines.delay

@Composable
fun CompareButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 16.dp else 100.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), // 살짝 튕기는 느낌 추가
        label = "cornerRadius"
    )

    // 2초 후 자동 축소
    LaunchedEffect(Unit) {
        delay(2000)
        isExpanded = false
    }

    Surface(
        onClick = { onClick() },
        shape = RoundedCornerShape(cornerRadius),
        color = PrimaryGreen,
        shadowElevation = 2.dp
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = if (isExpanded) 16.dp else 12.dp,
                vertical = 12.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_compare),
                contentDescription = null,
                tint = Color.White,
            )
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = "전략 비교하기",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    softWrap = false
                )
            }
        }
    }
}