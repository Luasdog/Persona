package com.example.persona

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import com.example.persona.ui.AppContent
import com.example.persona.ui.theme.PersonaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonaTheme {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    AppContent()
                }
            }
        }
    }
}