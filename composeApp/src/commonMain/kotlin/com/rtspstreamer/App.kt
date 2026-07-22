package com.rtspstreamer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rtspstreamer.domain.models.StreamConfig
import com.rtspstreamer.domain.models.StreamState
import com.rtspstreamer.ui.screens.MainScreen
import com.rtspstreamer.ui.screens.SettingsScreen
import com.rtspstreamer.ui.theme.RTSPStreamerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Root composable for the RTSP Streamer app.
 *
 * Manages navigation between MainScreen and SettingsScreen.
 * The actual streaming logic is injected via platform-specific entry points.
 */
@Composable
fun App(
  streamState: StateFlow<StreamState>,
  isMuted: StateFlow<Boolean>,
  currentConfig: StreamConfig,
  onToggleStream: () -> Unit,
  onSwitchCamera: () -> Unit,
  onToggleMute: () -> Unit,
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
        streamState = streamState,
        isMuted = isMuted,
        onToggleStream = onToggleStream,
        onSwitchCamera = onSwitchCamera,
        onToggleMute = onToggleMute,
        onOpenSettings = { showSettings = true },
        onCopyUrl = onCopyUrl,
        onViewCreated = onViewCreated,
      )
    }
  }
}
