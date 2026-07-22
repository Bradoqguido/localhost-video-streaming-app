package com.rtspstreamer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific camera preview surface.
 * Android: wraps RootEncoder's OpenGlView
 * iOS: wraps AVCaptureVideoPreviewLayer via UIKitView
 */
@Composable
expect fun CameraPreview(
  onViewCreated: (Any) -> Unit,
  modifier: Modifier = Modifier,
)
