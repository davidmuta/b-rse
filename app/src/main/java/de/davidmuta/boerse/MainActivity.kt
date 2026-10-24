package de.davidmuta.boerse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.davidmuta.boerse.data.Design
import de.davidmuta.boerse.ui.BoerseApp
import de.davidmuta.boerse.ui.theme.BoerseTheme
import de.davidmuta.boerse.viewmodel.BoerseViewModel

class MainActivity : ComponentActivity() {

    private val vm: BoerseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val einstellungen by vm.einstellungen.collectAsState()
            val dunkel = when (einstellungen.design) {
                Design.HELL -> false
                Design.DUNKEL -> true
                Design.SYSTEM -> isSystemInDarkTheme()
            }
            // Farbe der Symbole in der Statusleiste (Uhr, Akku) passend zum gewählten Design.
            DisposableEffect(dunkel) {
                val transparent = android.graphics.Color.TRANSPARENT
                val stil = if (dunkel) SystemBarStyle.dark(transparent)
                else SystemBarStyle.light(transparent, transparent)
                enableEdgeToEdge(statusBarStyle = stil, navigationBarStyle = stil)
                onDispose { }
            }
            BoerseTheme(dunkel = dunkel) {
                BoerseApp(vm)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.appSichtbar()
    }

    override fun onStop() {
        vm.appUnsichtbar()
        super.onStop()
    }
}
