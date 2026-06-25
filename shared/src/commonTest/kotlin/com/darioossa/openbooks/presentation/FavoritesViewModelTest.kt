package com.darioossa.openbooks.presentation

import app.cash.turbine.test
import com.darioossa.openbooks.domain.ObserveFavoritesUseCase
import com.darioossa.openbooks.domain.ToggleFavoriteUseCase
import com.darioossa.openbooks.domain.entities.Book
import com.darioossa.openbooks.presentation.favorites.FavoritesState
import com.darioossa.openbooks.presentation.favorites.FavoritesViewModel
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest : BaseViewModelTest() {
    private val observeFavoritesUseCase = mock<ObserveFavoritesUseCase>()
    private val toggleFavoriteUseCase = mock<ToggleFavoriteUseCase>()

    private fun viewModel() =
        FavoritesViewModel(
            observeFavorites = observeFavoritesUseCase,
            toggleFavorite = toggleFavoriteUseCase,
        )

    @Test
    fun `favorites present emits Loading then Content`() =
        runTest(dispatcher) {
            every { observeFavoritesUseCase.invoke() } returns flowOf(listOf(oliverTwist))

            viewModel().state.test {
                awaitItem() shouldBe FavoritesState.Loading
                awaitItem() shouldBe FavoritesState.Content(listOf(oliverTwist))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `no favorites emits Loading then Empty`() =
        runTest(dispatcher) {
            every { observeFavoritesUseCase.invoke() } returns flowOf(emptyList())

            viewModel().state.test {
                awaitItem() shouldBe FavoritesState.Loading
                awaitItem() shouldBe FavoritesState.Empty
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `remove delegates the selected book to toggle favorite`() =
        runTest(dispatcher) {
            every { observeFavoritesUseCase.invoke() } returns flowOf(listOf(oliverTwist))
            everySuspend { toggleFavoriteUseCase.invoke(oliverTwist) } returns Unit

            viewModel().remove(oliverTwist)
            advanceUntilIdle()

            verifySuspend { toggleFavoriteUseCase.invoke(oliverTwist) }
        }

    private companion object {
        val oliverTwist =
            Book(
                key = "OL45804W",
                title = "Oliver Twist",
                authors = listOf("Charles Dickens"),
                coverUrl = null,
                firstPublishYear = null,
            )
    }
}
