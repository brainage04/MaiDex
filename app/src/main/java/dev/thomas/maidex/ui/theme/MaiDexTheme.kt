package dev.thomas.maidex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MaiDexColors = lightColorScheme(
    primary = Color(0xFF00639A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE5FF),
    onPrimaryContainer = Color(0xFF001D32),
    secondary = Color(0xFF4F616E),
    secondaryContainer = Color(0xFFD2E5F5),
    tertiary = Color(0xFF66587B),
    background = Color(0xFFF8FAFD),
    surface = Color(0xFFF8FAFD),
    surfaceVariant = Color(0xFFDFE3E7),
)

@Composable
fun MaiDexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaiDexColors,
        content = content,
    )
}
