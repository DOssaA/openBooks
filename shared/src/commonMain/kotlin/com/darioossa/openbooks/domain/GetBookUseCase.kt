package com.darioossa.openbooks.domain

import com.darioossa.openbooks.OpenForTest
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OpenForTest
class GetBookUseCase(
    private val dataSource: BooksDataSource,
) {
    suspend operator fun invoke(key: String): Book =
        withContext(Dispatchers.IO) {
            dataSource.getBook(key)
        }
}
