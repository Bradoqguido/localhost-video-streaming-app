package com.rtspstreamer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtspstreamer.domain.models.StreamState
import com.rtspstreamer.ui.theme.GlassBorder
import com.rtspstreamer.ui.theme.GlassWhite
import com.rtspstreamer.ui.theme.LiveRed

/**
 * Overlay showing RTSP URL, connected clients, FPS, and uptime.
 * Slides in when streaming, slides out when stopped.
 */
@Composable
fun StatusOverlay(
  streamState: StreamState,
  onCopyUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isStreaming = streamState is StreamState.Streaming

  AnimatedVisibility(
    visible = isStreaming,
    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
    modifier = modifier,
  ) {
    if (streamState is StreamState.Streaming) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(GlassWhite)
          .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
          .padding(16.dp),
      ) {
        // ── RTSP URL (tap to copy) ──
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22000000))
            .clickable { onCopyUrl(streamState.rtspUrl) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "📡",
            fontSize = 16.sp,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = streamState.rtspUrl,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.weight(1f),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy URL",
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp),
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Stats row ──
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
          StatChip(
            icon = Icons.Default.Speed,
            value = "${((streamState.fps * 10).toInt() / 10f)}",
            label = "fps",
          )
          StatChip(
            icon = Icons.Default.Timer,
            value = formatUptime(streamState.uptimeMs),
            label = "uptime",
          )
        }

        // ── Live indicator ──
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "●",
            color = LiveRed,
            fontSize = 10.sp,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = LiveRed,
          )
        }
      }
    }
  }
}

@Composable
private fun StatChip(
  icon: ImageVector,
  value: String,
  label: String,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = Color.White.copy(alpha = 0.7f),
      modifier = Modifier.size(16.dp),
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      color = Color.White,
    )
    Spacer(modifier = Modifier.width(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = Color.White.copy(alpha = 0.6f),
    )
  }
}

private fun pad(value: Long): String = if (value < 10) "0$value" else "$value"

private fun formatUptime(ms: Long): String {
  val totalSeconds = ms / 1000
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60
  return if (hours > 0) {
    "$hours:${pad(minutes)}:${pad(seconds)}"
  } else {
    "${pad(minutes)}:${pad(seconds)}"
  }
}
