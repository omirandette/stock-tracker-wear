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
- `:phone` (`phone/`) — android-application, `applicationId = com.stocktracker.phone`, package `com.stocktracker.phone.*`. Coexists with the watch install.
  - `ui/` — Material 3 Compose screens + ViewModels + theme. List-detail layout via `NavigableListDetailPaneScaffold` (material3-adaptive-navigation), fold-aware automatically.
  - `sync/` — `WatchlistPublisher` observes `repository.watchAll()` and pushes symbol changes to the watch via `WatchlistTransport` (real impl: `DataLayerWatchlistTransport` → `DataClient.putDataItem('/watchlist/symbols')`).
  - `MainPhoneActivity.kt`, `StockPhoneApp.kt` — launcher + Application; DI via shared `RepositoryFactory`, sync wired in `onCreate`.
  - `testutil/MainDispatcherRule.kt` — phone-local copy, same reason as watch.
- `gradle/libs.versions.toml` — dependency version catalog

## Code Style
- Kotlin conventions, Compose for Wear OS on `:watch`
- JDK 17, Gradle with version catalogs
- Keep Compose screens stateless; state lives in ViewModel
- Shared UI primitives in `:shared/ui` must use `androidx.compose.foundation` only — no Material library dependency. Each platform-specific module wraps them in its own Material container and passes TextStyle params.

## Shell Commands
- Do not use arbitrary sleep values — default to `sleep 10` unless a longer wait is justified
- Do not use arbitrary tail/head values — default to `tail -20` / `head -20` unless more lines are needed

## Effort Level (`/effort`)

User default is `xhigh` (set in `~/.claude/settings.json`). Switch with `/effort high` or `/effort xhigh` at the start of a task — don't toggle mid-task.

**Use `xhigh` for:**
- Architecture / module-split decisions, DI choices, data-flow design
- Debugging anything where the cause isn't obvious (race conditions, Compose recomposition, Room invalidation, lifecycle issues)
- Planning multi-PR work (the `/plan` flow, especially Phase 2 Plan agents)
- Code review of subtle changes — security, concurrency, state mutation paths
- First pass on any PR that touches the sync bridge between watch and phone

**Use `high` for:**
- Mechanical refactors (renames, package moves, imports) where correctness is easy to verify
- Docs-only PRs (README, CLAUDE.md, workflow YAML)
- Config bumps (dependency versions, gradle property tweaks, AVD setup)
- Writing tests for code that's already designed and implemented
- Small UI polish PRs after an architectural one has already landed (e.g., PR #43's follow-up back-button commit)

**Rules of thumb:**
- If the task is "one file, one obvious change," `high` is enough.
- If the task requires tracing behaviour across modules or coordinating multiple concurrent things, start in `xhigh`.
- Don't use `xhigh` as a default just to be safe — `high` is the sensible floor, `xhigh` is the deliberate upgrade for reasoning-heavy work.
- If you start a task in `high` and hit a wall, switch to `xhigh` explicitly (`/effort xhigh`) and note the upgrade. Don't silently spin at `high` on a problem that isn't matching its complexity.

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
- **Debug with a failing unit/Roborazzi test FIRST.** When the user reports a bug, reproduce it in a JVM test (unit test, Robolectric, or Roborazzi snapshot) before doing any `installDebug` + `adb shell input` + `screencap` work on an emulator. Emulator round-trips burn context and credits; one Roborazzi PNG captures the same rendering state in seconds on the JVM. Escalate to an emulator only when the bug provably cannot be reproduced off-device (WindowSizeClass, IME, GMS), and even then write a specific instrumented test — don't iterate manual taps. Never loop `install → tap → screencap → read`.
- Before merging any PR, all three of these must be true:
  1. `build` check = SUCCESS
  2. `claude-review` check = SUCCESS (infrastructure failures like Anthropic rate limits are NOT an exception — wait for reset and retrigger; never `--admin` past a failed `claude-review`)
  3. The review body and any comments have been read via `gh pr view <num> --json reviews,comments,statusCheckRollup` and substantive feedback addressed. Silent green (check SUCCESS + empty review body + no comments) is the only "merge immediately" case.
- Each PR and commit should have a single intent/purpose
- Keep PRs under 200 lines of code; 400 lines max in exceptional cases (mechanical module splits may exceed this)
- Keep `README.md` up to date — if a PR changes features, commands, setup, or project structure, update the README in the same PR
