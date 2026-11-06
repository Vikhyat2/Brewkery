package com.bookverse.brewkery.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Brew {
    val Accent = Color(0xFFD9532B)
    val AccentDeep = Color(0xFF9E3518)
    val Espresso = Color(0xFF1C110C)
    val Cream = Color(0xFFFFFAF6)
    val Peach = Color(0xFFFBEDE4)
    val PeachDeep = Color(0xFFF6E3D6)
    val Border = Color(0xFFF1DED4)
    val Muted = Color(0xFF7A6A62)
    val BadgeBg = Color(0xFFFFF0C2)
    val BadgeText = Color(0xFF8A5A00)
    val Gold = Color(0xFFF2B01E)
    val Success = Color(0xFF0F8A5F)
    val Danger = Color(0xFFE0245E)

    val CtaGradient = Brush.horizontalGradient(listOf(Accent, AccentDeep))

    /** Prices use a monospace face, like the mocks. */
    val PriceStyle = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Accent)
}

private val Scheme = lightColorScheme(
    primary = Brew.Accent,
    onPrimary = Color.White,
    secondary = Brew.Espresso,
    onSecondary = Color.White,
    background = Brew.Cream,
    onBackground = Brew.Espresso,
    surface = Color.White,
    onSurface = Brew.Espresso,
    onSurfaceVariant = Brew.Muted,
    outline = Brew.Border,
    error = Brew.Danger,
)

private val Type = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.6.sp),
)

@Composable
fun BrewkeryTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Type, content = content)
}
