package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.mtaa.app.data.Product
import com.mtaa.app.theme.*

// This screen receives the specific Product clicked
data class ProductDetailScreen(val product: Product) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollState = rememberScrollState()

        Scaffold(
            bottomBar = {
                // Sticky Bottom "Buy" Button
                Surface(shadowElevation = 16.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Price", color = MtaaSlate.copy(alpha = 0.6f), fontSize = 12.sp)
                            Text(
                                "KES ${product.price}",
                                color = MtaaOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Button(
                            onClick = { /* TODO: Trigger M-Pesa */ },
                            colors = ButtonDefaults.buttonColors(containerColor = MtaaOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(50.dp).width(160.dp)
                        ) {
                            Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MtaaCream)
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // 1. Full Width Image
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    AsyncImage(
                        model = product.image_url,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Back Button overlay
                    IconButton(
                        onClick = { navigator.pop() },
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                }

                // 2. Product Info
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Verified Seller",
                            color = MtaaGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, null, tint = MtaaGreen, modifier = Modifier.size(14.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MtaaSlate
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MtaaOrangeDim.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Description
                    Text("Description", fontWeight = FontWeight.Bold, color = MtaaSlate, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MtaaSlate.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. Location Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = MtaaOrange)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Location", fontWeight = FontWeight.Bold, color = MtaaSlate)
                                Text("20th Century Plaza, 2nd Floor", color = MtaaSlate.copy(alpha = 0.7f))
                            }
                        }
                    }

                    // Extra space for scrolling above the sticky button
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}