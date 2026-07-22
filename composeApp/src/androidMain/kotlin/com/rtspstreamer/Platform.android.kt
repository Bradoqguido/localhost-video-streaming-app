package com.rtspstreamer

import java.net.Inet4Address
import java.net.NetworkInterface

actual fun getLocalIpAddress(): String {
  try {
    val interfaces = NetworkInterface.getNetworkInterfaces()
    while (interfaces.hasMoreElements()) {
      val networkInterface = interfaces.nextElement()
      if (networkInterface.isLoopback || !networkInterface.isUp) continue

      val addresses = networkInterface.inetAddresses
      while (addresses.hasMoreElements()) {
        val address = addresses.nextElement()
        if (address is Inet4Address && !address.isLoopbackAddress) {
          return address.hostAddress ?: "0.0.0.0"
        }
      }
    }
  } catch (_: Exception) {
    // Fallback
  }
  return "0.0.0.0"
}

actual fun getPlatformName(): String = "Android"
