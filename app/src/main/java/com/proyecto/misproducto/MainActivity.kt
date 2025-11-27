package com.proyecto.misproducto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.proyecto.misproducto.navigation.NavigationGraph
import com.proyecto.misproducto.ui.theme.MisProductoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MisProductoTheme {
                NavigationGraph()
            }
        }
    }
}

