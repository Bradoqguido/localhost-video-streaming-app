# RTSP Streamer — Requirements

## Product Vision
Turn any Android or iOS phone into a professional RTSP camera source for live production.

## User Stories

### US-01: Start Camera Stream
**As a** video producer,
**I want to** start an RTSP stream from my phone camera,
**So that** I can use my phone as a wireless camera in vMix/OBS.

**Acceptance Criteria:**
- App displays live camera preview on launch
- Tapping "GO LIVE" starts RTSP server on configured port
- RTSP URL is displayed prominently on screen
- Stream is visible in VLC, OBS, or vMix within 5 seconds

### US-02: Copy Stream URL
**As a** user,
**I want to** copy the RTSP URL to clipboard with one tap,
**So that** I can quickly paste it into my production software.

**Acceptance Criteria:**
- Tapping the URL copies it to system clipboard
- Toast/snackbar confirms the copy action

### US-03: Switch Camera
**As a** user,
**I want to** toggle between front and back camera,
**So that** I can choose the best angle.

**Acceptance Criteria:**
- Camera switches without interrupting the stream
- Toggle button reflects current camera state

### US-04: Mute Microphone
**As a** user,
**I want to** mute/unmute the microphone,
**So that** I can control audio independently.

**Acceptance Criteria:**
- Mute icon changes state visually
- Audio stops/resumes in the stream without video interruption

### US-05: Configure Stream Settings
**As a** technical user,
**I want to** configure resolution, frame rate, bitrate, and codec,
**So that** I can optimize for my network and quality needs.

**Acceptance Criteria:**
- Settings accessible via gear icon
- Resolution: 480p, 720p, 1080p
- Frame rate: 24, 25, 30, 60 fps
- Bitrate: configurable in Mbps
- Codec: H.264 or H.265
- Changes apply on save

### US-06: Background Streaming (Android)
**As a** user,
**I want** streaming to continue when the app is in the background,
**So that** I can use other apps while streaming.

**Acceptance Criteria:**
- Foreground service notification shows stream URL
- Notification has a "Stop" action
- Stream continues uninterrupted when app is backgrounded

## Non-Functional Requirements

- **Latency:** < 300ms end-to-end on local network
- **Stability:** No crashes during 1+ hour continuous streaming at 1080p30
- **Battery:** < 15% battery drain per hour at 720p30
- **Compatibility:** Android 8.0+ (API 26), iOS 15+
