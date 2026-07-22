package com.rtspstreamer.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rtspstreamer.domain.models.StreamState
import com.rtspstreamer.ui.components.CameraPreview
import com.rtspstreamer.ui.components.StatusOverlay
import com.rtspstreamer.ui.components.StreamControls
import com.rtspstreamer.ui.theme.LiveRed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Main screen: full-screen camera preview with overlay controls.
 *
 * Layout:
 * - Camera preview fills entire screen
 * - Status overlay slides down from top when streaming
 * - Controls anchored to bottom with glassmorphism bg
 */
@Composable
fun MainScreen(
  streamState: StateFlow<StreamState>,
  isMuted: StateFlow<Boolean>,
  onToggleStream: () -> Unit,
  onSwitchCamera: () -> Unit,
  onToggleMute: () -> Unit,
  onOpenSettings: () -> Unit,
  onCopyUrl: (String) -> Unit,
  onViewCreated: (Any) -> Unit,
) {
  val state by streamState.collectAsState()
  val muted by isMuted.collectAsState()
  val isStreaming = state is StreamState.Streaming

  // Subtle red border when live
  val borderColor by animateColorAsState(
    targetValue = if (isStreaming) LiveRed.copy(alpha = 0.6f) else Color.Transparent,
    animationSpec = tween(500),
    label = "liveBorder",
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    // ── Camera Preview (full screen) ──
    CameraPreview(
      onViewCreated = onViewCreated,
      modifier = Modifier.fillMaxSize(),
    )

    // ── Status overlay (top, animated) ──
    StatusOverlay(
      streamState = state,
      onCopyUrl = onCopyUrl,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
        .padding(top = 16.dp),
    )

    // ── Bottom controls ──
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding(),
    ) {
      StreamControls(
        streamState = state,
        isMuted = muted,
        onToggleStream = onToggleStream,
        onSwitchCamera = onSwitchCamera,
        onToggleMute = onToggleMute,
        onOpenSettings = onOpenSettings,
      )

      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}
