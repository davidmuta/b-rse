package de.davidmuta.boerse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Farben, die Material 3 nicht von sich aus kennt (grün/rot für Kursänderungen usw.). */
data class ZusatzFarben(
    val plusHintergrund: Color,
    val plusText: Color,
    val minusHintergrund: Color,
    val minusText: Color,
    val gruen: Color,
    val lila: Color,
    val heute: Color,
    val spreadStreifen: Color
)

private val HellZusatz = ZusatzFarben(
    plusHintergrund = Color(0xFFDCFCE7),
    plusText = Color(0xFF166534),
    minusHintergrund = Color(0xFFFEE2E2),
    minusText = Color(0xFF991B1B),
    gruen = Color(0xFF0F766E),
    lila = Color(0xFF7C3AED),
    heute = Color(0xFFC2410C),
    spreadStreifen = Color(0xFF374151)
)

private val DunkelZusatz = ZusatzFarben(
    plusHintergrund = Color(0xFF14532D),
    plusText = Color(0xFFBBF7D0),
    minusHintergrund = Color(0xFF7F1D1D),
    minusText = Color(0xFFFECACA),
    gruen = Color(0xFF2DD4BF),
    lila = Color(0xFFA78BFA),
    heute = Color(0xFFFB923C),
    spreadStreifen = Color(0xFF9CA3AF)
)

val LocalZusatzFarben = staticCompositionLocalOf { HellZusatz }

private val HellFarben = lightColorScheme(
    primary = Color(0xFF1D4ED8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE4FB),
    onPrimaryContainer = Color(0xFF0B1B4D),
    secondaryContainer = Color(0xFFE3E8F0),
    onSecondaryContainer = Color(0xFF111827),
    background = Color(0xFFF3F5F9),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF1F3F7),
    onSurfaceVariant = Color(0xFF4B5563),
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    outline = Color(0xFF9CA3AF),
    outlineVariant = Color(0xFFE5E7EB)
)

private val DunkelFarben = darkColorScheme(
    primary = Color(0xFF93B4FF),
    onPrimary = Color(0xFF0B1B4D),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBE4FB),
    secondaryContainer = Color(0xFF2C3446),
    onSecondaryContainer = Color(0xFFE5E7EB),
    background = Color(0xFF0F1115),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF181B21),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF242832),
    onSurfaceVariant = Color(0xFFA3AAB5),
    surfaceContainerLow = Color(0xFF181B21),
    surfaceContainer = Color(0xFF181B21),
    surfaceContainerHigh = Color(0xFF1F232B),
    outline = Color(0xFF6B7280),
    outlineVariant = Color(0xFF2C313B)
)

@Composable
fun BoerseTheme(dunkel: Boolean, inhalt: @Composable () -> Unit) {
    CompositionLocalProvider(LocalZusatzFarben provides if (dunkel) DunkelZusatz else HellZusatz) {
        MaterialTheme(
            colorScheme = if (dunkel) DunkelFarben else HellFarben,
            content = inhalt
        )
    }
}
