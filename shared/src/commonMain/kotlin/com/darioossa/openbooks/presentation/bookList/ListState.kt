package com.darioossa.openbooks.presentation.bookList

import com.darioossa.openbooks.domain.entities.Book

sealed interface ListState {
    data object Idle : ListState

    data object Loading : ListState

    data class Success(
        val books: List<Book>,
        val loadingMore: Boolean,
        val endReached: Boolean,
    ) : ListState

    data object Empty : ListState

    data object Error : ListState
}
