package com.rtspstreamer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rtspstreamer.domain.models.StreamState
import com.rtspstreamer.ui.theme.GlassBorder
import com.rtspstreamer.ui.theme.GlassWhite
import com.rtspstreamer.ui.theme.LiveGreen
import com.rtspstreamer.ui.theme.LiveRed

/**
 * Bottom control bar with camera switch, mute, settings, and the big GO LIVE button.
 */
@Composable
fun StreamControls(
  streamState: StreamState,
  isMuted: Boolean,
  onToggleStream: () -> Unit,
  onSwitchCamera: () -> Unit,
  onToggleMute: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isStreaming = streamState is StreamState.Streaming

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // ── Secondary controls row ──
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(GlassWhite)
        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Mic toggle
      IconButton(onClick = onToggleMute) {
        Icon(
          imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = if (isMuted) "Unmute" else "Mute",
          tint = if (isMuted) LiveRed else Color.White,
        )
      }

      // Settings
      IconButton(onClick = onOpenSettings) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = Color.White,
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // ── GO LIVE button ──
    GoLiveButton(
      isStreaming = isStreaming,
      onClick = onToggleStream,
    )
  }
}

@Composable
private fun GoLiveButton(
  isStreaming: Boolean,
  onClick: () -> Unit,
) {
  val bgColor by animateColorAsState(
    targetValue = if (isStreaming) LiveRed else LiveGreen,
    animationSpec = tween(300),
    label = "goLiveBg",
  )

  // Pulsing dot when live
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0.3f,
    animationSpec = infiniteRepeatable(
      animation = tween(800),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulseAlpha",
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp)
      .clip(RoundedCornerShape(28.dp))
      .background(bgColor)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (isStreaming) {
        // Pulsing red dot
        Box(
          modifier = Modifier
            .size(12.dp)
            .alpha(pulseAlpha)
            .clip(CircleShape)
            .background(Color.White),
        )
        Spacer(modifier = Modifier.width(10.dp))
      }

      Text(
        text = if (isStreaming) "STOP STREAM" else "GO LIVE",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
      )
    }
  }
}
