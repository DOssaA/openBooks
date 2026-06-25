package com.darioossa.openbooks.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.Test

/**
 * Proves favorites survive an "app restart" without an emulator: write through one database
 * instance, close it, then open a brand-new instance backed by the same file and read the row
 * back. Runs on the JVM host (Robolectric supplies the Context and a shadow SQLite engine via the
 * framework [AndroidSQLiteDriver]), so it is gated by `:shared:allTests`. Production and the
 * instrumented test use the bundled driver; persistence semantics are identical.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // compileSdk 37 is newer than Robolectric ships images for; pin a supported level.
class FavoritesPersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "favorites-restart-test.db"

    private fun openDatabase(): FavoritesDatabase =
        Room
            .databaseBuilder<FavoritesDatabase>(context, context.getDatabasePath(dbName).absolutePath)
            .setDriver(AndroidSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    @AfterTest
    fun cleanUp() {
        context.getDatabasePath(dbName).delete()
    }

    @Test
    fun favoriteSurvivesDatabaseReopen() =
        runTest {
            val dune =
                Favorite(
                    workKey = "/works/OL893415W",
                    title = "Dune",
                    author = "Frank Herbert",
                    coverUrl = null,
                )

            val first = openDatabase()
            first.favoritesDao().upsert(dune)
            first.favoritesDao().isFavorite(dune.workKey) shouldBe true
            first.close()

            // Simulate a fresh process: new instance, same file.
            val reopened = openDatabase()
            reopened.favoritesDao().isFavorite(dune.workKey) shouldBe true
            reopened.favoritesDao().observeFavorites().first() shouldBe listOf(dune)
            reopened.close()
        }

    @Test
    fun deleteRemovesFavorite() =
        runTest {
            val db = openDatabase()
            val dao = db.favoritesDao()
            val book = Favorite("/works/OL1W", "Title", "Author", coverUrl = null)

            dao.upsert(book)
            dao.isFavorite(book.workKey) shouldBe true

            dao.delete(book.workKey)
            dao.isFavorite(book.workKey) shouldBe false
            dao.observeFavorites().first() shouldBe emptyList()

            db.close()
        }
}
