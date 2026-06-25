package com.darioossa.openbooks.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites ORDER BY title")
    fun observeFavorites(): Flow<List<Favorite>>

    @Upsert
    suspend fun upsert(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE workKey = :workKey")
    suspend fun delete(workKey: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE workKey = :workKey)")
    suspend fun isFavorite(workKey: String): Boolean
}
