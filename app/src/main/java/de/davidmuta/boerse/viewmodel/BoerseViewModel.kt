package de.davidmuta.boerse.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.davidmuta.boerse.data.Aktualisierung
import de.davidmuta.boerse.data.CboeKursQuelle
import de.davidmuta.boerse.data.Design
import de.davidmuta.boerse.data.Einstellungen
import de.davidmuta.boerse.data.EinstellungenSpeicher
import de.davidmuta.boerse.data.KursRepository
import de.davidmuta.boerse.data.KursSpeicher
import de.davidmuta.boerse.data.KursStand
import de.davidmuta.boerse.data.Notiz
import de.davidmuta.boerse.data.NotizenSpeicher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Collections

data class KursZustand(
    val stand: KursStand? = null,
    val laedt: Boolean = false,
    val fehler: String? = null
)

/** Hält alle Daten der App und die Logik dazu (MVVM: das „ViewModel“). */
class BoerseViewModel(app: Application) : AndroidViewModel(app) {

    private val kursRepository = KursRepository(CboeKursQuelle(), KursSpeicher(app))
    private val einstellungenSpeicher = EinstellungenSpeicher(app)
    private val notizenSpeicher = NotizenSpeicher(app)

    private val _kurse = MutableStateFlow(KursZustand(stand = kursRepository.gespeicherterStand()))
    val kurse: StateFlow<KursZustand> = _kurse.asStateFlow()

    private val _einstellungen = MutableStateFlow(einstellungenSpeicher.lade())
    val einstellungen: StateFlow<Einstellungen> = _einstellungen.asStateFlow()

    private val _notizen = MutableStateFlow(notizenSpeicher.lade().sortedByDescending { it.erstelltUm })
    val notizen: StateFlow<List<Notiz>> = _notizen.asStateFlow()

    private val ladeSperre = Mutex()
    private var automatik: Job? = null
    private var sichtbar = false

    // ---------- Kurse ----------

    /** Die App ist auf dem Bildschirm: automatisches Neuladen starten. */
    fun appSichtbar() {
        sichtbar = true
        starteAutomatik()
    }

    /** Die App ist im Hintergrund: nichts mehr abfragen. */
    fun appUnsichtbar() {
        sichtbar = false
        automatik?.cancel()
    }

    fun aktualisierePerHand() {
        viewModelScope.launch { aktualisiereJetzt() }
    }

    private fun starteAutomatik() {
        automatik?.cancel()
        automatik = viewModelScope.launch {
            val minuten = _einstellungen.value.aktualisierung.minuten
            if (minuten == null) {
                // „Per Hand“: nur beim allerersten Start einmal laden, damit etwas zu sehen ist.
                if (_kurse.value.stand == null) aktualisiereJetzt()
                return@launch
            }
            val intervall = minuten * 60_000L
            while (isActive) {
                val alter = System.currentTimeMillis() - (_kurse.value.stand?.abgerufenUm ?: 0L)
                if (alter >= intervall) {
                    val geklappt = aktualisiereJetzt()
                    // Bei einem Fehler nach 2 Minuten erneut versuchen, sonst nach dem Intervall.
                    delay(if (geklappt) intervall else 2 * 60_000L)
                } else {
                    delay((intervall - alter).coerceIn(1_000L, intervall))
                }
            }
        }
    }

    private suspend fun aktualisiereJetzt(): Boolean = ladeSperre.withLock {
        _kurse.update { it.copy(laedt = true) }
        try {
            val stand = kursRepository.aktualisiere()
            _kurse.value = KursZustand(stand = stand, laedt = false, fehler = null)
            true
        } catch (e: CancellationException) {
            _kurse.update { it.copy(laedt = false) }
            throw e
        } catch (e: Exception) {
            _kurse.update { it.copy(laedt = false, fehler = fehlerText(e)) }
            false
        }
    }

    private fun fehlerText(e: Exception): String = when (e) {
        is UnknownHostException, is ConnectException ->
            "Keine Internetverbindung. Angezeigt wird der zuletzt gespeicherte Stand."
        is SocketTimeoutException ->
            "Die Kursquelle antwortet gerade nicht. Angezeigt wird der zuletzt gespeicherte Stand."
        else ->
            "Kurse konnten nicht geladen werden (${e.message ?: e.javaClass.simpleName}). Angezeigt wird der zuletzt gespeicherte Stand."
    }

    // ---------- Einstellungen ----------

    fun setzeAktualisierung(a: Aktualisierung) {
        aendereEinstellungen { it.copy(aktualisierung = a) }
        if (sichtbar) starteAutomatik()
    }

    fun setzeDesign(d: Design) = aendereEinstellungen { it.copy(design = d) }

    /** Verschiebt einen Wert in der Reihenfolge um eine Stelle (richtung -1 = hoch, +1 = runter). */
    fun verschiebe(kuerzel: String, richtung: Int) = aendereEinstellungen { e ->
        val liste = e.reihenfolge.toMutableList()
        val von = liste.indexOf(kuerzel)
        val nach = von + richtung
        if (von >= 0 && nach in liste.indices) Collections.swap(liste, von, nach)
        e.copy(reihenfolge = liste)
    }

    private fun aendereEinstellungen(aenderung: (Einstellungen) -> Einstellungen) {
        val neu = aenderung(_einstellungen.value)
        _einstellungen.value = neu
        einstellungenSpeicher.speichere(neu)
    }

    // ---------- Notizen ----------

    fun notiz(id: Long): Notiz? = _notizen.value.firstOrNull { it.id == id }

    /** Speichert eine neue (id = null) oder geänderte Notiz. Leere Notizen werden nicht gespeichert. */
    fun speichereNotiz(id: Long?, titel: String, text: String) {
        val t = titel.trim()
        val x = text.trimEnd()
        val liste = _notizen.value.toMutableList()
        if (t.isEmpty() && x.isBlank()) {
            if (id != null) liste.removeAll { it.id == id }
        } else if (id == null) {
            val jetzt = System.currentTimeMillis()
            liste.add(Notiz(id = jetzt, titel = t, text = x, erstelltUm = jetzt))
        } else {
            val i = liste.indexOfFirst { it.id == id }
            if (i >= 0) liste[i] = liste[i].copy(titel = t, text = x)
        }
        setzeNotizen(liste)
    }

    fun loescheNotiz(id: Long) = setzeNotizen(_notizen.value.filterNot { it.id == id })

    private fun setzeNotizen(liste: List<Notiz>) {
        val sortiert = liste.sortedByDescending { it.erstelltUm }
        _notizen.value = sortiert
        viewModelScope.launch(Dispatchers.IO) { notizenSpeicher.speichere(sortiert) }
    }
}
