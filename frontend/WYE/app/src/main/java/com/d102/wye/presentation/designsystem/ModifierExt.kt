package com.d102.wye.presentation.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.bottomShadow(
    color: Color = Color.Black.copy(alpha = 0.08f), // 그림자 색상 및 투명도
    offsetY: Dp = 6.dp,                             // 아래로 밀어낼 거리
    blurRadius: Dp = 8.dp                           // 그림자의 퍼짐 정도
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = android.graphics.Color.TRANSPARENT

        // 안드로이드 기본 Paint 속성을 이용해 하단 방향으로 그림자 생성
        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            0f,            // X축 이동 없음
            offsetY.toPx(), // Y축 아래로 이동
            color.toArgb()
        )

        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            paint = paint
        )
    }
}