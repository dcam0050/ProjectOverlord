#!/bin/bash

# Project Overlord - Build, Install, and Launch Script
# Usage: ./build_and_run.sh [--release]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PACKAGE_NAME="com.productions666.overlord"
MAIN_ACTIVITY=".presentation.MainActivity"
BUILD_TYPE="debug"

# Parse arguments
if [ "$1" == "--release" ]; then
    BUILD_TYPE="release"
fi

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}  Project Overlord - Build & Run${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Check if device is connected
echo -e "${YELLOW}▸ Checking for connected devices...${NC}"
DEVICE_COUNT=$(adb devices | grep -c "device$" || true)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}✗ No Android device connected!${NC}"
    echo -e "  Connect a device via USB or start an emulator."
    exit 1
fi

echo -e "${GREEN}✓ Found $DEVICE_COUNT device(s)${NC}"
echo ""

# Build the app
echo -e "${YELLOW}▸ Building $BUILD_TYPE APK...${NC}"
if [ "$BUILD_TYPE" == "release" ]; then
    ./gradlew assembleRelease --no-daemon
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
else
    ./gradlew assembleDebug --no-daemon
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}✗ Build failed - APK not found at $APK_PATH${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Build successful${NC}"
echo ""

# Install the app
echo -e "${YELLOW}▸ Installing APK...${NC}"
adb install -r "$APK_PATH"
echo -e "${GREEN}✓ Installation successful${NC}"
echo ""

# Launch the app
echo -e "${YELLOW}▸ Launching app...${NC}"
adb shell am start -n "$PACKAGE_NAME/$PACKAGE_NAME$MAIN_ACTIVITY"
echo -e "${GREEN}✓ App launched${NC}"
echo ""

# Show logcat option
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  ✓ Done!${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "To view logs, run:"
echo -e "  ${YELLOW}adb logcat -s Overlord:* AlarmScheduler:* AlarmService:*${NC}"
echo ""

