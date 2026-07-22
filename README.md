# Local Video Streamer (MJPEG HTTP)

Turn your Android or iOS phone into a professional wireless camera for live production.

Stream live camera feed via a built-in MJPEG HTTP server directly to **vMix**, **OBS Studio**, **VLC**, or any compatible MJPEG receiver on your local network.

## Features

- 📷 **Camera Streaming** — High-performance back camera capture at 720p/1080p
- 📡 **Built-in HTTP Server** — Runs directly on the phone; receivers pull the stream using a simple HTTP URL (e.g. `http://192.168.1.50:8554/`)
- ⚙️ **Configurable** — Port, name, and bitrates customizable via settings panel
- 🔄 **Dynamic Orientation Alignment** — Automatically matches device physical rotation (Portrait & Widescreen Landscape) on both platforms
- 📋 **One-tap URL copy** — Copy stream HTTP URL to clipboard for easy configuration
- 🌙 **Dark theme** — Premium dark UI with glassmorphism overlays

## Quick Start

### 1. Build & Install

```bash
# Android
./gradlew :composeApp:installDebug

# iOS (requires Xcode)
open iosApp/iosApp.xcodeproj
```

### 2. Start Streaming

1. Open the app on your phone
2. Grant camera permissions
3. Tap **GO LIVE**
4. Note the HTTP URL shown on screen (e.g., `http://192.168.1.50:8554/`)

### 3. Connect from your PC

- **vMix**: Add Input -> Web Browser -> Paste URL, or Add Input -> Video Delay / Stream -> Select MJPEG -> Paste URL.
- **VLC**: Media -> Open Network Stream -> Paste URL -> Play.
- **OBS**: Add Source -> Media Source (uncheck Local File) or Browser Source -> Paste URL.

## Tech Stack

| Component | Technology |
|---|---|
| Shared UI | Compose Multiplatform |
| Shared Logic | Kotlin Multiplatform |
| Android Streaming | Native Camera2 + ImageReader + ServerSocket |
| iOS Streaming | Native AVFoundation + Network (NWListener) |
| Protocol | MJPEG over HTTP (server pull mode) |

## Project Structure

```
├── composeApp/src/
│   ├── commonMain/     # Shared UI + domain models
│   ├── androidMain/    # Android Camera2 + HTTP Server integration
│   └── iosMain/        # iOS Swift bridge mapping
├── iosApp/             # iOS Swift application & SwiftRtspStreamer bridge
└── docs/               # Requirements + setup guides
```

## Requirements

- **Android:** API 26+ (Android 8.0 Oreo)
- **iOS:** iOS 15+
- **Build:** JDK 17, Android SDK 35, Xcode 15+

## License

MIT
