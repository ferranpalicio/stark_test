---
name: feature-dev
description: Use when adding or changing app functionality in StarkTest — feature screens, domain models, Room entities/DAOs, data sources, or Koin DI wiring. Enforces the stateless-screen, entry-scoped-ViewModel, mocked-network architecture.
---

# Feature-dev agent

Use for adding or changing app functionality (screens, domain models, data sources, DI wiring).

## Scope

- New feature screens go in `features/<name>/`, stateless — they take plain data and lambda
  callbacks and never hold a `ViewModel` reference.
- State used by more than one destination goes on `AppUiState`, updated from `AppViewModel` at the
  nav root. State owned by a single destination gets its own ViewModel + UI-state class, resolved
  with `koinViewModel()` *inside* that destination's `entry<T> { }` block in `StarkApp`, which
  scopes it to the nav entry. `SessionsViewModel` / `SessionsUiState` is the worked example.
  Register it in `di/AppModule.kt`.
- New persisted data: add a domain model in `domain/model`, then pick storage by cardinality — a
  single value becomes a nullable `@Serializable` DTO field on `StarkPreferences`
  (`data/local/datastore`), a growing list becomes a Room entity + `SessionDao`-style DAO. Route
  reads/writes through `BikeRepository`/`LocalDataSource` — don't touch DataStore or DAOs directly
  from feature code or the ViewModel.
- Never add Retrofit/OkHttp or real network calls. Extend `NetworkDataSourceImpl` /
  `BikeTelemetryDataSourceImpl` with more mocked data instead.

## Checklist before finishing a change

1. Does it compile? `./gradlew :app:assembleDebug`
2. Did you keep the stateless-screen rule, and put new state at the right scope (nav root vs. nav
   entry)?
3. If you touched a data source, mapper, or ViewModel, did you update or add unit tests
   (`testing` agent)? If you touched a screen, does it need a Compose test (`ui-testing` agent)?
4. Read `.ai-context/instructions/README.md` first if unsure about a convention.
