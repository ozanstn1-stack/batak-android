package com.batak.turkce.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.batak.turkce.model.AppTheme

object BatakColors {
    val FeltDeep = Color(0xFF06291C)
    val Felt = Color(0xFF0B4A32)
    val FeltLight = Color(0xFF14724F)
    val WoodDark = Color(0xFF2E1B10)
    val Wood = Color(0xFF4A2D1B)
    val WoodLight = Color(0xFF6B452A)
    val Gold = Color(0xFFE3C26A)
    val GoldDark = Color(0xFFA8843A)
    val Cream = Color(0xFFF5EFDF)
    val Ink = Color(0xFF1B1410)
    val Danger = Color(0xFFD96A5A)
    val Success = Color(0xFF7BD8A0)
    val CardRed = Color(0xFFC1252B)
    val CardBlack = Color(0xFF1C1C1E)
}

private val DarkScheme = darkColorScheme(
    primary = BatakColors.Gold,
    onPrimary = Color(0xFF2B1F05),
    secondary = Color(0xFF9FD4B6),
    onSecondary = Color(0xFF0B2B1D),
    background = Color(0xFF08160F),
    onBackground = BatakColors.Cream,
    surface = Color(0xFF10251B),
    onSurface = BatakColors.Cream,
    surfaceVariant = Color(0xFF1B3426),
    onSurfaceVariant = Color(0xFFC9D8CD),
    outline = Color(0xFF3E5A4A),
    error = BatakColors.Danger
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1D6B4E),
    onPrimary = Color.White,
    secondary = Color(0xFF8A6A24),
    onSecondary = Color.White,
    background = Color(0xFFF3EDDD),
    onBackground = Color(0xFF231A12),
    surface = Color(0xFFFFFBF1),
    onSurface = Color(0xFF231A12),
    surfaceVariant = Color(0xFFE7DEC8),
    onSurfaceVariant = Color(0xFF4C4433),
    outline = Color(0xFFA79B7E),
    error = BatakColors.Danger
)

val BatakTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 46.sp,
        letterSpacing = 3.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = 1.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.4.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.6.sp
    )
)

@Composable
fun BatakTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val dark = when (theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = BatakTypography,
        content = content
    )
}
