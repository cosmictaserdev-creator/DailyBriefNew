package com.example.dailybreafrewrite.feature.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OnboardingUiState(
    val allInterests: List<Interest> = listOf(
        Interest("Technology", "📱"),
        Interest("Science", "🔬"),
        Interest("Sports", "⚽"),
        Interest("Health", "💪"),
        Interest("Finance", "💰"),
        Interest("Politics", "🗳️"),
        Interest("Entertainment", "🎬"),
        Interest("World", "🌍"),
    ),
    val selectedInterests: Set<String> = emptySet(),
)

data class Interest(
    val name: String,
    val emoji: String,
)

sealed interface OnboardingAction {
    data class ToggleInterest(val name: String) : OnboardingAction
    data object Submit : OnboardingAction
}

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    val canSubmit: Boolean get() = _state.value.selectedInterests.isNotEmpty()

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.ToggleInterest -> {
                _state.update { state ->
                    val updated = if (state.selectedInterests.contains(action.name)) {
                        state.selectedInterests - action.name
                    } else {
                        state.selectedInterests + action.name
                    }
                    state.copy(selectedInterests = updated)
                }
            }
            OnboardingAction.Submit -> { }
        }
    }
}
