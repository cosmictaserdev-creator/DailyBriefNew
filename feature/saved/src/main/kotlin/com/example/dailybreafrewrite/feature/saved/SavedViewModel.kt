package com.example.dailybreafrewrite.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailybreafrewrite.core.common.Resource
import com.example.dailybreafrewrite.core.common.UiText
import com.example.dailybreafrewrite.core.domain.model.Article
import com.example.dailybreafrewrite.core.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedUiState(
    val savedArticles: List<Article> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: UiText? = null,
)

sealed interface SavedAction {
    data class ToggleSaved(val url: String) : SavedAction
    data class UpdateSearch(val query: String) : SavedAction
    data object DismissError : SavedAction
}

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: ArticleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SavedUiState())
    val state: StateFlow<SavedUiState> = _state.asStateFlow()

    init {
        observeSaved()
    }

    fun onAction(action: SavedAction) {
        when (action) {
            is SavedAction.ToggleSaved -> toggleSaved(action.url)
            is SavedAction.UpdateSearch -> {
                _state.update { it.copy(searchQuery = action.query) }
            }
            SavedAction.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun observeSaved() {
        viewModelScope.launch {
            repository.observeSavedArticles().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                savedArticles = resource.data,
                                isLoading = false,
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = it.error ?: resource.message,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun toggleSaved(url: String) {
        viewModelScope.launch {
            when (val result = repository.toggleSaved(url)) {
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Success -> { }
            }
        }
    }
}
