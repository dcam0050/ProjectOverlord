# Spotify App Remote SDK Setup

The Spotify App Remote SDK is not available via Maven repositories and must be downloaded manually.

## Download Instructions

1. Visit the [Spotify Android SDK GitHub Releases](https://github.com/spotify/android-sdk/releases)
2. Download the latest `spotify-app-remote-release-X.X.X.aar` file
3. Place it in this `libs/` directory
4. Rename it to `spotify-app-remote.aar`

## Current Setup

The project is configured to use the AAR file from this directory once downloaded.

## Alternative: Manual Module Import

If you prefer to import it as a module in Android Studio:
1. File → New → New Module
2. Select "Import .JAR/.AAR Package"
3. Browse to the downloaded AAR file
4. Name it `spotify-app-remote`
5. Update `app/build.gradle.kts` to use: `implementation(project(":spotify-app-remote"))`

