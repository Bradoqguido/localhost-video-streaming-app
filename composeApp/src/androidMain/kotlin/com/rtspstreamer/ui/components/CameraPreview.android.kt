package com.rtspstreamer.ui.components

import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android camera preview using native TextureView for custom Camera2 rendering.
 */
@Composable
actual fun CameraPreview(
  onViewCreated: (Any) -> Unit,
  modifier: Modifier,
) {
  AndroidView(
    factory = { context ->
      TextureView(context).apply {
        keepScreenOn = true
        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
          override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            onViewCreated(this@apply)
          }

          override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

          override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

          override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
      }
    },
    modifier = modifier,
  )
}
