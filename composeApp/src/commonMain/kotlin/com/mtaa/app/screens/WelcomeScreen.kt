package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.mtaa.app.theme.*

class WelcomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // NOTE: Auto-Login logic has been moved to MainActivity.kt to prevent crashes.
        // This screen now only shows buttons.

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
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MtaaOrange,
                    shadowElevation = 16.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("M", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
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

                // 1. Sign Up Button
                Button(
                    onClick = { navigator.push(SignUpScreen()) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MtaaOrange),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text("List My Business", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Login Button
                TextButton(
                    onClick = { navigator.push(LoginScreen()) }
                ) {
                    Text("I already have an account", color = MtaaSlate)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Buyer Button
                OutlinedButton(
                    onClick = {
                        // Push adds to stack safely so "Back" works
                        navigator.push(BuyerHomeScreen())
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MtaaOrange)
                ) {
                    Text("Just Browsing?", color = MtaaOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}