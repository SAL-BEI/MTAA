package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mtaa.app.data.ProductRepository
import com.mtaa.app.theme.*
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

class AddProductScreen : Screen {

    // THIS ANNOTATION IS REQUIRED FOR TopAppBar
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val repository = remember { ProductRepository() }

        // Form State
        var name by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedImageName by remember { mutableStateOf<String?>(null) }
        var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        // Image Picker
        val launcher = rememberFilePickerLauncher(
            type = PickerType.Image,
            mode = PickerMode.Single,
            title = "Select Product Image"
        ) { file ->
            scope.launch {
                file?.let {
                    selectedImageName = it.name
                    imageBytes = it.readBytes()
                }
            }
        }


        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("New Product", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MtaaCream)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MtaaCream)
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Image Upload Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { launcher.launch() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                tint = if (selectedImageName != null) MtaaGreen else MtaaSlate
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedImageName ?: "Tap to upload photo",
                                color = MtaaSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Inputs
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MtaaOrange, focusedLabelColor = MtaaOrange
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (KES)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MtaaOrange, focusedLabelColor = MtaaOrange
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MtaaOrange, focusedLabelColor = MtaaOrange
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 3. Submit Button
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val success = repository.createProduct(
                                name = name,
                                desc = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                imageBytes = imageBytes
                            )
                            isLoading = false
                            if (success) {
                                navigator.pop() // Go back to Dashboard
                            }
                        }
                    },
                    enabled = name.isNotEmpty() && price.isNotEmpty() && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MtaaOrange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("List Item", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}