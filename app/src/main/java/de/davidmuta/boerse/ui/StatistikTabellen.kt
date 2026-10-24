package de.davidmuta.boerse.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Ist gerade das dunkle Design aktiv? */
@Composable
private fun istDunkel(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

// ---------------------------------------------------------------------------
// S2.2 Rücksetzer im Jahresverlauf
// ---------------------------------------------------------------------------

/** Rückgang, Anteil der Jahre in %, „im Schnitt alle …“ (S&P 500, 1928–2025). */
private val ruecksetzerDaten = listOf(
    Triple("−1 %", 100, "jedes Jahr"),
    Triple("−5 %", 94, "1,1 Jahre"),
    Triple("−10 %", 63, "1,6 Jahre"),
    Triple("−15 %", 40, "2,5 Jahre"),
    Triple("−20 %", 26, "4 Jahre"),
    Triple("−25 %", 20, "5 Jahre"),
    Triple("−30 %", 10, "10 Jahre"),
    Triple("−40 %", 6, "16 Jahre"),
    Triple("−50 %", 2, "49 Jahre")
)

/** Hellrot bis dunkelrot: (Hintergrund, Schrift) je Zeile. */
private val rotHell = listOf(
    0xFFFEF2F2 to 0xFF991B1B, 0xFFFEE2E2 to 0xFF991B1B, 0xFFFECACA to 0xFF7F1D1D,
    0xFFFCA5A5 to 0xFF7F1D1D, 0xFFF87171 to 0xFF450A0A, 0xFFDC2626 to 0xFFFFFFFF,
    0xFFB91C1C to 0xFFFFFFFF, 0xFF991B1B to 0xFFFFFFFF, 0xFF7F1D1D to 0xFFFFFFFF
)
private val rotDunkel = listOf(
    0xFF3B1D1D to 0xFFFECACA, 0xFF4A1F1F to 0xFFFECACA, 0xFF5C2121 to 0xFFFECACA,
    0xFF7F1D1D to 0xFFFECACA, 0xFF991B1B to 0xFFFFFFFF, 0xFFB91C1C to 0xFFFFFFFF,
    0xFFDC2626 to 0xFFFFFFFF, 0xFFEF4444 to 0xFF450A0A, 0xFFF87171 to 0xFF450A0A
)

/** Seite „Rücksetzer“. */
@Composable
fun RuecksetzerSeite() {
    val farben = MaterialTheme.colorScheme
    val rot = if (istDunkel()) rotDunkel else rotHell
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Wie oft fiel der S&P 500 innerhalb eines Kalenderjahres mindestens um diesen Wert? Zeitraum 1928–2025.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = farben.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Karte(Modifier.padding(horizontal = 12.dp)) {
            Row(
                modifier = Modifier.heightIn(min = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KopfText("Rückgang", Modifier.width(64.dp))
                KopfText("Anteil der Jahre", Modifier.weight(1f))
                KopfText("Im Schnitt alle", Modifier.width(76.dp), TextAlign.End)
            }
            ruecksetzerDaten.forEachIndexed { i, (rueckgang, anteil, alle) ->
                HorizontalDivider(color = farben.outlineVariant)
                Row(
                    modifier = Modifier.heightIn(min = 46.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Feld(
                        text = rueckgang,
                        hintergrund = Color(rot[i].first),
                        schrift = Color(rot[i].second),
                        modifier = Modifier.width(64.dp),
                        gewicht = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(farben.surfaceVariant)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(anteil / 100f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(farben.primary)
                            )
                        }
                        Text(
                            "$anteil %",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(40.dp)
                        )
                    }
                    Text(alle, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.width(76.dp))
                }
            }
        }
        Text(
            "Lesebeispiel: Einen Rückgang von mindestens 10 % gab es in 63 % aller Jahre – im Schnitt also etwa alle 1,6 Jahre.",
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = farben.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// S2.3 Saisonalität nach Monat
// ---------------------------------------------------------------------------

/** Ein Monat: Ø Rendite + Stufe, Im Plus + Stufe, bester / schlechtester Monat, Schwankung. */
private data class Monat(
    val name: String,
    val rendite: String, val renditeStufe: Int,
    val imPlus: String, val imPlusStufe: Int,
    val bester: String, val schlechtester: String,
    val schwankung: String
)

/** S&P 500, 1964–2024 (Quelle laut Entwurf: Topdown Charts, LSEG). Stufen 1 = negativ … 5 = sehr stark. */
private val saisonDaten = listOf(
    Monat("Jan", "+1,1 %", 4, "59 %", 3, "+13 %", "−9 %", "4,9 %"),
    Monat("Feb", "0,0 %", 2, "54 %", 2, "+7 %", "−11 %", "3,9 %"),
    Monat("Mär", "+1,1 %", 4, "66 %", 4, "+10 %", "−13 %", "3,8 %"),
    Monat("Apr", "+1,5 %", 5, "72 %", 5, "+13 %", "−9 %", "4,1 %"),
    Monat("Mai", "+0,3 %", 2, "61 %", 3, "+9 %", "−8 %", "3,6 %"),
    Monat("Jun", "+0,2 %", 2, "59 %", 3, "+7 %", "−9 %", "3,5 %"),
    Monat("Jul", "+0,8 %", 3, "54 %", 2, "+9 %", "−8 %", "4,1 %"),
    Monat("Aug", "+0,1 %", 2, "56 %", 2, "+12 %", "−15 %", "4,7 %"),
    Monat("Sep", "−0,7 %", 1, "46 %", 1, "+9 %", "−12 %", "4,4 %"),
    Monat("Okt", "+1,0 %", 4, "59 %", 3, "+16 %", "−22 %", "6,0 %"),
    Monat("Nov", "+1,6 %", 5, "69 %", 5, "+11 %", "−11 %", "4,4 %"),
    Monat("Dez", "+1,2 %", 4, "70 %", 5, "+11 %", "−9 %", "3,5 %")
)

/** Farbstufen 1–5: (Hintergrund, Schrift). Index 0 ist unbenutzt. */
private val stufenHell = listOf(
    0L to 0L,
    0xFFFCA5A5 to 0xFF7F1D1D, 0xFFFEE2E2 to 0xFF991B1B, 0xFFF1F5F9 to 0xFF0F172A,
    0xFFD1FAE5 to 0xFF065F46, 0xFF6EE7B7 to 0xFF064E3B
)
private val stufenDunkel = listOf(
    0L to 0L,
    0xFF7F1D1D to 0xFFFECACA, 0xFF3B1D1D to 0xFFFCA5A5, 0xFF242832 to 0xFFE5E7EB,
    0xFF12352A to 0xFFA7F3D0, 0xFF065F46 to 0xFFD1FAE5
)

/** Seite „Saisonalität“. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaisonSeite() {
    val farben = MaterialTheme.colorScheme
    val dunkel = istDunkel()
    val stufen = if (dunkel) stufenDunkel else stufenHell
    val bestFarbe = if (dunkel) Color(0xFF6EE7B7) else Color(0xFF047857)
    val schlechtFarbe = if (dunkel) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
    fun bg(stufe: Int) = Color(stufen[stufe].first)
    fun fg(stufe: Int) = Color(stufen[stufe].second)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Durchschnittliche Entwicklung des S&P 500 je Kalendermonat, 1964–2024.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = farben.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Kachel("Stärkster", Color(0xFF059669), "November", "Ø +1,6 %")
            Kachel("Schwächster", Color(0xFFDC2626), "September", "Ø −0,7 %")
            Kachel("Ausschläge", Color(0xFFD97706), "Oktober", "+16 % / −22 %")
        }
        Karte(Modifier.padding(horizontal = 10.dp)) {
            Row(
                modifier = Modifier.heightIn(min = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KopfText("Monat", Modifier.width(34.dp))
                KopfText("Ø Rendite", Modifier.weight(1f), TextAlign.Center)
                KopfText("Im Plus", Modifier.weight(1f), TextAlign.Center)
                KopfText("Spanne", Modifier.weight(1f), TextAlign.Center)
                KopfText("Schwank.", Modifier.weight(1f), TextAlign.Center)
            }
            saisonDaten.forEach { m ->
                val gewicht = if (m.name == "Okt") FontWeight.ExtraBold else FontWeight.Medium
                HorizontalDivider(color = farben.outlineVariant)
                Row(
                    modifier = Modifier.heightIn(min = 44.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(m.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                    Feld(m.rendite, bg(m.renditeStufe), fg(m.renditeStufe), Modifier.weight(1f), FontWeight.Bold)
                    Feld(m.imPlus, bg(m.imPlusStufe), fg(m.imPlusStufe), Modifier.weight(1f), FontWeight.SemiBold)
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(m.bester, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = gewicht, color = bestFarbe)
                        Text(m.schlechtester, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = gewicht, color = schlechtFarbe)
                    }
                    Text(
                        m.schwankung,
                        fontSize = 13.sp,
                        fontWeight = gewicht,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            listOf(5 to "sehr stark", 4 to "überdurchschnittlich", 3 to "mittel", 2 to "schwach", 1 to "negativ / unter 50 %")
                .forEach { (stufe, text) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(bg(stufe))
                                .border(1.dp, farben.outlineVariant, RoundedCornerShape(3.dp))
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(text, fontSize = 11.sp, color = farben.onSurfaceVariant)
                    }
                }
        }
        Text(
            "Ø Rendite = durchschnittliche Veränderung im Monat · Im Plus = Anteil der Jahre mit positivem Monat · " +
                "Spanne = bester und schlechtester Monatswert · Schwank. = Standardabweichung. Quelle: Topdown Charts, LSEG.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = farben.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Bausteine
// ---------------------------------------------------------------------------

@Composable
private fun Karte(innen: Modifier, inhalt: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(innen) { inhalt() }
    }
}

@Composable
private fun KopfText(text: String, modifier: Modifier, ausrichtung: TextAlign = TextAlign.Start) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = ausrichtung,
        maxLines = 1,
        modifier = modifier
    )
}

/** Farbiges Feld mit Zahl (z. B. „−10 %“). */
@Composable
private fun Feld(text: String, hintergrund: Color, schrift: Color, modifier: Modifier, gewicht: FontWeight) {
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(hintergrund),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 13.sp, fontWeight = gewicht, color = schrift, maxLines = 1)
    }
}

@Composable
private fun RowScope.Kachel(titel: String, punkt: Color, wert: String, zusatz: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.weight(1f)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(punkt)
                )
                Spacer(Modifier.width(5.dp))
                Text(titel, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(wert, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(zusatz, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}
