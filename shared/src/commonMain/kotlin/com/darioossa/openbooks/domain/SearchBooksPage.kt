package com.darioossa.openbooks.domain

import com.darioossa.openbooks.domain.entities.Book

data class SearchBooksPage(
    val books: List<Book>,
    val endReached: Boolean,
)
