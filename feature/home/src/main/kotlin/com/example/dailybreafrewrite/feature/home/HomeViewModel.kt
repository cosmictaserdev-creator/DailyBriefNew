package com.example.dailybreafrewrite.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailybreafrewrite.core.common.AppConstants
import com.example.dailybreafrewrite.core.domain.model.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val articles: List<Article> = emptyList(),
    val selectedCategory: String = AppConstants.CATEGORIES.first(),
    val isLoading: Boolean = false,
)

sealed interface HomeAction {
    data class SelectCategory(val category: String) : HomeAction
    data object SwipeLeft : HomeAction
    data object SwipeRight : HomeAction
    data object TappedCard : HomeAction
}

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadMockArticles()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.SelectCategory -> {
                _state.update { it.copy(selectedCategory = action.category) }
            }
            HomeAction.SwipeLeft -> { }
            HomeAction.SwipeRight -> { }
            HomeAction.TappedCard -> { }
        }
    }

    private fun loadMockArticles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val now = ZonedDateTime.now()
            _state.update {
                it.copy(
                    articles = mockArticles(now),
                    isLoading = false,
                )
            }
        }
    }
}

internal fun mockArticles(
    now: ZonedDateTime,
    formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME,
): List<Article> = buildList {
    val data = listOf(
        listOf(
            "Breaking: AI Model Achieves Human-Level Reasoning in Scientific Research",
            "Researchers at DeepMind have announced a breakthrough in artificial intelligence, demonstrating a model capable of human-level reasoning in multiple scientific domains.",
            "https://picsum.photos/seed/ai/400/300",
            "TechCrunch",
        ),
        listOf(
            "Global Climate Summit Reaches Historic Agreement on Carbon Emissions",
            "World leaders have signed a landmark agreement to reduce carbon emissions by 60% by 2035, marking the most ambitious climate target in history.",
            "https://picsum.photos/seed/climate/400/300",
            "Reuters",
        ),
        listOf(
            "Major League Baseball Announces Expansion to Two New Cities",
            "MLB commissioners have voted unanimously to expand the league, bringing professional baseball to Nashville and Portland starting in 2028.",
            "https://picsum.photos/seed/baseball/400/300",
            "ESPN",
        ),
        listOf(
            "Federal Reserve Holds Interest Rates Steady Amid Mixed Economic Signals",
            "The Federal Reserve announced it would maintain current interest rates, citing moderate economic growth and persistent but easing inflation.",
            "https://picsum.photos/seed/finance/400/300",
            "Bloomberg",
        ),
        listOf(
            "New Study Reveals Surprising Benefits of a Four-Day Work Week",
            "A year-long study involving 2,000 companies found that a four-day work week led to a 34% increase in productivity and a 40% drop in employee burnout.",
            "https://picsum.photos/seed/workweek/400/300",
            "Forbes",
        ),
        listOf(
            "SpaceX Successfully Tests Next-Generation Starship Engine",
            "SpaceX completed a full-duration static fire test of its new Raptor 3 engine, which promises 15% more thrust than its predecessor.",
            "https://picsum.photos/seed/space/400/300",
            "The Verge",
        ),
        listOf(
            "WHO Declares End to Global Health Emergency After Coordinated Response",
            "The World Health Organization has officially declared the end of the global health emergency, citing successful vaccination campaigns worldwide.",
            "https://picsum.photos/seed/health/400/300",
            "BBC",
        ),
        listOf(
            "Electric Vehicle Sales Surge Past 40% Market Share in Europe",
            "EV sales in Europe have crossed the 40% threshold for the first time, driven by affordable new models and expanded charging infrastructure.",
            "https://picsum.photos/seed/ev/400/300",
            "Financial Times",
        ),
        listOf(
            "Ancient City Discovered Beneath Amazon Rainforest Using LIDAR Technology",
            "Archaeologists have uncovered the remains of a sprawling ancient city beneath the Amazon canopy, revealing a complex civilization dating back 2,500 years.",
            "https://picsum.photos/seed/amazon/400/300",
            "National Geographic",
        ),
        listOf(
            "Quantum Computing Breakthrough Enables Error-Free Calculations at Scale",
            "A team of physicists has achieved the first practical demonstration of fault-tolerant quantum computing, running complex algorithms without errors.",
            "https://picsum.photos/seed/quantum/400/300",
            "Wired",
        ),
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
