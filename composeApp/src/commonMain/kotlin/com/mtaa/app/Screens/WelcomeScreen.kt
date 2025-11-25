package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mtaa.app.MtaaSupabase
import com.mtaa.app.data.AuthRepository
import com.mtaa.app.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay // Required for the crash fix

class WelcomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepo = remember { AuthRepository() }

        // 1. AUTO-LOGIN LOGIC
        LaunchedEffect(Unit) {
            // FIX 1: Add a tiny delay to ensure the screen is ready (Prevent 'DESTROYED' crash)
            delay(100)

            val session = MtaaSupabase.client.auth.currentSessionOrNull()
            if (session != null) {
                val hasShop = authRepo.hasBusiness()

                // FIX 2: Use replaceAll to clear history so they can't go back to Welcome
                if (hasShop) {
                    navigator.replaceAll(SellerDashboardScreen())
                } else {
                    navigator.replaceAll(SellerOnboardingScreen())
                }
            }
        }

        // 2. The UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MtaaCream)
        ) {
            // Background Decoration
            Box(
                modifier = Modifier
                    .offset(x = (-100).dp, y = (-100).dp)
                    .size(300.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(MtaaOrangeDim.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MtaaOrange,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("M", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Mtaa Market",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MtaaSlate,
                        letterSpacing = (-1).sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Trusted local shopping in Nairobi's top buildings.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MtaaSlate.copy(alpha = 0.7f)
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Sign Up Button
                Button(
                    onClick = {
                        navigator.push(SignUpScreen())
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MtaaOrange),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text("List My Business", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login Button
                TextButton(
                    onClick = {
                        navigator.push(LoginScreen())
                    }
                ) {
                    Text("I already have an account", color = MtaaSlate)
                }
            }
        }
    }
}