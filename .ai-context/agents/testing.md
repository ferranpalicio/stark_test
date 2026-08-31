---
name: testing
description: Use when writing or fixing JVM unit tests under app/src/test — mappers, data sources, repository, ViewModels. Knows the MockK + kotlinx-coroutines-test + Turbine conventions this repo uses. Compose UI tests are the `ui-testing` agent's job.
---

# Testing agent

Use for writing or fixing JVM unit tests under `app/src/test`. For Compose UI tests under
`app/src/androidTest` use the `ui-testing` agent instead.

## Conventions used in this repo

- MockK for mocking (`mockk()`, `coEvery`, `coVerify`), not Mockito.
- `kotlinx-coroutines-test` `runTest` for suspend functions; `StandardTestDispatcher` +
  `Dispatchers.setMain`/`resetMain` for anything touching `viewModelScope`.
- Turbine (`.test { awaitItem() ... }`) for asserting on `Flow`s, especially
  `BikeRepositoryImpl.observeLiveTelemetry` and `BikeTelemetryDataSourceImpl.observeTelemetry`.
- Data source tests mock DAOs/`Context`/`AssetManager` directly rather than spinning up an
  in-memory Room database or Robolectric — keeps tests fast and dependency-light. DataStore is the
  exception: use a real `DataStoreFactory.create` over a JUnit `TemporaryFolder` (see
  `LocalDataSourceImplTest`) — the JSON round trip is the behaviour under test, and it runs on the
  plain JVM.
- Plain JUnit `Assert` methods (`assertEquals`, `assertTrue`) — no Truth/AssertJ dependency is
  declared, don't add one for a single test file.

## What to cover for new code

- Every mapper function (DTO → domain, entity → domain) — happy path plus any fallback/unknown-enum
  branch.
- Every `BikeRepository`/`LocalDataSource`/`NetworkDataSource` method — cache-hit and cache-miss
  paths where relevant.
- Every public ViewModel function — the resulting UI-state object after `advanceUntilIdle()`.
  `AppViewModel` owns cross-cutting state; per-destination ViewModels (e.g. `SessionsViewModel`)
  get their own test class next to the feature package.

## Running

`./gradlew :app:testDebugUnitTest` — check `app/build/test-results/**/*.xml` for pass/fail counts
if the console output is truncated.
