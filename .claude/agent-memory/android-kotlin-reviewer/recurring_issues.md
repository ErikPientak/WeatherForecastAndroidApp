---
name: recurring_issues
description: Known recurring bugs, duplicated logic, and dead code in WeatherForeCastAndroidApp — re-verify these are still present (or fixed) on each new review pass
metadata:
  type: project
---

# Recurring issues to re-check each review (confirmed present as of commit e916a5b, 2026-09-09)

## Triplicated forecast-mapping logic (Home/Forecast/Detail ViewModels)
`HomeScreenViewModel`, `ForecastViewModel`, `DetailScreenViewModel` each independently define near-identical
private extension functions: `List<HourlyEntry>.toForecastItems()` and `List<HourlyEntry>.toPrecipitationPoints()`,
plus inline `forecast.daily.map { WeeklyForecastDay(...) }` / `PrecipitationPoint(...)` blocks. Candidate for
extraction into a shared mapper/use-case class (e.g. `ForecastPresentationMapper`) injected into all three VMs.

## Fake/duplicated day-night heuristic
`HomeScreenViewModel.toForecastItems()` and `DetailScreenViewModel.toForecastItems()` both set each
`HourlyForecastItem.isDay` via a hardcoded `LocalDateTime.now().hour in 6..18` heuristic — using the *device's*
current local hour, not the target place's actual sunrise/sunset (which the API already provides via
`DailyEntry.sunrise`/`sunset`, and which `CurrentConditions.isDay` already carries correctly and IS used
correctly elsewhere, e.g. `SavedPlaceScreenViewModel.toSavedLocationWeather` and
`DetailScreenViewModel`'s top-level `isDay`). This is both a duplication smell and a real accuracy bug for
non-local/searched places and for winter/summer daylight variation.

## Missing location-permission gate in ForecastViewModel
`HomeScreenViewModel` has a `hasLocationPermission` flag that gates the `Gps` branch of `loadForecast()` so it
doesn't call `LocationTracker.getCurrentLocation()` (a `@SuppressLint("MissingPermission")` call) before
permission is actually confirmed granted. `ForecastViewModel.loadForecast()` has **no such gate** — its `init`
block collects `activeLocationController.activeLocation` and calls `loadForecast()` immediately, which can hit
`LocationTracker.getCurrentLocation()` before permission is granted, especially if the user navigates to the
Forecast tab during/before Home's permission flow resolves. Since neither `LocationTracker` nor `loadForecast()`
catches exceptions from `getCurrentLocation()` (only the repository call itself is wrapped in `runCatching`), a
`SecurityException` here is uncaught in `viewModelScope.launch` and will crash the app.

## DetailScreenViewModel swallows forecast-load failures
`DetailScreenViewModel.loadForecast()` only has `.onSuccess { ... }` on the `Result` from
`weatherRepository.getForecast(...)` — no `.onFailure`. On failure, `uiState` stays on `Loading` forever with no
error state and no retry affordance (infinite spinner).

## Dead code: SavedPlacesScreenActions / delete-place feature
`ui/screens/savedPlaces/SavedPlacesScreenActions.kt` (interface with `deletePlace`) is defined but never
implemented or referenced anywhere. `SavedPlaceScreenViewModel.deletePlace(place: SavedPlace)` exists but is an
empty `{}` body and is never called from `SavedPlacesScreen`/`SavedLocationCard` (no delete/unsave affordance
wired in the UI at all). `DetailScreenViewModel.deletePlace()` (the one that's actually wired to the UI, via
Detail's trash-can icon) is the real/working delete path — don't confuse the two when reviewing.

## Stray unused imports (copy-paste leftovers)
`ui/screens/home/HomeScreen.kt` and `HomeScreenActions.kt` both import
`com.example.weatherforecastandroidapp.ui.screens.forecast.ForecastScreenActions` unused (copy-paste leftover
from Forecast). `HomeScreen.kt` also has unused `androidx.compose.runtime.collectAsState` (superseded by
`collectAsStateWithLifecycle`) and unused `androidx.compose.material.icons.filled.Settings` (leftover from the
"Removed Settings Icon" commit per git log — the icon button was deleted but the import wasn't).

## `runCatching` around suspend network calls catches CancellationException
`WeatherRepositoryImpl.getForecast()` and `PlacesRepositoryImpl.search()` both wrap the Retrofit call in
`runCatching { ... }`. Plain `runCatching` catches `CancellationException` too, which breaks structured
concurrency (a cancelled coroutine's cancellation gets silently turned into a `Result.failure` instead of
propagating). Should re-throw `CancellationException` (e.g. via a `runSuspendCatching` helper that checks
`is CancellationException -> throw it`) or use a custom wrapper.
