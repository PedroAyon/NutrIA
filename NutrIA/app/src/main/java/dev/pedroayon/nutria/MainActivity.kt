package dev.pedroayon.nutria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.pedroayon.nutria.core.ui.navigation.AppEntryHost
import dev.pedroayon.nutria.core.ui.theme.NutriIATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriIATheme {
                AppEntryHost()
            }
        }
    }
}
