package com.darioossa.openbooks.domain.dataSource

import com.darioossa.openbooks.domain.SearchBooksPage
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.flow.Flow

interface BooksDataSource {
    suspend fun searchBooks(
        query: String,
        page: Int,
    ): Flow<SearchBooksPage>

    fun observeFavoriteKeys(): Flow<Set<String>>

    suspend fun toggleFavorite(book: Book)
}
