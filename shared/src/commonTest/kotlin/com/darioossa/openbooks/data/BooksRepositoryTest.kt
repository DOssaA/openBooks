package com.darioossa.openbooks.data

import app.cash.turbine.test
import com.darioossa.openbooks.data.local.BooksLocalSource
import com.darioossa.openbooks.data.remote.BooksRemoteSource
import com.darioossa.openbooks.domain.SearchBooksPage
import com.darioossa.openbooks.domain.entities.Book
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class BooksRepositoryTest {
    private val remote = mock<BooksRemoteSource>()
    private val local = mock<BooksLocalSource>()
    private val repository = BooksRepository(remote, local)

    @Test
    fun `searchBooks delegates to the remote source and emits its page`() =
        runTest {
            val page =
                SearchBooksPage(
                    books =
                        listOf(
                            Book(
                                key = "OL45804W",
                                title = "Oliver Twist",
                                authors = listOf("Charles Dickens"),
                                coverUrl = null,
                                firstPublishYear = 1838,
                            ),
                        ),
                    endReached = false,
                )
            everySuspend { remote.search("dickens", page = 2) } returns page

            repository.searchBooks("dickens", page = 2).test {
                awaitItem() shouldBe page
                awaitComplete()
            }
            verifySuspend { remote.search("dickens", page = 2) }
        }

    @Test
    fun `getBook delegates to the remote source`() =
        runTest {
            val book =
                Book(
                    key = "OL45804W",
                    title = "Oliver Twist",
                    authors = emptyList(),
                    coverUrl = null,
                    firstPublishYear = null,
                    description = "A full description.",
                )
            everySuspend { remote.getBook("OL45804W") } returns book

            repository.getBook("OL45804W") shouldBe book

            verifySuspend { remote.getBook("OL45804W") }
        }

    @Test
    fun `observeFavorites emits the local source flow`() =
        runTest {
            val favorites = listOf(oliverTwist)
            every { local.observeFavorites() } returns flowOf(favorites)

            repository.observeFavorites().test {
                awaitItem() shouldBe favorites
                awaitComplete()
            }
        }

    @Test
    fun `observeFavoriteKeys emits the local source key set`() =
        runTest {
            val keys = setOf(oliverTwist.key)
            every { local.observeFavoriteKeys() } returns flowOf(keys)

            repository.observeFavoriteKeys().test {
                awaitItem() shouldBe keys
                awaitComplete()
            }
        }

    @Test
    fun `toggleFavorite delegates to the local source`() =
        runTest {
            everySuspend { local.toggleFavorite(oliverTwist) } returns Unit

            repository.toggleFavorite(oliverTwist)

            verifySuspend { local.toggleFavorite(oliverTwist) }
        }

    private companion object {
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
