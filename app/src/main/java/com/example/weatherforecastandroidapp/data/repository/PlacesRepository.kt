package com.example.weatherforecastandroidapp.data.repository

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.model.SavedPlace
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun observeSavedPlaces(): Flow<List<SavedPlace>>
    suspend fun search(query: String): Result<List<PlaceSearchResult>>
    suspend fun addPlace(result: PlaceSearchResult)
    suspend fun removePlace(place: SavedPlace)
}
