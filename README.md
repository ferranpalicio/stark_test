# StarkTest

Android app implementing the Stark mobile assessment: bike telemetry, bike/user data, and a
riding-simulation toggle, built as a single-`Activity` Compose app.

## Architecture
- **Single Activity + Jetpack Compose**, Material 3 + `material3.adaptive`. The app is
  compact-width-only: portrait shows a bottom `NavigationBar`, landscape (compact height) swaps to
  a `NavigationRail`.
- **Navigation 3** (`androidx.navigation3`) drives the four top-level destinations
  (`AppDestination`), backed by a simple in-memory `NavBackStack`.
- **MVVM with  one root ViewModel.** `AppViewModel` is injected once, in `StarkApp`. 
  Most of feature screen (`features/bikelive`, `bikedata`, `userdata`, `settings`) is a
  stateless composable — plain data in, lambda callbacks out. This is for simplicity reasons, not a
  preference: On a real scenario (and with more time), I'd have split more functionality into more
  viewmodels and states for every feature. 
- **Koin** for DI (`di/DataModule.kt`, `di/AppModule.kt`), bootstrapped from `StarkTestApp`.
- The app only ever tracks one bike and one rider, so `user`, `bike`, `batterySummary`, 
  `rideSettings` and `diagnostics` live in a typed `DataStore<StarkPreferences>`. Sessions are 
  stored in a database (Room).
- **A ride in progress isn't stored at all.** It *is* the telemetry `Flow`, started once when the
  riding toggle flips on; its running totals live in `AppUiState` and reach Room exactly once, when
  the toggle flips back off. That single transition is the whole persistence rule — no lifecycle
  hooks, no half-written state to reconcile at startup, no second copy of the session to keep in
  sync. The cost is that a ride is lost if the process dies while it's running, which is an
  acceptable trade for a simulated ride.
- **No networking library.** Retrofit/OkHttp are deliberately absent — `NetworkDataSourceImpl`
  returns a hardcoded `User` after a simulated delay, and `BikeTelemetryDataSourceImpl` derives
  incrementally-changing telemetry from a bundled `assets/bike_telemetry.json` template, emitting
  one snapshot every 15 seconds via `Flow`.

## Agentic setup
- This is just a small sample of how I set up agentic development. In real, more complex scenarios, 
  I would also use skills for specialized tasks, as well as add more documents, tools, and plugins 
  to provide richer context to the AI.

## Trade-offs
- A single modules (app module) is used for simplicity. Clear separation of concerns is achieved
  through the package structure: **data**, **domain**, **features**. Yes, I know the advantages of
  multi-module (faster builds, team scalability, reusability etc.), and I would apply this on a real
  project, but for this assessment the single-module approach is simpler.
- It's clearly visible which part of the UI has more work. With more time, I'd have dedicated more 
  time to implement more complex (and nice) UI.
- A big root ViewModel means `AppUiState` aggregates unrelated concerns (user, bike overview,
  live telemetry, riding toggle) in one object. Fine at this app's size; would need splitting
  (e.g. per-feature state slices combined at the root) if the app grew much further.
- Should separate DataStore files for each top-level preference (user, bike, batterySummary, 
  rideSettings, diagnostics) to avoid a single large JSON file. This is a trade-off between 
  simplicity and performance: the app's small size means the JSON is tiny, so the single-file 
  approach is simpler and faster than multiple files.

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
