package com.rtspstreamer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.flow.StateFlow

/**
 * Main screen: full-screen camera preview with a minimal landscape top HUD,
 * vertical pop-up sliders for zoom and screen brightness, flashlight toggle, and a centered crosshair stability level.
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
  var showZoomSlider by remember { mutableStateOf(false) }
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

    // ── Center Screen: Stability Level Crosshair ──
    Box(
      modifier = Modifier.align(Alignment.Center),
      contentAlignment = Alignment.Center
    ) {
      // Thin leveling line
      Box(
        modifier = Modifier
          .width(140.dp)
          .height(1.5.dp)
          .background(if (isStable) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f))
      )
      // Side tick marks
      Row(
        modifier = Modifier.width(156.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Box(
          modifier = Modifier
            .size(2.dp, 8.dp)
            .background(if (isStable) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f))
        )
        Box(
          modifier = Modifier
            .size(2.dp, 8.dp)
            .background(if (isStable) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f))
        )
      }
    }

    // ── Minimal Top HUD Row ──
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      // ── Left Side: URL & FPS info ──
      Box(
        modifier = Modifier.padding(top = 4.dp)
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

            Text(
              text = "${((fpsVal * 10).toInt() / 10f)} FPS",
              color = LiveGreen,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
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

      // ── Right Side Controls and Pop-up Sliders Drawer ──
      Column(
        horizontalAlignment = Alignment.End
      ) {
        // Buttons Row with 12.dp spacing
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Zoom magnifier Button
          IconButton(
            onClick = {
              showZoomSlider = !showZoomSlider
              showBrightnessSlider = false
            },
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(GlassWhite)
              .border(1.dp, GlassBorder, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Camera Zoom",
              tint = if (showZoomSlider) Color(0xFFFFD700) else Color.White,
              modifier = Modifier.size(18.dp)
            )
          }

          // Brightness Sun Button
          IconButton(
            onClick = {
              showBrightnessSlider = !showBrightnessSlider
              showZoomSlider = false
            },
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

          // Flashlight Toggle Button
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

          // Settings Gear Button
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

        Spacer(modifier = Modifier.height(10.dp))

        // Sliders Drawer (displays directly below their top-right control buttons)
        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.padding(end = 4.dp)
        ) {
          // Zoom Slider Bubble
          AnimatedVisibility(
            visible = showZoomSlider,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            VerticalSliderBubble(
              label = "ZOOM",
              value = zoomLevel,
              onValueChange = {
                zoomLevel = it
                streamer.setZoom(it)
              }
            )
          }

          // Brightness Slider Bubble
          AnimatedVisibility(
            visible = showBrightnessSlider,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            VerticalSliderBubble(
              label = "BRIGHT",
              value = brightnessLevel,
              onValueChange = {
                brightnessLevel = it
                streamer.setScreenBrightness(it)
              },
              valueRange = 0.1f..1.0f
            )
          }
        }
      }
    }
  }
}

@Composable
private fun VerticalSliderBubble(
  label: String,
  value: Float,
  onValueChange: (Float) -> Unit,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(44.dp)
      .clip(RoundedCornerShape(22.dp))
      .background(Color(0x99000000))
      .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
      .padding(vertical = 12.dp)
  ) {
    Text(
      text = label,
      color = Color.White.copy(alpha = 0.6f),
      fontSize = 8.sp,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(10.dp))
    
    // Custom vertical fader strip
    BoxWithConstraints(
      modifier = Modifier
        .width(18.dp)
        .height(130.dp)
        .clip(RoundedCornerShape(9.dp))
        .background(Color(0x33FFFFFF))
        .pointerInput(valueRange, value) {
          detectDragGestures { change, dragAmount ->
            change.consume()
            val height = size.height
            val delta = -dragAmount.y / height
            val rangeSpan = valueRange.endInclusive - valueRange.start
            val newValue = (value + delta * rangeSpan).coerceIn(valueRange.start, valueRange.endInclusive)
            onValueChange(newValue)
          }
        }
        .pointerInput(valueRange) {
          detectTapGestures { offset ->
            val height = size.height
            val ratio = 1f - (offset.y / height)
            val rangeSpan = valueRange.endInclusive - valueRange.start
            val newValue = (valueRange.start + ratio * rangeSpan).coerceIn(valueRange.start, valueRange.endInclusive)
            onValueChange(newValue)
          }
        }
    ) {
      val heightPx = constraints.maxHeight
      val ratio = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
      val density = LocalDensity.current

      // Active fill (drawn from bottom up)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(with(density) { (heightPx * ratio).toDp() })
          .align(Alignment.BottomCenter)
          .background(Color.White)
      )
      
      // Level indicator line
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(with(density) { (heightPx * ratio).toDp() })
          .align(Alignment.BottomCenter)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .align(Alignment.TopCenter)
            .background(Color(0xFFFFD700))
        )
      }
    }
  }
}
