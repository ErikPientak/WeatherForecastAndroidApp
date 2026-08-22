package com.example.weatherforecastandroidapp.data.repository

import com.example.weatherforecastandroidapp.data.local.SavedPlaceDao
import com.example.weatherforecastandroidapp.data.local.SavedPlaceEntity
import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.model.SavedPlace
import com.example.weatherforecastandroidapp.data.remote.GeocodingApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    private val geocodingApiService: GeocodingApiService,
    private val savedPlaceDao: SavedPlaceDao,
) : PlacesRepository {

    override fun observeSavedPlaces(): Flow<List<SavedPlace>> =
        savedPlaceDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun search(query: String): Result<List<PlaceSearchResult>> = runCatching {
        geocodingApiService.search(name = query, count = 10, language = "en", format = "json")
            .results
            .orEmpty()
            .map {
                PlaceSearchResult(
                    name = it.name,
                    admin1 = it.admin1,
                    country = it.country,
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            }
    }

    override suspend fun addPlace(result: PlaceSearchResult) {
        savedPlaceDao.insert(
            SavedPlaceEntity(
                name = result.name,
                admin1 = result.admin1,
                country = result.country,
                latitude = result.latitude,
                longitude = result.longitude,
                addedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun removePlace(place: SavedPlace) {
        savedPlaceDao.deleteById(place.id)
    }

    private fun SavedPlaceEntity.toDomain() = SavedPlace(
        id = id,
        name = name,
        admin1 = admin1,
        country = country,
        latitude = latitude,
        longitude = longitude,
    )
}
