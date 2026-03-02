# CLAUDE.md

## Build & Test
- `./gradlew build` — compile + lint
- `./gradlew test` — run unit tests (CI)
- `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest` — Compose UI + Room tests (emulator only, not physical watch, not in CI)
- `./gradlew :app:recordRoborazziDebug` — record PriceChart snapshot golden images
- `./gradlew :app:verifyRoborazziDebug` — verify snapshots against golden images (CI)
- API key: set `alphaVantageKey` in `gradle.properties` (defaults to "demo")

## Project Structure
- `app/src/main/java/com/stocktracker/` — source root
  - `data/api/` — Retrofit interface, API DTOs
  - `data/local/` — Room entity, DAO, database
  - `data/repository/` — StockRepository (API + Room)
  - `model/` — UI data classes
  - `presentation/` — Compose screens, ViewModel, theme
  - `tile/` — Wear OS Tile service
  - `MainActivity.kt` — single activity entry point
  - `StockApp.kt` — Application class (manual DI)
- `gradle/libs.versions.toml` — dependency version catalog

## Code Style
- Kotlin conventions, Compose for Wear OS
- JDK 17, Gradle with version catalogs
- Keep Compose screens stateless; state lives in ViewModel

## Shell Commands
- Do not use arbitrary sleep values — default to `sleep 10` unless a longer wait is justified
- Do not use arbitrary tail/head values — default to `tail -20` / `head -20` unless more lines are needed

## Testing Conventions
- Unit tests use JUnit 4 + MockK + `kotlinx-coroutines-test`
- `MainDispatcherRule` for ViewModel tests (`testutil/MainDispatcherRule.kt`)
- Backtick test names describing behavior (e.g., `` `loadChart exposes data on success` ``)
- `runTest` + `advanceUntilIdle()` pattern for coroutine tests
- Test fixtures in `testutil/TestFixtures.kt` — reuse `chartResponse()`, `quoteResult()`, etc.
- MockWebServer for integration tests (`integration/` package)

## Git Workflow
- Trunk-based development: short-lived feature branches, squash-merge to `main`
- Branch protection on `main`: PRs required, CI must pass
- Do not commit unless explicitly asked
- Before creating a commit, run ALL local tests: `./gradlew test`, `./gradlew :app:verifyRoborazziDebug`, AND `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest`
- Always run tests before creating a PR
- Each PR and commit should have a single intent/purpose
- Keep PRs under 200 lines of code; 400 lines max in exceptional cases
- Keep `README.md` up to date — if a PR changes features, commands, setup, or project structure, update the README in the same PR
