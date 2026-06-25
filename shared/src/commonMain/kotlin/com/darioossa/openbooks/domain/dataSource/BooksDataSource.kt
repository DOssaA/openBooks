package com.darioossa.openbooks.domain.dataSource

import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.flow.Flow

interface BooksDataSource {
    suspend fun searchBooks(query: String): Flow<List<Book>>
}
