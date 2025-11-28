package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow // <--- CRITICAL IMPORT
import com.mtaa.app.data.SellerRepository
import com.mtaa.app.theme.*
import kotlinx.coroutines.launch

class SellerOnboardingScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val repository = remember { SellerRepository() }

        var businessName by remember { mutableStateOf("") }
        var kraPin by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxSize().background(MtaaCream).padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { 0.25f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MtaaOrange,
                    trackColor = MtaaOrangeDim,
                )
                Spacer(modifier = Modifier.height(32.dp))

                Text("Tell us about your business", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MtaaSlate))
                Text("We verify every business to ensure trust.", style = MaterialTheme.typography.bodyMedium.copy(color = MtaaSlate.copy(alpha = 0.7f)))

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MtaaOrange, focusedLabelColor = MtaaOrange, cursorColor = MtaaOrange)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = kraPin,
                    onValueChange = { kraPin = it },
                    label = { Text("KRA PIN (e.g., P05...)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MtaaOrange, focusedLabelColor = MtaaOrange, cursorColor = MtaaOrange)
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val success = repository.createBusiness(businessName, kraPin)
                        isLoading = false
                        if (success) {
                            navigator.push(DocumentUploadScreen())
                        }
                    }
                },
                enabled = businessName.isNotEmpty() && kraPin.isNotEmpty() && !isLoading,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MtaaOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Next Step", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}