package com.rtspstreamer.domain.models

/**
 * Represents the current state of the RTSP streaming session.
 */
sealed class StreamState {
  /** No camera preview or stream active. */
  data object Idle : StreamState()

  /** Camera preview is active but not streaming. */
  data object Previewing : StreamState()

  /** Actively streaming via RTSP server. */
  data class Streaming(
    val rtspUrl: String,
    val connectedClients: Int = 0,
    val fps: Float = 0f,
    val uptimeMs: Long = 0L,
  ) : StreamState()

  /** An error occurred. */
  data class Error(val message: String) : StreamState()
}

/**
 * Events emitted by the streamer to the UI.
 */
sealed class StreamEvent {
  data class ClientConnected(val clientIp: String) : StreamEvent()
  data class ClientDisconnected(val clientIp: String) : StreamEvent()
  data class StreamError(val message: String) : StreamEvent()
}
