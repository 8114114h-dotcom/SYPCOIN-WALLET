package com.sypcoin.wallet.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SypGold        = Color(0xFFD4A017)
val SypGoldLight   = Color(0xFFF5C842)
val SypGoldDark    = Color(0xFF9A7200)
val SypDarkBg      = Color(0xFF0A0C12)
val SypSurface     = Color(0xFF13161F)
val SypCard        = Color(0xFF1C2030)
val SypCardLight   = Color(0xFF242840)
val SypTextPrimary = Color(0xFFF0F2FF)
val SypTextSecond  = Color(0xFF7B82A0)
val SypSuccess     = Color(0xFF2ECC71)
val SypError       = Color(0xFFE74C3C)
val SypWarning     = Color(0xFFF39C12)
val SypBlue        = Color(0xFF3498DB)

private val DarkColorScheme = darkColorScheme(
    primary          = SypGold,
    onPrimary        = Color.Black,
    primaryContainer = SypGoldDark,
    secondary        = SypGoldLight,
    background       = SypDarkBg,
    surface          = SypSurface,
    surfaceVariant   = SypCard,
    onBackground     = SypTextPrimary,
    onSurface        = SypTextPrimary,
    onSurfaceVariant = SypTextSecond,
    error            = SypError,
)

@Composable
fun SypcoinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}
