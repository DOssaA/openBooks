package com.darioossa.openbooks.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.darioossa.openbooks.data.local.FavoritesDao
import com.darioossa.openbooks.data.local.FavoritesDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule =
    module {
        single<FavoritesDatabase> {
            val context: Context = androidContext()
            val dbPath = context.getDatabasePath(FavoritesDatabase.NAME).absolutePath
            Room
                .databaseBuilder<FavoritesDatabase>(context = context, name = dbPath)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }
        single<FavoritesDao> { get<FavoritesDatabase>().favoritesDao() }
    }
