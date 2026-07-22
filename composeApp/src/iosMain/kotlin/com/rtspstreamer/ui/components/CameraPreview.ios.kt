package com.rtspstreamer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.interop.UIKitView
import platform.UIKit.UIView
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS camera preview using native UIKitView wrapper.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(
  onViewCreated: (Any) -> Unit,
  modifier: Modifier,
) {
  UIKitView(
    factory = {
      val view = UIView()
      onViewCreated(view)
      view
    },
    modifier = modifier,
    update = { _ -> }
  )
}
