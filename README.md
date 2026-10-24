# Börse

Private Android-App: VIX, VVIX, VIX3M und SPY auf einen Blick, Spread VIX/VIX3M, Notizen.

## Neueste Version installieren

1. Auf GitHub angemeldet diese Seite öffnen: **Releases → „Neueste Testversion“**
2. Auf `boerse.apk` tippen und herunterladen.
3. Die Datei öffnen und „Installieren“ antippen.

Jede Änderung am Code baut automatisch eine neue Testversion (siehe Reiter **Actions**).

## Technik

Kotlin, Jetpack Compose, Material 3. Gebaut mit GitHub Actions (`.github/workflows/build.yml`).
Die Testversionen sind mit einem festen Test-Schlüssel signiert (`app/signing/debug.keystore`),
damit Updates sich über die alte Version installieren lassen. Für die fertige Version
kommt ein eigener, geheimer Schlüssel hinzu.
