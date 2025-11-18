# Spotify App Remote SDK Setup Guide

## Quick Setup

1. **Download the SDK**:
   - Visit: https://github.com/spotify/android-sdk/releases
   - Download: `spotify-app-remote-release-0.8.0.aar`
   - Or search for the latest `spotify-app-remote-release-*.aar` file

2. **Place the file**:
   - Copy the downloaded `.aar` file to: `libs/spotify-app-remote.aar`
   - (Rename it to `spotify-app-remote.aar`)

3. **Enable in build.gradle.kts**:
   - Open `app/build.gradle.kts`
   - Find the Spotify SDK section (around line 115)
   - Uncomment: `implementation(files("../libs/spotify-app-remote.aar"))`
   - Remove or comment out the placeholder comment

4. **Sync and build**:
   ```bash
   ./gradlew assembleDebug
   ```

## Alternative: Direct Download Command

If you have the direct download URL, you can use:

```bash
cd libs
wget -O spotify-app-remote.aar "https://github.com/spotify/android-sdk/releases/download/app-remote-release-0.8.0/spotify-app-remote-release-0.8.0.aar"
```

## Verification

After adding the file, verify it exists:
```bash
ls -lh libs/spotify-app-remote.aar
```

The file should be several MB in size (not empty).

## Note

The project will build successfully without the Spotify SDK - it's optional. The alarm engine will use local audio sounds until Spotify integration is fully implemented.

