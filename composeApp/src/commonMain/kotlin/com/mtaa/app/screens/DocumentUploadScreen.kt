package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mtaa.app.data.SellerRepository
import com.mtaa.app.theme.*
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

class DocumentUploadScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val repository = remember { SellerRepository() }

        var selectedFileName by remember { mutableStateOf<String?>(null) }
        var fileBytes by remember { mutableStateOf<ByteArray?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        // Setup the File Picker
        val launcher = rememberFilePickerLauncher(
            type = PickerType.ImageAndVideo, // Or PickerType.File to allow PDF
            mode = PickerMode.Single,
            title = "Select Business Certificate"
        ) { file ->
            // This runs when the user picks a file
            scope.launch {
                file?.let {
                    selectedFileName = it.name
                    fileBytes = it.readBytes() // Read file into memory
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().background(MtaaCream).padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Progress Bar (Step 2 of 4)
                LinearProgressIndicator(
                    progress = { 0.50f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MtaaOrange,
                    trackColor = MtaaOrangeDim,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Verify Business",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MtaaSlate)
                )
                Text(
                    text = "Upload your Business Registration Certificate.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MtaaSlate.copy(alpha = 0.7f))
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Upload Card Area
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { launcher.launch() }, // Open picker on click
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, MtaaOrangeDim, RoundedCornerShape(16.dp)), // Dotted border look
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedFileName == null) Icons.Default.CloudUpload else Icons.Default.CheckCircle,
                                contentDescription = "Upload",
                                tint = if (selectedFileName == null) MtaaSlate else MtaaGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = selectedFileName ?: "Tap to select Document",
                                color = MtaaSlate,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Next Button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        if (selectedFileName != null && fileBytes != null) {
                            val success = repository.uploadCertificate(selectedFileName!!, fileBytes!!)
                            isLoading = false
                            if (success) {
                                // Success! In next step we will build Location Map
                                // Navigate to Step 3
                                navigator.push(LocationVerificationScreen())
                            }
                        }
                    }
                },
                enabled = selectedFileName != null && !isLoading,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MtaaOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Upload & Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}