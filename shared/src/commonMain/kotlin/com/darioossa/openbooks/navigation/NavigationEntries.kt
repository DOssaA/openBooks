@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.darioossa.openbooks.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import com.darioossa.openbooks.presentation.bookList.BookListScreen
import com.darioossa.openbooks.presentation.favorites.FavoritesScreen

@Composable
fun EntryProviderScope<Route>.BooksListEntry() {
    entry<BooksList>(
        metadata = ListDetailSceneStrategy.listPane(),
    ) {
        BookListScreen()
    }
}

@Composable
fun EntryProviderScope<Route>.BookDetailEntry() {
    entry<BookDetail>(
        metadata = ListDetailSceneStrategy.detailPane(),
    ) { book ->
        // Screen content
    }
}

@Composable
fun EntryProviderScope<Route>.FavoritesEntry() {
    entry<FavoritesList>(
        metadata = ListDetailSceneStrategy.extraPane(),
    ) {
        FavoritesScreen()
    }
}
