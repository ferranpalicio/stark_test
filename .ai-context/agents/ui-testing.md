---
name: ui-testing
description: Use when writing or fixing Compose UI tests under app/src/androidTest — screen-level behaviour, state branches, layout arrangement, semantics. Knows the createAndroidComposeRule + preview-fixture conventions this repo uses. JVM unit tests are the `testing` agent's job.
---

# UI-testing agent

Use for Compose UI tests under `app/src/androidTest`. For JVM unit tests under `app/src/test`
(mappers, data sources, repository, ViewModels) use the `testing` agent instead.

Reference: https://developer.android.com/develop/ui/compose/testing

## Where these tests run

Instrumented, on a device or emulator: `./gradlew :app:connectedDebugAndroidTest`. There is no
Robolectric in this project — don't add it, and don't move UI tests into `app/src/test`. Check a
device is attached with `adb devices` before running; the task fails with
`No connected devices!` otherwise.

## Conventions used in this repo

- Rule is `createAndroidComposeRule<ComponentActivity>()`, **not** `<MainActivity>`. Screens are
  stateless composables, so a test drives them directly with `rule.setContent { StarkTheme { … } }`
  and never boots the app graph, Koin, or a ViewModel. `ComponentActivity` comes from the
  `ui-test-manifest` debug artifact and gives `rule.activity` for resource lookups.
- **Reuse the `@Preview` fixture as the test data.** Each feature package keeps its mock in a
  `previews.kt` top-level `val` (e.g. `features/bikelive/previews.kt` → `bikeTelemetry`), which the
  `androidTest` source set can import directly. Derive variants with `.copy(...)` rather than
  hand-rolling a second fixture — one fixture keeps previews and tests describing the same screen.
- **Never hardcode a string that comes from `strings.xml`.** Build the expectation with the same
  call the composable makes: `rule.activity.getString(R.string.power_ratio, "53.6", "60.0")`. A
  hardcoded literal silently passes when the format string changes.
- **Don't add `testTag`s to production composables just to make a test findable.** Prefer, in
  order: `onNodeWithText`, `onNodeWithContentDescription`, a semantics matcher
  (`hasProgressBarRangeInfo`, `hasScrollAction`, `isSelected`…), then hierarchy selectors
  (`onChildren`, `hasAnyAncestor`). A tag is a last resort for a node with no user-visible
  identity, and needs a comment saying why.
- Assert **layout arrangement** with bounds, not fixed dp. `getUnclippedBoundsInRoot()` on two
  nodes and compare (`a.left > b.right` for side-by-side, `a.top > b.bottom` for stacked).
  `assertTopPositionInRootIsEqualTo(120.dp)` is brittle across densities and screen sizes.
- Force a viewport instead of relying on the device's: wrap the content in
  `DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(800.dp, 400.dp)))`.
  That is how landscape branches are exercised on a portrait emulator, and it makes bounds
  assertions device-independent. `Modifier.size()` will *not* work — it is coerced by the parent's
  constraints; `requiredSize` escapes them but pushes content off screen, breaking
  `assertIsDisplayed()`.
- Lazy lists only compose visible items, so a node further down does not exist yet.
  `onNode(hasScrollAction()).performScrollToNode(hasText("…"))` first, then assert. Plain
  `performScrollTo()` fails on a node that was never composed.
- Plain JUnit `Assert` (`assertTrue`, `assertEquals`) for anything the Compose assertions don't
  cover — no Truth/AssertJ is declared, don't add one.

## What to cover for a screen

1. **Every branch of the screen's `when`** — each `UiState` arm plus any non-state gate. For
   `BikeLiveScreen` that is `!isRiding` / `Loading` / `Error` / `Empty` / `Success`, and
   `!isRiding` must win even when telemetry is `Success`.
2. **Every field the success state renders**, against the preview fixture — value, unit, formatted
   string, and content description.
3. **Conditional sections**, both present and absent, including the exact condition. E.g.
   `BikeLiveScreen` gates the whole warnings block on `warnings.isNotEmpty()`, so fault codes alone
   render nothing — pin that, it is easy to break.
4. **Each layout variant** the screen switches between (`isLandscape`), asserted by arrangement.

## Running

```
adb devices                              # confirm an emulator/device is attached
./gradlew :app:connectedDebugAndroidTest
```

Results land in `app/build/reports/androidTests/connected/` and
`app/build/outputs/androidTest-results/connected/**/*.xml` if the console output is truncated.
