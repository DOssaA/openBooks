package com.darioossa.openbooks.data.remote

import com.darioossa.openbooks.data.remote.dto.SearchResponseDto
import com.darioossa.openbooks.data.remote.dto.WorkDto
import com.darioossa.openbooks.domain.entities.Book
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

interface BooksRemoteSource {
    suspend fun search(
        query: String,
        page: Int,
    ): List<Book>

    suspend fun getBook(key: String): Book
}

class BooksRemote(
    private val client: HttpClient,
) : BooksRemoteSource {
    override suspend fun search(
        query: String,
        page: Int,
    ): List<Book> {
        val response: SearchResponseDto =
            client
                .get("/search.json") {
                    parameter("q", query)
                    parameter("page", page)
                    parameter("limit", PAGE_SIZE)
                }.body()
        return response.docs.map { it.toBook() }
    }

    override suspend fun getBook(key: String): Book {
        val work: WorkDto = client.get("$WORKS_PREFIX${key.toWorkKey()}.json").body()
        return work.toBook()
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
