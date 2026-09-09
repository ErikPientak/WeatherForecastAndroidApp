WeatherForeCastAndroidApp
==========================

A weather forecast Android app built with Jetpack Compose (Material 3), MVVM, Hilt DI,
Retrofit + kotlinx.serialization, and Room. Weather and geocoding data come from the
Open-Meteo API (no API key required).

Package root: com.example.weatherforecastandroidapp
Module layout: single :app module (see settings.gradle.kts)


Features
--------
- Home: current conditions (temperature, high/low, humidity, wind, UV, pressure, dew
  point) plus an hourly forecast strip, based on device GPS location.
- Forecast: daily/hourly precipitation-chance graph and a weekly forecast card, with
  in-screen city search (DockedSearchBar) to check any place without changing Home.
- Saved Places: grid of bookmarked locations with live current-weather summaries,
  backed by Room. Tapping a card opens Detail.
- Detail: full weather breakdown (Home + Forecast cards combined) for a single saved
  place, without disturbing the app's shared "active location".
- Shared cross-screen search/active-location: searching a city on Home or Forecast
  updates both via a shared ActiveLocationController, so they stay in sync.
- Runtime location-permission handling (ACCESS_FINE_LOCATION) with a dedicated
  "permission required" UI state on Home and Forecast.
- Save/duplicate handling: bookmarking a place shows a snackbar confirmation and
  prevents saving the same place twice; the bookmark icon reflects saved state.
- Custom "Atmospheric Material" theme (light + partial dark), Roboto Flex variable
  font, full i18n (English + Czech).


Architecture
------------
- UI: Jetpack Compose, screens under ui/screens/<name>/ (home, forecast, savedPlaces,
  detail), each with a ViewModel + sealed UiState + sealed ScreenActions.
- Reusable UI: ui/elements/ (BaseScreen, LoadingScreen, CitySearchBar, snackbar) and
  ui/elements/cards/ (HomeCard, MetricCard, HourlyForecastCard,
  PrecipitationChanceGraphCard, WeeklyForecastCard, SavedLocationCard).
- Navigation: navigation/ package, type-safe Navigation Compose routes
  (Destination.kt) driven from NavGraph.kt. Screens use plain onXClick callbacks;
  ViewModels never depend on navigation.
- Data: data/model (domain models), data/remote (Retrofit services + DTOs),
  data/local (Room: SavedPlaceEntity/Dao/AppDatabase), data/location
  (LocationTracker, ActiveLocationController), data/repository
  (WeatherRepository, PlacesRepository).
- DI: Hilt modules under di/ (NetworkModule, RepositoryModule, DatabaseModule,
  LocationModule). Application class: WeatherForecastApp.
- util/: WeatherCodeMapper, UvIndexMapper, PressureCategoryMapper,
  WindDirectionMapper — raw value -> localized string-resource label.


Build config
------------
- compileSdk / targetSdk: 37, minSdk: 33
- Java / Kotlin target compatibility: 11
- Dependency versions centralized in gradle/libs.versions.toml


Known gaps
----------
- No unsave/delete action exists on the Saved Places grid itself (only from Detail).
- Dark theme only overrides primary/secondary/tertiary; the rest falls back to
  Material 3 stock dark colors.
- No Room migration strategy defined yet.
