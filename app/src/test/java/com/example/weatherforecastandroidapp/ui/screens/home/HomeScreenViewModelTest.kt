package com.example.weatherforecastandroidapp.ui.screens.home

import android.location.Location
import com.example.weatherforecastandroidapp.MainDispatcherRule
import com.example.weatherforecastandroidapp.R
import com.example.weatherforecastandroidapp.data.location.ActiveLocationController
import com.example.weatherforecastandroidapp.data.location.LocationTracker
import com.example.weatherforecastandroidapp.data.model.CurrentConditions
import com.example.weatherforecastandroidapp.data.model.DailyEntry
import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.model.WeatherForecast
import com.example.weatherforecastandroidapp.data.repository.PlacesRepository
import com.example.weatherforecastandroidapp.data.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * See PlacesRepositoryImplTest / WeatherRepositoryImplTest for the base MockK/coroutines-test
 * patterns. Testing a ViewModel adds two new wrinkles on top of those, both explained inline
 * below: (1) viewModelScope needs Dispatchers.Main set up via [MainDispatcherRule], and (2) since
 * HomeScreenViewModel launches its work with `viewModelScope.launch { }` instead of running it
 * synchronously, we need `advanceUntilIdle()` after triggering an action to let that launched
 * coroutine actually finish before we check `uiState.value`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    // @get:Rule (not @Rule on a val alone) is required for JUnit rules backed by a property in
    // Kotlin - JUnit needs to see this as a public method annotated @Rule under the hood.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val weatherRepository = mockk<WeatherRepository>()
    private val locationTracker = mockk<LocationTracker>()
    private val placesRepository = mockk<PlacesRepository>()

    // ActiveLocationController is a real (non-mocked) instance here, not a mockk<>() fake. It's a
    // plain, cheap, side-effect-free class once its own dependency (placesRepository) is mocked -
    // faking its StateFlow properties convincingly with MockK would take more setup than just
    // using the real thing. Prefer a real collaborator over a mock whenever the real one is simple
    // enough to construct directly; save mocking for things that are expensive, slow, or reach
    // outside the process (network, disk, Android framework classes).
    private val activeLocationController = ActiveLocationController(placesRepository)

    private lateinit var viewModel: HomeScreenViewModel

    @Before
    fun setUp() {
        viewModel = HomeScreenViewModel(
            weatherRepository = weatherRepository,
            locationTracker = locationTracker,
            activeLocationController = activeLocationController,
            placesRepository = placesRepository,
        )
    }

    @Test
    fun `initial state is Loading before permission is resolved`() = runTest(mainDispatcherRule.testDispatcher) {
        // HomeScreenViewModel's init block immediately launches a collector on activeLocation,
        // which calls loadForecast(). Since activeLocation defaults to Gps and hasLocationPermission
        // starts false, loadForecast() bails out before touching uiState (see the permission-gate
        // comment in the ViewModel itself) - so uiState should still be sitting at its initial
        // Loading value even after that launched coroutine has had a chance to run.
        //
        // advanceUntilIdle() runs every coroutine queued on the shared test dispatcher (see the
        // comment on MainDispatcherRule.testDispatcher for why runTest is passed that same
        // dispatcher instance) until none are left runnable. Without this call, the init block's
        // viewModelScope.launch { ... } coroutines would simply never execute during this test -
        // StandardTestDispatcher queues work instead of running it immediately.
        advanceUntilIdle()

        assertEquals(HomeScreenUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `LocationPermissionDenied shows PermissionRequired`() {
        // No advanceUntilIdle() needed here - the Denied branch in onAction() sets uiState
        // synchronously with no viewModelScope.launch involved.
        viewModel.onAction(HomeScreenActions.LocationPermissionDenied)

        assertEquals(HomeScreenUiState.PermissionRequired, viewModel.uiState.value)
    }

    @Test
    fun `LocationPermissionGranted with a GPS fix loads the forecast successfully`() = runTest(mainDispatcherRule.testDispatcher) {
        val fakeLocation = mockk<Location>()
        every { fakeLocation.latitude } returns 50.0755
        every { fakeLocation.longitude } returns 14.4378
        coEvery { locationTracker.getCurrentLocation() } returns fakeLocation
        coEvery { weatherRepository.getForecast(50.0755, 14.4378) } returns Result.success(fakeForecast())

        viewModel.onAction(HomeScreenActions.LocationPermissionGranted)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        check(state is HomeScreenUiState.Success) { "Expected Success but was $state" }
        assertEquals(21, state.temperature) // 21.5 truncated via .toInt()
        assertEquals(2, state.weatherCode)
        assertEquals(true, state.isDay)
        assertEquals(25, state.highTemperature) // from daily[0].tempMax
        assertEquals(15, state.lowTemperature)  // from daily[0].tempMin
        assertEquals(60, state.humidity)
        assertEquals(12.0, state.windSpeed, 0.0)
        assertEquals(270, state.windDirection)
        assertEquals(1013.0, state.pressure, 0.0)
        assertEquals(13, state.dewPoint) // 13.5 truncated via .toInt()
        assertEquals("", state.locationName) // GPS branch always uses "" for the name
        assertEquals(false, state.isSaved)   // GPS branch always hardcodes isSaved = false
    }

    @Test
    fun `LocationPermissionGranted with no GPS fix shows a location error`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { locationTracker.getCurrentLocation() } returns null

        viewModel.onAction(HomeScreenActions.LocationPermissionGranted)
        advanceUntilIdle()

        assertEquals(
            HomeScreenUiState.Error(R.string.error_location_not_found),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `LocationPermissionGranted with a network failure shows a forecast error`() = runTest(mainDispatcherRule.testDispatcher) {
        val fakeLocation = mockk<Location>()
        every { fakeLocation.latitude } returns 50.0755
        every { fakeLocation.longitude } returns 14.4378
        coEvery { locationTracker.getCurrentLocation() } returns fakeLocation
        coEvery { weatherRepository.getForecast(any(), any()) } returns Result.failure(IOException("no connection"))

        viewModel.onAction(HomeScreenActions.LocationPermissionGranted)
        advanceUntilIdle()

        assertEquals(
            HomeScreenUiState.Error(R.string.error_could_not_load_forecast),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `PlaceSelected loads the forecast for the searched place, not GPS`() = runTest(mainDispatcherRule.testDispatcher) {
        val place = PlaceSearchResult(
            name = "Prague",
            admin1 = "Prague",
            country = "Czechia",
            latitude = 50.0755,
            longitude = 14.4378,
        )
        // Note: unlike the GPS tests above, we don't stub locationTracker at all here - the
        // Searched branch in loadForecast() never calls it, and MockK would throw if it were
        // called unexpectedly. Not stubbing it is itself an assertion: "this code path must not
        // touch GPS."
        coEvery { placesRepository.isPlaceSaved(place.latitude, place.longitude) } returns false
        coEvery { weatherRepository.getForecast(place.latitude, place.longitude) } returns Result.success(fakeForecast())

        // PlaceSelected is the action HomeScreen dispatches when the user taps a search result.
        // Internally it calls activeLocationController.selectPlace(place), which updates the
        // shared StateFlow that HomeScreenViewModel's init block is already collecting - THAT
        // collection (not this onAction call directly) is what re-triggers loadForecast(). This
        // is the same reactive path a search made on ForecastScreen would take to update Home.
        viewModel.onAction(HomeScreenActions.PlaceSelected(place))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        check(state is HomeScreenUiState.Success) { "Expected Success but was $state" }
        assertEquals(place.name, state.locationName) // Searched branch uses the place's real name...
        assertEquals(false, state.isSaved)            // ...and looks up isSaved instead of hardcoding it
        assertEquals(21, state.temperature)
        assertEquals(2, state.weatherCode)

        // onPlaceSelected() also closes the search bar as a side effect - worth asserting since
        // it's easy to accidentally break while touching this method later.
        assertEquals(false, viewModel.searchState.value.isActive)
    }

    @Test
    fun `PlaceSelected reflects isSaved true when the place was already saved`() = runTest(mainDispatcherRule.testDispatcher) {
        val place = PlaceSearchResult(
            name = "Brno",
            admin1 = "South Moravian",
            country = "Czechia",
            latitude = 49.1951,
            longitude = 16.6068,
        )
        coEvery { placesRepository.isPlaceSaved(place.latitude, place.longitude) } returns true
        coEvery { weatherRepository.getForecast(place.latitude, place.longitude) } returns Result.success(fakeForecast())

        viewModel.onAction(HomeScreenActions.PlaceSelected(place))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        check(state is HomeScreenUiState.Success) { "Expected Success but was $state" }
        // This is the contrast worth having a dedicated test for: the GPS branch always hardcodes
        // isSaved = false (see the GPS success test above), but the Searched branch actually asks
        // PlacesRepository. Only testing one place-saved value here wouldn't prove that distinction.
        assertEquals(true, state.isSaved)
    }

    @Test
    fun `PlaceSaved does nothing when the active location is GPS`() = runTest(mainDispatcherRule.testDispatcher) {
        // activeLocationController starts at ActiveLocation.Gps by default and nothing in this
        // test changes that. onPlaceSaved()'s guard (`if (location is ActiveLocation.Searched)`)
        // should make this a complete no-op - there's no "current searched place" to save.
        viewModel.onAction(HomeScreenActions.PlaceSaved)
        advanceUntilIdle()

        coVerify(exactly = 0) { placesRepository.addPlace(any()) }
    }

    @Test
    fun `PlaceSaved emits place_saved_message and sets isSaved when newly saved`() = runTest(mainDispatcherRule.testDispatcher) {
        val place = PlaceSearchResult(
            name = "Prague",
            admin1 = "Prague",
            country = "Czechia",
            latitude = 50.0755,
            longitude = 14.4378,
        )
        // isPlaceSaved is only consulted while loading the forecast (see loadForecast()'s Searched
        // branch); it's a separate check from addPlace() below and deliberately returns false here
        // to represent "not saved yet" at load time, which is what makes a subsequent save
        // meaningful. Select the place first so there's a "current searched place" for
        // onPlaceSaved() to act on, and so uiState is Success before we assert isSaved changed on it.
        coEvery { placesRepository.isPlaceSaved(place.latitude, place.longitude) } returns false
        coEvery { weatherRepository.getForecast(place.latitude, place.longitude) } returns Result.success(fakeForecast())
        viewModel.onAction(HomeScreenActions.PlaceSelected(place))
        advanceUntilIdle()

        coEvery { placesRepository.addPlace(place) } returns true // true = newly saved (wasn't a duplicate)

        // saveResultEvent is a SharedFlow with replay = 0: a value emitted into it is only ever
        // delivered to collectors that are ALREADY suspended waiting on it at the moment of
        // emit() - there's no history for a collector that starts listening afterward, unlike a
        // StateFlow's .value. So to observe an emission in a test, we must start collecting
        // BEFORE triggering the action, using our own background coroutine (`launch` here is
        // TestScope's own launch, from runTest's CoroutineScope). toList(events) collects forever
        // (SharedFlow never completes) into a live list, so we cancel the job manually once we're
        // done asserting - otherwise it would hang the test. (Turbine's `flow.test { awaitItem() }`
        // is the library that exists specifically to make this pattern less manual, if this
        // project ever adds it.)
        val events = mutableListOf<Int>()
        val collectJob = launch { viewModel.saveResultEvent.toList(events) }

        viewModel.onAction(HomeScreenActions.PlaceSaved)
        advanceUntilIdle()
        collectJob.cancel()

        assertEquals(listOf(R.string.place_saved_message), events)
        val state = viewModel.uiState.value
        check(state is HomeScreenUiState.Success) { "Expected Success but was $state" }
        assertTrue(state.isSaved)
    }

    @Test
    fun `PlaceSaved emits place_already_saved_message when the place was already saved`() = runTest(mainDispatcherRule.testDispatcher) {
        val place = PlaceSearchResult(
            name = "Prague",
            admin1 = "Prague",
            country = "Czechia",
            latitude = 50.0755,
            longitude = 14.4378,
        )
        coEvery { placesRepository.isPlaceSaved(place.latitude, place.longitude) } returns false
        coEvery { weatherRepository.getForecast(place.latitude, place.longitude) } returns Result.success(fakeForecast())
        viewModel.onAction(HomeScreenActions.PlaceSelected(place))
        advanceUntilIdle()

        coEvery { placesRepository.addPlace(place) } returns false // false = already existed, duplicate rejected

        val events = mutableListOf<Int>()
        val collectJob = launch { viewModel.saveResultEvent.toList(events) }

        viewModel.onAction(HomeScreenActions.PlaceSaved)
        advanceUntilIdle()
        collectJob.cancel()

        // The message differs from the test above, but note uiState.isSaved is set to true either
        // way (see the ViewModel's onPlaceSaved()) - from the UI's perspective, "already saved" and
        // "newly saved" both end with the bookmark icon showing filled, just with a different
        // snackbar wording. Worth knowing that's deliberate, not an oversight, if you're ever
        // tempted to only flip isSaved on the "newly saved" branch.
        assertEquals(listOf(R.string.place_already_saved_message), events)
    }

    /**
     * A minimal-but-realistic WeatherForecast for stubbing weatherRepository.getForecast(...).
     * hourly is deliberately empty: HomeScreenViewModel.toForecastItems() filters hourly entries
     * by comparing their timestamp to LocalDate.now()/LocalDateTime.now() - the real system clock,
     * with no way to inject a fake one. A non-empty hourly fixture would need timestamps computed
     * relative to "right now" to survive that filter, and the "Now" label / isDay flag inside
     * toForecastItems() would still depend on the exact hour the test happens to run in. Leaving
     * hourly empty sidesteps that time-based flakiness entirely; it does mean this test doesn't
     * verify toForecastItems() itself - that mapping would need its own dedicated test with an
     * injected/fake clock, which the ViewModel doesn't currently support (worth flagging as a
     * follow-up: extracting a Clock/TimeProvider dependency would make that testable).
     */
    private fun fakeForecast(): WeatherForecast = WeatherForecast(
        current = CurrentConditions(
            time = "2026-09-11T12:00",
            temperature = 21.5,
            apparentTemperature = 20.0,
            humidity = 60,
            windSpeed = 12.0,
            windDirection = 270,
            pressure = 1013.0,
            isDay = true,
            weatherCode = 2,
            dewPoint = 13.5,
        ),
        hourly = emptyList(),
        daily = listOf(
            DailyEntry(
                date = "2026-09-11",
                weatherCode = 2,
                tempMax = 25.0,
                tempMin = 15.0,
                precipitationProbabilityMax = 10,
                sunrise = "2026-09-11T06:30",
                sunset = "2026-09-11T19:45",
                uvIndexMax = 6.0,
                daylightDuration = 47700.0,
            ),
        ),
    )
}
