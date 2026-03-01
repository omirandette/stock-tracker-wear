# CLAUDE.md

## Build & Test
- `./gradlew build` — compile + lint
- `./gradlew test` — run unit tests
- API key: set `alphaVantageKey` in `gradle.properties` (defaults to "demo")

## Project Structure
- `app/src/main/java/com/stocktracker/` — source root
  - `data/api/` — Retrofit interface, API DTOs
  - `data/local/` — Room entity, DAO, database
  - `data/repository/` — StockRepository (API + Room)
  - `model/` — UI data classes
  - `presentation/` — Compose screens, ViewModel, theme
  - `MainActivity.kt` — single activity entry point
  - `StockApp.kt` — Application class (manual DI)
- `gradle/libs.versions.toml` — dependency version catalog

## Code Style
- Kotlin conventions, Compose for Wear OS
- JDK 17, Gradle with version catalogs
- Keep Compose screens stateless; state lives in ViewModel

## Git Workflow
- Trunk-based development: short-lived feature branches, squash-merge to `main`
- Branch protection on `main`: PRs required, CI must pass
- Do not commit unless explicitly asked
- Always run tests before creating a PR
- Keep PRs under 200 lines of code; 400 lines max in exceptional cases
