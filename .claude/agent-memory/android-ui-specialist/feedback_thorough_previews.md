---
name: feedback-thorough-previews
description: Provide multiple @Preview variants covering edge cases and both themes by default, not just one minimal example per theme
metadata:
  type: feedback
---

Ship several `@Preview` composables per component up front — different data states/edge cases (e.g. varying weather conditions, day/night, negative or large values) crossed with light/dark theme — rather than the bare minimum (one light + one dark with a single happy-path data point).

**Why:** after `HomeCard.kt` shipped with just a light and a dark preview (one data point each), the user's very next message was "add more previews" — a real, if quiet, signal that the default expectation here is broader preview coverage, not minimal coverage.

**How to apply:** for `MetricCard.kt` this was applied proactively from the start: 2 grid previews (light/dark, matching the reference screenshots exactly) plus 4 standalone single-tile previews, without waiting to be asked. Keep doing this for new composables in `ui/elements/` — err toward more preview variants (multiple weather codes, boundary values, both themes) rather than fewer. See [[user-profile]].
