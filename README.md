# Overlord - Transit Alarm App

An Android app that ensures users don't miss their public-transport departure by using Google transit routing and a multi-stage alarm system.

## Features

- **Transit Planning**: Plan routes using Google Routes/Directions API
- **Multi-Stage Alarms**: Wake up → Get out of bed → Leave the building
- **Intrusive Alarm UX**: Full-screen, sound, vibration, optional Spotify integration
- **Reliable Scheduling**: Uses AlarmManager with boot recovery

## Architecture

- **Presentation Layer**: Jetpack Compose UI
- **Domain Layer**: Use cases and business logic
- **Data Layer**: Repositories for Google APIs, Spotify, and local Room database
- **Alarm Engine**: Native Android AlarmManager with foreground service

## Setup

1. **Copy API keys configuration**:
   ```bash
   cp local.properties.example local.properties
   ```

2. **Add your API keys to `local.properties`**:
   - `GOOGLE_MAPS_API_KEY`: Your Google Maps API key (with Routes and Places APIs enabled)
   - `SPOTIFY_CLIENT_ID`: Your Spotify app client ID
   - `SPOTIFY_REDIRECT_URI`: Your Spotify redirect URI (default: `com.666Productions.Overlord://callback`)

3. **Build and run**:
   ```bash
   ./gradlew assembleDebug
   ```

## Project Structure

```
app/src/main/java/com/productions666/overlord/
├── alarm/              # Core alarm engine
│   ├── AlarmScheduler.kt
│   ├── AlarmBroadcastReceiver.kt
│   ├── AlarmService.kt
│   ├── AlarmActivity.kt
│   └── BootCompletedReceiver.kt
├── data/
│   ├── model/          # Data models
│   ├── database/        # Room database, entities, DAOs
│   └── repository/     # Data repositories (TODO)
├── domain/
│   ├── model/          # Domain models
│   └── usecase/        # Business logic use cases
└── presentation/       # Compose UI screens
    ├── screen/
    └── theme/
```

## Current Status

✅ **Completed**:
- Project structure and Gradle setup
- Data models and Room database
- Core alarm engine (AlarmManager, BroadcastReceiver, Service, Activity)
- Boot recovery for alarms
- Basic UI structure

🚧 **In Progress**:
- Domain layer repositories
- Google Maps API integration
- Spotify integration
- Full UI screens

## Permissions

The app requires:
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: For finding nearby transit stops
- `POST_NOTIFICATIONS`: For alarm notifications
- `SCHEDULE_EXACT_ALARM`: For precise alarm scheduling
- `WAKE_LOCK`: To wake device for alarms
- `VIBRATE`: For alarm vibration
- `FOREGROUND_SERVICE`: For alarm playback service

## Notes

- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)
- Package: `com.productions666.overlord`

# ProjectOverlord
