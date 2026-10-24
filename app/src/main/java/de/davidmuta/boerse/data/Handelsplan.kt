package de.davidmuta.boerse.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Ein Punkt der Checkliste im Handelsplan. */
data class CheckPunkt(
    val id: Long,
    val text: String,
    val erledigt: Boolean
)

/** Die 7 Schritte „Ablauf vor jedem Trade“ aus dem Handelsplan – Vorbelegung der Checkliste. */
val ablaufVorJedemTrade: List<String> = listOf(
    "Optionsliquidität prüfen",
    "Referenzindex bestimmen",
    "Markt-, Sektor- und Aktienampel prüfen",
    "Earnings-Kalender bis einschließlich Verfall kontrollieren",
    "Laufzeit und Delta 10–20 prüfen",
    "Initiale Buying Power (max. 3 %) und VIX-Gesamtgrenze prüfen",
    "Trade eröffnen und Ausstiege festlegen"
)

/** Speichert die Checkliste in einer Datei auf dem Handy. */
class ChecklistenSpeicher(context: Context) {
    private val datei = File(context.filesDir, "checkliste.json")
    private val sperre = Any()

    /** Beim allerersten Start (noch keine Datei) kommt die Vorbelegung. */
    fun lade(): List<CheckPunkt> = synchronized(sperre) {
        if (!datei.exists()) {
            return ablaufVorJedemTrade.mapIndexed { i, text -> CheckPunkt(id = i + 1L, text = text, erledigt = false) }
        }
        try {
            val liste = JSONArray(datei.readText())
            (0 until liste.length()).map { i ->
                val p = liste.getJSONObject(i)
                CheckPunkt(
                    id = p.getLong("id"),
                    text = p.optString("text", ""),
                    erledigt = p.optBoolean("erledigt", false)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun speichere(punkte: List<CheckPunkt>) = synchronized(sperre) {
        val liste = JSONArray()
        punkte.forEach { p ->
            liste.put(
                JSONObject()
                    .put("id", p.id)
                    .put("text", p.text)
                    .put("erledigt", p.erledigt)
            )
        }
        // Erst in eine Hilfsdatei schreiben, dann umbenennen: so geht bei einem Absturz nichts verloren.
        val neu = File(datei.parentFile, "checkliste.json.neu")
        neu.writeText(liste.toString())
        if (!neu.renameTo(datei)) {
            datei.writeText(liste.toString())
            neu.delete()
        }
    }
}
