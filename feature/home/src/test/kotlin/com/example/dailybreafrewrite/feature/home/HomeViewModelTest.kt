package com.example.dailybreafrewrite.feature.home

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads articles`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.articles.isNotEmpty())
            assertEquals("Trending", state.selectedCategory)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `select category updates state`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel()
        advanceUntilIdle()

        viewModel.onAction(HomeAction.SelectCategory("Sports"))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Sports", state.selectedCategory)
        }
    }

    @Test
    fun `articles contain mock data`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            val articles = state.articles
            assertEquals(10, articles.size)

            val first = articles.first()
            assertNotNull(first.title)
            assertNotNull(first.url)
            assertNotNull(first.sourceName)
        }
    }

    @Test
    fun `warmup articles are generated with valid data`() {
        val now = java.time.ZonedDateTime.now()
        val articles = mockArticles(now)

        assertEquals(10, articles.size)
        articles.forEach { article ->
            assertTrue(article.url.startsWith("https://"))
            assertNotNull(article.title)
            assertNotNull(article.sourceName)
        }
    }
}
