---
name: build_config
description: Build/Gradle/DI configuration facts for WeatherForeCastAndroidApp relevant to production-readiness reviews
metadata:
  type: project
---

# Build & config facts (verified 2026-09-09, app/build.gradle.kts + gradle/libs.versions.toml)

- `release` build type explicitly has `optimization { enable = false }` (this project uses the newer AGP 9.x
  `optimization {}` DSL, not the old `isMinifyEnabled`) — R8/minify/shrink is OFF for release builds.
- No `proguard-rules.pro` file exists anywhere in the repo (confirmed via glob) — consistent with minify being
  off, but flag as a gap the moment optimization is turned on for a real release.
- `minSdk = 33`, `targetSdk = compileSdk = 37`, Kotlin 2.2.10, AGP 9.3.1, Compose BOM 2026.02.01, Hilt 2.60.1,
  Room 2.8.4, Retrofit 2.9.0 + kotlinx-serialization converter, OkHttp 5.5.0 (via Retrofit's default client —
  no explicit `OkHttpClient` is built anywhere, see below).
- `NetworkModule.kt`: two `Retrofit` instances (`@WeatherRetrofit`, `@GeocodingRetrofit`) both against
  `api.open-meteo.com` / `geocoding-api.open-meteo.com` (free, no API key). Neither configures a custom
  `OkHttpClient` — no explicit connect/read/write timeouts, no logging interceptor (even debug-gated), no
  cache, no retry-on-connection-failure override. Relies entirely on Retrofit/OkHttp defaults.
- Room: single `AppDatabase` version 1, `exportSchema = false`, no migration strategy defined yet (fine at
  v1, but will need `fallbackToDestructiveMigration()` or real `Migration` objects the moment schema changes).
- No Crashlytics/Timber/any logging framework anywhere in the app — confirmed via grep for `Log\.` (zero
  matches). Means no production error visibility beyond what StateFlow error states surface to the user.
