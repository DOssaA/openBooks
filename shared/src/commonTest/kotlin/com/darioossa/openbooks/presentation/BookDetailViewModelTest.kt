package com.darioossa.openbooks.presentation

import app.cash.turbine.test
import com.darioossa.openbooks.domain.GetBookUseCase
import com.darioossa.openbooks.domain.ObserveFavoriteKeysUseCase
import com.darioossa.openbooks.domain.ToggleFavoriteUseCase
import com.darioossa.openbooks.domain.entities.Book
import com.darioossa.openbooks.presentation.bookDetail.BookDetailViewModel
import com.darioossa.openbooks.presentation.bookDetail.DetailState
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
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
class BookDetailViewModelTest : BaseViewModelTest() {
    private val getBookUseCase = mock<GetBookUseCase>()
    private val toggleFavoriteUseCase = mock<ToggleFavoriteUseCase>()
    private val observeFavoriteKeysUseCase = mock<ObserveFavoriteKeysUseCase>()

    private fun viewModel(favoriteKeys: Set<String> = emptySet()): BookDetailViewModel {
        every { observeFavoriteKeysUseCase.invoke() } returns flowOf(favoriteKeys)
        return BookDetailViewModel(
            getBook = getBookUseCase,
            toggleFavorite = toggleFavoriteUseCase,
            observeFavoriteKeys = observeFavoriteKeysUseCase,
        )
    }

    @Test
    fun `onBookOpened emits Loading then Success with the fetched description`() =
        runTest(dispatcher) {
            everySuspend { getBookUseCase.invoke(oliverTwist.key) } returns
                oliverTwist.copy(authors = emptyList(), description = FULL_DESCRIPTION)
            val viewModel = viewModel()

            viewModel.state.test {
                awaitItem() shouldBe DetailState.Loading
                viewModel.onBookOpened(oliverTwist)
                awaitItem() shouldBe
                    DetailState.Success(
                        book = oliverTwist.copy(description = FULL_DESCRIPTION),
                        isFavorite = false,
                    )
                cancelAndIgnoreRemainingEvents()
            }
            verifySuspend { getBookUseCase.invoke(oliverTwist.key) }
        }

    @Test
    fun `getBook failure emits Loading then Error`() =
        runTest(dispatcher) {
            everySuspend { getBookUseCase.invoke(oliverTwist.key) } throws RuntimeException()
            val viewModel = viewModel()

            viewModel.state.test {
                awaitItem() shouldBe DetailState.Loading
                viewModel.onBookOpened(oliverTwist)
                awaitItem() shouldBe DetailState.Error
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a favorited key surfaces as isFavorite in Success`() =
        runTest(dispatcher) {
            everySuspend { getBookUseCase.invoke(oliverTwist.key) } returns
                oliverTwist.copy(description = FULL_DESCRIPTION)
            val viewModel = viewModel(favoriteKeys = setOf(oliverTwist.key))

            viewModel.state.test {
                awaitItem() shouldBe DetailState.Loading
                viewModel.onBookOpened(oliverTwist)
                awaitItem() shouldBe
                    DetailState.Success(
                        book = oliverTwist.copy(description = FULL_DESCRIPTION),
                        isFavorite = true,
                    )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleFavorite delegates the opened book`() =
        runTest(dispatcher) {
            val opened = oliverTwist.copy(description = FULL_DESCRIPTION)
            everySuspend { getBookUseCase.invoke(oliverTwist.key) } returns opened
            everySuspend { toggleFavoriteUseCase.invoke(opened) } returns Unit
            val viewModel = viewModel()

            viewModel.onBookOpened(oliverTwist)
            advanceUntilIdle()
            viewModel.toggleFavorite()
            advanceUntilIdle()

            verifySuspend { toggleFavoriteUseCase.invoke(opened) }
        }

    private companion object {
        const val FULL_DESCRIPTION = "A long, full description fetched from the Work endpoint."
        val oliverTwist =
            Book(
                key = "OL45804W",
                title = "Oliver Twist",
                authors = listOf("Charles Dickens"),
                coverUrl = null,
                firstPublishYear = 1838,
            )
    }
}
