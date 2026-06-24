package com.example.dailybreafrewrite.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailybreafrewrite.core.common.AppConstants
import com.example.dailybreafrewrite.core.common.Resource
import com.example.dailybreafrewrite.core.common.UiText
import com.example.dailybreafrewrite.core.domain.model.Article
import com.example.dailybreafrewrite.core.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Article> = emptyList(),
    val trendingSearches: List<String> = AppConstants.TRENDING_SEARCHES,
    val isSearching: Boolean = false,
    val error: UiText? = null,
)

sealed interface SearchAction {
    data class UpdateQuery(val query: String) : SearchAction
    data class SelectTrending(val query: String) : SearchAction
    data object ClearQuery : SearchAction
    data object DismissError : SearchAction
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ArticleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.UpdateQuery -> {
                _state.update { it.copy(query = action.query) }
                debouncedSearch(action.query)
            }
            is SearchAction.SelectTrending -> {
                _state.update { it.copy(query = action.query) }
                searchNow(action.query)
            }
            SearchAction.ClearQuery -> {
                _state.update { it.copy(query = "", results = emptyList()) }
                searchJob?.cancel()
            }
            SearchAction.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun debouncedSearch(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300L)
            searchNow(query)
        }
    }

    private fun searchNow(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, error = null) }

            when (repository.searchRemote(query)) {
                is Resource.Error -> {
                    _state.update { it.copy(isSearching = false) }
                }
                is Resource.Success -> { }
            }

            repository.searchArticles(query).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                results = resource.data,
                                isSearching = false,
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isSearching = false,
                                error = it.error ?: resource.message,
                            )
                        }
                    }
                }
            }
        }
    }
}
