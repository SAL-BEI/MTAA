package com.mtaa.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Verified
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

// --- APP IMPORTS ---
import com.mtaa.app.MtaaSupabase
import com.mtaa.app.data.CartRepository
import com.mtaa.app.data.Product

// --- SUPABASE IMPORTS ---
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.functions.*
import io.github.jan.supabase.realtime.* import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

// ---------------------------------------------------------
// MODELS
// ---------------------------------------------------------

@Serializable
data class Order(
    val id: String? = null,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("product_id") val productId: String,
    val amount: Double,
    val status: String = "pending_payment"
)

@Serializable
data class PaymentRequest(
    val phoneNumber: String,
    val amount: Double,
    val orderId: String
)

// ---------------------------------------------------------
// SCREEN
// ---------------------------------------------------------

data class ProductDetailScreen(val product: Product) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, InternalAPI::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollState = rememberScrollState()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        var isProcessing by remember { mutableStateOf(false) }

        var isAddedToCart by remember {
            mutableStateOf(product.id?.let { CartRepository.isInCart(it) } ?: false)
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Product Details", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navigator.push(CartScreen())
                        }) {
                            Icon(Icons.Default.ShoppingCart, "Cart")
                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    shadowElevation = 16.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Price", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                "KES ${product.price}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFFFFA500)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 1. CART BUTTON
                            OutlinedButton(
                                onClick = {
                                    if (product.id != null) {
                                        if (isAddedToCart) {
                                            CartRepository.removeFromCart(product.id)
                                            isAddedToCart = false
                                        } else {
                                            CartRepository.addToCart(product)
                                            isAddedToCart = true
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text(if (isAddedToCart) "Remove" else "Cart")
                            }

                            // 2. BUY NOW BUTTON
                            Button(
                                onClick = {
                                    scope.launch {
                                        isProcessing = true
                                        try {
                                            // A. Auth Check
                                            val user = MtaaSupabase.client.auth.currentUserOrNull()
                                            if (user == null) {
                                                snackbarHostState.showSnackbar("Please login to buy")
                                                isProcessing = false
                                                return@launch
                                            }

                                            val productId = product.id ?: throw Exception("Product ID is missing")

                                            // B. Create Order
                                            val newOrder = Order(
                                                buyerId = user.id,
                                                sellerId = product.owner_id,
                                                productId = productId,
                                                amount = product.price
                                            )

                                            val createdOrder = MtaaSupabase.client.from("orders")
                                                .insert(newOrder) { select() }
                                                .decodeSingle<Order>()

                                            val orderId = createdOrder.id ?: throw Exception("Failed to create order")

                                            // --- FIX: Correct Realtime Syntax ---
                                            // 1. Create the channel
                                            val channel = MtaaSupabase.client.channel("order-$orderId")

                                            // 2. Create the flow using the LAMBDA syntax (Curly braces)
                                            // We removed the 'filter' property to avoid the "private" error.
                                            val changeFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                                                table = "orders"
                                            }

                                            // 3. Launch Listener
                                            scope.launch {
                                                channel.subscribe()
                                                changeFlow.collect { change ->
                                                    val updatedOrder = change.decodeRecord<Order>()

                                                    // 4. MANUAL FILTER: Check the ID here instead!
                                                    if (updatedOrder.id == orderId) {
                                                        if (updatedOrder.status == "escrow_locked") {
                                                            snackbarHostState.showSnackbar("Payment Received! ✅")
                                                            isProcessing = false
                                                            channel.unsubscribe()
                                                            this.cancel()
                                                        } else if (updatedOrder.status == "cancelled") {
                                                            snackbarHostState.showSnackbar("Payment Cancelled ❌")
                                                            isProcessing = false
                                                            channel.unsubscribe()
                                                            this.cancel()
                                                        }
                                                    }
                                                }
                                            }

                                            // C. Trigger M-PESA
                                            val myTestPhone = "254724894722" // Replace later

                                            val paymentRequest = PaymentRequest(
                                                phoneNumber = myTestPhone,
                                                amount = 1.0,
                                                orderId = orderId
                                            )

                                            MtaaSupabase.client.functions.invoke(
                                                function = "mpesa-push",
                                                body = Json.encodeToJsonElement(paymentRequest)
                                            )

                                            snackbarHostState.showSnackbar("Check your phone for the PIN prompt!")

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            snackbarHostState.showSnackbar("Error: ${e.message}")
                                            isProcessing = false
                                        }
                                    }
                                },
                                enabled = !isProcessing,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Buy Now")
                                }
                            }
                        }
                    }
                }
            }
        ) { padding: PaddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Product Image
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    AsyncImage(
                        model = product.image_url,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Details
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Verified Seller", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(product.description, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}