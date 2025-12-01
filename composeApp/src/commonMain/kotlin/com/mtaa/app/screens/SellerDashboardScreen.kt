package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
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
import coil3.compose.AsyncImage
import com.mtaa.app.MtaaSupabase
import com.mtaa.app.data.Product
import com.mtaa.app.data.ProductRepository
import com.mtaa.app.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class SellerDashboardScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val repository = remember { ProductRepository() }

        var products by remember { mutableStateOf<List<Product>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        fun refreshProducts() {
            scope.launch {
                val user = MtaaSupabase.client.auth.currentUserOrNull()
                if (user == null) {
                    navigator.replaceAll(LoginScreen())
                    return@launch
                }
                isLoading = true
                products = repository.getMyProducts()
                isLoading = false
            }
        }

        LaunchedEffect(Unit) {
            refreshProducts()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Shop", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MtaaCream),
                    navigationIcon = {
                        // FIX: Explicitly go to BuyerHomeScreen instead of just "popping"
                        IconButton(onClick = {
                            navigator.replaceAll(BuyerHomeScreen())
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Home")
                        }
                    },
                    actions = {
                        IconButton(onClick = { refreshProducts() }) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                        IconButton(onClick = {
                            scope.launch {
                                MtaaSupabase.client.auth.signOut()
                                // FIX: Go to Welcome Screen on Logout
                                navigator.replaceAll(WelcomeScreen())
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, "Logout")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(AddProductScreen()) },
                    containerColor = MtaaOrange,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add Product")
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MtaaCream)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MtaaOrange
                    )
                } else if (products.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No products yet.", color = MtaaSlate)
                        Text("Click + to add your first item.", color = MtaaSlate.copy(alpha = 0.6f))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(
                                product = product,
                                onDelete = {
                                    scope.launch {
                                        if (product.id != null) {
                                            val success = repository.deleteProduct(product.id)
                                            if (success) {
                                                refreshProducts()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProductCard(product: Product, onDelete: () -> Unit) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                Column {
                    AsyncImage(
                        model = product.image_url,
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.LightGray),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = MtaaSlate
                        )
                        Text(
                            text = "KES ${product.price}",
                            color = MtaaOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.White.copy(alpha = 0.7f), androidx.compose.foundation.shape.CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}