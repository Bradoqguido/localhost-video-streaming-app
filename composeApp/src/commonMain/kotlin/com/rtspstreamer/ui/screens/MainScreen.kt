package com.rtspstreamer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtspstreamer.domain.IRtspStreamer
import com.rtspstreamer.domain.models.StreamState
import com.rtspstreamer.ui.components.CameraPreview
import com.rtspstreamer.ui.theme.GlassBorder
import com.rtspstreamer.ui.theme.GlassWhite
import com.rtspstreamer.ui.theme.LiveGreen
import com.rtspstreamer.ui.theme.LiveRed

/**
 * Main screen: full-screen camera preview with a minimal landscape top HUD,
 * vertical zoom slider, screen brightness controllers, flashlight toggle, and a level stability bar.
 */
@Composable
fun MainScreen(
  streamer: IRtspStreamer,
  onToggleStream: () -> Unit,
  onOpenSettings: () -> Unit,
  onCopyUrl: (String) -> Unit,
  onViewCreated: (Any) -> Unit,
) {
  val state by streamer.state.collectAsState()
  val isStable by streamer.isLevelStable.collectAsState()
  val isStreaming = state is StreamState.Streaming

  // HUD local states
  var isFlashlightOn by remember { mutableStateOf(false) }
  var showBrightnessSlider by remember { mutableStateOf(false) }
  var brightnessLevel by remember { mutableStateOf(0.7f) }
  var zoomLevel by remember { mutableStateOf(0f) }

  // Subtle red border overlay when live
  val borderColor by animateColorAsState(
    targetValue = if (isStreaming) LiveRed.copy(alpha = 0.5f) else Color.Transparent,
    animationSpec = tween(500),
    label = "liveBorder",
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .border(4.dp, borderColor),
  ) {
    // ── Camera Preview (full screen) ──
    CameraPreview(
      onViewCreated = onViewCreated,
      modifier = Modifier.fillMaxSize(),
    )

    // ── Left Side: Vertical Zoom Slider (positioned below the URI field) ──
    Column(
      modifier = Modifier
        .align(Alignment.TopStart)
        .statusBarsPadding()
        .padding(start = 16.dp, top = 72.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "ZOOM",
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Box(
        modifier = Modifier
          .height(150.dp)
          .width(36.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(Color(0x33000000))
          .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
      ) {
        Slider(
          value = zoomLevel,
          onValueChange = {
            zoomLevel = it
            streamer.setZoom(it)
          },
          valueRange = 0f..1f,
          colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
          ),
          modifier = Modifier
            .graphicsLayer { rotationZ = -90f }
            .width(120.dp)
        )
      }
    }

    // ── Center Bottom: Level/Stability Bar ──
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = if (isStable) "LEVEL STABLE" else "TILTED",
        color = if (isStable) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(6.dp))
      Box(
        modifier = Modifier
          .width(120.dp)
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(Color(0x44000000))
          .border(0.5.dp, Color(0x44FFFFFF), RoundedCornerShape(3.dp))
          .padding(1.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(2.dp))
            .background(if (isStable) Color(0xFFFFD700) else Color.White.copy(alpha = 0.4f))
        )
      }
    }

    // ── Top minimal HUD ──
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // ── Left Side: URL & FPS info ──
      Box(
        modifier = Modifier.weight(1f, fill = false)
      ) {
        if (state is StreamState.Streaming) {
          val streamUrl = (state as StreamState.Streaming).rtspUrl
          val fpsVal = (state as StreamState.Streaming).fps

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0x77000000))
              .border(0.5.dp, Color(0x44FFFFFF), RoundedCornerShape(8.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            // URL label (click to copy)
            Row(
              modifier = Modifier.clickable { onCopyUrl(streamUrl) },
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = streamUrl,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy URL",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .width(1.dp)
                .height(12.dp)
                .background(Color(0x33FFFFFF))
            )
            Spacer(modifier = Modifier.width(8.dp))

            // FPS indicator
            Text(
              text = "${((fpsVal * 10).toInt() / 10f)} FPS",
              color = LiveGreen,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          // Idle / Previewing State label
          Text(
            text = "Ready to Stream",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0x55000000))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      // ── Right Side: Settings, Brightness, Flashlight & Start/Stop controls ──
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Brightness Expandable Slider
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          AnimatedVisibility(
            visible = showBrightnessSlider,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
          ) {
            Box(
              modifier = Modifier
                .width(100.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0x77000000))
                .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp),
              contentAlignment = Alignment.Center
            ) {
              Slider(
                value = brightnessLevel,
                onValueChange = {
                  brightnessLevel = it
                  streamer.setScreenBrightness(it)
                },
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(
                  thumbColor = Color.White,
                  activeTrackColor = Color.White,
                  inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
              )
            }
          }

          IconButton(
            onClick = { showBrightnessSlider = !showBrightnessSlider },
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(GlassWhite)
              .border(1.dp, GlassBorder, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.WbSunny,
              contentDescription = "Screen Brightness",
              tint = if (showBrightnessSlider) Color(0xFFFFD700) else Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Flashlight (Torch) Toggle
        IconButton(
          onClick = {
            isFlashlightOn = !isFlashlightOn
            streamer.setFlashlightEnabled(isFlashlightOn)
          },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(GlassWhite)
            .border(1.dp, GlassBorder, CircleShape)
        ) {
          Icon(
            imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
            contentDescription = "Flashlight",
            tint = if (isFlashlightOn) Color(0xFFFFD700) else Color.White,
            modifier = Modifier.size(18.dp)
          )
        }

        // Settings Button
        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(GlassWhite)
            .border(1.dp, GlassBorder, CircleShape)
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }

        // Start / Stop Stream button
        Box(
          modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isStreaming) LiveRed else LiveGreen)
            .clickable { onToggleStream() }
            .padding(horizontal = 16.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isStreaming) "● STOP" else "GO LIVE",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
