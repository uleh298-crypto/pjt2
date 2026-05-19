package com.d102.wye.presentation.auth.join.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.BadgeConservative
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun JoinSuccessContent(
    email: String
) {
    Spacer(modifier = Modifier.height(60.dp))


    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    color = BadgeConservative,
                    shape = RoundedCornerShape(70.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "회원가입을 축하합니다!",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "이제 나만의 ETF 포트폴리오를 만들고\n미래 자산을 시뮬레이션해 보세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .background(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
