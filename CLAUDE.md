# CLAUDE.md

## Build & Test
- `./gradlew build` — compile + lint (all modules)
- `./gradlew test` — run unit tests across `:shared` and `:watch` (CI)
- `ANDROID_SERIAL=emulator-5554 ./gradlew :watch:connectedDebugAndroidTest` — Compose UI + Room tests (emulator only, not physical watch, not in CI)
- `./gradlew :shared:recordRoborazziDebug` — record PriceChart snapshot golden images
- `./gradlew :shared:verifyRoborazziDebug` — verify snapshots against golden images (CI)

## Project Structure

Multi-module Gradle project:

- `:shared` (`shared/`) — android-library, reused by every app module
  - `data/api/` — Retrofit interface, API DTOs
  - `data/local/` — Room entity, DAO, database
  - `data/repository/` — `StockRepository` (API + Room)
  - `model/` — Pure data classes (Stock, ChartPoint, TimePeriod, SearchResult)
  - `ui/` — Platform-neutral Compose primitives: `PriceChart`, `StockRowContent`, `StockColors`, `Formatters`. Uses `androidx.compose.foundation` only — no Material.
  - `di/RepositoryFactory.kt` — Single DI entry point called by every app `Application` class
  - `testutil/` (test source set) — `MainDispatcherRule`, `FakeStockDao`, `TestFixtures`, `LiveApiTest` category marker
- `:watch` (`watch/`) — android-application, `applicationId = com.stocktracker`, package `com.stocktracker.watch.*`
  - `presentation/` — Wear Compose screens, ViewModels, theme
  - `tile/` — Wear OS Tile service
  - `MainActivity.kt`, `StockApp.kt` — launcher + Application
  - `testutil/MainDispatcherRule.kt` — watch-local copy (duplicated from `:shared` because AGP testFixtures didn't resolve cleanly)
- `gradle/libs.versions.toml` — dependency version catalog

## Code Style
- Kotlin conventions, Compose for Wear OS on `:watch`
- JDK 17, Gradle with version catalogs
- Keep Compose screens stateless; state lives in ViewModel
- Shared UI primitives in `:shared/ui` must use `androidx.compose.foundation` only — no Material library dependency. Each platform-specific module wraps them in its own Material container and passes TextStyle params.

## Shell Commands
- Do not use arbitrary sleep values — default to `sleep 10` unless a longer wait is justified
- Do not use arbitrary tail/head values — default to `tail -20` / `head -20` unless more lines are needed

## Emulator Setup
- The Wear OS emulator is required for `connectedDebugAndroidTest` — it is part of the standard pre-commit workflow
- If no emulator is installed/running, Claude MUST install and start one — do not ask the user to do it
- `sdkmanager` is at `/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager`
- `avdmanager` is at `/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/avdmanager`
- `adb` is at `/opt/homebrew/share/android-commandlinetools/platform-tools/adb`
- `emulator` lives under `/opt/homebrew/share/android-commandlinetools/emulator/emulator` after installing the `emulator` package
- Install a Wear OS system image (e.g. `sdkmanager "system-images;android-34;android-wear;arm64-v8a"`), create an AVD, then launch with `emulator @<avd-name> -no-snapshot -no-window &` and poll `adb devices` until it shows `emulator-5554 device`

## Testing Conventions
- Unit tests use JUnit 4 + MockK + `kotlinx-coroutines-test`
- `MainDispatcherRule` for ViewModel tests — `:shared` version for shared tests, `:watch` local copy for watch tests
- Backtick test names describing behavior (e.g., `` `loadChart exposes data on success` ``)
- `runTest` + `advanceUntilIdle()` pattern for coroutine tests
- Test fixtures in `shared/src/test/java/com/stocktracker/shared/testutil/TestFixtures.kt` — reuse `chartResponse()`, `quoteResult()`, etc.
- MockWebServer for integration tests (`shared/src/test/.../integration/`)
- Side-effect tests must assert from cold state — don't pre-populate the ViewModel's cached StateFlow, read `repository.watchAll()` directly in the test and `coVerify` the side effect

## Git Workflow
- Trunk-based development: short-lived feature branches, squash-merge to `main`
- Branch protection on `main`: PRs required, CI must pass
- Do not commit unless explicitly asked
- Before creating a commit, run ALL local tests: `./gradlew test`, `./gradlew :shared:verifyRoborazziDebug`, AND `ANDROID_SERIAL=emulator-5554 ./gradlew :watch:connectedDebugAndroidTest`
- Always run tests before creating a PR
- Each PR and commit should have a single intent/purpose
- Keep PRs under 200 lines of code; 400 lines max in exceptional cases (mechanical module splits may exceed this)
- Keep `README.md` up to date — if a PR changes features, commands, setup, or project structure, update the README in the same PR
