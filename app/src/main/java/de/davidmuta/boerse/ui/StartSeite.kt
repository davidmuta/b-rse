package de.davidmuta.boerse.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.davidmuta.boerse.R
import de.davidmuta.boerse.data.Kurs
import de.davidmuta.boerse.data.KursStand
import de.davidmuta.boerse.data.WertInfo
import de.davidmuta.boerse.data.Werte
import de.davidmuta.boerse.ui.theme.LocalZusatzFarben
import de.davidmuta.boerse.viewmodel.KursZustand

/** Startseite: eine Karte pro Wert, darunter der Spread, unten die Datenquelle. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartSeite(
    zustand: KursZustand,
    reihenfolge: List<String>,
    onAktualisieren: () -> Unit
) {
    val context = LocalContext.current
    val werte = reihenfolge.mapNotNull { Werte.nachKuerzel(it) }
    val kurse = zustand.stand?.kurse.orEmpty()

    Column(Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = zustand.laedt,
            onRefresh = onAktualisieren,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                zustand.fehler?.let { text ->
                    item(key = "fehler") { FehlerHinweis(text) }
                }
                items(werte, key = { it.kuerzel }) { wert ->
                    WertKarte(
                        wert = wert,
                        kurs = kurse[wert.kuerzel],
                        onTradingView = { oeffneTradingView(context, wert) }
                    )
                }
                item(key = "spread") {
                    SpreadKarte(vix = kurse["VIX"], vix3m = kurse["VIX3M"])
                }
            }
        }
        QuellenLeiste(zustand.stand)
    }
}

@Composable
private fun WertKarte(wert: WertInfo, kurs: Kurs?, onTradingView: () -> Unit) {
    val zusatz = LocalZusatzFarben.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(wert.farbe)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        wert.kuerzel,
                        fontSize = if (wert.kuerzel.length > 4) 11.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = wert.farbe
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(wert.kuerzel, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        wert.beschreibung,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        kurs?.let { zahl(it.preis) } ?: "–",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (kurs != null) {
                        val plus = kurs.aenderungProzent >= 0
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (plus) zusatz.plusHintergrund else zusatz.minusHintergrund
                        ) {
                            Text(
                                prozent(kurs.aenderungProzent),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (plus) zusatz.plusText else zusatz.minusText
                            )
                        }
                    }
                }
                IconButton(onClick = onTradingView) {
                    Icon(
                        painterResource(R.drawable.ic_oeffnen),
                        contentDescription = "${wert.kuerzel} in TradingView öffnen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpreadKarte(vix: Kurs?, vix3m: Kurs?) {
    val zusatz = LocalZusatzFarben.current
    val verhaeltnis = if (vix != null && vix3m != null && vix3m.preis != 0.0) vix.preis / vix3m.preis else null
    val differenz = if (vix != null && vix3m != null) vix.preis - vix3m.preis else null

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(zusatz.spreadStreifen)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Spread VIX / VIX3M",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Volatilitätskurve",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    SpreadWert("Verhältnis", verhaeltnis?.let { zahl(it) } ?: "–", Modifier.weight(1f))
                    SpreadWert("Differenz", differenz?.let { zahlMitVorzeichen(it) } ?: "–", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SpreadWert(bezeichnung: String, wert: String, modifier: Modifier) {
    Column(modifier) {
        Text(bezeichnung, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(wert, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FehlerHinweis(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(20.dp)
            )
            Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Leiste über der Fußleiste: Datenquelle, mit ausklappbaren Details. */
@Composable
private fun QuellenLeiste(stand: KursStand?) {
    var offen by remember { mutableStateOf(false) }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append("Datenquelle") }
                        append(" · Cboe, ca. 15 Min. verzögert")
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { offen = !offen }) {
                    Icon(
                        if (offen) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                        contentDescription = if (offen) "Details ausblenden" else "Details einblenden"
                    )
                }
            }
            AnimatedVisibility(visible = offen) {
                val handel = stand?.kurse?.get("VIX")?.letzterHandel
                Column(
                    modifier = Modifier.padding(end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DetailZeile("Letzter Abruf", stand?.let { zeitpunkt(it.abgerufenUm) } ?: "noch keiner")
                    DetailZeile("Letzter Börsenkurs", handel?.let { boersenzeit(it) } ?: "–")
                    Text(
                        "Zum Neuladen die Startseite nach unten ziehen.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailZeile(bezeichnung: String, wert: String) {
    Row {
        Text(bezeichnung, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(wert, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** Öffnet den Wert in TradingView: in der App, falls installiert, sonst im Browser. */
private fun oeffneTradingView(context: Context, wert: WertInfo) {
    val adresse = "https://www.tradingview.com/chart/?symbol=" + Uri.encode(wert.tradingViewSymbol)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(adresse))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Keine App zum Öffnen gefunden", Toast.LENGTH_SHORT).show()
    }
}
