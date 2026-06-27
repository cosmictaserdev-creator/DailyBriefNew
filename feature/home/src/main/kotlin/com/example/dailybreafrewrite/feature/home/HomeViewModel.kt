package com.example.dailybreafrewrite.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailybreafrewrite.core.common.AppConstants
import com.example.dailybreafrewrite.core.common.Resource
import com.example.dailybreafrewrite.core.common.UiText
import com.example.dailybreafrewrite.core.domain.model.Article
import com.example.dailybreafrewrite.core.domain.model.FilterPreferences
import com.example.dailybreafrewrite.core.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val articles: List<Article> = emptyList(),
    val selectedCategory: String = AppConstants.CATEGORIES.first(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val filterPreferences: FilterPreferences = FilterPreferences(),
    val showFilterSheet: Boolean = false,
)

sealed interface HomeAction {
    data class SelectCategory(val category: String) : HomeAction
    data object SwipeLeft : HomeAction
    data object SwipeRight : HomeAction
    data class ToggleSaved(val url: String) : HomeAction
    data object TappedCard : HomeAction
    data object Refresh : HomeAction
    data object DismissError : HomeAction
    data object ShowFilterSheet : HomeAction
    data object HideFilterSheet : HomeAction
    data class ApplyFilter(val filter: FilterPreferences) : HomeAction
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ArticleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var observeJob: Job? = null
    private var swipedCount = 0

    init {
        observeCategory(_state.value.selectedCategory)
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.SelectCategory -> {
                _state.update { it.copy(selectedCategory = action.category) }
                observeCategory(action.category)
            }
            HomeAction.SwipeLeft -> onCardSwiped()
            HomeAction.SwipeRight -> onCardSwiped()
            is HomeAction.ToggleSaved -> toggleSaved(action.url)
            HomeAction.TappedCard -> { }
            HomeAction.Refresh -> pullToRefresh()
            HomeAction.ShowFilterSheet -> _state.update { it.copy(showFilterSheet = true) }
            HomeAction.HideFilterSheet -> _state.update { it.copy(showFilterSheet = false) }
            is HomeAction.ApplyFilter -> {
                _state.update {
                    it.copy(
                        filterPreferences = action.filter,
                        showFilterSheet = false,
                    )
                }
            }
            HomeAction.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun onCardSwiped() {
        swipedCount++
        val articleCount = _state.value.articles.size
        val remaining = articleCount - swipedCount

        if (remaining <= AppConstants.Card.PREFETCH_THRESHOLD && _state.value.hasMorePages) {
            val nextPage = _state.value.currentPage + 1
            _state.update { it.copy(currentPage = nextPage) }
            viewModelScope.launch {
                when (val result = repository.refreshHeadlines(
                    category = _state.value.selectedCategory,
                    page = nextPage,
                )) {
                    is Resource.Error -> {
                        _state.update { it.copy(
                            error = result.message,
                            hasMorePages = false,
                        ) }
                    }
                    is Resource.Success -> { }
                }
            }
        }
    }

    private fun toggleSaved(url: String) {
        viewModelScope.launch {
            repository.toggleSaved(url)
        }
    }

    private fun observeCategory(category: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isRefreshing = false, error = null) }

            repository.observeHeadlines(category).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val articles = resource.data
                        if (articles.isEmpty()) {
                            if (!_state.value.isRefreshing) {
                                _state.update { it.copy(isRefreshing = true) }
                                when (val result = repository.refreshHeadlines(category, 1)) {
                                    is Resource.Error -> {
                                        _state.update { it.copy(isLoading = false, isRefreshing = false, error = result.message) }
                                    }
                                    is Resource.Success -> {
                                        _state.update { it.copy(isLoading = false, isRefreshing = false) }
                                    }
                                }
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    articles = articles,
                                    isLoading = false,
                                    isRefreshing = false,
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = it.error ?: resource.message,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun pullToRefresh() {
        _state.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.refreshHeadlines(
                category = _state.value.selectedCategory,
                page = _state.value.currentPage,
            )) {
                is Resource.Error -> {
                    _state.update {
                        it.copy(isRefreshing = false, error = result.message)
                    }
                }
                is Resource.Success -> {
                    _state.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }
}

internal fun mockArticles(
    now: java.time.ZonedDateTime,
    formatter: java.time.format.DateTimeFormatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME,
): List<Article> = buildList {
    val data = listOf(
        listOf("Breaking: AI Model Achieves Human-Level Reasoning in Scientific Research",
            "Researchers at DeepMind have announced a breakthrough in artificial intelligence.",
            "https://picsum.photos/seed/ai/400/300", "TechCrunch"),
        listOf("Global Climate Summit Reaches Historic Agreement on Carbon Emissions",
            "World leaders have signed a landmark agreement to reduce carbon emissions.",
            "https://picsum.photos/seed/climate/400/300", "Reuters"),
        listOf("Major League Baseball Announces Expansion to Two New Cities",
            "MLB commissioners have voted unanimously to expand the league.",
            "https://picsum.photos/seed/baseball/400/300", "ESPN"),
        listOf("Federal Reserve Holds Interest Rates Steady Amid Mixed Economic Signals",
            "The Federal Reserve announced it would maintain current interest rates.",
            "https://picsum.photos/seed/finance/400/300", "Bloomberg"),
        listOf("New Study Reveals Surprising Benefits of a Four-Day Work Week",
            "A year-long study found that a four-day work week led to a 34% increase in productivity.",
            "https://picsum.photos/seed/workweek/400/300", "Forbes"),
        listOf("SpaceX Successfully Tests Next-Generation Starship Engine",
            "SpaceX completed a full-duration static fire test of its new Raptor 3 engine.",
            "https://picsum.photos/seed/space/400/300", "The Verge"),
        listOf("WHO Declares End to Global Health Emergency",
            "The WHO has officially declared the end of the global health emergency.",
            "https://picsum.photos/seed/health/400/300", "BBC"),
        listOf("Electric Vehicle Sales Surge Past 40% Market Share in Europe",
            "EV sales in Europe have crossed the 40% threshold for the first time.",
            "https://picsum.photos/seed/ev/400/300", "Financial Times"),
        listOf("Ancient City Discovered Beneath Amazon Rainforest",
            "Archaeologists have uncovered the remains of a sprawling ancient city.",
            "https://picsum.photos/seed/amazon/400/300", "National Geographic"),
        listOf("Quantum Computing Breakthrough Enables Error-Free Calculations",
            "A team of physicists has achieved the first practical demonstration of fault-tolerant quantum computing.",
            "https://picsum.photos/seed/quantum/400/300", "Wired"),
    )

    data.forEachIndexed { index, item ->
        add(
            Article(
                url = "https://example.com/article/$index",
                title = item[0],
                description = item[1],
                content = item[1],
                sourceName = item[3],
                sourceId = item[3].lowercase().replace(" ", "-"),
                author = null,
                urlToImage = item[2],
                publishedAt = now.minusHours(index.toLong()).format(formatter),
                category = AppConstants.CATEGORIES[index % AppConstants.CATEGORIES.size],
            )
        )
    }
}
