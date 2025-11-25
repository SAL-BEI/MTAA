package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
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
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mtaa.app.data.Product
import com.mtaa.app.data.ProductRepository
import com.mtaa.app.theme.*
import kotlinx.coroutines.launch

class SellerDashboardScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val repository = remember { ProductRepository() }

        // State to hold our list of products
        var products by remember { mutableStateOf<List<Product>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        // 1. Fetch Data when screen opens
        LaunchedEffect(Unit) {
            products = repository.getMyProducts()
            isLoading = false
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Shop", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MtaaCream),
                    actions = {
                        IconButton(onClick = {
                            // Refresh Button Logic
                            scope.launch {
                                isLoading = true
                                products = repository.getMyProducts()
                                isLoading = false
                            }
                        }) {
                            Icon(Icons.Default.Refresh, "Refresh")
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
                    // Empty State
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No products yet.", color = MtaaSlate)
                        Text("Click + to add your first item.", color = MtaaSlate.copy(alpha = 0.6f))
                    }
                } else {
                    // 2. The Grid of Products
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // 2 columns like a real mall app
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(product)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProductCard(product: Product) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Placeholder for Image (We will add real image loading later)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image", color = Color.Gray)
                }

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
        }
    }
}