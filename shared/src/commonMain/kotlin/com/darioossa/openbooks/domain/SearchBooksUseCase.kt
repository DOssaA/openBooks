package com.darioossa.openbooks.domain

import com.darioossa.openbooks.OpenForTest
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

@OpenForTest
class SearchBooksUseCase(
    private val dataSource: BooksDataSource,
) {
    suspend operator fun invoke(query: String): Flow<List<Book>> = dataSource.searchBooks(query).flowOn(Dispatchers.IO)
}
