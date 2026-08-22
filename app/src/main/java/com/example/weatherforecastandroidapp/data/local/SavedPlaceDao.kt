package com.example.weatherforecastandroidapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {

    @Query("SELECT * FROM saved_places ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<SavedPlaceEntity>>

    @Insert
    suspend fun insert(place: SavedPlaceEntity): Long

    @Delete
    suspend fun delete(place: SavedPlaceEntity)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun deleteById(id: Long)
}
