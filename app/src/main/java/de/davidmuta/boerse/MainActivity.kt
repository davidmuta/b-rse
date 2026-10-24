package de.davidmuta.boerse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Erste Version (Meilenstein 1): zeigt nur, dass Bau und Installation funktionieren.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF1D4ED8))) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF3F5F9)) {
                    Willkommen()
                }
            }
        }
    }
}

@Composable
fun Willkommen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Börse",
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1D4ED8)
        )
        Text(
            text = "Installation erfolgreich",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Version " + BuildConfig.VERSION_NAME,
            fontSize = 14.sp,
            color = Color(0xFF4B5563),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
