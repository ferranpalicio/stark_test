---
name: testing
description: Use when writing or fixing unit tests under app/src/test — mappers, data sources, repository, AppViewModel. Knows the MockK + kotlinx-coroutines-test + Turbine conventions this repo uses. UI/instrumented tests are out of scope.
---

# Testing agent

Use for writing or fixing unit tests under `app/src/test`. UI/instrumented tests are out of scope.

## Conventions used in this repo

- MockK for mocking (`mockk()`, `coEvery`, `coVerify`), not Mockito.
- `kotlinx-coroutines-test` `runTest` for suspend functions; `StandardTestDispatcher` +
  `Dispatchers.setMain`/`resetMain` for anything touching `viewModelScope`.
- Turbine (`.test { awaitItem() ... }`) for asserting on `Flow`s, especially
  `BikeRepositoryImpl.observeLiveTelemetry` and `BikeTelemetryDataSourceImpl.observeTelemetry`.
- Data source tests mock DAOs/`Context`/`AssetManager` directly rather than spinning up an
  in-memory Room database or Robolectric — keeps tests fast and dependency-light.
- Plain JUnit `Assert` methods (`assertEquals`, `assertTrue`) — no Truth/AssertJ dependency is
  declared, don't add one for a single test file.

## What to cover for new code

- Every mapper function (DTO → domain, entity → domain) — happy path plus any fallback/unknown-enum
  branch.
- Every `BikeRepository`/`LocalDataSource`/`NetworkDataSource` method — cache-hit and cache-miss
  paths where relevant.
- Every public `AppViewModel` function — resulting `AppUiState` after `advanceUntilIdle()`.

## Running

`./gradlew :app:testDebugUnitTest` — check `app/build/test-results/**/*.xml` for pass/fail counts
if the console output is truncated.
