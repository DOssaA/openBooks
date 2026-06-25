package com.darioossa.openbooks.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.darioossa.openbooks.data.local.FavoritesDao
import com.darioossa.openbooks.data.local.FavoritesDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single

actual val platformModule =
    module {
        single { create(::provideFavoritesDatabase) }
        single { create(::provideFavoritesDao) }
    }

fun provideFavoritesDatabase(context: Context): FavoritesDatabase {
    val dbPath = context.getDatabasePath(FavoritesDatabase.NAME).absolutePath
    return Room
        .databaseBuilder<FavoritesDatabase>(context = context, name = dbPath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

fun provideFavoritesDao(database: FavoritesDatabase): FavoritesDao = database.favoritesDao()
