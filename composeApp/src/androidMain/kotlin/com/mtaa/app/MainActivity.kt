package com.mtaa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.preference.PreferenceManager
import com.russhwolf.settings.SharedPreferencesSettings
import com.mtaa.app.screens.BuyerHomeScreen
import com.mtaa.app.screens.WelcomeScreen
import io.github.jan.supabase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Supabase with Android Preferences
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val settings = SharedPreferencesSettings(sharedPrefs)
        MtaaSupabase.init(settings)

        // 2. Decide the Start Screen BEFORE showing UI
        // If user is logged in -> Go to Buyer Market
        // If user is NOT logged in -> Go to Welcome Screen
        val startScreen = if (MtaaSupabase.client.auth.currentSessionOrNull() != null) {
            BuyerHomeScreen()
        } else {
            WelcomeScreen()
        }

        setContent {
            // Pass the decided screen to the App composable
            App(startScreen)
        }
    }
}