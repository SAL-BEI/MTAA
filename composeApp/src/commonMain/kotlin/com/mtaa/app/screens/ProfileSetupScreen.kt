package com.mtaa.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mtaa.app.data.AuthRepository
import com.mtaa.app.theme.MtaaOrange

// --- PEEKABOO IMPORTS (These will turn white after Gradle Sync) ---
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import kotlinx.coroutines.launch

data class ProfileSetupScreen(val userId: String, val userEmail: String) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        // Ensure AuthRepository is initialized correctly
        val authRepository = remember { AuthRepository() }

        // Form State
        var fullName by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        // Image State
        var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
        var displayImage by remember { mutableStateOf<ImageBitmap?>(null) }

        // --- IMAGE PICKER SETUP ---
        // This launcher handles opening the gallery and getting the result
        val singleImagePicker = rememberImagePickerLauncher(
            selectionMode = SelectionMode.Single,
            scope = scope,
            onResult = { byteArrays ->
                // When user picks an image, save it to state
                byteArrays.firstOrNull()?.let {
                    selectedImageBytes = it
                    displayImage = it.toImageBitmap()
                }
            }
        )

        // Logic to Save Profile
        fun saveProfile() {
            scope.launch {
                isLoading = true
                errorMessage = ""

                // 1. Format Phone: 0712... -> 254712...
                val cleanPhone = phoneNumber.trim()
                val formattedPhone = when {
                    cleanPhone.startsWith("0") -> cleanPhone.replaceFirst("0", "254")
                    cleanPhone.startsWith("254") -> cleanPhone
                    else -> "254$cleanPhone"
                }

                // 2. Save profile to Supabase (Using Upsert to avoid "Failed to save" errors)
                val success = authRepository.saveUserProfile(
                    userId = userId,
                    email = userEmail,
                    fullName = fullName.trim(),
                    phone = formattedPhone,
                    role = "buyer"
                    // Note: We are not uploading the image to Storage yet, just the text data
                )

                if (success) {
                    // 3. Navigate to Buyer Home Screen
                    navigator.push(BuyerHomeScreen())
                } else {
                    errorMessage = "Failed to save profile. Check your internet or try again."
                }

                isLoading = false
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Complete Profile", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- 1. Profile Photo Picker ---
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            // Launch Gallery when clicked
                            singleImagePicker.launch()
                        }
                ) {
                    if (displayImage != null) {
                        // Show the User's Selected Image
                        Image(
                            bitmap = displayImage!!,
                            contentDescription = "Selected Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.dp, Color.LightGray, CircleShape)
                        )
                    } else {
                        // Show Placeholder Avatar
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(60.dp),
                                tint = Color.Gray
                            )
                        }
                    }

                    // Camera Icon Badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MtaaOrange)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 2. Email Field (Read Only) ---
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = {},
                    label = { Text("Email Address") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 3. Full Name Field ---
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = ""
                    },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Alvin Kipchirchir") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 4. Phone Number Field ---
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            phoneNumber = it
                            errorMessage = ""
                        }
                    },
                    label = { Text("M-PESA Number") },
                    placeholder = { Text("712345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("+254 ", fontWeight = FontWeight.Bold) },
                    isError = phoneNumber.isNotEmpty() && phoneNumber.length < 9
                )

                if (phoneNumber.isNotEmpty() && phoneNumber.length < 9) {
                    Text(
                        text = "Phone number must be 9-10 digits",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp)
                    )
                } else {
                    Text(
                        text = "Used for payments and verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp)
                    )
                }

                // Error Message Display
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- 5. Submit Button ---
                Button(
                    onClick = { saveProfile() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MtaaOrange),
                    shape = RoundedCornerShape(16.dp),
                    enabled = fullName.isNotEmpty() && phoneNumber.length >= 9 && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Finish Setup", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}