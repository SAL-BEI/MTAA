package com.mtaa.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.mtaa.app.screens.WelcomeScreen
import com.mtaa.app.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = MtaaOrange,
            background = MtaaCream,
            surface = Color.White,
            onSurface = MtaaSlate
        )
    ) {
        // Start navigation at the Welcome Screen
        Navigator(WelcomeScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}