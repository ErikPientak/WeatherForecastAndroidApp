package com.example.weatherforecastandroidapp.data.repository

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.model.SavedPlace
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun observeSavedPlaces(): Flow<List<SavedPlace>>
    suspend fun search(query: String): Result<List<PlaceSearchResult>>
    /** Inserts [result] as a saved place. Returns false without inserting if it's already saved. */
    suspend fun addPlace(result: PlaceSearchResult): Boolean
    suspend fun removePlace(place: SavedPlace)
    suspend fun isPlaceSaved(latitude: Double, longitude: Double): Boolean
}
