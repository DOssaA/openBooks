package com.darioossa.openbooks.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * On-device proof that favorites persist across a process restart, exercising the production
 * [BundledSQLiteDriver] path. Runs on an emulator/device (`:shared:connectedAndroidDeviceTest`);
 * it is not gated by the current CI, so it is verified manually before merge.
 */
@RunWith(AndroidJUnit4::class)
class FavoritesDatabaseInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val dbName = "favorites-instrumented-test.db"

    private fun openDatabase(): FavoritesDatabase =
        Room
            .databaseBuilder<FavoritesDatabase>(context, context.getDatabasePath(dbName).absolutePath)
            .setDriver(BundledSQLiteDriver())
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
            assertTrue(first.favoritesDao().isFavorite(dune.workKey))
            first.close()

            val reopened = openDatabase()
            assertTrue(reopened.favoritesDao().isFavorite(dune.workKey))
            assertEquals(listOf(dune), reopened.favoritesDao().observeFavorites().first())
            reopened.close()
        }
}
