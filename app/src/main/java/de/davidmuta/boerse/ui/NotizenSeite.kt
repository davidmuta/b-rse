package de.davidmuta.boerse.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.davidmuta.boerse.data.Notiz

/** Liste aller Notizen, neueste zuerst. */
@Composable
fun NotizenSeite(notizen: List<Notiz>, onOeffnen: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "kopf") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp, end = 2.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notizen", fontSize = 19.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(
                    when (notizen.size) {
                        0 -> "noch keine"
                        1 -> "1 Notiz"
                        else -> "${notizen.size} Notizen · neueste zuerst"
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (notizen.isEmpty()) {
            item(key = "leer") {
                Text(
                    "Tippe unten rechts auf „Neue Notiz“, um deine erste Notiz anzulegen.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp, start = 2.dp, end = 2.dp)
                )
            }
        }
        items(notizen, key = { it.id }) { notiz ->
            Surface(
                onClick = { onOeffnen(notiz.id) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            notiz.titel.ifBlank { "Ohne Titel" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            notizDatum(notiz.erstelltUm),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (notiz.text.isNotBlank()) {
                        Text(
                            notiz.text,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/** Eine Notiz anlegen oder bearbeiten. Zurück speichert automatisch. */
@Composable
fun NotizBearbeitenSeite(
    notiz: Notiz?,
    onFertig: (titel: String, text: String) -> Unit,
    onLoeschen: () -> Unit
) {
    var titel by rememberSaveable { mutableStateOf(notiz?.titel ?: "") }
    var text by rememberSaveable { mutableStateOf(notiz?.text ?: "") }
    var loeschenFragen by remember { mutableStateOf(false) }

    BackHandler { onFertig(titel, text) }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onFertig(titel, text) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Speichern und zurück")
                }
                Text(
                    if (notiz == null) "Neue Notiz" else "Notiz bearbeiten",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (notiz != null) {
                    IconButton(onClick = { loeschenFragen = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Notiz löschen")
                    }
                }
                IconButton(onClick = { onFertig(titel, text) }) {
                    Icon(Icons.Filled.Check, contentDescription = "Speichern")
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            OutlinedTextField(
                value = titel,
                onValueChange = { titel = it },
                label = { Text("Titel") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            )
        }
    }

    if (loeschenFragen) {
        AlertDialog(
            onDismissRequest = { loeschenFragen = false },
            title = { Text("Notiz löschen?") },
            text = { Text("Die Notiz wird endgültig gelöscht.") },
            confirmButton = {
                TextButton(onClick = {
                    loeschenFragen = false
                    onLoeschen()
                }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { loeschenFragen = false }) { Text("Abbrechen") }
            }
        )
    }
}
