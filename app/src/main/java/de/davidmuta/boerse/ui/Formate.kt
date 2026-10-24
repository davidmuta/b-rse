package de.davidmuta.boerse.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val deutsch = Locale.GERMANY
private val zahlFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(deutsch))

/** 1234.5 → "1.234,50" */
fun zahl(wert: Double): String = zahlFormat.format(wert)

/** 0.42 → "+0,42", -1.2 → "−1,20" */
fun zahlMitVorzeichen(wert: Double): String {
    val zeichen = when {
        wert > 0 -> "+"
        wert < 0 -> "−"
        else -> "±"
    }
    return zeichen + zahlFormat.format(abs(wert))
}

/** 2.1594 → "+2,16 %" */
fun prozent(wert: Double): String = zahlMitVorzeichen(wert) + " %"

fun datumHeute(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy", deutsch))

fun wochentagHeute(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE", deutsch))

/** Zeitpunkt als „heute, 08:48“, „gestern, 19:39“ oder „21.10., 08:48“. */
fun zeitpunkt(millis: Long): String {
    val zeit = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    val tag = zeit.toLocalDate()
    val heute = LocalDate.now()
    val uhr = zeit.format(DateTimeFormatter.ofPattern("HH:mm", deutsch))
    return when (tag) {
        heute -> "heute, $uhr"
        heute.minusDays(1) -> "gestern, $uhr"
        else -> zeit.format(DateTimeFormatter.ofPattern("dd.MM., HH:mm", deutsch))
    }
}

/** Datum einer Notiz, z. B. „24.10.2026“ */
fun notizDatum(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy", deutsch))

/** Zeit des letzten Handels (New Yorker Zeit) in deutscher Ortszeit, z. B. „Fr. 23.10., 22:15 Uhr“. */
fun boersenzeit(newYorkZeit: String): String = try {
    LocalDateTime.parse(newYorkZeit)
        .atZone(ZoneId.of("America/New_York"))
        .withZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EE dd.MM., HH:mm 'Uhr'", deutsch))
} catch (e: Exception) {
    "–"
}
