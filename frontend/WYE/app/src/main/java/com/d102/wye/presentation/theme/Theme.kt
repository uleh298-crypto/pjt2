package com.d102.wye.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextOnColored,
    background = Background,
    surface = SurfaceWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextOnColored,
    background = Background,
    surface = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private const val REFERENCE_WIDTH_DP = 411f

@Composable
fun WYETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val targetDensity = displayMetrics.widthPixels / REFERENCE_WIDTH_DP
    val currentFontScale = LocalDensity.current.fontScale

    CompositionLocalProvider(
        LocalDensity provides Density(density = targetDensity, fontScale = currentFontScale)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}