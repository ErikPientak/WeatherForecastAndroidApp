---
name: feedback-ask-before-big-changes
description: Ask clarifying questions before writing large amounts of code, especially on ambiguous UI/visual work
metadata:
  type: feedback
---

Before implementing a new composable/screen where there are multiple reasonable design choices (icon sourcing, parameter shape, exact color roles, etc.), surface the open questions and a proposed default, then wait for confirmation rather than guessing silently. Once the user confirms/answers, proceed straight to implementation without re-litigating.

**Why:** explicit project instruction — the user is learning through this project and wants to be in the loop on judgment calls that materially change the implementation, but doesn't want to be asked to approve mechanical follow-through once direction is set.

**How to apply:** for new UI components, ask about (a) asset/data gaps not obviously solved by existing code (e.g. no matching icon set), (b) the public API shape (primitives vs. domain models), (c) any visual choice made by eyeballing a reference image against theme tokens rather than reading it directly off a spec. Skip asking for mechanical implementation details. See [[user-profile]].
