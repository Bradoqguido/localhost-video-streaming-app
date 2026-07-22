package com.rtspstreamer

/**
 * Platform-specific utilities.
 */
expect fun getLocalIpAddress(): String

expect fun getPlatformName(): String
