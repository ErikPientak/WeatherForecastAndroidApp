---
name: architecture
description: Established architectural patterns and conventions in WeatherForeCastAndroidApp (per-screen MVI shape, shared location/search controller, navigation, repository layer)
metadata:
  type: project
---

# Architecture conventions (confirmed as of 2026-09-09)

## Per-screen MVI-ish shape
Every screen (Home, Forecast, SavedPlaces, Detail) follows the same shape:
- `<Screen>UiState` sealed interface: `Loading`, `Error(@StringRes errorMessage: Int)`, `Success(...)`, occasionally extra states like `PermissionRequired` (Home only).
- `<Screen>Actions` sealed interface consumed via a single `onAction(action)` dispatcher in the ViewModel.
- `@HiltViewModel` classes expose `uiState: StateFlow<...>` (private `MutableStateFlow` + `asStateFlow()`).
- Composable `<Screen>Screen()` calls `hiltViewModel<...>()`, collects state with `collectAsStateWithLifecycle()`, and delegates to a stateless `<Screen>ScreenContent(state, onAction)` composable — good separation for preview/testability.
- `data.model.*` (domain models like `WeatherForecast`, `HourlyEntry`) are intentionally decoupled from `ui/elements/cards/*` presentation models (`HourlyForecastItem`, `PrecipitationPoint`, `WeeklyForecastDay`, `SavedLocationWeather`). Mapping is done in each ViewModel via private extension functions — see [[recurring_issues]] for the duplication this causes.

## ActiveLocationController (shared cross-screen search/location state)
`data/location/ActiveLocationController.kt` — `@Singleton`, owns its own `CoroutineScope(SupervisorJob())` (deliberately NOT `viewModelScope`, since it must outlive any one screen's ViewModel). Holds `activeLocation` (`ActiveLocation.Gps | Searched`), debounced (350ms) `searchQuery`/`searchResults`/`isSearching`. Both `HomeScreenViewModel` and `ForecastViewModel` mirror this into their own local search state via near-identical `viewModelScope.launch { controller.xxx.collect { ... } }` blocks (4 blocks each, copy-pasted) — see [[recurring_issues]].

## Navigation
- `navigation/NavGraph.kt` is the only place that wires navigation. Screens take plain `onXClick: () -> Unit` (or typed) callback params — **never** an `INavigationRouter` injected into a ViewModel. `INavigationRouter`/`NavigationRouterImpl` wrap `NavController` and are only instantiated in `NavGraph.kt` via `remember { NavigationRouterImpl(navController) }`.
- Bottom nav (`Home`/`Forecast`/`SavedPlaces`) shown only when current back stack entry matches one of those three `Destination`s; `Settings`/`Detail` are pushed without bottom bar.
- `Destination` is a `sealed interface` with `@Serializable` entries (type-safe nav args), including `DetailScreen(placeId, placeName, placeLatitude, placeLongitude)`.

## Repository / data layer
- `WeatherRepository`/`PlacesRepository` interfaces + Impl, bound via Hilt `@Binds` in `RepositoryModule`.
- Both repos wrap network calls in `runCatching { ... }` returning `kotlin.Result<T>` — note this does **not** rethrow `CancellationException`, a recurring risk, see [[recurring_issues]].
- Retrofit: two separate `Retrofit` instances (`@WeatherRetrofit`/`@GeocodingRetrofit` qualifiers) both pointing at Open-Meteo's free API (no API key/auth needed) — `NetworkModule.kt`.
- Room: single `AppDatabase` (version 1, `exportSchema = false`), single `SavedPlaceEntity`/`SavedPlaceDao`.
