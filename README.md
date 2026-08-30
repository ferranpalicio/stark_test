# StarkTest

Android app implementing the Stark mobile assessment: bike telemetry, bike/user data, and a
riding-simulation toggle, built as a single-`Activity` Compose app.

## Architecture

- **Single Activity + Jetpack Compose**, Material 3 + `material3.adaptive`. The app is
  compact-width-only: portrait shows a bottom `NavigationBar`, landscape (compact height) swaps to
  a `NavigationRail`. No multi-pane/list-detail layouts — a phone-sized, single-pane app has no use
  for them, so the `adaptive-navigation3` dependency is present (per the brief's "whatever
  adaptive-integration deps are needed") but not exercised in a `ListDetailScene`-style layout.
- **Navigation 3** (`androidx.navigation3`) drives the four top-level destinations
  (`AppDestination`), backed by a simple in-memory `NavBackStack`.
- **MVVM with exactly one root ViewModel.** `AppViewModel` is injected once, in `StarkApp` (the nav
  root). Every feature screen (`features/bikelive`, `bikedata`, `sessions`, `userdata`, `settings`) is a
  stateless composable — plain data in, lambda callbacks out. This was a hard requirement, not a
  preference: it keeps state in one place and makes every screen trivially testable/previewable
  without DI.
- **Koin** for DI (`di/DataModule.kt`, `di/AppModule.kt`), bootstrapped from `StarkTestApp`.
- **DataStore for single-valued state, Room for lists.** The app only ever tracks one bike and one
  rider, so `user`, `bike`, `batterySummary`, `rideSettings` and `diagnostics` live in a typed
  `DataStore<StarkPreferences>` (JSON via kotlinx-serialization) rather than in single-row tables —
  "zero or one value" is what a nullable field expresses natively, with no schema or migration.
  Room keeps the one genuinely multi-row dataset: `session`, one row per completed ride.
- **A ride in progress isn't stored at all.** It *is* the telemetry `Flow`, started once when the
  riding toggle flips on; its running totals live in `AppUiState` and reach Room exactly once, when
  the toggle flips back off. That single transition is the whole persistence rule — no lifecycle
  hooks, no half-written state to reconcile at startup, no second copy of the session to keep in
  sync. The cost is that a ride is lost if the process dies while it's running, which is an
  acceptable trade for a simulated ride.
- **No networking library.** Retrofit/OkHttp are deliberately absent — `NetworkDataSourceImpl`
  returns a hardcoded `User` after a simulated delay, and `BikeTelemetryDataSourceImpl` derives
  incrementally-changing telemetry from a bundled `assets/bike_telemetry.json` template, emitting
  one snapshot every 15 seconds via `Flow`. Every ride starts from zero — since backgrounding ends
  the ride, there is nothing to resume.
- **Kotlin explicit backing fields** (`AppViewModel.uiState`) instead of the usual
  `_uiState`/`uiState` pair. This is an unstable, opt-in language feature
  (`-XXLanguage:+ExplicitBackingFields`) — it was mandated for this assessment, but be aware it
  carries no compiler stability guarantee across Kotlin versions.

## Trade-offs

- Explicit backing fields required decompiling the Kotlin compiler jar (`javap` on
  `kotlin-compiler-embeddable`) to find the correct opt-in flag, since it isn't a dedicated CLI
  switch and isn't well documented at this Kotlin version. Given its "unsafe internal compiler
  argument" warning, this would not be a default choice for production code outside the scope of
  this assessment's requirements.
- A single root ViewModel means `AppUiState` aggregates unrelated concerns (user, bike overview,
  live telemetry, riding toggle) in one object. Fine at this app's size; would need splitting
  (e.g. per-feature state slices combined at the root) if the app grew much further.
- Diagnostics fault codes/warnings are nested lists inside the DataStore JSON rather than
  normalized tables — simpler for this app's scale, at the cost of not being queryable in SQL.
- Splitting storage across two mechanisms is a judgement call: DataStore reads the whole file per
  access, which is right for a handful of small values and wrong for session history, so each
  dataset went where it fits. The cost is two things to reason about instead of one.

## Testing

Unit tests only (`app/src/test`), covering mappers, data sources, the repository, and
`AppViewModel`. UI/instrumented testing is out of scope per the assessment brief.

```
./gradlew :app:assembleDebug      # build
./gradlew :app:testDebugUnitTest  # unit tests
```

## Agentic context

`.ai-context/instructions/README.md` has the conventions an AI coding agent needs to know before
touching this repo (architecture rules, persistence semantics, testing conventions).
`.ai-context/agents/` has task-specific guidance for feature work and testing. `.github/copilot-instructions.md`
and `.claude/CLAUDE.md` are symlinks to the same instructions file, so all three tools read one
source of truth.
