package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.mtaa.app.MtaaSupabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order as PostgrestOrder
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- LOCAL DATA MODEL ---
// We define this here to ensure this screen always has access to the Order structure.
@Serializable
data class SellerOrder(
    val id: String? = null,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("product_id") val productId: String,
    val amount: Double,
    val status: String = "pending_payment",
    @SerialName("created_at") val createdAt: String? = null
)

class SellerOrdersScreen : Screen {

    @Composable
    override fun Content() {
        var orders by remember { mutableStateOf<List<SellerOrder>>(emptyList()) }
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        // 1. Fetch Orders on Load & Subscribe to Realtime
        LaunchedEffect(Unit) {
            val user = MtaaSupabase.client.auth.currentUserOrNull()
            if (user != null) {
                // A. Initial Fetch
                try {
                    val result = MtaaSupabase.client.from("orders")
                        .select {
                            filter {
                                eq("seller_id", user.id)
                                neq("status", "pending_payment")
                            }
                            order("created_at", PostgrestOrder.DESCENDING)
                        }
                    orders = result.decodeList<SellerOrder>()
                } catch (e: Exception) {
                    println("Error fetching: ${e.message}")
                }

                // B. Realtime Listener (FIXED SYNTAX)
                val channel = MtaaSupabase.client.channel("seller-orders")

                // FIX: Configuration goes inside the curly braces
                val changeFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                    table = "orders"
                    // We removed 'filter' here because it causes access errors.
                    // We will filter manually inside the loop below.
                }

                scope.launch {
                    channel.subscribe()
                    changeFlow.collect { change ->
                        // Refresh list when ANY update comes in
                        try {
                            // Decode the record to check if it belongs to us
                            val updatedOrder = change.decodeRecord<SellerOrder>()

                            // Manual Filter: Only refresh if this order belongs to ME
                            if (updatedOrder.sellerId == user.id) {
                                val result = MtaaSupabase.client.from("orders")
                                    .select {
                                        filter { eq("seller_id", user.id); neq("status", "pending_payment") }
                                        order("created_at", PostgrestOrder.DESCENDING)
                                    }
                                orders = result.decodeList<SellerOrder>()
                            }
                        } catch(e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(title = { Text("Incoming Orders") })
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (orders.isEmpty()) {
                    item { Text("No active orders yet.") }
                }

                items(orders) { order ->
                    OrderCard(order) {
                        // Logic to mark as shipped
                        scope.launch {
                            try {
                                MtaaSupabase.client.from("orders")
                                    .update({ set("status", "shipped") }) {
                                        filter { eq("id", order.id ?: "") }
                                    }
                                snackbarHostState.showSnackbar("Marked as Shipped! 🚚")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    @Composable
    fun OrderCard(order: SellerOrder, onShip: () -> Unit) {
        Card(elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Order #${order.id?.takeLast(4)}", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Amount: KES ${order.amount}",
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", style = MaterialTheme.typography.bodyMedium)
                    StatusChip(order.status)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Only show "Ship" button if funds are locked (Paid)
                if (order.status == "escrow_locked") {
                    Button(
                        onClick = onShip,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Shipped")
                    }
                } else if (order.status == "shipped") {
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                        Text("Waiting for Buyer Confirmation")
                    }
                }
            }
        }
    }

    @Composable
    fun StatusChip(status: String) {
        val (color, text) = when (status) {
            "pending_payment" -> Color.Gray to "Pending"
            "escrow_locked" -> Color(0xFF4CAF50) to "Paid (Escrow)"
            "shipped" -> Color(0xFF2196F3) to "Shipped"
            "completed" -> Color(0xFFFFC107) to "Completed"
            "cancelled" -> Color.Red to "Cancelled"
            else -> Color.Black to status
        }

        Surface(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = color,

                fontWeight = FontWeight.Bold
            )
        }
    }
}