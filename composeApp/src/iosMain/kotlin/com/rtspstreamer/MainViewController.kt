package com.rtspstreamer

import androidx.compose.ui.window.ComposeUIViewController
import com.rtspstreamer.domain.models.StreamConfig
import com.rtspstreamer.domain.models.StreamState
import kotlinx.coroutines.flow.MutableStateFlow
import platform.UIKit.UIViewController

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Creates the iOS UIViewController hosting the Compose Multiplatform UI.
 * Called from SwiftUI via UIViewControllerRepresentable.
 */
fun MainViewController(): UIViewController {
  val streamer = IosRtspStreamer.instance
  var configState by mutableStateOf(StreamConfig())

  return ComposeUIViewController {
    App(
      streamState = streamer.state,
      isMuted = streamer.isMicrophoneMuted,
      currentConfig = configState,
      onToggleStream = {
        when (streamer.state.value) {
          is StreamState.Previewing -> streamer.startStreaming()
          is StreamState.Streaming -> streamer.stopStreaming()
          else -> streamer.startPreview()
        }
      },
      onSwitchCamera = { streamer.switchCamera() },
      onToggleMute = {
        streamer.setMicrophoneMuted(!streamer.isMicrophoneMuted.value)
      },
      onConfigChanged = { newConfig ->
        streamer.updateConfig(newConfig)
      },
      onCopyUrl = { url ->
        // TODO: UIPasteboard.general.string = url
      },
      onViewCreated = { view ->
        IosRtspStreamer.delegate?.attachPreview(view)
      },
    )
  }
}
