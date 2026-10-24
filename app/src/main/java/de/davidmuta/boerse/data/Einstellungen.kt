package de.davidmuta.boerse.data

import android.content.Context

/** Wie oft die Kurse automatisch neu geladen werden. */
enum class Aktualisierung(val minuten: Int?, val bezeichnung: String) {
    PER_HAND(null, "Per Hand"),
    MIN_15(15, "15 Min."),
    MIN_30(30, "30 Min."),
    MIN_60(60, "60 Min.")
}

/** Helles oder dunkles Aussehen der App. */
enum class Design(val bezeichnung: String) {
    HELL("Hell"),
    DUNKEL("Dunkel"),
    SYSTEM("Wie Handy")
}

data class Einstellungen(
    val aktualisierung: Aktualisierung = Aktualisierung.MIN_15,
    val design: Design = Design.HELL,
    val reihenfolge: List<String> = Werte.startseite.map { it.kuerzel }
)

/** Speichert die Einstellungen dauerhaft auf dem Handy. */
class EinstellungenSpeicher(context: Context) {
    private val prefs = context.getSharedPreferences("einstellungen", Context.MODE_PRIVATE)

    fun lade(): Einstellungen {
        val aktualisierung = Aktualisierung.entries
            .firstOrNull { it.name == prefs.getString("aktualisierung", null) }
            ?: Aktualisierung.MIN_15
        val design = Design.entries
            .firstOrNull { it.name == prefs.getString("design", null) }
            ?: Design.HELL
        // Gespeicherte Reihenfolge übernehmen, neue Werte hinten anhängen,
        // unbekannte und nicht mehr gezeigte (z. B. VIX3M) weglassen.
        val startseite = Werte.startseite.map { it.kuerzel }
        val gespeichert = prefs.getString("reihenfolge", null)
            ?.split(",")
            ?.filter { it in startseite }
            ?.distinct()
            ?: emptyList()
        val reihenfolge = gespeichert + startseite.filter { it !in gespeichert }
        return Einstellungen(aktualisierung, design, reihenfolge)
    }

    fun speichere(e: Einstellungen) {
        prefs.edit()
            .putString("aktualisierung", e.aktualisierung.name)
            .putString("design", e.design.name)
            .putString("reihenfolge", e.reihenfolge.joinToString(","))
            .apply()
    }
}
