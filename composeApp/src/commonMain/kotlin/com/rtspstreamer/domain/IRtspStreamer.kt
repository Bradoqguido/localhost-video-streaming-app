package com.rtspstreamer.domain

import com.rtspstreamer.domain.models.StreamConfig
import com.rtspstreamer.domain.models.StreamEvent
import com.rtspstreamer.domain.models.StreamState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-agnostic interface for RTSP streaming.
 *
 * Each platform (Android/iOS) provides its own implementation
 * wrapping the respective RootEncoder library.
 */
interface IRtspStreamer {
  /** Current stream state (reactive). */
  val state: StateFlow<StreamState>

  /** One-shot events (client connected, errors, etc.). */
  val events: SharedFlow<StreamEvent>

  /** Current configuration. */
  val config: StreamConfig

  /** Start camera preview without streaming. */
  fun startPreview()

  /** Stop camera preview. */
  fun stopPreview()

  /** Start the RTSP server and begin streaming. */
  fun startStreaming()

  /** Stop streaming but keep preview active. */
  fun stopStreaming()

  /** Toggle between front and back camera. */
  fun switchCamera()

  /** Mute or unmute the microphone. */
  fun setMicrophoneMuted(muted: Boolean)

  /** Whether microphone is currently muted. */
  val isMicrophoneMuted: StateFlow<Boolean>

  /** Update streaming configuration. Requires restart if currently streaming. */
  fun updateConfig(newConfig: StreamConfig)

  /** Release all resources. Call when done. */
  fun release()
}
