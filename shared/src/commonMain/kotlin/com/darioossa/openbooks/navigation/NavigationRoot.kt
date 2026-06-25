package com.darioossa.openbooks.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NavigationRoot() {
    val backStack = remember { mutableStateListOf<Route>(BooksList) }
    val strategy = rememberListDetailSceneStrategy<Route>()
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                slideOutHorizontally(targetOffsetX = { -it })
        },
        sceneStrategies = listOf(strategy),
        entryProvider =
            entryProvider<Route> {
                BooksListEntry(
                    onBookClick = { key ->
                        if (backStack.lastOrNull() != BookDetail(key)) {
                            backStack.add(BookDetail(key))
                        }
                    },
                    onFavoritesClick = {
                        if (backStack.lastOrNull() != FavoritesList) {
                            backStack.add(FavoritesList)
                        }
                    },
                )
                BookDetailEntry()
                FavoritesEntry()
            },
    )
}
