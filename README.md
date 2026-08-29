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
  root). Every feature screen (`features/bikelive`, `bikedata`, `userdata`, `settings`) is a
  stateless composable — plain data in, lambda callbacks out. This was a hard requirement, not a
  preference: it keeps state in one place and makes every screen trivially testable/previewable
  without DI.
- **Koin** for DI (`di/DataModule.kt`, `di/AppModule.kt`), bootstrapped from `StarkTestApp`.
- **Room** for persistence. Bike-related tables (`bike`, `battery_summary`, `ride_settings`,
  `user`, `diagnostics`) are single-row (`id = 0`, `REPLACE` conflict strategy) since the app only
  ever tracks one bike and one rider; `session` is the one multi-row table, one row per ride.
- **No networking library.** Retrofit/OkHttp are deliberately absent — `NetworkDataSourceImpl`
  returns a hardcoded `User` after a simulated delay, and `BikeTelemetryDataSourceImpl` derives
  incrementally-changing telemetry from a bundled `assets/bike_telemetry.json` template, emitting
  one snapshot every 60 seconds via `Flow`. It accepts nullable `initialTimestamp`/`initialSession`
  so the repository can tell a fresh connection from a resumed ride.
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
- Diagnostics fault codes/warnings are stored as JSON text columns in Room rather than normalized
  into their own tables — simpler for this app's scale, at the cost of not being queryable in SQL.

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
