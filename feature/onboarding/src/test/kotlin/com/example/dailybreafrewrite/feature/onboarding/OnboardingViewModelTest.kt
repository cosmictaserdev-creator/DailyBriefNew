package com.example.dailybreafrewrite.feature.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {

    @Test
    fun `initial state has all interests and empty selection`() {
        val viewModel = OnboardingViewModel()

        assertEquals(8, viewModel.state.value.allInterests.size)
        assertTrue(viewModel.state.value.selectedInterests.isEmpty())
    }

    @Test
    fun `toggle interest adds to selection`() {
        val viewModel = OnboardingViewModel()

        viewModel.onAction(OnboardingAction.ToggleInterest("Technology"))

        assertTrue(viewModel.state.value.selectedInterests.contains("Technology"))
        assertEquals(1, viewModel.state.value.selectedInterests.size)
    }

    @Test
    fun `toggle interest removes from selection`() {
        val viewModel = OnboardingViewModel()

        viewModel.onAction(OnboardingAction.ToggleInterest("Sports"))
        assertTrue(viewModel.state.value.selectedInterests.contains("Sports"))

        viewModel.onAction(OnboardingAction.ToggleInterest("Sports"))
        assertFalse(viewModel.state.value.selectedInterests.contains("Sports"))
    }

    @Test
    fun `can submit when interests selected`() {
        val viewModel = OnboardingViewModel()

        assertFalse(viewModel.canSubmit)

        viewModel.onAction(OnboardingAction.ToggleInterest("Science"))

        assertTrue(viewModel.canSubmit)
    }

    @Test
    fun `all interests have names and emojis`() {
        val viewModel = OnboardingViewModel()

        viewModel.state.value.allInterests.forEach { interest ->
            assertTrue(interest.name.isNotBlank())
            assertTrue(interest.emoji.isNotBlank())
        }
    }
}
