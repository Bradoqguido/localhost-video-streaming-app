package com.rtspstreamer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rtspstreamer.domain.models.Resolution
import com.rtspstreamer.domain.models.StreamConfig
import com.rtspstreamer.domain.models.VideoCodec

/**
 * Settings screen for configuring stream parameters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  currentConfig: StreamConfig,
  onSave: (StreamConfig) -> Unit,
  onBack: () -> Unit,
) {
  var sourceName by remember { mutableStateOf(currentConfig.sourceName) }
  var rtmpServerIp by remember { mutableStateOf(currentConfig.rtmpServerIp) }
  var port by remember { mutableStateOf(currentConfig.port.toString()) }
  var resolution by remember { mutableStateOf(currentConfig.resolution) }
  var frameRate by remember { mutableStateOf(currentConfig.frameRate.toString()) }
  var videoBitRate by remember { mutableStateOf((currentConfig.videoBitRate / 1_000_000).toString()) }
  var videoCodec by remember { mutableStateOf(currentConfig.videoCodec) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    TopAppBar(
      title = { Text("Stream Settings") },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
      ),
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // ── Source Name ──
      SectionLabel("Source Name")
      OutlinedTextField(
        value = sourceName,
        onValueChange = { sourceName = it },
        label = { Text("Name shown to receivers") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )

      // ── Resolution ──
      SectionLabel("Resolution")
      ResolutionSelector(
        selected = resolution,
        onSelect = { resolution = it },
      )

      // ── Frame Rate ──
      SectionLabel("Frame Rate (fps)")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf("24", "25", "30", "60").forEach { fps ->
          Button(
            onClick = { frameRate = fps },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (frameRate == fps)
                MaterialTheme.colorScheme.primary
              else
                MaterialTheme.colorScheme.surfaceContainer,
              contentColor = if (frameRate == fps)
                MaterialTheme.colorScheme.onPrimary
              else
                MaterialTheme.colorScheme.onSurface,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
          ) {
            Text(fps)
          }
        }
      }

      // ── Video Bitrate ──
      SectionLabel("Video Bitrate (Mbps)")
      OutlinedTextField(
        value = videoBitRate,
        onValueChange = { videoBitRate = it.filter { c -> c.isDigit() || c == '.' } },
        label = { Text("Default: 4 Mbps") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )

      // ── Video Codec ──
      SectionLabel("Video Codec")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        VideoCodec.entries.forEach { codec ->
          Button(
            onClick = { videoCodec = codec },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (videoCodec === codec)
                MaterialTheme.colorScheme.primary
              else
                MaterialTheme.colorScheme.surfaceContainer,
              contentColor = if (videoCodec === codec)
                MaterialTheme.colorScheme.onPrimary
              else
                MaterialTheme.colorScheme.onSurface,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
          ) {
            Text(codec.label)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // ── Save button ──
      Button(
        onClick = {
          onSave(
            currentConfig.copy(
              sourceName = sourceName,
              rtmpServerIp = rtmpServerIp,
              port = port.toIntOrNull() ?: 8554,
              resolution = resolution,
              frameRate = frameRate.toIntOrNull() ?: 30,
              videoBitRate = ((videoBitRate.toFloatOrNull() ?: 4f) * 1_000_000).toInt(),
              videoCodec = videoCodec,
            )
          )
          onBack()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        shape = RoundedCornerShape(16.dp),
      ) {
        Text("Save & Apply")
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun ResolutionSelector(
  selected: Resolution,
  onSelect: (Resolution) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Resolution.entries.forEach { res ->
      Button(
        onClick = { onSelect(res) },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (selected === res)
            MaterialTheme.colorScheme.primary
          else
            MaterialTheme.colorScheme.surfaceContainer,
          contentColor = if (selected === res)
            MaterialTheme.colorScheme.onPrimary
          else
            MaterialTheme.colorScheme.onSurface,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f),
      ) {
        Text(res.label)
      }
    }
  }
}
