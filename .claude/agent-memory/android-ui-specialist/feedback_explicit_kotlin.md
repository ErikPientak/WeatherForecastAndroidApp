---
name: feedback-explicit-kotlin
description: Prefer explicit, readable Kotlin over clever/terse constructs
metadata:
  type: feedback
---

Avoid nested `Pair`s and multi-level destructuring. Prefer explicit named parameters and small data classes even when a terser construct exists.

**Why:** nested `Pair` + multi-level destructuring previously caused real, hard-to-debug compile errors for this user (stated explicitly as project guidance).

**How to apply:** when a composable or function needs to bundle multiple values, reach for a data class or plain named parameters before reaching for `Pair`/`Triple`. Applies to all Kotlin in this project, not just UI code. See [[user-profile]].
