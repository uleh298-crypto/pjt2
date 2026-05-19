package com.d102.wye.presentation.home.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.NewsDetail
import com.d102.wye.domain.model.RelatedEtf
import com.d102.wye.domain.repository.NewsRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<NewsDetailUiModel>>(UiState.Loading)
    val uiState: StateFlow<UiState<NewsDetailUiModel>> = _uiState.asStateFlow()

    private var loadedNewsId: Long? = null

    /** 전달받은 뉴스 ID로 상세 데이터를 조회한다. */
    fun loadNewsDetail(newsId: Long) {
        if (loadedNewsId == newsId && _uiState.value is UiState.Success) return
        loadedNewsId = newsId

        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            when (val result = newsRepository.getNewsDetail(newsId)) {
                is BaseResult.Success -> _uiState.update { UiState.Success(result.data.toUiModel()) }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }

    /** 마지막으로 조회한 뉴스 ID로 상세 데이터를 다시 불러온다. */
    fun refresh() {
        loadedNewsId?.let { loadNewsDetail(it) }
    }

    /** 상세 도메인 모델을 화면 전용 UI 모델로 변환한다. */
    private fun NewsDetail.toUiModel() = NewsDetailUiModel(
        id = id,
        title = title,
        date = publishedAt.toDisplayDate(),
        thumbnailUrl = thumbnailUrl,
        tags = keywords,
        aiSummary = aiSummary,
        body = content,
        sourceUrl = sourceUrl,
        source = source,
        relatedEtfs = relatedEtfs.map { it.toUiModel() }
    )

    /** 관련 ETF 도메인 모델을 화면 카드 UI 모델로 변환한다. */
    private fun RelatedEtf.toUiModel() = RelatedEtfUiModel(
        ticker = ticker,
        name = name,
        manager = manager,
        changeRate = changeRate
    )

    /** 서버 시간을 상세 화면 표기용 날짜 문자열로 변환한다. */
    private fun String.toDisplayDate(): String {
        return runCatching {
            LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        }.getOrDefault(this)
    }
}
