package com.mtaa.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import cafe.adriel.voyager.core.screen.Screen // Import Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import com.mtaa.app.screens.WelcomeScreen
import com.mtaa.app.theme.*

// Add the 'startScreen' parameter with a default
@Composable
fun App(startScreen: Screen = WelcomeScreen()) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = MtaaOrange,
            background = MtaaCream,
            surface = Color.White,
            onSurface = MtaaSlate
        )
    ) {
        // Use the passed startScreen instead of hardcoding WelcomeScreen
        Navigator(startScreen) { navigator ->
            CurrentScreen()
        }
    }
}