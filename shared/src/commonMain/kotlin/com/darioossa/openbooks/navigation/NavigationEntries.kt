@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.darioossa.openbooks.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope

fun EntryProviderScope<Route>.BooksListEntry() {
    entry<BooksList>(
        metadata = ListDetailSceneStrategy.listPane()
    ) {
        // Screen content
    }
}

fun EntryProviderScope<Route>.BookDetailEntry() {
    entry<BookDetail>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { book ->
        // Screen content
    }
}

fun EntryProviderScope<Route>.FavoritesEntry() {
    entry<FavoritesList>(
        metadata = ListDetailSceneStrategy.extraPane()
    ) {
        // Screen content
    }
}