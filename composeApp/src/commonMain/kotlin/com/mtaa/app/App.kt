package com.mtaa.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        WelcomeScreen()
    }
}

@Composable
fun WelcomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MtaaCream)
    ) {
        // 1. Background Decoration (Subtle Gradient Blob)
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

        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Area (Placeholder Icon)
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

            // Headline
            Text(
                text = "Mtaa Market",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MtaaSlate,
                    letterSpacing = (-1).sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subheadline
            Text(
                text = "Trusted local shopping in Nairobi's top buildings.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MtaaSlate.copy(alpha = 0.7f)
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Premium Button
            Button(
                onClick = { /* TODO: Navigate to Login */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MtaaOrange
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Button
            TextButton(onClick = { /* TODO: Login */ }) {
                Text("I already have an account", color = MtaaSlate)
            }
        }
    }
}