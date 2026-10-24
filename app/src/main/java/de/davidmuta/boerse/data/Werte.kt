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
    val farbe: Color
)

/** Alle Werte der ersten Version. Weitere Werte kommen später hier dazu. */
object Werte {
    val alle: List<WertInfo> = listOf(
        WertInfo("VIX", "Volatilität S&P 500, 30 Tage", "_VIX", "CBOE:VIX", Color(0xFF1D4ED8)),
        WertInfo("VVIX", "Volatilität des VIX", "_VVIX", "CBOE:VVIX", Color(0xFF7C3AED)),
        WertInfo("VIX3M", "Volatilität S&P 500, 3 Monate", "_VIX3M", "CBOE:VIX3M", Color(0xFF0F766E)),
        WertInfo("SPY", "ETF auf den S&P 500", "SPY", "AMEX:SPY", Color(0xFFC2410C))
    )

    fun nachKuerzel(kuerzel: String): WertInfo? = alle.firstOrNull { it.kuerzel == kuerzel }
}
