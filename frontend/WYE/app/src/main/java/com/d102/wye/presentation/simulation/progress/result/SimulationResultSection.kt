package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.presentation.designsystem.bottomShadow
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.model.SimulationUiModel
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.simulation.progress.components.AiReviewButton

@Composable
fun SimulationResultSection(
    formState: SimulationFormState,
    simulationState: UiState<SimulationUiModel>,
    onOverlayToggled: (Boolean) -> Unit,
    overlayPoints:  List<BacktestPoint>?,
    onAiDiagnosisClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    idleGuideMessage: String
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .bottomShadow(offsetY = 4.dp, blurRadius = 6.dp)
                .background(Color.White)
                .padding(16.dp)
        ) {
            when (formState.selectedTabIndex) {
                0 -> YieldTrendView(formState, simulationState, onOverlayToggled, overlayPoints, idleGuideMessage)
                1 -> PortfolioAnalysisView(simulationState, onDictionaryClick)
            }
        }

        AiReviewButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp),
            isEmpty = simulationState !is UiState.Success,
            onClick = onAiDiagnosisClick
        )
    }
}