package com.d102.wye.presentation.mypage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.mypage.components.MyDataConsentDialog
import com.d102.wye.presentation.mypage.components.myPageAccountSection
import com.d102.wye.presentation.mypage.components.myPageEtfSection
import com.d102.wye.presentation.mypage.components.MyPageProfileHeader
import com.d102.wye.presentation.mypage.components.NicknameEditDialog
import com.d102.wye.presentation.mypage.components.myPageSettingsSection
import com.d102.wye.presentation.mypage.components.myPageSupportSection
import com.d102.wye.presentation.model.UiState
import kotlinx.coroutines.flow.collect

@Composable
fun MyPageScreen(
    onLikedEtfClick: (ticker: String) -> Unit,
    onLogoutClick: () -> Unit,
    onHoldingEtfMoreClick: () -> Unit = {},
    onLikedEtfListClick: () -> Unit = {},
    onPasswordChangeClick: () -> Unit = {},
    onAlertSettingClick: () -> Unit = {},

    onFaqClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.toString()?.let(viewModel::onProfileImageSelected)
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar(message = (uiState as UiState.Error).message)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                MyPageEvent.LogoutSuccess -> onLogoutClick()
                is MyPageEvent.ShowMessage -> snackbarHostState.showSnackbar(message = event.message)
            }
        }
    }

    MyPageScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onLikedEtfClick = onLikedEtfClick,
        onLogoutClick = { viewModel.logout() },
        onHoldingEtfMoreClick = onHoldingEtfMoreClick,
        onLikedEtfListClick = onLikedEtfListClick,
        onPasswordChangeClick = onPasswordChangeClick,
        onAlertSettingClick = onAlertSettingClick,
        onFaqClick = onFaqClick,
        onTermsClick = onTermsClick,
        onNicknameEditClick = { viewModel.showNicknameEditDialog() },
        onNicknameDraftChange = { viewModel.onNicknameDraftChange(it) },
        onNicknameEditDismiss = { viewModel.dismissNicknameEditDialog() },
        onNicknameSave = { viewModel.saveNickname() },
        onProfileImageDelete = { viewModel.deleteProfileImage() },
        onMyDataConnectClick = { viewModel.showMyDataConsentDialog() },
        onMyDataConsentCheckedChange = { viewModel.setMyDataConsentChecked(it) },
        onMyDataConsentDismiss = { viewModel.dismissMyDataConsentDialog() },
        onMyDataConsentSubmit = { viewModel.submitMyDataConsent() },
        onProfileImageChange = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    )
}

@Composable
private fun MyPageScreenContent(
    uiState: UiState<MyPageData>,
    snackbarHostState: SnackbarHostState,
    onLikedEtfClick: (ticker: String) -> Unit,
    onLogoutClick: () -> Unit,
    onHoldingEtfMoreClick: () -> Unit,
    onLikedEtfListClick: () -> Unit,
    onPasswordChangeClick: () -> Unit,
    onAlertSettingClick: () -> Unit,

    onFaqClick: () -> Unit,
    onTermsClick: () -> Unit,
    onNicknameEditClick: () -> Unit,
    onNicknameDraftChange: (String) -> Unit,
    onNicknameEditDismiss: () -> Unit,
    onNicknameSave: () -> Unit,
    onProfileImageDelete: () -> Unit,
    onMyDataConnectClick: () -> Unit,
    onMyDataConsentCheckedChange: (Boolean) -> Unit,
    onMyDataConsentDismiss: () -> Unit,
    onMyDataConsentSubmit: () -> Unit,
    onProfileImageChange: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            is UiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    WyeTopBar(title = "마이페이지")

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                MyPageProfileHeader(
                                    nickname = uiState.data.nickname,
                                    profileImage = uiState.data.profileImage,
                                    onProfileImageChangeClick = onProfileImageChange,
                                    onProfileImageDeleteClick = onProfileImageDelete
                                )
                            }
                        }

                        myPageEtfSection(
                            data = uiState.data,
                            onHoldingEtfMoreClick = onHoldingEtfMoreClick,
                            onHoldingEtfClick = onLikedEtfClick,
                            onLikedEtfListClick = onLikedEtfListClick,
                            onMyDataConnectClick = onMyDataConnectClick,
                        )

                        myPageAccountSection(
                            onNicknameChangeClick = onNicknameEditClick,
                            onPasswordChangeClick = onPasswordChangeClick,
                            onLogoutClick = onLogoutClick
                        )

                        myPageSettingsSection(
                            onAlertSettingClick = onAlertSettingClick,
                        )

                        myPageSupportSection(
                            onFaqClick = onFaqClick,
                            onTermsClick = onTermsClick
                        )
                    }

                    if (uiState.data.isNicknameDialogVisible) {
                        NicknameEditDialog(
                            currentNickname = uiState.data.nickname,
                            nicknameDraft = uiState.data.nicknameDraft,
                            validationMessage = uiState.data.nicknameValidationMessage,
                            isSaving = uiState.data.isNicknameSaving,
                            onNicknameChange = onNicknameDraftChange,
                            onDismiss = onNicknameEditDismiss,
                            onSave = onNicknameSave
                        )
                    }

                    if (uiState.data.isMyDataConsentDialogVisible) {
                        MyDataConsentDialog(
                            isChecked = uiState.data.isMyDataConsentChecked,
                            onCheckedChange = onMyDataConsentCheckedChange,
                            onDismiss = onMyDataConsentDismiss,
                            onConfirm = onMyDataConsentSubmit,
                        )
                    }
                }
            }

            is UiState.Error -> Unit
            UiState.Idle -> Unit
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
