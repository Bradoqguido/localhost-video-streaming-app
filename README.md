# RTSP Streamer

Turn your Android or iOS phone into a professional wireless camera for live production.

Stream camera + microphone via RTSP to **vMix**, **OBS Studio**, **VLC**, or any RTSP-compatible receiver on your local network.

## Features

- 📷 **Camera Streaming** — Front or back camera with one-tap switching
- 🎙️ **Audio** — Microphone capture with mute toggle
- 📡 **RTSP Server** — Built-in server, no external infrastructure needed
- ⚙️ **Configurable** — Resolution (480p–1080p), frame rate (24–60fps), bitrate, H.264/H.265
- 📋 **One-tap URL copy** — Copy RTSP URL to clipboard for easy setup
- 🔋 **Background streaming** — Foreground service keeps stream alive (Android)
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
2. Grant camera + microphone permissions
3. Tap **GO LIVE**
4. Note the RTSP URL shown on screen (e.g., `rtsp://192.168.1.50:8554/live`)

### 3. Connect from your PC

See [Receiver Setup Guide](docs/RECEIVER_SETUP.md) for vMix, OBS, VLC instructions.

## Tech Stack

| Component | Technology |
|---|---|
| Shared UI | Compose Multiplatform |
| Shared Logic | Kotlin Multiplatform |
| Android Streaming | RootEncoder (RtspServerCamera2) |
| iOS Streaming | RootEncoder-iOS (Swift) |
| Video Codec | H.264 / H.265 (hardware-accelerated) |
| Audio Codec | AAC |
| Protocol | RTSP (server mode) |

## Project Structure

```
├── composeApp/src/
│   ├── commonMain/     # Shared UI + domain models
│   ├── androidMain/    # RootEncoder integration
│   └── iosMain/        # RootEncoder-iOS bridge
├── iosApp/             # iOS Xcode project
└── docs/               # Requirements + setup guides
```

## Requirements

- **Android:** API 26+ (Android 8.0 Oreo)
- **iOS:** iOS 15+
- **Build:** JDK 17, Android SDK 35, Xcode 15+

## License

MIT
