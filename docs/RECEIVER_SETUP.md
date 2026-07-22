# Receiver Setup Guide

How to connect to the RTSP Streamer app from your production software.

## Prerequisites

1. **Same network**: Your phone and computer must be on the same WiFi/LAN
2. **RTSP URL**: Displayed in the app when streaming (e.g., `rtsp://192.168.1.50:8554/live`)

---

## vMix

1. Click **Add Input** → **More** → **Stream/SRT**
2. In the **URL** field, paste: `rtsp://PHONE_IP:8554/live`
3. Set **Stream Type** to **RTSP over TCP** for best stability
4. Click **OK**
5. The phone camera should appear as an input within 2-3 seconds

> **Tip:** Enable "Low Latency" in the input settings for real-time production.

---

## OBS Studio

1. Click **Sources** → **+** → **Media Source**
2. Uncheck **"Local File"**
3. In **Input** field, paste: `rtsp://PHONE_IP:8554/live`
4. Set **Input Format** to `rtsp`
5. Check **"Use hardware decoding when available"**
6. Click **OK**

> **For lower latency**, add these to **FFmpeg Options**:
> ```
> rtsp_transport=tcp
> buffer_size=0
> ```

---

## VLC Media Player (Quick Test)

1. **Media** → **Open Network Stream** (Ctrl+N)
2. Paste: `rtsp://PHONE_IP:8554/live`
3. Click **Play**

> **For low latency playback**, go to:
> Tools → Preferences → Input/Codecs → Set "Network caching" to `100` ms

---

## ffplay (CLI Test)

```bash
ffplay -rtsp_transport tcp -fflags nobuffer -flags low_delay rtsp://PHONE_IP:8554/live
```

---

## ffmpeg (Record to File)

```bash
ffmpeg -rtsp_transport tcp -i rtsp://PHONE_IP:8554/live -c copy output.mp4
```

---

## Troubleshooting

| Problem | Solution |
|---|---|
| "Connection refused" | Check firewall allows port 8554 (or custom port) |
| "No route to host" | Verify phone and PC are on same WiFi network |
| Black screen in receiver | Try switching from UDP to TCP transport |
| High latency (>1s) | Reduce "Network caching" / "Buffer" in player settings |
| Audio but no video | Receiver may not support H.265 — switch to H.264 in app settings |
