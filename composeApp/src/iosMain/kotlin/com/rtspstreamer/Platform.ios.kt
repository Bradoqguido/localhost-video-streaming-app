package com.rtspstreamer

import platform.UIKit.UIDevice
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
actual fun getLocalIpAddress(): String {
  // Simplified: in production, iterate ifaddrs for "en0" (WiFi)
  return "0.0.0.0" // Placeholder — requires full cinterop for ifaddrs
}

actual fun getPlatformName(): String =
  UIDevice.currentDevice.systemName + " " + UIDevice.currentDevice.systemVersion
