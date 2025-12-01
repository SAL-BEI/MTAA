package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.mtaa.app.data.CartRepository
import com.mtaa.app.data.Product

class CartScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // Load cart items initially
        var cartItems by remember { mutableStateOf(CartRepository.getCartItems()) }

        // Calculate Total
        val totalAmount = cartItems.sumOf { it.price }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Cart (${cartItems.size})", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            },
            bottomBar = {
                if (cartItems.isNotEmpty()) {
                    Surface(shadowElevation = 16.dp, color = Color.White) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("KES $totalAmount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFFA500))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // TODO: Navigate to Payment/Escrow Screen passing the total
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Your cart is empty", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { product ->
                        CartItemRow(
                            product = product,
                            onRemove = {
                                if (product.id != null) {
                                    CartRepository.removeFromCart(product.id)
                                    // Refresh list
                                    cartItems = CartRepository.getCartItems()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun CartItemRow(product: Product, onRemove: () -> Unit) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = product.image_url,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("KES ${product.price}", color = Color(0xFFFFA500), fontWeight = FontWeight.Medium)
                }

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, "Remove", tint = Color.Red)
                }
            }
        }
    }
}