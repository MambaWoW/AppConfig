package io.github.mambawow.appconfig.panel.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.Background
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.Primary
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.Surface

/**
 * Theme system for the configuration panel
 * Following Single Responsibility Principle - only handles theming
 */
enum class ThemeType {
    Material, Cupertino
}

val LocalThemeType = staticCompositionLocalOf<ThemeType> {
    ThemeType.Material
}

/**
 * Centralized style definitions for the configuration panel UI
 */
object AppTextStyles {
    
    val BodyNormal = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
    
    val BodyBold = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )
    
    val BodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
    
    val Headline = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    )
}

/**
 * Color palette for the configuration panel
 */
fun colorScheme(theme: ThemeType): ColorScheme {
    return when (theme) {
        ThemeType.Material -> MaterialColors
        ThemeType.Cupertino -> CupertinoColors
    }.let {
        lightColorScheme(
            primary = it.Primary,
            background = it.Background,
            surface = it.Surface,
            surfaceContainer = it.surfaceContainer,
            onSurface = it.onSurface
        )
    }
}

sealed interface Colors {
    val Primary: Color
    val Secondary: Color
    val Background: Color
    val Surface: Color
    val onSurface: Color
    val surfaceContainer: Color
}

object MaterialColors: Colors {
    override val Primary: Color = Color(0xFF1C2526)
    override val Secondary: Color = Color(0xFF1C2526)
    override val Background: Color = Color.White
    override val Surface: Color = Color.White
    override val onSurface: Color = Color(0xFF1C2526)
    override val surfaceContainer: Color = Color.White
}

object CupertinoColors: Colors {
    override val Primary = Color(0xFF1C2526)
    override val Secondary = Color.Gray
    override val Background = Color.White
    override val Surface = Color.White
    override val onSurface: Color = Color(0xFF1C2526)
    override val surfaceContainer: Color = Color.White
}

object AppColors {
    val Primary = Color(0xFF1C2526)
    val Secondary = Color.Gray
    val Background = Color(0xFFF2F2F7)
    val Surface = Color(0xFFF2F2F7)
    val Divider = Color(0xffc6c6c8)
    val Error = Color.Red
    val Success = Color.Green
    val SystemBlue = Color(0, 122, 255)
    val SystemGreen = Color(52, 199, 89)
    val SystemGray = Color(0xff7f7f7f)
} 