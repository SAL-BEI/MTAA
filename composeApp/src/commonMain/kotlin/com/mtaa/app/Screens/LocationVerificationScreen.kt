package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mtaa.app.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationVerificationScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        // Mock State
        var isVerifying by remember { mutableStateOf(false) }
        var isVerified by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf("Stand inside your shop and click Verify.") }

        Box(
            modifier = Modifier.fillMaxSize().background(MtaaCream)
        ) {
            // 1. The "Fake" Map (Gray Placeholder)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 250.dp) // Leave space for bottom sheet
                    .background(Color(0xFFE5E7EB)), // Light Gray "Map" color
                contentAlignment = Alignment.Center
            ) {
                // A fake "Map Pin" in the center
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Pin",
                    tint = MtaaOrange,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "(Map Loading...)",
                    color = MtaaSlate.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 80.dp)
                )
            }

            // 2. The Bottom Sheet (Premium Control Panel)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(300.dp),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color.LightGray, CircleShape)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isVerified) "Location Verified" else "Verify Location",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if(isVerified) MtaaGreen else MtaaSlate
                            )
                        )
                        if (isVerified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.CheckCircle, "Success", tint = MtaaGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MtaaSlate.copy(alpha = 0.7f)),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // 3. The Action Button
                    Button(
                        onClick = {
                            scope.launch {
                                isVerifying = true
                                statusMessage = "Checking GPS satellites..."
                                delay(1500) // Fake loading
                                statusMessage = "Calculating distance to building..."
                                delay(1500)

                                // MOCK SUCCESS
                                isVerifying = false
                                isVerified = true
                                statusMessage = "Success! You are at the correct location."// MOCK SUCCESS
                                isVerifying = false
                                isVerified = true
                                statusMessage = "Success! You are at the correct location."

                                // Wait 1 second then go to Dashboard
                                delay(1000)
                                navigator.push(SellerDashboardScreen())
                            }
                        },
                        enabled = !isVerifying && !isVerified,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVerified) MtaaGreen else MtaaOrange
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Verifying GPS...")
                        } else {
                            Icon(Icons.Default.MyLocation, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isVerified) "Done" else "Verify My Location")
                        }
                    }
                }
            }
        }
    }
}