package com.rtspstreamer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rtspstreamer.domain.IRtspStreamer
import com.rtspstreamer.domain.models.StreamConfig
import com.rtspstreamer.ui.screens.MainScreen
import com.rtspstreamer.ui.screens.SettingsScreen
import com.rtspstreamer.ui.theme.RTSPStreamerTheme

/**
 * Root composable for the RTSP Streamer app.
 *
 * Manages navigation between MainScreen and SettingsScreen.
 * The actual streaming logic is injected via platform-specific entry points.
 */
@Composable
fun App(
  streamer: IRtspStreamer,
  currentConfig: StreamConfig,
  onToggleStream: () -> Unit,
  onConfigChanged: (StreamConfig) -> Unit,
  onCopyUrl: (String) -> Unit,
  onViewCreated: (Any) -> Unit,
) {
  RTSPStreamerTheme(darkTheme = true) {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
      SettingsScreen(
        currentConfig = currentConfig,
        onSave = onConfigChanged,
        onBack = { showSettings = false },
      )
    } else {
      MainScreen(
        streamer = streamer,
        onToggleStream = onToggleStream,
        onOpenSettings = { showSettings = true },
        onCopyUrl = onCopyUrl,
        onViewCreated = onViewCreated,
      )
    }
  }
}
