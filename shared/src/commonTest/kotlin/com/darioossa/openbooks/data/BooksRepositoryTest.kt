package com.darioossa.openbooks.data

import app.cash.turbine.test
import com.darioossa.openbooks.data.local.BooksLocalSource
import com.darioossa.openbooks.data.remote.BooksRemoteSource
import com.darioossa.openbooks.domain.SearchBooksPage
import com.darioossa.openbooks.domain.entities.Book
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
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
}
