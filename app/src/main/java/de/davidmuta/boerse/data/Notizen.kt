package de.davidmuta.boerse.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Notiz(
    val id: Long,
    val titel: String,
    val text: String,
    val erstelltUm: Long
)

/** Speichert alle Notizen in einer Datei auf dem Handy. */
class NotizenSpeicher(context: Context) {
    private val datei = File(context.filesDir, "notizen.json")
    private val sperre = Any()

    fun lade(): List<Notiz> = synchronized(sperre) {
        if (!datei.exists()) return emptyList()
        try {
            val liste = JSONArray(datei.readText())
            (0 until liste.length()).map { i ->
                val n = liste.getJSONObject(i)
                Notiz(
                    id = n.getLong("id"),
                    titel = n.optString("titel", ""),
                    text = n.optString("text", ""),
                    erstelltUm = n.getLong("erstelltUm")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun speichere(notizen: List<Notiz>) = synchronized(sperre) {
        val liste = JSONArray()
        notizen.forEach { n ->
            liste.put(
                JSONObject()
                    .put("id", n.id)
                    .put("titel", n.titel)
                    .put("text", n.text)
                    .put("erstelltUm", n.erstelltUm)
            )
        }
        // Erst in eine Hilfsdatei schreiben, dann umbenennen: so geht bei einem Absturz nichts verloren.
        val neu = File(datei.parentFile, "notizen.json.neu")
        neu.writeText(liste.toString())
        if (!neu.renameTo(datei)) {
            datei.writeText(liste.toString())
            neu.delete()
        }
    }
}
