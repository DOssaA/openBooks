package com.darioossa.openbooks.presentation.bookList

import com.darioossa.openbooks.domain.entities.Book

sealed class ListState {
    data object Empty : ListState()

    data object Loading : ListState()

    data class Success(
        val booksList: List<Book>,
    ) : ListState()

    data object Error : ListState()
}
