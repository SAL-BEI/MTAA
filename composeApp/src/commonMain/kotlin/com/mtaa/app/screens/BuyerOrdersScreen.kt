package com.mtaa.app.screens

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

// --- LOCAL DATA MODEL FOR BUYER ---
@Serializable
data class BuyerOrder(
    val id: String? = null,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("product_id") val productId: String,
    val amount: Double,
    val status: String = "pending_payment",
    @SerialName("created_at") val createdAt: String? = null
)

class BuyerOrdersScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        var orders by remember { mutableStateOf<List<BuyerOrder>>(emptyList()) }
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            val user = MtaaSupabase.client.auth.currentUserOrNull()
            if (user != null) {
                // A. Initial Fetch
                try {
                    val result = MtaaSupabase.client.from("orders")
                        .select {
                            filter { eq("buyer_id", user.id) }
                            order("created_at", PostgrestOrder.DESCENDING)
                        }
                    orders = result.decodeList<BuyerOrder>()
                } catch (e: Exception) {
                    println("Error fetching buyer orders: ${e.message}")
                }

                // B. Realtime Listener
                val channel = MtaaSupabase.client.channel("buyer-orders")

                // Correct V3 Syntax
                val changeFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                    table = "orders"
                }

                scope.launch {
                    channel.subscribe()
                    changeFlow.collect { change ->
                        try {
                            val updated = change.decodeRecord<BuyerOrder>()
                            // Only update if this order belongs to ME (the buyer)
                            if (updated.buyerId == user.id) {
                                val result = MtaaSupabase.client.from("orders")
                                    .select {
                                        filter { eq("buyer_id", user.id) }
                                        order("created_at", PostgrestOrder.DESCENDING)
                                    }
                                orders = result.decodeList<BuyerOrder>()
                            }
                        } catch(e: Exception) { e.printStackTrace() }
                    }
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = { TopAppBar(title = { Text("My Purchases") }) }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                if (orders.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("You haven't bought anything yet.", color = Color.Gray)
                        }
                    }
                }

                items(orders) { order ->
                    BuyerOrderCard(order) {
                        // LOGIC: Confirm Receipt -> Release Funds
                        scope.launch {
                            try {
                                MtaaSupabase.client.from("orders")
                                    .update({ set("status", "completed") }) {
                                        filter { eq("id", order.id ?: "") }
                                    }
                                snackbarHostState.showSnackbar("Order Completed! Funds Released. 🎉")
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
    fun BuyerOrderCard(order: BuyerOrder, onConfirm: () -> Unit) {
        Card(elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Order #${order.id?.takeLast(4)}", style = MaterialTheme.typography.titleMedium)
                    Text("KES ${order.amount}", fontWeight = FontWeight.Bold, color = Color(0xFFFFA500))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", style = MaterialTheme.typography.bodyMedium)
                    StatusChip(order.status)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The Magic Button: Only visible when Shipped
                if (order.status == "shipped") {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirm Receipt (Release Funds)")
                    }
                } else if (order.status == "completed") {
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                        Text("Transaction Closed")
                    }
                } else if (order.status == "escrow_locked") {
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                        Text("Waiting for Seller to Ship...")
                    }
                }
            }
        }
    }

    @Composable
    fun StatusChip(status: String) {
        val (color, text) = when (status) {
            "pending_payment" -> Color.Gray to "Pending"
            "escrow_locked" -> Color.Blue to "Paid (Escrow)"
            "shipped" -> Color(0xFF2196F3) to "Shipped (On way)"
            "completed" -> Color(0xFF4CAF50) to "Completed"
            "cancelled" -> Color.Red to "Cancelled"
            else -> Color.Black to status
        }
        Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)) {
            Text(text, modifier = Modifier.padding(8.dp, 4.dp), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}