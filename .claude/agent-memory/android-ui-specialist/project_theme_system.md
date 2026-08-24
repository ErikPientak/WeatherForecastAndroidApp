---
name: project-theme-system
description: How this app's Material3 theme is built — "Atmospheric Material" / "Midnight Atmospheric" custom design system, source-of-truth specs, and token wiring
metadata:
  type: project
---

This app implements a custom M3 theme called "Atmospheric Material" (light) / "Midnight Atmospheric" (dark), sourced from two design-spec text files at the repo root:
- `DESIGN..txt` (light — note the literal double-dot filename)
- `DESIGNDARK.txt` (dark)

Both specs describe brand/style (Modern Corporate + Glassmorphism), color roles, elevation/depth treatment (glassmorphic cards: light = surface @80% opacity + 20px backdrop blur; dark = surface-container @85% opacity + 24px backdrop blur — note: this project does NOT currently implement real backdrop blur anywhere, cards just use flat/gradient fills), and shape scale (`rounded-xl` 24-32px for standard cards, full pill for buttons/chips, 16px for small components like chips).

Token implementation, all in `app/src/main/java/com/example/weatherforecastandroidapp/ui/theme/`:
- `Color.kt` — raw hex constants for both schemes; dark tokens are prefixed `Dark*` and are an independent set (not derived from light — e.g. tertiaryFixed differs between the two specs).
- `Theme.kt` — builds `lightColorScheme()`/`darkColorScheme()` from those constants, including the M3 **fixed color roles** (`primaryFixed`, `primaryFixedDim`, `secondaryFixed`, `tertiaryFixed`, etc.) — these ARE wired up and usable via `MaterialTheme.colorScheme.primaryFixed` etc., not just theoretical M3 API surface. `dynamicColor` defaults to `false` deliberately (would override the branded palette on Android 12+).
- `Type.kt` — real bundled variable font "Roboto Flex" (`res/font/roboto_flex.ttf`); weights are dialed via `FontVariation.weight()`, not separate font files. Also defines `DisplayLargeMobile` (72sp, light weight) as a **non-standard M3 slot** — per spec, this is what current-temperature displays should use on mobile instead of `typography.displayLarge` (96sp).
- `Shape.kt` — `Shapes` 5-tier scale: extraSmall 8dp, small 16dp, medium 24dp, large 32dp, extraLarge 48dp. For "rounded-xl" cards (24-32px per spec), `shapes.large` (32dp) is the right pick, not `extraLarge`.

Known gap: `app/build.gradle.kts` only depends on `material-icons-core`, not `material-icons-extended` — there's no weather-style vector iconography (sun/cloud/etc.) available yet. `util/WeatherCodeMapper.kt` maps Open-Meteo WMO codes to description text + an emoji as the only current icon stand-in. If reference designs call for vector weather icons, this is a real gap to raise with the user, not something to silently paper over — see [[feedback-ask-before-big-changes]].

**Confirmed decision (do not re-ask):** the user explicitly declined adding `material-icons-extended` as a dependency (asked when building `MetricCard.kt`'s Humidity/Wind/UV Index/Pressure tiles, where emoji is a weaker fit than it was for weather codes — no emoji reads as "atmospheric pressure"). Stick with emoji glyphs for icon needs project-wide unless the user raises it again; picked `🌀` (cyclone) as pressure's fallback, a common informal convention in weather-widget UIs, and flagged it in-code as a weak match.

Second design-system pattern beyond `HomeCard`'s "Main Forecast Card": `DESIGN..txt` also calls out **"Grid Cards"** — square-ish metric tiles (UV Index, Visibility, etc.), implemented as `ui/elements/MetricCard.kt`. Its background/shape choice: `colorScheme.surfaceContainer` + `shapes.medium` (24dp) in both themes — chosen by eyeballing reference PNGs against `Color.kt` (matched almost exactly) rather than the elevation-spec prose, since light/dark spec text disagreed with each other and with the screenshots on which surface token to use (see the "Elevation & Depth" sections of `DESIGN..txt` vs `DESIGNDARK.txt` — light says base `surface`, dark says `surface-container`; the actual screenshots matched `surfaceContainer` in both cases).

No `Brush`/gradient usage existed anywhere in the codebase before `ui/elements/HomeCard.kt` introduced the first one (vertical gradient built from `colorScheme` roles, switched via `isSystemInDarkTheme()` rather than hardcoded hex, since the light and dark gradients use different token pairs/order — not a simple light/dark swap of the same two roles).

`ui/elements/` convention: keep composables lean like `BaseScreen.kt`/`LoadingScreen.kt` — plain `@Composable fun`, minimal ceremony, no heavy KDoc.

Third `ui/elements/` pattern beyond Main Forecast Card (`HomeCard`) and Grid Card (`MetricCard`): the horizontally scrollable row card, e.g. `HourlyForecastCard.kt` — header `Text` (titleMedium) above a `LazyRow` of per-item `Column`s (time label / emoji / value), background `surfaceContainer` + `shapes.medium`, same as `MetricCard`. Each row item takes a small local presentation data class (not the domain model), matching `MetricCard`'s decoupling — left a one-line comment pointing at the future domain→presentation mapper rather than building it.

Accessibility technique used there for a row item whose visual state (an emoji) doesn't have adjacent visible text spelling it out: put an explicit `contentDescription = "..."` INSIDE the same `Modifier.semantics(mergeDescendants = true) {}` block on the item's root, spelling out everything (e.g. "Now, partly cloudy, 24 degrees") — this overrides/replaces the merged child text rather than supplementing it, so it's the right tool specifically when visible text alone wouldn't say enough (contrast with `MetricCard`, where visible text already says enough and bare `semantics(mergeDescendants = true) {}` with no override is sufficient). The emoji itself still gets `clearAndSetSemantics {}` since the parent's explicit description already covers it.

For a row of items where one entry (e.g. "now"/"current") needs visually emphasized styling vs the rest: chose index-based (`index == 0` in the list) rather than adding an extra boolean field to the presentation data class, since the task spec explicitly fixed that data class's fields. Documented as a design choice in a KDoc comment. If a future card needs the "current" item to NOT always be first, this'll need revisiting to an explicit flag.
