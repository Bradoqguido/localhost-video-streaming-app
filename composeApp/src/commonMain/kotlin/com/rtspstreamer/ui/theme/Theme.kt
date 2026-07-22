package com.rtspstreamer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Color Palette ──────────────────────────────────────────
private val StreamRed = Color(0xFFE53935)
private val StreamRedDark = Color(0xFFB71C1C)
private val StreamGreen = Color(0xFF43A047)
private val SurfaceDark = Color(0xFF121218)
private val SurfaceContainerDark = Color(0xFF1E1E2A)
private val SurfaceContainerHighDark = Color(0xFF2A2A3A)
private val OnSurfaceDark = Color(0xFFE8E8F0)
private val OnSurfaceVariantDark = Color(0xFF9898A8)
private val AccentBlue = Color(0xFF5C7AFF)
private val AccentBlueDim = Color(0xFF3D56C7)

val LiveRed = StreamRed
val LiveGreen = StreamGreen
val GlassWhite = Color(0x22FFFFFF)
val GlassBorder = Color(0x33FFFFFF)

private val DarkColorScheme = darkColorScheme(
  primary = AccentBlue,
  onPrimary = Color.White,
  primaryContainer = AccentBlueDim,
  secondary = StreamGreen,
  onSecondary = Color.White,
  error = StreamRed,
  onError = Color.White,
  errorContainer = StreamRedDark,
  background = SurfaceDark,
  onBackground = OnSurfaceDark,
  surface = SurfaceDark,
  onSurface = OnSurfaceDark,
  surfaceContainer = SurfaceContainerDark,
  surfaceContainerHigh = SurfaceContainerHighDark,
  onSurfaceVariant = OnSurfaceVariantDark,
  outline = Color(0xFF3A3A4A),
  outlineVariant = Color(0xFF2A2A3A),
)

private val LightColorScheme = lightColorScheme(
  primary = AccentBlue,
  onPrimary = Color.White,
  secondary = StreamGreen,
  error = StreamRed,
  background = Color(0xFFF5F5FA),
  onBackground = Color(0xFF1A1A2E),
  surface = Color.White,
  onSurface = Color(0xFF1A1A2E),
  surfaceContainer = Color(0xFFEEEEF4),
  onSurfaceVariant = Color(0xFF666680),
)

private val AppTypography = Typography(
  headlineLarge = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.5).sp,
  ),
  headlineMedium = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
  ),
  titleLarge = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
  ),
  titleMedium = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.15.sp,
  ),
  bodyLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
  ),
  bodyMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  bodySmall = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
  ),
  labelLarge = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
)

@Composable
fun RTSPStreamerTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    content = content,
  )
}
