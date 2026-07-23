package com.rtspstreamer

import com.rtspstreamer.domain.IRtspStreamer
import com.rtspstreamer.domain.models.StreamConfig
import com.rtspstreamer.domain.models.StreamEvent
import com.rtspstreamer.domain.models.StreamState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS implementation of [IRtspStreamer].
 *
 * Delegates to RootEncoder-iOS (Swift) via the [RtspBridgeIOS] protocol.
 * The Swift bridge is registered from the iOS app's entry point.
 *
 * TODO: Implement Swift bridge integration once RootEncoder-iOS is added via SPM.
 */
interface IosStreamerDelegate {
  fun startPreview()
  fun stopPreview()
  fun startStreaming()
  fun stopStreaming()
  fun switchCamera()
  fun setMicrophoneMuted(muted: Boolean)
  fun attachPreview(view: Any)
  fun setFlashlightEnabled(enabled: Boolean)
  fun setZoom(level: Float)
  fun setScreenBrightness(level: Float)
}

class IosRtspStreamer : IRtspStreamer {

  private val _state = MutableStateFlow<StreamState>(StreamState.Idle)
  override val state: StateFlow<StreamState> = _state.asStateFlow()

  private val _events = MutableSharedFlow<StreamEvent>(extraBufferCapacity = 16)
  override val events: SharedFlow<StreamEvent> = _events.asSharedFlow()

  private val _isMuted = MutableStateFlow(false)
  override val isMicrophoneMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private val _isLevelStable = MutableStateFlow(true)
  override val isLevelStable: StateFlow<Boolean> = _isLevelStable.asStateFlow()

  private var _config = StreamConfig()
  override val config: StreamConfig get() = _config

  companion object {
    var delegate: IosStreamerDelegate? = null
    val instance = IosRtspStreamer()
  }

  fun updateState(state: StreamState) {
    _state.value = state
  }

  fun emitEvent(event: StreamEvent) {
    _events.tryEmit(event)
  }

  override fun startPreview() {
    delegate?.startPreview() ?: run {
      _state.value = StreamState.Previewing
    }
  }

  override fun stopPreview() {
    delegate?.stopPreview() ?: run {
      _state.value = StreamState.Idle
    }
  }

  override fun startStreaming() {
    delegate?.startStreaming() ?: run {
      val localIp = getLocalIpAddress()
      _state.value = StreamState.Streaming(
        rtspUrl = _config.buildRtspUrl(localIp),
        connectedClients = 0,
        fps = _config.frameRate.toFloat(),
        uptimeMs = 0,
      )
    }
  }

  override fun stopStreaming() {
    delegate?.stopStreaming() ?: run {
      _state.value = StreamState.Previewing
    }
  }

  override fun switchCamera() {
    delegate?.switchCamera()
  }

  override fun setMicrophoneMuted(muted: Boolean) {
    _isMuted.value = muted
    delegate?.setMicrophoneMuted(muted)
  }

  override fun updateConfig(newConfig: StreamConfig) {
    _config = newConfig
    delegate?.startPreview()
  }

  override fun setFlashlightEnabled(enabled: Boolean) {
    delegate?.setFlashlightEnabled(enabled)
  }

  override fun setZoom(level: Float) {
    delegate?.setZoom(level)
  }

  override fun setScreenBrightness(level: Float) {
    delegate?.setScreenBrightness(level)
  }

  fun updateLevelStable(stable: Boolean) {
    _isLevelStable.value = stable
  }

  override fun release() {
    delegate?.stopPreview()
    _state.value = StreamState.Idle
  }
}
