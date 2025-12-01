package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mtaa.app.MtaaMap // Import our new Map component
import com.mtaa.app.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationVerificationScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        var isVerifying by remember { mutableStateOf(false) }
        var isVerified by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf("Pin your exact shop location.") }

        // Store the selected coordinates (Defaults to Nairobi)
        var selectedLat by remember { mutableStateOf(-1.286389) }
        var selectedLng by remember { mutableStateOf(36.817223) }

        Box(
            modifier = Modifier.fillMaxSize().background(MtaaCream)
        ) {
            // 1. THE REAL GOOGLE MAP
            MtaaMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 250.dp), // Space for bottom sheet
                onLocationSelected = { lat, lng ->
                    selectedLat = lat
                    selectedLng = lng
                    statusMessage = "Selected: $lat, $lng"
                }
            )

            // 2. The Bottom Sheet
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

                    Button(
                        onClick = {
                            scope.launch {
                                isVerifying = true
                                statusMessage = "Checking GPS satellites..."
                                delay(1500) // Simulating API call
                                isVerifying = false
                                isVerified = true
                                statusMessage = "Success! Location Verified."
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