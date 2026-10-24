package de.davidmuta.boerse.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Ein Kurs, so wie ihn die Datenquelle liefert. */
data class Kurs(
    val kuerzel: String,
    val preis: Double,
    val aenderung: Double,
    val aenderungProzent: Double,
    /** Zeit des letzten Handels in New Yorker Ortszeit, z. B. "2026-10-23T16:15:01" */
    val letzterHandel: String
)

/** Alle Kurse eines Abrufs und wann sie abgerufen wurden. */
data class KursStand(
    val kurse: Map<String, Kurs>,
    val abgerufenUm: Long
)

/**
 * Eine Datenquelle für Kurse. Weil die Cboe-Schnittstelle nicht offiziell dokumentiert ist,
 * lässt sich die Quelle hier austauschen, ohne den Rest der App zu ändern.
 */
interface KursQuelle {
    suspend fun holeKurse(werte: List<WertInfo>): Map<String, Kurs>
}

/** Kurse von Cboe (ca. 15 Minuten verzögert, kostenlos, ohne Anmeldung). */
class CboeKursQuelle : KursQuelle {

    override suspend fun holeKurse(werte: List<WertInfo>): Map<String, Kurs> =
        withContext(Dispatchers.IO) {
            werte.associate { wert -> wert.kuerzel to holeEinen(wert) }
        }

    private fun holeEinen(wert: WertInfo): Kurs {
        val adresse = "https://cdn-api.cboe.com/api/global/delayed_quotes/quotes/${wert.cboeSymbol}.json"
        val verbindung = URL(adresse).openConnection() as HttpURLConnection
        try {
            verbindung.connectTimeout = 10_000
            verbindung.readTimeout = 10_000
            verbindung.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) BoerseApp")
            verbindung.setRequestProperty("Accept", "application/json")
            val code = verbindung.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw IOException("Cboe antwortet mit Code $code")
            }
            val text = verbindung.inputStream.bufferedReader().use { it.readText() }
            val daten = JSONObject(text).getJSONObject("data")
            return Kurs(
                kuerzel = wert.kuerzel,
                preis = daten.getDouble("current_price"),
                aenderung = daten.optDouble("price_change", 0.0),
                aenderungProzent = daten.optDouble("price_change_percent", 0.0),
                letzterHandel = daten.optString("last_trade_time", "")
            )
        } finally {
            verbindung.disconnect()
        }
    }
}

/** Speichert den letzten Kursstand auf dem Handy, damit er beim Öffnen sofort da ist. */
class KursSpeicher(context: Context) {
    private val prefs = context.getSharedPreferences("kurse", Context.MODE_PRIVATE)

    fun lade(): KursStand? {
        val text = prefs.getString("stand", null) ?: return null
        return try {
            val obj = JSONObject(text)
            val liste = obj.getJSONArray("kurse")
            val kurse = (0 until liste.length()).map { i ->
                val k = liste.getJSONObject(i)
                Kurs(
                    kuerzel = k.getString("kuerzel"),
                    preis = k.getDouble("preis"),
                    aenderung = k.getDouble("aenderung"),
                    aenderungProzent = k.getDouble("aenderungProzent"),
                    letzterHandel = k.optString("letzterHandel", "")
                )
            }.associateBy { it.kuerzel }
            KursStand(kurse, obj.getLong("abgerufenUm"))
        } catch (e: Exception) {
            null
        }
    }

    fun speichere(stand: KursStand) {
        val liste = JSONArray()
        stand.kurse.values.forEach { k ->
            liste.put(
                JSONObject()
                    .put("kuerzel", k.kuerzel)
                    .put("preis", k.preis)
                    .put("aenderung", k.aenderung)
                    .put("aenderungProzent", k.aenderungProzent)
                    .put("letzterHandel", k.letzterHandel)
            )
        }
        val obj = JSONObject().put("abgerufenUm", stand.abgerufenUm).put("kurse", liste)
        prefs.edit().putString("stand", obj.toString()).apply()
    }
}

/** Verbindet Datenquelle und Zwischenspeicher. */
class KursRepository(
    private val quelle: KursQuelle,
    private val speicher: KursSpeicher
) {
    fun gespeicherterStand(): KursStand? = speicher.lade()

    suspend fun aktualisiere(): KursStand {
        val kurse = quelle.holeKurse(Werte.alle)
        val stand = KursStand(kurse, System.currentTimeMillis())
        speicher.speichere(stand)
        return stand
    }
}
