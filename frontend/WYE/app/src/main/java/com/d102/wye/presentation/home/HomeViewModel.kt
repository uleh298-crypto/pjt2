package com.d102.wye.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.News
import com.d102.wye.domain.model.TopVolumeEtf
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.repository.NewsRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val etfRepository: EtfRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeData>>(UiState.Idle)
    val uiState: StateFlow<UiState<HomeData>> = _uiState.asStateFlow()

    init {
        loadHomeData()
        startTop10Polling()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            coroutineScope {
                val newsDeferred = async { newsRepository.getNewsList(lastId = null) }
                val topVolumeDeferred = async { etfRepository.getTopVolumeEtfs() }

                when (val newsResult = newsDeferred.await()) {
                    is BaseResult.Error -> _uiState.update { UiState.Error(newsResult.error.message) }
                    is BaseResult.Success -> {
                        when (val topVolumeResult = topVolumeDeferred.await()) {
                            is BaseResult.Success -> {
                                _uiState.update {
                                    UiState.Success(
                                        HomeData(
                                            top10Etfs = topVolumeResult.data.items.map { it.toHomeTop10UiModel() },
                                            top10UpdatedText = formatTop10UpdatedText(topVolumeResult.data.timestamp),
                                            newsList = newsResult.data.news
                                                .take(HOME_NEWS_LIMIT)
                                                .map { it.toHomeNewsUiModel() },
                                            isTop10Refreshing = false
                                        )
                                    )
                                }
                            }
                            is BaseResult.Error -> _uiState.update { UiState.Error(topVolumeResult.error.message) }
                        }
                    }
                }
            }
        }
    }

    private fun startTop10Polling() {
        viewModelScope.launch {
            while (true) {
                delay(TOP10_POLLING_INTERVAL_MS)
                if (shouldPollTop10()) {
                    refreshTop10EtfsInternal()
                }
            }
        }
    }

    fun refreshTop10Etfs() {
        viewModelScope.launch {
            refreshTop10EtfsInternal()
        }
    }

    private suspend fun refreshTop10EtfsInternal() {
        val currentState = _uiState.value as? UiState.Success ?: return
        if (currentState.data.isTop10Refreshing) return

        _uiState.update {
            UiState.Success(currentState.data.copy(isTop10Refreshing = true))
        }

        when (val result = etfRepository.getTopVolumeEtfs()) {
            is BaseResult.Success -> {
                _uiState.update {
                    UiState.Success(
                        currentState.data.copy(
                            top10Etfs = result.data.items.map { etf -> etf.toHomeTop10UiModel() },
                            top10UpdatedText = formatTop10UpdatedText(result.data.timestamp),
                            isTop10Refreshing = false
                        )
                    )
                }
            }
            is BaseResult.Error -> {
                _uiState.update {
                    UiState.Success(currentState.data.copy(isTop10Refreshing = false))
                }
            }
        }
    }

    /** 뉴스 도메인 모델을 홈 화면 뉴스 카드 UI 모델로 변환한다. */
    private fun News.toHomeNewsUiModel() = HomeNewsUiModel(
        id = id,
        category = categoryName,
        title = title,
        timeAgo = publishedAt.toTimeAgo(),
        source = source,
        thumbnailUrl = thumbnailUrl
    )

    /** 거래량 TOP 10 도메인 모델을 홈 탭 UI 모델로 변환한다. */
    private fun TopVolumeEtf.toHomeTop10UiModel() = Top10EtfUiModel(
        ticker = ticker,
        name = name,
        changeRate = dailyReturn
    )

    private fun formatTop10UpdatedText(timestamp: String?): String {
        val parsed = timestamp?.let {
            runCatching { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }.getOrNull()
        } ?: LocalDateTime.now()

        return if (shouldShowClosingText()) {
            val now = nowKst()
            var date = now.toLocalDate()
            if (now.toLocalTime() < LocalTime.of(9, 0)) date = date.minusDays(1)
            while (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                date = date.minusDays(1)
            }
            date.format(DateTimeFormatter.ofPattern("yy.MM.dd")) + " 종가"
        } else {
            parsed.format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm"))
        }
    }

    private fun nowKst(): ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

    private fun shouldPollTop10(): Boolean {
        val now = nowKst()
        val day = now.dayOfWeek
        val time = now.toLocalTime()
        val isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
        return !isWeekend && !time.isBefore(MARKET_OPEN_TIME) && time.isBefore(MARKET_CLOSE_TIME)
    }

    private fun shouldShowClosingText(): Boolean {
        val now = nowKst()
        val day = now.dayOfWeek
        val time = now.toLocalTime()
        val isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
        return isWeekend || time.isBefore(MARKET_OPEN_TIME) || !time.isBefore(MARKET_CLOSE_TIME)
    }

    /** 서버 시간을 홈 화면 카드용 상대 시간 문자열로 변환한다. */
    private fun String.toTimeAgo(): String {
        return runCatching {
            val publishedAt = LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
            val duration = Duration.between(publishedAt, LocalDateTime.now())
            when {
                duration.toMinutes() < 1 -> "방금 전"
                duration.toHours() < 1 -> "${duration.toMinutes()}분 전"
                duration.toDays() < 1 -> "${duration.toHours()}시간 전"
                duration.toDays() < 7 -> "${duration.toDays()}일 전"
                else -> publishedAt.toLocalDate().toString()
            }
        }.getOrDefault(this)
    }

}

data class HomeData(
    val top10Etfs: List<Top10EtfUiModel>,
    val top10UpdatedText: String,
    val newsList: List<HomeNewsUiModel>,
    val isTop10Refreshing: Boolean,
)

data class Top10EtfUiModel(
    val ticker: String,
    val name: String,
    val changeRate: Double
)

data class HomeNewsUiModel(
    val id: Long,
    val category: String,
    val title: String,
    val timeAgo: String,
    val source: String,
    val thumbnailUrl: String? = null
)

private const val HOME_NEWS_LIMIT = 2
private const val TOP10_POLLING_INTERVAL_MS = 60_000L
private val MARKET_OPEN_TIME: LocalTime = LocalTime.of(9, 0)
private val MARKET_CLOSE_TIME: LocalTime = LocalTime.of(15, 30)
