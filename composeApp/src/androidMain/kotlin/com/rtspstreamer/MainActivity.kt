package com.rtspstreamer

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.pedro.library.view.OpenGlView
import com.rtspstreamer.domain.models.StreamConfig
import com.rtspstreamer.domain.models.StreamState
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

  private val streamer = AndroidRtspStreamer()
  private var currentConfig = StreamConfig()
  private var openGlView: OpenGlView? = null

  private val requiredPermissions = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
  )

  private val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val allGranted = permissions.values.all { it }
    if (allGranted) {
      initializeStreamer()
    } else {
      Toast.makeText(this, "Camera and microphone permissions are required", Toast.LENGTH_LONG).show()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    if (hasPermissions()) {
      initializeStreamer()
    } else {
      permissionLauncher.launch(requiredPermissions)
    }

    setContent {
      App(
        streamer = streamer,
        currentConfig = currentConfig,
        onToggleStream = {
          when (streamer.state.value) {
            is StreamState.Idle -> {
              // Need to init first
              if (hasPermissions()) {
                initializeStreamer()
                streamer.startStreaming()
              }
            }
            is StreamState.Previewing -> streamer.startStreaming()
            is StreamState.Streaming -> streamer.stopStreaming()
            is StreamState.Error -> {
              // Retry
              initializeStreamer()
            }
          }
        },
        onConfigChanged = { newConfig ->
          currentConfig = newConfig
          streamer.updateConfig(newConfig)
        },
        onCopyUrl = { url ->
          val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          clipboard.setPrimaryClip(ClipData.newPlainText("RTSP URL", url))
          Toast.makeText(this, "URL copied: $url", Toast.LENGTH_SHORT).show()
        },
        onViewCreated = { view ->
          if (view is android.view.TextureView) {
            streamer.attachView(view)
            streamer.startPreview()
          }
        },
      )
    }
  }

  override fun onResume() {
    super.onResume()
    // Re-attach view if needed
  }

  override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
    super.onConfigurationChanged(newConfig)
    if (streamer.state.value is StreamState.Previewing) {
      streamer.stopPreview()
      streamer.startPreview()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    streamer.release()
  }

  private fun hasPermissions(): Boolean =
    requiredPermissions.all {
      ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

  private fun initializeStreamer() {
    // The OpenGlView will be created by the Compose CameraPreview.
    // For now, we start preview when the view is available.
    // In production, use a CompositionLocal or callback to link them.
  }
}
