package com.example.weatherforecastandroidapp.data.location

import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.repository.PlacesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * Shared, screen-independent "which place is active" + search state, so a search made on
 * HomeScreen and ForecastScreen stay in sync instead of each screen keeping its own override.
 * Session-only by design (in-memory, resets to [ActiveLocation.Gps] on process death) — not
 * persisted to disk.
 *
 * Owns its own [CoroutineScope] rather than using a ViewModel's `viewModelScope`: this is a
 * `@Singleton`, shared by multiple screens' ViewModels, and must outlive any single screen's
 * ViewModel (which gets cleared when that screen is navigated away from and recreated).
 */
@Singleton
class ActiveLocationController @Inject constructor(
    private val placesRepository: PlacesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob())

    private val _activeLocation = MutableStateFlow<ActiveLocation>(ActiveLocation.Gps)
    val activeLocation: StateFlow<ActiveLocation> = _activeLocation.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<PlaceSearchResult>>(emptyList())
    val searchResults: StateFlow<List<PlaceSearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    // Mirrors ForecastViewModel's original debounced-search shape (350ms, cancel-then-relaunch).
    // isSearching only flips true after the debounce delay (right before the network call), same
    // as the original, so fast typing doesn't flicker a spinner during the debounce window itself.
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = scope.launch {
            delay(SEARCH_DEBOUNCE_MS.milliseconds)
            _isSearching.value = true
            placesRepository.search(query)
                .onSuccess { results -> _searchResults.value = results }
                .onFailure { _searchResults.value = emptyList() }
            _isSearching.value = false
        }
    }

    fun selectPlace(place: PlaceSearchResult) {
        searchJob?.cancel()
        _activeLocation.value = ActiveLocation.Searched(place)
        clearSearch()
    }

    fun useDeviceLocation() {
        searchJob?.cancel()
        _activeLocation.value = ActiveLocation.Gps
        clearSearch()
    }

    private fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
