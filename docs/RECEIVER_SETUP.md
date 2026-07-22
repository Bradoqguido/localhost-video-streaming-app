# Receiver Setup Guide

How to connect to the MJPEG HTTP Streamer app from your production software.

## Prerequisites

1. **Same network**: Your phone and computer must be on the same Wi-Fi/LAN network.
2. **HTTP URL**: Displayed in the app when streaming (e.g., `http://192.168.1.50:8554/`).

---

## vMix

vMix supports MJPEG natively using either the Web Browser input or the IP Video Input:

### Option A: Web Browser Input (Recommended)
1. Click **Add Input** → **Web Browser**
2. In the **URL** field, paste: `http://PHONE_IP:8554/`
3. Click **OK**

### Option B: Video Delay / Stream Input
1. Click **Add Input** → **Stream/SRT**
2. Select **Stream Type**: `IP Camera (RTSP, TS, MJPEG)`
3. In the **URL** field, paste: `http://PHONE_IP:8554/`
4. Set **Stream Format** / **Codec**: Select `MJPEG`
5. Click **OK**

---

## OBS Studio

### Option A: Browser Source (Lowest Latency)
1. Click **Sources** → **+** → **Browser**
2. Name it (e.g., "Phone Camera") and click **OK**
3. In the **URL** field, paste: `http://PHONE_IP:8554/`
4. Set the width and height to match your stream settings (e.g. `1280` x `720`)
5. Click **OK**

### Option B: Media Source
1. Click **Sources** → **+** → **Media Source**
2. Uncheck **"Local File"**
3. In the **Input** field, paste: `http://PHONE_IP:8554/`
4. Set **Input Format** to `mjpeg`
5. Click **OK**

---

## VLC Media Player (Quick Test)

1. Go to **Media** → **Open Network Stream** (Ctrl+N)
2. Paste: `http://PHONE_IP:8554/`
3. Click **Play**

---

## Troubleshooting

| Problem | Solution |
|---|---|
| "Connection refused" / "Unable to connect" | Check that your PC's firewall allows inbound connections on port 8554 (or the port configured in settings) |
| "No route to host" | Verify phone and PC are on the same Wi-Fi network and that AP Isolation (Client Isolation) is disabled on your router |
| Frozen image in VLC / vMix after stop | When stopping the stream, the app shuts down the socket. Some receivers display the last received JPEG frame indefinitely instead of closing the window. Tap **Go Live** again or reload the input to reconnect |
