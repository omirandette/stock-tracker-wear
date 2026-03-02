# Stock Tracker for Wear OS

A Wear OS app to track stock prices on your wrist. Manage a watchlist, view interactive price charts across multiple time periods, and search for new stocks to follow.

## Features

- **Watchlist** — view current price, change, and last-updated timestamp for each stock
- **Stock search** — real-time search powered by Yahoo Finance; tap a result to add it
- **Price charts** — swipe or use the rotary crown to browse 9 time periods (1D, 5D, 1M, 3M, 6M, 12M, 5Y, YTD, MAX)
- **Auto-refresh** — stale data is refreshed automatically in the background
- **Long-press to remove** — long-press a stock card to delete it from your watchlist
- **Cloud backup** — watchlist is backed up via Android Auto Backup and restored on reinstall (Wear OS 4+)
- **Tile** — swipe from the watch face to glance at your top 3 stocks without opening the app; tap to launch

## Setup

No API key is required — the app fetches data directly from Yahoo Finance.

```
./gradlew build
```

Requires JDK 17 and the Android SDK (API 34).

## Build & Test

```bash
./gradlew build                          # compile + lint
./gradlew test                           # unit tests (CI)
./gradlew connectedDebugAndroidTest      # Compose UI + Room tests (emulator)
./gradlew :app:recordRoborazziDebug      # record snapshot golden images
./gradlew :app:verifyRoborazziDebug      # verify snapshots against golden images
```

## Architecture

- **Kotlin + Jetpack Compose** for Wear OS
- **Room** for local watchlist persistence
- **Retrofit** for Yahoo Finance API calls
- **Manual DI** via `StockApp` application class
- Stateless Compose screens; state lives in ViewModels

## Project Structure

```
app/src/main/java/com/stocktracker/
├── data/api/          # StockDataSource interface, Yahoo Finance DTOs
├── data/local/        # Room entity, DAO, database
├── data/repository/   # StockRepository (API + Room)
├── model/             # UI data classes (Stock, ChartPoint, TimePeriod, etc.)
├── presentation/      # Compose screens, ViewModels, PriceChart, theme
├── tile/              # Wear OS Tile service
├── MainActivity.kt    # Single activity entry point
└── StockApp.kt        # Application class (manual DI)
```
