package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.data.models.AppSettings

private val DefaultShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val RoundedShapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ReadTrackerTheme(
    settings: AppSettings = AppSettings(),
    darkTheme: Boolean = true,
    roundedInputFields: Boolean = settings.roundedInputFields,
    content: @Composable () -> Unit
) {
    val colorScheme = remember(settings) { buildColorSchemeFromSettings(settings) }
    val statusColors = remember(settings) { buildStatusColorsFromSettings(settings) }
    val shapes = if (roundedInputFields) RoundedShapes else DefaultShapes

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = generateSequence(view.context) { 
                if (it is android.content.ContextWrapper) it.baseContext else null 
            }.filterIsInstance<Activity>().firstOrNull()
            
            activity?.window?.let { window ->
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalStatusColors provides statusColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = shapes,
            content = content
        )
    }
}

