package de.davidmuta.boerse.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.davidmuta.boerse.data.CheckPunkt
import de.davidmuta.boerse.ui.theme.LocalZusatzFarben

/**
 * Seite „Handelsplan“: oben ein Umschalter zwischen dem festen Short-Put-Handelsplan
 * (aus dem PDF des Auftraggebers, Stand 24.10.2026) und der Checkliste zum Abhaken.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandelsplanSeite(
    vix: Double?,
    checkliste: List<CheckPunkt>,
    onUmschalten: (Long) -> Unit,
    onLoeschen: (Long) -> Unit,
    onHinzufuegen: (String) -> Unit,
    onZuruecksetzen: () -> Unit
) {
    var ansicht by rememberSaveable { mutableIntStateOf(0) }
    val erledigt = checkliste.count { it.erledigt }

    Column(Modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp)
        ) {
            val titel = listOf("Handelsplan", "Checkliste $erledigt/${checkliste.size}")
            titel.forEachIndexed { i, t ->
                SegmentedButton(
                    selected = ansicht == i,
                    onClick = { ansicht = i },
                    shape = SegmentedButtonDefaults.itemShape(index = i, count = titel.size),
                    icon = {}
                ) {
                    Text(t, fontSize = 14.sp, maxLines = 1)
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (ansicht == 0) {
                PlanText(vix = vix, onZurCheckliste = { ansicht = 1 })
            } else {
                Checkliste(checkliste, erledigt, onUmschalten, onLoeschen, onHinzufuegen, onZuruecksetzen)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Ansicht „Handelsplan“ – fester Text
// ---------------------------------------------------------------------------

private val strategieRegeln = listOf(
    "Basiswerte" to "Diskretionäre Auswahl von Aktien und ETFs mit liquiden Optionen",
    "Laufzeit bei Eröffnung" to "40–50 Kalendertage bis Verfall (DTE)",
    "Delta des verkauften Puts" to "10–20",
    "Gewinnmitnahme" to "Rückkauf bei 50 % der ursprünglich vereinnahmten Prämie",
    "Verlustausstieg" to "Rückkauf bei Nettoverlust von 2 × ursprünglicher Prämie; kein Rollen von Verlustpositionen",
    "Zeitmanagement" to "Spätestens bei 21 DTE Position überprüfen",
    "Earnings-Filter" to "Bei Aktien keine Earnings zwischen Einstieg und Verfall (einschließlich Verfallstag). " +
        "Bestätigte und erwartete Termine prüfen; bei unklarem Termin kein Einstieg. ETFs ohne eigene Earnings ausgenommen."
)

/** VIX von (einschließlich) … bis unter …, Stufe, max. Auslastung, bei 100.000 €. */
private data class VixStufe(val von: Double, val bis: Double, val stufe: String, val max: String, val euro: String)

private val vixStufen = listOf(
    VixStufe(0.0, 15.0, "Unter 15", "25 %", "25.000 €"),
    VixStufe(15.0, 20.0, "15 bis unter 20", "30 %", "30.000 €"),
    VixStufe(20.0, 30.0, "20 bis unter 30", "40 %", "40.000 €"),
    VixStufe(30.0, 40.0, "30 bis unter 40", "45 %", "45.000 €"),
    VixStufe(40.0, Double.MAX_VALUE, "Ab 40", "50 %", "50.000 €")
)

private enum class Ampel { GRUEN, GELB, ROT }

private data class AmpelZeile(val tag: String, val woche: String, val gesamt: String, val ampel: Ampel)

private val ampelZeilen = listOf(
    AmpelZeile("Grün", "Grün", "GRÜN – Freigabe", Ampel.GRUEN),
    AmpelZeile("Grün", "Rot", "GELB – keine Freigabe", Ampel.GELB),
    AmpelZeile("Rot", "Grün", "GELB – keine Freigabe", Ampel.GELB),
    AmpelZeile("Rot", "Rot", "ROT – keine Freigabe", Ampel.ROT)
)

private val indexZuordnung = listOf(
    "Aktie im Nasdaq 100" to "Nasdaq 100 (Vorrang bei Doppelzugehörigkeit)",
    "Sonst: Aktie im S&P 500" to "S&P 500",
    "Sonst: Aktie im Russell 2000" to "Russell 2000"
)

private val ablaufSchritte = listOf(
    "Optionsliquidität prüfen.",
    "Referenzindex bestimmen.",
    "Markt-, Sektor- und Aktienampel prüfen.",
    "Earnings-Kalender bis einschließlich Verfall kontrollieren.",
    "Laufzeit und Delta 10–20 prüfen.",
    "Initiale Buying Power (max. 3 %) und VIX-Gesamtgrenze prüfen.",
    "Trade eröffnen und Ausstiege festlegen."
)

/** Text mit fett gesetzten Stellen: **so** wird fett. */
private fun fett(text: String): AnnotatedString = buildAnnotatedString {
    text.split("**").forEachIndexed { i, teil ->
        if (i % 2 == 1) withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append(teil) } else append(teil)
    }
}

@Composable
private fun PlanText(vix: Double?, onZurCheckliste: () -> Unit) {
    val farben = MaterialTheme.colorScheme
    val zusatz = LocalZusatzFarben.current
    val dunkel = farben.background.luminance() < 0.5f

    // Kopf
    PlanKarte {
        Text("SHORT PUT", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = farben.primary)
        Text("Handelsplan", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Text("Kompakt und regelbasiert · Stand 24.10.2026", fontSize = 12.sp, color = farben.onSurfaceVariant)
    }

    // 1. Strategie und Einstieg
    PlanKarte(abstand = 0.dp) {
        Ueberschrift("1. Strategie und Einstieg")
        Spacer(Modifier.height(6.dp))
        strategieRegeln.forEach { (kriterium, regel) ->
            HorizontalDivider(color = farben.outlineVariant)
            Column(Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(kriterium, fontSize = 12.sp, color = farben.onSurfaceVariant)
                Text(regel, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }

    // 2. Positionsgröße und Gesamtauslastung
    PlanKarte {
        Ueberschrift("2. Positionsgröße und Gesamtauslastung")
        Fliesstext(fett("Initiale Buying Power je Position **maximal 3 %** des Depotwerts – bei 100.000 EUR maximal 3.000 EUR anfängliche Margin."))
        Tabelle {
            TabellenKopf(listOf("VIX" to 1.4f, "Max. gesamt" to 1f, "Bei 100.000 €" to 1f))
            vixStufen.forEach { s ->
                val aktuell = vix != null && vix >= s.von && vix < s.bis
                HorizontalDivider(color = farben.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (aktuell) farben.primary.copy(alpha = 0.12f) else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val gewicht = if (aktuell) FontWeight.Medium else FontWeight.Normal
                    Column(Modifier.weight(1.4f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(s.stufe, fontSize = 14.sp, fontWeight = gewicht)
                        if (aktuell && vix != null) {
                            Text(
                                "aktuell · VIX ${zahl(vix)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = farben.onPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(farben.primary)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(s.max, fontSize = 14.sp, fontWeight = gewicht, modifier = Modifier.weight(1f))
                    Text(s.euro, fontSize = 14.sp, fontWeight = gewicht, modifier = Modifier.weight(1f))
                }
            }
        }
        Text(
            "Die VIX-Stufen sind die vereinbarten Obergrenzen, keine verifizierte Originaltabelle von Tastytrade. Keine Zielauslastung.",
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = farben.onSurfaceVariant
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(farben.surfaceVariant)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Buying Power – kurz erklärt", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            listOf(
                "Buying Power ist die vom Broker für einen Trade benötigte Sicherheitsleistung (Margin).",
                "Initiale Buying Power = Sicherheitsleistung bei Eröffnung; sie kann später steigen.",
                "3.000 EUR Margin bedeuten nicht, dass maximal 3.000 EUR verloren werden können."
            ).forEach { Text("• $it", fontSize = 13.sp, lineHeight = 19.sp) }
        }
    }

    // 3. Ampelmodell
    PlanKarte {
        Ueberschrift("3. Ampelmodell: Grün ist Pflicht")
        Fliesstext(
            fett(
                "Für Gesamtmarkt, Sektor und Aktie gilt jeweils: Schlusskurs des letzten abgeschlossenen " +
                    "**Tagescharts über MA21** = Tagesampel grün; Schlusskurs des letzten abgeschlossenen " +
                    "**Wochencharts über MA21** = Wochenampel grün. Andernfalls ist die jeweilige Teilampel rot."
            )
        )
        val gelb = if (dunkel) Color(0xFF422006) to Color(0xFFFDE68A) else Color(0xFFFEF3C7) to Color(0xFF92400E)
        Tabelle {
            TabellenKopf(listOf("Tag" to 1f, "Woche" to 1f, "Gesamtampel" to 1.6f))
            ampelZeilen.forEach { z ->
                val (hintergrund, schrift) = when (z.ampel) {
                    Ampel.GRUEN -> zusatz.plusHintergrund to zusatz.plusText
                    Ampel.GELB -> gelb
                    Ampel.ROT -> zusatz.minusHintergrund to zusatz.minusText
                }
                HorizontalDivider(color = farben.outlineVariant)
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(z.tag, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(z.woche, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Box(Modifier.weight(1.6f)) {
                        Text(
                            z.gesamt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = schrift,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(hintergrund)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }

    // 4. Top-down-Freigabe
    PlanKarte {
        Ueberschrift("4. Top-down-Freigabe")
        Fliesstext(
            fett(
                "Gesamtmarkt **GRÜN** + Sektor **GRÜN** + Aktie **GRÜN** = Short Put grundsätzlich erlaubt. " +
                    "Jede gelbe oder rote Ebene sperrt neue Einstiege."
            )
        )
        Tabelle {
            TabellenKopf(listOf("Indexzuordnung" to 1f, "Marktampel" to 1f))
            indexZuordnung.forEach { (wenn, ampel) ->
                HorizontalDivider(color = farben.outlineVariant)
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(wenn, fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.weight(1f))
                    Text(ampel, fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }

    // 5. Ablauf vor jedem Trade
    PlanKarte {
        Ueberschrift("5. Ablauf vor jedem Trade")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ablaufSchritte.forEachIndexed { i, schritt ->
                Row {
                    Text("${i + 1}.", fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.width(22.dp))
                    Text(schritt, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
        FilledTonalButton(onClick = onZurCheckliste) {
            Text("Zur Checkliste")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Ansicht „Checkliste“
// ---------------------------------------------------------------------------

@Composable
private fun Checkliste(
    punkte: List<CheckPunkt>,
    erledigt: Int,
    onUmschalten: (Long) -> Unit,
    onLoeschen: (Long) -> Unit,
    onHinzufuegen: (String) -> Unit,
    onZuruecksetzen: () -> Unit
) {
    val farben = MaterialTheme.colorScheme
    val zusatz = LocalZusatzFarben.current
    var neuerPunkt by rememberSaveable { mutableStateOf("") }
    val hinzufuegen = {
        if (neuerPunkt.isNotBlank()) {
            onHinzufuegen(neuerPunkt)
            neuerPunkt = ""
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = farben.surface,
        border = BorderStroke(1.dp, farben.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Checkliste vor dem Trade", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        when {
                            punkte.isEmpty() -> "keine Punkte"
                            erledigt == punkte.size -> "Alle ${punkte.size} Punkte erledigt"
                            else -> "$erledigt von ${punkte.size} erledigt"
                        },
                        fontSize = 13.sp,
                        color = farben.onSurfaceVariant
                    )
                }
                TextButton(onClick = onZuruecksetzen) { Text("Alle zurücksetzen") }
            }
            // Fortschrittsbalken
            Box(
                Modifier
                    .padding(top = 6.dp, bottom = 8.dp, end = 12.dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(farben.surfaceVariant)
            ) {
                if (punkte.isNotEmpty() && erledigt > 0) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(erledigt.toFloat() / punkte.size)
                            .clip(RoundedCornerShape(3.dp))
                            .background(zusatz.gruen)
                    )
                }
            }
            if (punkte.isEmpty()) {
                HorizontalDivider(color = farben.outlineVariant)
                Text(
                    "Noch keine Punkte – unten einen Punkt hinzufügen.",
                    fontSize = 14.sp,
                    color = farben.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            punkte.forEach { p ->
                HorizontalDivider(color = farben.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .toggleable(value = p.erledigt, role = Role.Checkbox, onValueChange = { onUmschalten(p.id) })
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = p.erledigt, onCheckedChange = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            p.text,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            color = if (p.erledigt) farben.onSurfaceVariant else farben.onSurface,
                            textDecoration = if (p.erledigt) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                    IconButton(onClick = { onLoeschen(p.id) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Punkt „${p.text}“ löschen",
                            tint = farben.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = farben.surface,
        border = BorderStroke(1.dp, farben.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = neuerPunkt,
                onValueChange = { neuerPunkt = it },
                label = { Text("Neuer Punkt") },
                placeholder = { Text("z. B. Ordergröße nachrechnen") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { hinzufuegen() }),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = hinzufuegen, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hinzufügen")
            }
        }
    }

    Text(
        "Vorbelegt mit den 7 Schritten aus „Ablauf vor jedem Trade“. Punkte lassen sich abhaken, löschen und ergänzen; " +
            "der Stand bleibt gespeichert. „Alle zurücksetzen“ entfernt nur die Haken – für den nächsten Trade.",
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = farben.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

// ---------------------------------------------------------------------------
// Bausteine
// ---------------------------------------------------------------------------

@Composable
private fun PlanKarte(abstand: androidx.compose.ui.unit.Dp = 10.dp, inhalt: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(abstand), content = inhalt)
    }
}

@Composable
private fun Ueberschrift(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun Fliesstext(text: AnnotatedString) {
    Text(text, fontSize = 14.sp, lineHeight = 20.sp)
}

@Composable
private fun Tabelle(inhalt: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = inhalt)
    }
}

@Composable
private fun TabellenKopf(spalten: List<Pair<String, Float>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        spalten.forEach { (text, anteil) ->
            Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(anteil))
        }
    }
}
