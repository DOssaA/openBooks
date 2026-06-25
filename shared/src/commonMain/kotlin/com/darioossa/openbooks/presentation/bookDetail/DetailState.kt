package com.darioossa.openbooks.presentation.bookDetail

import com.darioossa.openbooks.domain.entities.Book

sealed interface DetailState {
    data object Loading : DetailState

    data class Success(
        val book: Book,
        val isFavorite: Boolean,
    ) : DetailState

    data object Error : DetailState
}
