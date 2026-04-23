# Stock Tracker for Wear OS

A Wear OS app to track stock prices on your wrist. Manage a watchlist, view interactive price charts across multiple time periods, and search for new stocks to follow.

## Features

- **Watchlist** — view current price, change, and last-updated timestamp for each stock
- **Stock search** — real-time search powered by Yahoo Finance; tap a result to add it
- **Price charts** — swipe or use the rotary crown to browse 9 time periods (1D, 5D, 1M, 3M, 6M, 12M, 5Y, YTD, MAX)
- **Auto-refresh** — stale data is refreshed automatically in the background
- **Long-press to remove** — long-press a stock card to delete it from your watchlist
- **Cloud backup** — watchlist is backed up via Android Auto Backup and restored on reinstall (Wear OS 4+)
- **Tile** — swipe from the watch face to glance at your top 5 stocks without opening the app; tap to launch

## Setup

No API key is required — the app fetches data directly from Yahoo Finance.

```
./gradlew build
```

Requires JDK 17 and the Android SDK (API 34).

## Build & Test

```bash
./gradlew build                                                  # compile + lint (all modules)
./gradlew test                                                   # unit tests across :shared and :watch (CI)
ANDROID_SERIAL=emulator-5554 ./gradlew :watch:connectedDebugAndroidTest  # Compose UI + Room tests (emulator)
./gradlew :shared:recordRoborazziDebug                           # record snapshot golden images
./gradlew :shared:verifyRoborazziDebug                           # verify snapshots against golden images
./gradlew :watch:installDebug                                    # install watch app (requires adb device)
```

## Architecture

- **Kotlin + Jetpack Compose** — Wear Compose for the watch UI, platform-neutral `foundation` composables shared in `:shared/ui`
- **Room** for local watchlist persistence
- **Retrofit** for Yahoo Finance API calls
- **Manual DI** via `RepositoryFactory` in `:shared/di`, invoked from each app module's `Application` class
- Stateless Compose screens; state lives in ViewModels

## Project Structure

Multi-module Gradle project: `:shared` (android-library) + `:watch` (android-application) + `:phone` (android-application). Watch and phone apps coexist — different `applicationId`s (`com.stocktracker` vs `com.stocktracker.phone`).

```
shared/src/main/java/com/stocktracker/shared/
├── data/api/         # StockDataSource interface, Yahoo Finance DTOs
├── data/local/       # Room entity, DAO, database
├── data/repository/  # StockRepository (API + Room)
├── model/            # Pure data classes (Stock, ChartPoint, TimePeriod, SearchResult)
├── ui/               # Platform-neutral Compose: PriceChart, StockRowContent, StockColors, Formatters
└── di/               # RepositoryFactory

watch/src/main/java/com/stocktracker/watch/
├── presentation/     # Wear Compose screens, ViewModels, theme
├── tile/             # Wear OS Tile service
├── MainActivity.kt   # Launcher
└── StockApp.kt       # Application class (calls RepositoryFactory)

phone/src/main/java/com/stocktracker/phone/
├── ui/                    # Material 3 Compose screens, ViewModels, theme, NavigableListDetailPaneScaffold
├── sync/                  # WatchlistPublisher + Wearable Data Layer transport (phone → watch sync)
├── MainPhoneActivity.kt
└── StockPhoneApp.kt       # Application class (calls RepositoryFactory + starts WatchlistPublisher)
```

The phone app is fold-aware: folded (compact width) shows a single pane that stack-navigates to detail on tap; unfolded (expanded width) shows list and detail side-by-side. Built with `androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold`.

### Watch ↔ phone sync

The phone is the sole editor of the watchlist (add/remove/search). The watch observes changes over the Wearable Data Layer and keeps a local Room mirror in sync:

- Phone: `WatchlistPublisher` debounces `repository.watchAll()` emissions and writes the symbol list to `DataClient` at path `/watchlist/symbols`.
- Watch: `WatchDataLayerListener` (a `WearableListenerService`) receives the update, diffs against its Room DB, inserts placeholder rows for new symbols (the existing auto-refresh loop backfills prices), and deletes removed ones.
- Only symbols cross the link — prices remain device-local and refresh from Yahoo Finance independently on each side.
- Auto Backup continues to handle cross-device restore on reinstall as a fallback.
