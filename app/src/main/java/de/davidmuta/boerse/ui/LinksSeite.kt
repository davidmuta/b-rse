package de.davidmuta.boerse.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.davidmuta.boerse.R
import de.davidmuta.boerse.data.LinkEintrag
import de.davidmuta.boerse.data.LinkGruppe
import de.davidmuta.boerse.data.LinkListe
import de.davidmuta.boerse.data.LinkSymbol
import de.davidmuta.boerse.ui.theme.LocalZusatzFarben

/** Links: Übersicht der drei Gruppen (Morgenroutine, Trading, Borrow-Fee's). */
@Composable
fun LinksSeite(onGruppe: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(LinkListe.gruppen) { nummer, gruppe ->
            Eintrag(
                symbol = symbolBild(gruppe.symbol),
                symbolFarbe = symbolFarbe(gruppe.symbol),
                titel = gruppe.titel,
                untertitel = if (gruppe.links.size == 1) "1 Link" else "${gruppe.links.size} Links",
                rechts = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { onGruppe(nummer) }
            )
        }
    }
}

/** Die Links einer Gruppe. Ein Tipp öffnet die Seite im Browser. */
@Composable
fun LinkGruppeSeite(gruppe: LinkGruppe) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(gruppe.links) { _, link ->
            LinkKarte(link) { oeffneAdresse(context, link.adresse) }
        }
    }
}

@Composable
private fun LinkKarte(link: LinkEintrag, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(link.titel, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    seitenName(link.adresse),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                painterResource(R.drawable.ic_oeffnen),
                contentDescription = "Im Browser öffnen",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun Eintrag(
    symbol: Int,
    symbolFarbe: Color,
    titel: String,
    untertitel: String,
    rechts: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 72.dp)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(symbolFarbe.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(symbol), contentDescription = null, tint = symbolFarbe, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(titel, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(untertitel, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            rechts()
        }
    }
}

private fun symbolBild(s: LinkSymbol): Int = when (s) {
    LinkSymbol.SONNE -> R.drawable.ic_sonne
    LinkSymbol.KURS -> R.drawable.ic_kurs
    LinkSymbol.PROZENT -> R.drawable.ic_prozent
}

@Composable
private fun symbolFarbe(s: LinkSymbol): Color = when (s) {
    LinkSymbol.SONNE -> Color(0xFFD97706)
    LinkSymbol.KURS -> MaterialTheme.colorScheme.primary
    LinkSymbol.PROZENT -> LocalZusatzFarben.current.gruen
}

/** „https://www.finviz.com/groups“ → „finviz.com“ */
private fun seitenName(adresse: String): String {
    val host = Uri.parse(adresse).host ?: return adresse
    return host.removePrefix("www.").removePrefix("edition.")
}

/** Öffnet eine Internetadresse im Browser (oder in der passenden App). */
fun oeffneAdresse(context: Context, adresse: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(adresse)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Keine App zum Öffnen gefunden", Toast.LENGTH_SHORT).show()
    }
}
