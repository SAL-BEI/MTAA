package com.mtaa.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mtaa.app.theme.* // Ensure your theme colors are imported
import org.jetbrains.compose.resources.painterResource
import mtaamarket.composeapp.generated.resources.Res
import mtaamarket.composeapp.generated.resources.mtaa_logo
import mtaamarket.composeapp.generated.resources.nairobi_bg // This will appear after you Build

class WelcomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Box(modifier = Modifier.fillMaxSize()) {

            // --- LAYER 1: The Cinematic Background ---
            Image(
                painter = painterResource(Res.drawable.nairobi_bg),
                contentDescription = "Background",
                contentScale = ContentScale.Crop, // This makes it fill the screen
                modifier = Modifier.fillMaxSize()
            )

            // --- LAYER 2: The "Readability" Gradient ---
            // This darkens the image from bottom (heavy) to top (light)
            // so the white text pops without hiding the photo completely.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f), // Top: Mostly clear
                                Color.Black.copy(alpha = 0.6f), // Middle: Darker
                                Color.Black.copy(alpha = 0.95f) // Bottom: Almost black
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // --- LAYER 3: The Content ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                verticalArrangement = Arrangement.Bottom, // Anchors UI to the thumb zone
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Logo Area: Clean and distinct
                // We use a white tint to make it look like a premium brand mark
                Image(
                    painter = painterResource(Res.drawable.mtaa_logo),
                    contentDescription = "MtaaMarket Brand",
                    modifier = Modifier.size(80.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Headline: Bold and Direct
                Text(
                    text = "Welcome to your\nMtaa Market",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 44.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle: Trust-building language
                Text(
                    text = "The secure way to buy and sell within your building community.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // --- PRIMARY ACTION: List Business ---
                // Solid Orange button for the main goal
                Button(
                    onClick = { navigator.push(SignUpScreen()) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MtaaOrange),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "List My Business",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- SECONDARY ACTION: Browse ---
                // Transparent background with white border (Premium feel)
                OutlinedButton(
                    onClick = { navigator.push(BuyerHomeScreen()) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Just Browsing",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer Link
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Already a member?", color = Color.White.copy(alpha = 0.7f))
                    TextButton(onClick = { navigator.push(LoginScreen()) }) {
                        Text(
                            "Log In",
                            color = MtaaOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}