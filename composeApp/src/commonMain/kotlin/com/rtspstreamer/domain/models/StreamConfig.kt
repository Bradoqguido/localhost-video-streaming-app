package com.rtspstreamer.domain.models

/**
 * Configuration for the RTSP stream.
 * Controls resolution, codec, bitrate, and server settings.
 */
data class StreamConfig(
  val sourceName: String = "PhoneCam",
  val port: Int = 8554,
  val path: String = "/live",
  val resolution: Resolution = Resolution.HD_1080,
  val frameRate: Int = 30,
  val videoBitRate: Int = 4_000_000,
  val videoCodec: VideoCodec = VideoCodec.H264,
  val audioBitRate: Int = 128_000,
  val audioSampleRate: Int = 44_100,
  val audioChannels: Int = 1,
  val rtmpServerIp: String = "192.168.1.100",
) {
  /**
   * Builds the full RTSP URL for this config given a local IP.
   */
  fun buildRtspUrl(localIp: String): String =
    "rtsp://$localIp:$port$path"

  /**
   * Builds the full RTMP URL for this config.
   */
  fun buildRtmpUrl(): String =
    "rtmp://$rtmpServerIp:1935/live/$sourceName"
}

enum class Resolution(val width: Int, val height: Int, val label: String) {
  SD_480(854, 480, "480p"),
  HD_720(1280, 720, "720p"),
  HD_1080(1920, 1080, "1080p"),
}

enum class VideoCodec(val label: String) {
  H264("H.264"),
  H265("H.265 (HEVC)"),
}
