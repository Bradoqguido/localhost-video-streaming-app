#!/bin/bash

# Exit on error
set -e

echo "============================================="
echo "Building and Deploying RTSP Streamer App"
echo "============================================="

# Clean build and compile APK
echo "Step 1: Compiling debug APK..."
./gradlew :composeApp:assembleDebug --no-daemon

# Find the APK
APK_PATH="composeApp/build/outputs/apk/debug/composeApp-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    # Fallback to general output paths if different
    APK_PATH=$(find composeApp/build/outputs/apk/ -name "*.apk" | head -n 1)
fi

if [ -z "$APK_PATH" ]; then
    echo "❌ Error: Could not locate built APK."
    exit 1
fi

echo "✅ APK compiled successfully: $APK_PATH"

# Check if device is connected
echo "Step 2: Checking for connected Android devices..."
DEVICES=$(~/Library/Android/sdk/platform-tools/adb devices | grep -v "List of devices" | grep "device" || true)

if [ -z "$DEVICES" ]; then
    echo "❌ Error: No Android device detected via ADB. Connect your phone and enable USB Debugging."
    exit 1
fi

echo "Found connected device(s):"
echo "$DEVICES"

# Install APK via adb
echo "Step 3: Installing APK to device..."
echo "⚠️ Note: Watch your phone screen and click 'Install' if prompted (especially on Xiaomi/POCO devices)!"
echo "---------------------------------------------"

~/Library/Android/sdk/platform-tools/adb install -r "$APK_PATH"

echo "---------------------------------------------"
echo "✅ App installed successfully! Look for 'RTSP Streamer' on your phone."
echo "============================================="
