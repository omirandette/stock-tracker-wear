# Stock Tracker for Wear OS

A simple Wear OS app to track stock prices. Add and remove stocks from your watchlist and see today's price at a glance.

## Setup

This app uses the [Alpha Vantage API](https://www.alphavantage.co/) to fetch stock quotes. You need a free API key to make it work.

1. Get a free API key at https://www.alphavantage.co/support/#api-key
2. Create a `secrets.properties` file in the project root (it's gitignored):
   ```properties
   alphaVantageKey=YOUR_API_KEY_HERE
   ```
3. Build and run:
   ```
   ./gradlew build
   ```

Without a valid key the app defaults to `"demo"`, which only returns sample data for the `IBM` ticker.

## Build & Test

```
./gradlew build   # compile + lint
./gradlew test    # unit tests
```

Requires JDK 17 and the Android SDK (API 34).
