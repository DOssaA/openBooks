package com.darioossa.openbooks.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [Favorite::class], version = 1)
@ConstructedBy(FavoritesDatabaseConstructor::class)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        const val NAME = "favorites.db"
    }
}

// Room's compiler generates the `actual` implementation per platform.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpectClass")
expect object FavoritesDatabaseConstructor : RoomDatabaseConstructor<FavoritesDatabase> {
    override fun initialize(): FavoritesDatabase
}
