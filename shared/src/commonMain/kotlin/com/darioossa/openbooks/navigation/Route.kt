package com.darioossa.openbooks.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey

data object BooksList : Route

data class BookDetail(
    val key: String,
) : Route

data object FavoritesList : Route
