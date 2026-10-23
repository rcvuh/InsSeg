package com.pascm.inspseg

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pascm.inspseg.ui.navigation.InspSegNavHost
import com.pascm.inspseg.ui.theme.InspSegTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Barras de sistema transparentes con contenido claro: la app es negra
        // en todo dispositivo, independientemente del ajuste de apariencia.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            InspSegTheme {
                InspSegNavHost()
            }
        }
    }
}
