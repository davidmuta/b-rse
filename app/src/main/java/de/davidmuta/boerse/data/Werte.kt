package de.davidmuta.boerse.data

import androidx.compose.ui.graphics.Color

/** Beschreibt einen Wert, den die App anzeigt. */
data class WertInfo(
    /** Anzeigename, z. B. "VIX" */
    val kuerzel: String,
    /** Kurze Erklärung unter dem Namen */
    val beschreibung: String,
    /** Kürzel bei der Datenquelle Cboe, z. B. "_VIX" */
    val cboeSymbol: String,
    /** Kürzel bei TradingView, z. B. "CBOE:VIX" */
    val tradingViewSymbol: String,
    /** Farbe des Streifens links auf der Karte */
    val farbe: Color,
    /** Eigene Karte auf der Startseite? (VIX3M nicht – wird nur für den Spread abgerufen) */
    val aufStartseite: Boolean = true
)

/** Alle Werte der ersten Version. Weitere Werte kommen später hier dazu. */
object Werte {
    /** Alle Werte, die bei Cboe abgerufen werden. */
    val alle: List<WertInfo> = listOf(
        WertInfo("VIX", "Volatilität S&P 500, 30 Tage", "_VIX", "CBOE:VIX", Color(0xFF1D4ED8)),
        WertInfo("VVIX", "Volatilität des VIX", "_VVIX", "CBOE:VVIX", Color(0xFF7C3AED)),
        WertInfo("VIX3M", "Volatilität S&P 500, 3 Monate", "_VIX3M", "CBOE:VIX3M", Color(0xFF0F766E), aufStartseite = false),
        WertInfo("SPY", "ETF auf den S&P 500", "SPY", "AMEX:SPY", Color(0xFFC2410C))
    )

    /** Die Werte mit eigener Karte auf der Startseite (VIX, VVIX, SPY). */
    val startseite: List<WertInfo> = alle.filter { it.aufStartseite }

    fun nachKuerzel(kuerzel: String): WertInfo? = alle.firstOrNull { it.kuerzel == kuerzel }
}
