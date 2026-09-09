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

    @Query("SELECT EXISTS(SELECT 1 FROM saved_places WHERE latitude = :latitude AND longitude = :longitude)")
    suspend fun exists(latitude: Double, longitude: Double): Boolean

    @Insert
    suspend fun insert(place: SavedPlaceEntity): Long

    @Delete
    suspend fun delete(place: SavedPlaceEntity)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun deleteById(id: Long)
}
