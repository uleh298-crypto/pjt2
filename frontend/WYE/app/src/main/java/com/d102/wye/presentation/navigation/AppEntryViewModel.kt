package com.d102.wye.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 앱 시작 시 로그인 여부를 보고 최초 라우트를 결정한다. */
@HiltViewModel
class AppEntryViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    /** 로그인 상태를 화면에서 바로 구독할 수 있게 StateFlow로 노출한다. */
    val isLoggedIn: StateFlow<Boolean?> = authRepository.isLoggedIn
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}
