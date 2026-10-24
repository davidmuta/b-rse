package de.davidmuta.boerse.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.davidmuta.boerse.R
import de.davidmuta.boerse.ui.theme.LocalZusatzFarben
import java.time.LocalDate

/** Statistik: Liste der verfügbaren Statistiken. */
@Composable
fun StatistikSeite(onChart: () -> Unit) {
    val zusatz = LocalZusatzFarben.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "chart") {
            Surface(
                onClick = onChart,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 72.dp)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(zusatz.lila.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_kurs),
                            contentDescription = null,
                            tint = zusatz.lila,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Chart Jahresverlauf S&P 500", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "Durchschnitt der letzten 20 Jahre",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item(key = "hinweis") {
            Text(
                "Weitere Statistiken folgen.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }
    }
}

/**
 * Durchschnittlicher Jahresverlauf des S&P 500 (2006–2025) in Prozent ab 1. Januar.
 * Nachgezeichnet nach EquityClock, Werte gerundet. Paare: (Monat als Kommazahl 0–12, Prozent).
 */
private val jahresverlauf: List<Pair<Double, Double>> = listOf(
    0.0 to 0.0, 0.1 to 0.4, 0.3 to 0.6, 0.5 to 0.4, 0.7 to 0.5, 0.9 to 0.2,
    1.0 to 0.4, 1.1 to 0.9, 1.3 to 1.0, 1.5 to 1.4, 1.65 to 1.9, 1.8 to 1.5,
    2.0 to 0.9, 2.15 to 0.4, 2.35 to 0.0, 2.5 to -0.2, 2.7 to 0.3, 2.9 to 0.6,
    3.0 to 1.0, 3.2 to 1.5, 3.4 to 2.0, 3.6 to 1.9, 3.8 to 2.4,
    4.0 to 2.6, 4.15 to 3.1, 4.3 to 3.5, 4.5 to 3.3, 4.7 to 3.2, 4.85 to 2.7,
    5.0 to 3.6, 5.2 to 3.8, 5.4 to 4.1, 5.6 to 3.8, 5.8 to 3.5, 5.95 to 3.4,
    6.05 to 4.2, 6.3 to 4.8, 6.5 to 5.5, 6.7 to 6.0, 6.85 to 6.4,
    7.0 to 6.1, 7.2 to 5.8, 7.4 to 6.0, 7.6 to 5.8, 7.8 to 6.1,
    8.0 to 6.2, 8.15 to 6.6, 8.35 to 6.2, 8.5 to 6.6, 8.65 to 7.5, 8.8 to 6.8, 8.95 to 6.2,
    9.05 to 5.9, 9.15 to 5.7, 9.35 to 6.5, 9.55 to 6.9, 9.75 to 6.8, 9.95 to 7.3,
    10.1 to 7.8, 10.3 to 8.5, 10.5 to 8.6, 10.7 to 8.0, 10.85 to 8.2,
    11.0 to 9.0, 11.15 to 9.6, 11.35 to 9.7, 11.55 to 9.6, 11.7 to 10.0, 11.85 to 10.6, 12.0 to 10.4
)

private const val QUELLE_EQUITYCLOCK =
    "https://equityclock.com/2010/12/15/charts/sp-500-index-gspc-seasonal-chart/"

/** Wert der Kurve an einer Stelle des Jahres (lineare Verbindung zwischen den Punkten). */
private fun wertBei(monat: Double): Double {
    val danach = jahresverlauf.indexOfFirst { it.first >= monat }
    if (danach <= 0) return jahresverlauf.first().second
    val (m0, v0) = jahresverlauf[danach - 1]
    val (m1, v1) = jahresverlauf[danach]
    return v0 + (v1 - v0) * (monat - m0) / (m1 - m0)
}

/** Seite „Chart Jahresverlauf S&P 500“. */
@Composable
fun ChartJahresverlaufSeite() {
    val context = LocalContext.current
    val zusatz = LocalZusatzFarben.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(Modifier.padding(horizontal = 4.dp)) {
                    Text("So verläuft ein Börsenjahr im Schnitt", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "S&P 500, Durchschnitt 2006–2025, Wertentwicklung ab 1. Januar",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                JahresverlaufDiagramm(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(326f / 232f)
                        .semantics {
                            contentDescription = "Durchschnittlicher Jahresverlauf des S&P 500: von 0 Prozent im Januar " +
                                "auf rund 10 Prozent im Dezember, Rücksetzer Mitte Februar bis Mitte März, " +
                                "stärkster Anstieg ab Oktober"
                        }
                )
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendenEintrag("Ø-Verlauf") {
                        Box(
                            Modifier
                                .size(width = 14.dp, height = 3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    LegendenEintrag("heute") {
                        Box(
                            Modifier
                                .size(10.dp)
                                .border(2.dp, zusatz.heute, CircleShape)
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendenEintrag("schwache Phase") {
                        Box(
                            Modifier
                                .size(width = 12.dp, height = 10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(zusatz.minusHintergrund)
                        )
                    }
                    LegendenEintrag("starke Phase") {
                        Box(
                            Modifier
                                .size(width = 12.dp, height = 10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(zusatz.plusHintergrund)
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Kennzahl(
                    wert = "+10 %",
                    titel = "Ganzes Jahr",
                    text = "Im Schnitt rund 10 % Plus von Januar bis Dezember",
                    hintergrund = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    schrift = MaterialTheme.colorScheme.primary,
                    trennlinie = false
                )
                Kennzahl(
                    wert = "−2 %",
                    titel = "Mitte Februar bis Mitte März",
                    text = "Schwächste Phase des Jahres",
                    hintergrund = zusatz.minusHintergrund,
                    schrift = zusatz.minusText,
                    trennlinie = true
                )
                Kennzahl(
                    wert = "+4,5 %",
                    titel = "Oktober bis Dezember",
                    text = "Stärkste Phase des Jahres",
                    hintergrund = zusatz.plusHintergrund,
                    schrift = zusatz.plusText,
                    trennlinie = true
                )
            }
        }

        Text(
            "Nachgezeichnet nach EquityClock (20-Jahres-Durchschnitt bis 31.12.2025), Werte gerundet. " +
                "Kein Blick in die Zukunft: Einzelne Jahre weichen stark ab.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        TextButton(onClick = { oeffneAdresse(context, QUELLE_EQUITYCLOCK) }) {
            Text("Original ansehen")
        }
    }
}

@Composable
private fun LegendenEintrag(text: String, zeichen: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        zeichen()
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Kennzahl(
    wert: String,
    titel: String,
    text: String,
    hintergrund: Color,
    schrift: Color,
    trennlinie: Boolean
) {
    if (trennlinie) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            wert,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = schrift,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 64.dp)
                .clip(RoundedCornerShape(50))
                .background(hintergrund)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(titel, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun JahresverlaufDiagramm(modifier: Modifier) {
    val zusatz = LocalZusatzFarben.current
    val farben = MaterialTheme.colorScheme
    val textMesser = rememberTextMeasurer()
    val achsenStil = TextStyle(fontSize = 9.sp, color = farben.onSurfaceVariant)
    val monatStil = TextStyle(fontSize = 10.sp, color = farben.onSurfaceVariant)
    val heuteStil = TextStyle(fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = zusatz.heute)
    val endStil = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = farben.primary)
    val heute = remember {
        val d = LocalDate.now()
        (d.dayOfYear - 1).toDouble() / d.lengthOfYear() * 12.0
    }
    val heuteWert = remember(heute) { wertBei(heute) }

    Canvas(modifier) {
        val s = size.width / 326f
        val x0 = 36f * s
        val x1 = 318f * s
        val yt = 14f * s
        val yb = 196f * s
        fun x(m: Double): Float = (x0 + (m / 12.0) * (x1 - x0)).toFloat()
        fun y(v: Double): Float = (yt + (12.0 - v) / 14.0 * (yb - yt)).toFloat()

        // Schwache und starke Phase
        drawRect(
            color = zusatz.minusHintergrund.copy(alpha = 0.55f),
            topLeft = Offset(x(1.65), yt),
            size = Size(x(2.5) - x(1.65), yb - yt)
        )
        drawRect(
            color = zusatz.plusHintergrund.copy(alpha = 0.55f),
            topLeft = Offset(x(9.15), yt),
            size = Size(x(12.0) - x(9.15), yb - yt)
        )

        // Raster mit Prozent-Beschriftung
        for (v in -2..12 step 2) {
            val yy = y(v.toDouble())
            drawLine(
                color = if (v == 0) farben.outline else farben.outlineVariant,
                start = Offset(x0, yy),
                end = Offset(x1, yy),
                strokeWidth = (if (v == 0) 1.2f else 1f) * s
            )
            val beschriftung = when {
                v > 0 -> "+$v %"
                v < 0 -> "−${-v} %"
                else -> "0 %"
            }
            val text = textMesser.measure(beschriftung, achsenStil)
            drawText(text, topLeft = Offset(x0 - 6f * s - text.size.width, yy - text.size.height / 2f))
        }

        // Monate
        "JFMAMJJASOND".forEachIndexed { i, buchstabe ->
            val text = textMesser.measure(buchstabe.toString(), monatStil)
            drawText(text, topLeft = Offset(x(i + 0.5) - text.size.width / 2f, yb + 5f * s))
        }

        // Fläche und Linie
        val linie = Path()
        val flaeche = Path()
        jahresverlauf.forEachIndexed { i, (m, v) ->
            if (i == 0) {
                linie.moveTo(x(m), y(v))
                flaeche.moveTo(x(m), y(0.0))
            }
            linie.lineTo(x(m), y(v))
            flaeche.lineTo(x(m), y(v))
        }
        flaeche.lineTo(x(12.0), y(0.0))
        flaeche.close()
        drawPath(flaeche, color = farben.primary.copy(alpha = 0.10f))
        drawPath(
            linie,
            color = farben.primary,
            style = Stroke(width = 2.2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Heute
        val xh = x(heute)
        drawLine(
            color = zusatz.heute,
            start = Offset(xh, yt),
            end = Offset(xh, yb),
            strokeWidth = 1.2f * s,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f * s, 3f * s))
        )
        drawCircle(color = farben.surface, radius = 4f * s, center = Offset(xh, y(heuteWert)))
        drawCircle(color = zusatz.heute, radius = 4f * s, center = Offset(xh, y(heuteWert)), style = Stroke(2f * s))
        val heuteText = textMesser.measure("heute", heuteStil)
        val heuteX = if (xh - 4f * s - heuteText.size.width > x0) xh - 4f * s - heuteText.size.width else xh + 4f * s
        drawText(heuteText, topLeft = Offset(heuteX, yt + 2f * s))

        // Endwert
        drawCircle(color = farben.primary, radius = 3.5f * s, center = Offset(x(12.0), y(10.4)))
        val endText = textMesser.measure("+10,4 %", endStil)
        drawText(endText, topLeft = Offset(x(12.0) - 6f * s - endText.size.width, y(10.4) - 6f * s - endText.size.height))
    }
}
