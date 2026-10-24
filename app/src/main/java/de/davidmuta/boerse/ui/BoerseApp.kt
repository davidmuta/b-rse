package de.davidmuta.boerse.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.davidmuta.boerse.R
import de.davidmuta.boerse.data.KursStand
import de.davidmuta.boerse.ui.theme.LocalZusatzFarben
import de.davidmuta.boerse.viewmodel.BoerseViewModel

/** Die Bildschirme der App. */
sealed interface Seite {
    data object Start : Seite
    data object Notizen : Seite
    data class NotizBearbeiten(val id: Long?) : Seite
    data object Einstellungen : Seite
    data object Links : Seite
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoerseApp(vm: BoerseViewModel) {
    val kursZustand by vm.kurse.collectAsState()
    val einstellungen by vm.einstellungen.collectAsState()
    val notizen by vm.notizen.collectAsState()

    var seite by remember { mutableStateOf<Seite>(Seite.Start) }
    var menueOffen by remember { mutableStateOf(false) }

    // Zurück-Taste: Unterseiten führen zurück ins Menü, Notizen zur Startseite.
    BackHandler(enabled = seite != Seite.Start) {
        when (seite) {
            Seite.Einstellungen, Seite.Links -> {
                seite = Seite.Start
                menueOffen = true
            }
            else -> seite = Seite.Start
        }
    }

    val zurueckInsMenue = {
        seite = Seite.Start
        menueOffen = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            when (seite) {
                Seite.Start, Seite.Notizen -> Kopfbereich(kursZustand.stand)
                Seite.Einstellungen -> UnterseitenKopf("Einstellungen", zurueckInsMenue)
                Seite.Links -> UnterseitenKopf("Links", zurueckInsMenue)
                is Seite.NotizBearbeiten -> Unit
            }
        },
        bottomBar = {
            if (seite !is Seite.NotizBearbeiten) {
                Fussleiste(
                    seite = seite,
                    onStartseite = { seite = Seite.Start },
                    onNotizen = { seite = Seite.Notizen },
                    onMenue = { menueOffen = true }
                )
            }
        },
        floatingActionButton = {
            if (seite == Seite.Notizen) {
                ExtendedFloatingActionButton(
                    onClick = { seite = Seite.NotizBearbeiten(null) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Neue Notiz") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innen ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innen)
        ) {
            when (val s = seite) {
                Seite.Start -> StartSeite(
                    zustand = kursZustand,
                    reihenfolge = einstellungen.reihenfolge,
                    onAktualisieren = vm::aktualisierePerHand
                )
                Seite.Notizen -> NotizenSeite(
                    notizen = notizen,
                    onOeffnen = { id -> seite = Seite.NotizBearbeiten(id) }
                )
                is Seite.NotizBearbeiten -> key(s.id) {
                    NotizBearbeitenSeite(
                        notiz = s.id?.let { vm.notiz(it) },
                        onFertig = { titel, text ->
                            vm.speichereNotiz(s.id, titel, text)
                            seite = Seite.Notizen
                        },
                        onLoeschen = {
                            s.id?.let { vm.loescheNotiz(it) }
                            seite = Seite.Notizen
                        }
                    )
                }
                Seite.Einstellungen -> EinstellungenSeite(
                    einstellungen = einstellungen,
                    onAktualisierung = vm::setzeAktualisierung,
                    onDesign = vm::setzeDesign,
                    onVerschieben = vm::verschiebe
                )
                Seite.Links -> LinksSeite()
            }
        }
    }

    if (menueOffen) {
        Menue(
            onSchliessen = { menueOffen = false },
            onEinstellungen = {
                menueOffen = false
                seite = Seite.Einstellungen
            },
            onLinks = {
                menueOffen = false
                seite = Seite.Links
            }
        )
    }
}

/** Schmaler Kopfbereich wie in der Vorlage: Logo, Name, Kursstand, Datum. */
@Composable
fun Kopfbereich(stand: KursStand?) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Börse", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.size(4.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (stand == null) "Kurse · noch nicht geladen"
                                else "Kurse · ${zeitpunkt(stand.abgerufenUm)} · ca. 15 Min. verzögert",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(datumHeute(), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(
                        wochentagHeute(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/** Kopf für Unterseiten aus dem Menü: Pfeil zurück und Titel. */
@Composable
fun UnterseitenKopf(titel: String, onZurueck: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onZurueck) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück zum Menü")
                }
                Text(titel, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/** Fußleiste: Startseite links, Notizen in der Mitte, Menü rechts. */
@Composable
fun Fussleiste(
    seite: Seite,
    onStartseite: () -> Unit,
    onNotizen: () -> Unit,
    onMenue: () -> Unit
) {
    val farben = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onSurface,
        selectedTextColor = MaterialTheme.colorScheme.onSurface,
        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = seite == Seite.Start,
                onClick = onStartseite,
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                label = { Text("Startseite") },
                colors = farben
            )
            NavigationBarItem(
                selected = seite == Seite.Notizen,
                onClick = onNotizen,
                icon = { Icon(painterResource(R.drawable.ic_notizen), contentDescription = null) },
                label = { Text("Notizen") },
                colors = farben
            )
            NavigationBarItem(
                selected = seite == Seite.Einstellungen || seite == Seite.Links,
                onClick = onMenue,
                icon = { Icon(Icons.Filled.Menu, contentDescription = null) },
                label = { Text("Menü") },
                colors = farben
            )
        }
    }
}

/** Menü als Fenster von unten mit „Einstellungen“ und „Links“. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menue(
    onSchliessen: () -> Unit,
    onEinstellungen: () -> Unit,
    onLinks: () -> Unit
) {
    val zusatz = LocalZusatzFarben.current
    ModalBottomSheet(
        onDismissRequest = onSchliessen,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 28.dp)) {
            Text(
                "Menü",
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            MenueEintrag(
                symbol = rememberVectorPainter(Icons.Filled.Settings),
                symbolFarbe = MaterialTheme.colorScheme.primary,
                titel = "Einstellungen",
                untertitel = "Aktualisierung, Design, Reihenfolge",
                onClick = onEinstellungen
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(start = 66.dp, end = 8.dp)
            )
            MenueEintrag(
                symbol = painterResource(R.drawable.ic_link),
                symbolFarbe = zusatz.gruen,
                titel = "Links",
                untertitel = "Gespeicherte Webseiten öffnen",
                onClick = onLinks
            )
        }
    }
}

@Composable
private fun MenueEintrag(
    symbol: Painter,
    symbolFarbe: Color,
    titel: String,
    untertitel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = 8.dp, vertical = 10.dp),
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
            Icon(symbol, contentDescription = null, tint = symbolFarbe, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(titel, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(untertitel, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
