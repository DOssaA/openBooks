package com.darioossa.openbooks.presentation.favorites

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.darioossa.openbooks.domain.entities.Book
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34]) // compileSdk 37 is newer than Robolectric ships images for; pin a supported level.
class FavoritesScreenTest {
    @Test
    fun `content state lists each favorite`() =
        runComposeUiTest {
            setContent {
                FavoritesContent(
                    state = FavoritesState.Content(listOf(oliverTwist)),
                    onRemove = {},
                )
            }

            onNodeWithText("Oliver Twist").assertIsDisplayed()
            onNodeWithText("Charles Dickens").assertIsDisplayed()
        }

    @Test
    fun `empty state shows the empty message`() =
        runComposeUiTest {
            setContent {
                FavoritesContent(
                    state = FavoritesState.Empty,
                    onRemove = {},
                )
            }

            onNodeWithText("No favorites yet").assertIsDisplayed()
        }

    @Test
    fun `remove button reports the favorite to be removed`() =
        runComposeUiTest {
            var removed: Book? = null
            setContent {
                FavoritesContent(
                    state = FavoritesState.Content(listOf(oliverTwist)),
                    onRemove = { removed = it },
                )
            }

            onNodeWithText("Remove").performClick()

            assertTrue(removed == oliverTwist)
        }

    private companion object {
        val oliverTwist =
            Book(
                key = "OL45804W",
                title = "Oliver Twist",
                authors = listOf("Charles Dickens"),
                coverUrl = null,
                firstPublishYear = null,
            )
    }
}
