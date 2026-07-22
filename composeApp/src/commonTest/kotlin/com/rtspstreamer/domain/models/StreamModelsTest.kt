package com.rtspstreamer.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StreamConfigTest {

  @Test
  fun buildRtspUrl_generatesCorrectUrl() {
    val config = StreamConfig(
      port = 8554,
      path = "/live",
    )
    val url = config.buildRtspUrl("192.168.1.50")
    assertEquals("rtsp://192.168.1.50:8554/live", url)
  }

  @Test
  fun buildRtspUrl_withCustomPort() {
    val config = StreamConfig(port = 9000, path = "/camera")
    val url = config.buildRtspUrl("10.0.0.1")
    assertEquals("rtsp://10.0.0.1:9000/camera", url)
  }

  @Test
  fun defaultConfig_has1080p30() {
    val config = StreamConfig()
    assertEquals(Resolution.HD_1080, config.resolution)
    assertEquals(30, config.frameRate)
    assertEquals(4_000_000, config.videoBitRate)
    assertEquals(VideoCodec.H264, config.videoCodec)
  }

  @Test
  fun resolution_hasDimensions() {
    assertEquals(1920, Resolution.HD_1080.width)
    assertEquals(1080, Resolution.HD_1080.height)
    assertEquals(1280, Resolution.HD_720.width)
    assertEquals(720, Resolution.HD_720.height)
  }
}

class StreamStateTest {

  @Test
  fun idle_isCorrectType() {
    val state: StreamState = StreamState.Idle
    assertIs<StreamState.Idle>(state)
  }

  @Test
  fun streaming_holdsUrl() {
    val state = StreamState.Streaming(
      rtspUrl = "rtsp://192.168.1.50:8554/live",
      connectedClients = 2,
      fps = 29.8f,
      uptimeMs = 60_000L,
    )
    assertEquals("rtsp://192.168.1.50:8554/live", state.rtspUrl)
    assertEquals(2, state.connectedClients)
  }

  @Test
  fun error_holdsMessage() {
    val state = StreamState.Error("Camera unavailable")
    assertIs<StreamState.Error>(state)
    assertEquals("Camera unavailable", state.message)
  }
}
