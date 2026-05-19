package com.d102.wye.presentation.strategy.list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.designsystem.WyeBadge
import com.d102.wye.presentation.strategy.list.StrategyCardUiModel
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextTertiary

@Composable
fun StrategyCard(
    strategy: StrategyCardUiModel,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onEditClick: ((StrategyCardUiModel) -> Unit)? = null,
    onDeleteClick: ((String) -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(strategy.id.toLong()) },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (strategy.isMyData) PrimaryGreen else Divider
        )
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 왼쪽: 정보 영역
                Column(modifier = Modifier.weight(1f)) {
                    if (strategy.isMyData) {
                        WyeBadge(
                            shape = RoundedCornerShape(8.dp),
                            label = "MY 전략",
                            textStyle = MaterialTheme.typography.labelSmall,
                            color = PrimaryGreen,
                            textColor = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = strategy.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    if (!strategy.isMyData) {
                        Text(
                            text = "${strategy.date} 저장됨",
                            style = MaterialTheme.typography.labelMedium,
                            color = IconInactive
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 태그 로직: 최대 2개 + 나머지 개수 표시
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        strategy.tags.take(2).forEach { tag ->
                            WyeBadge(
                                shape = CircleShape,
                                label = tag,
                                textStyle = MaterialTheme.typography.labelSmall,
                                color = SurfaceVariant,
                                textColor = TextTertiary
                            )
                        }

                        // 남은 태그 개수 표시
                        if (strategy.tags.size > 2) {
                            Text(
                                text = "+${strategy.tags.size - 2}",
                                style = MaterialTheme.typography.labelSmall,
                                color = IconInactive,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }

            }

            // 우상단 고정 메뉴 아이콘
            if (!strategy.isMyData && (onEditClick != null || onDeleteClick != null)) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            tint = IconInactive
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = Color.White
                    ) {
                        onEditClick?.let {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "이름 수정",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    it(strategy)
                                }
                            )
                        }
                        onDeleteClick?.let {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "삭제",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = EtfRise
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    it(strategy.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}