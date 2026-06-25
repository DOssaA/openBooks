package com.darioossa.openbooks.navigation

import androidx.navigation3.runtime.NavKey
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey

data object BooksList : Route

data class BookDetail(
    val book: Book,
) : Route

data object FavoritesList : Route
