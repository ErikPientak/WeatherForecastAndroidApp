package com.example.weatherforecastandroidapp.data.location.ActiveLocation

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.repository.PlacesRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveLocationController@Inject constructor(
    private val placesRepository: PlacesRepository,
) {
    val activeLocation: StateFlow<ActiveLocation>
    val searchQuery: StateFlow<String>
    val searchResult: StateFlow<List<PlaceSearchResult>>

    fun onSearchQueryChange(query: String)
    fun selectePlace(place: PlaceSearchResult)
    fun useDeviceLocation()
}