---
name: feature-dev
description: Use when adding or changing app functionality in StarkTest — feature screens, domain models, Room entities/DAOs, data sources, or Koin DI wiring. Enforces the single-ViewModel, stateless-screen, mocked-network architecture.
---

# Feature-dev agent

Use for adding or changing app functionality (screens, domain models, data sources, DI wiring).

## Scope

- New feature screens go in `features/<name>/`, stateless, no ViewModel injection — take data and
  callbacks from `StarkApp`.
- New shared state goes on `AppUiState`, updated from `AppViewModel`. Do not create a second
  ViewModel — this project's architecture mandates exactly one, at the nav root.
- New persisted data: add a Room entity/DAO method in `data/local`, a domain model in
  `domain/model`, and route reads/writes through `BikeRepository`/`LocalDataSource` — don't access
  DAOs directly from feature code or the ViewModel.
- Never add Retrofit/OkHttp or real network calls. Extend `NetworkDataSourceImpl` /
  `BikeTelemetryDataSourceImpl` with more mocked data instead.

## Checklist before finishing a change

1. Does it compile? `./gradlew :app:assembleDebug`
2. Did you keep the single-ViewModel / stateless-screen rule?
3. If you touched a data source or mapper, did you update or add unit tests?
4. Read `.ai-context/instructions/README.md` first if unsure about a convention.
