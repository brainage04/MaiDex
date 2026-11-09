package io.github.brainage04.maidex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
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

private val DarkColors = darkColorScheme(
    primary = Color(0xFF94CCFF),
    onPrimary = Color(0xFF003353),
    primaryContainer = Color(0xFF004A75),
    onPrimaryContainer = Color(0xFFCDE5FF),
    secondary = Color(0xFFB6C9D8),
    onSecondary = Color(0xFF20333E),
    secondaryContainer = Color(0xFF374955),
    onSecondaryContainer = Color(0xFFD2E5F5),
    tertiary = Color(0xFFD1BFE7),
    onTertiary = Color(0xFF372A4A),
    tertiaryContainer = Color(0xFF4E4162),
    onTertiaryContainer = Color(0xFFEDDCFF),
    background = Color(0xFF101417),
    onBackground = Color(0xFFE0E3E6),
    surface = Color(0xFF101417),
    onSurface = Color(0xFFE0E3E6),
    surfaceVariant = Color(0xFF41474D),
    onSurfaceVariant = Color(0xFFC1C7CE),
    surfaceContainerLowest = Color(0xFF0B0F12),
    surfaceContainerLow = Color(0xFF181C20),
    surfaceContainer = Color(0xFF1C2024),
    surfaceContainerHigh = Color(0xFF262A2E),
    surfaceContainerHighest = Color(0xFF313539),
    outline = Color(0xFF8B9198),
    outlineVariant = Color(0xFF41474D),
)

/**
 * Colours the Material scheme has no slot for: the DX NET styled chart cards and score panels, the
 * medal tables and informational call-outs. Each has a light and a dark value.
 */
@Immutable
data class MaiDexExtendedColors(
    val chartCard: Color,
    val chartMeta: Color,
    val panel: Color,
    val panelBorder: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkMuted: Color,
    val scoreBorder: Color,
    val scoreGold: Color,
    val scoreBlue: Color,
    val scoreRed: Color,
    val scoreNeutral: Color,
    val positive: Color,
    val info: Color,
    val infoContainer: Color,
    val infoBorder: Color,
    val medalComboRow: Color,
    val medalComboLabel: Color,
    val medalPerfectRow: Color,
    val medalPerfectLabel: Color,
    val medalSyncRow: Color,
    val medalSyncLabel: Color,
    val medalOtherRow: Color,
    val medalOtherLabel: Color,
)

private val LightExtended = MaiDexExtendedColors(
    chartCard = Color(0xFFF8FAFB),
    chartMeta = Color(0xFF17344E),
    panel = Color(0xFFEEF1F3),
    panelBorder = Color(0xFFAEB9C0),
    ink = Color(0xFF172126),
    inkSoft = Color(0xFF26373F),
    inkMuted = Color(0xFF4D5A61),
    scoreBorder = Color(0xFF8D999F),
    scoreGold = Color(0xFFFFF1C7),
    scoreBlue = Color(0xFFDDEBFF),
    scoreRed = Color(0xFFFFE1E8),
    scoreNeutral = Color.White.copy(alpha = 0.9f),
    positive = Color(0xFF16804A),
    info = Color(0xFF087BA8),
    infoContainer = Color(0xFFE5F6FD),
    infoBorder = Color(0xFF73C8E8),
    medalComboRow = Color(0xFFE6F1FF),
    medalComboLabel = Color(0xFF086AA7),
    medalPerfectRow = Color(0xFFFFF0D8),
    medalPerfectLabel = Color(0xFFA65A00),
    medalSyncRow = Color(0xFFE0F7F4),
    medalSyncLabel = Color(0xFF00796B),
    medalOtherRow = Color(0xFFF2E8FF),
    medalOtherLabel = Color(0xFF6A36A8),
)

private val DarkExtended = MaiDexExtendedColors(
    chartCard = Color(0xFF1B2126),
    chartMeta = Color(0xFFB8D3EA),
    panel = Color(0xFF272E34),
    panelBorder = Color(0xFF4A565E),
    ink = Color(0xFFE6EDF1),
    inkSoft = Color(0xFFD0DAE0),
    inkMuted = Color(0xFFA9B6BE),
    scoreBorder = Color(0xFF5C6970),
    scoreGold = Color(0xFF433915),
    scoreBlue = Color(0xFF1C3350),
    scoreRed = Color(0xFF47202A),
    scoreNeutral = Color(0xFF272E34),
    positive = Color(0xFF6FD69B),
    info = Color(0xFF7FD0F2),
    infoContainer = Color(0xFF12313D),
    infoBorder = Color(0xFF2D6F88),
    medalComboRow = Color(0xFF1A3049),
    medalComboLabel = Color(0xFF9CCBFF),
    medalPerfectRow = Color(0xFF3F2E12),
    medalPerfectLabel = Color(0xFFFFC37A),
    medalSyncRow = Color(0xFF123A35),
    medalSyncLabel = Color(0xFF7FDCCB),
    medalOtherRow = Color(0xFF2F2242),
    medalOtherLabel = Color(0xFFD6B8FF),
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

object MaiDexTheme {
    val colors: MaiDexExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}

@Composable
fun MaiDexTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtendedColors provides if (darkTheme) DarkExtended else LightExtended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            content = content,
        )
    }
}
