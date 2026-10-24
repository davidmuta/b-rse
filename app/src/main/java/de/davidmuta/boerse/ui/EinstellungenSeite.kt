package de.davidmuta.boerse.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.davidmuta.boerse.R
import de.davidmuta.boerse.data.Aktualisierung
import de.davidmuta.boerse.data.Design
import de.davidmuta.boerse.data.Einstellungen
import de.davidmuta.boerse.data.Werte

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EinstellungenSeite(
    einstellungen: Einstellungen,
    onAktualisierung: (Aktualisierung) -> Unit,
    onDesign: (Design) -> Unit,
    onVerschieben: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Abschnitt(rememberVectorPainter(Icons.Filled.Refresh), "Kurse neu laden") {
            val auswahl = Aktualisierung.entries
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                auswahl.forEachIndexed { i, a ->
                    SegmentedButton(
                        selected = einstellungen.aktualisierung == a,
                        onClick = { onAktualisierung(a) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = auswahl.size),
                        icon = {}
                    ) {
                        Text(a.bezeichnung, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }
            Text(
                "Die Kurse sind ca. 15 Minuten verzögert. Häufigeres Laden bringt keine neueren Daten.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Abschnitt(painterResource(R.drawable.ic_design), "Design") {
            val auswahl = Design.entries
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                auswahl.forEachIndexed { i, d ->
                    SegmentedButton(
                        selected = einstellungen.design == d,
                        onClick = { onDesign(d) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = auswahl.size),
                        icon = {}
                    ) {
                        Text(d.bezeichnung, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }
        }

        Abschnitt(painterResource(R.drawable.ic_reihenfolge), "Reihenfolge auf der Startseite") {
            val liste = einstellungen.reihenfolge
            liste.forEachIndexed { i, kuerzel ->
                val wert = Werte.nachKuerzel(kuerzel) ?: return@forEachIndexed
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${i + 1}.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(24.dp)
                    )
                    Box(
                        Modifier
                            .width(6.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(wert.farbe)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(wert.kuerzel, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onVerschieben(kuerzel, -1) }, enabled = i > 0) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "$kuerzel nach oben")
                    }
                    IconButton(onClick = { onVerschieben(kuerzel, 1) }, enabled = i < liste.lastIndex) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "$kuerzel nach unten")
                    }
                }
            }
        }
    }
}

@Composable
private fun Abschnitt(symbol: Painter, titel: String, inhalt: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(symbol, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(titel, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            inhalt()
        }
    }
}
