package com.mtaa.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.mtaa.app.MtaaSupabase
import com.mtaa.app.data.AuthRepository
import com.mtaa.app.data.Product
import com.mtaa.app.data.ProductRepository
import com.mtaa.app.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class BuyerHomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val repository = remember { ProductRepository() }
        val authRepo = remember { AuthRepository() }

        var products by remember { mutableStateOf<List<Product>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        // Fetch products on load
        LaunchedEffect(Unit) {
            products = repository.getAllProducts()
            isLoading = false
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, "Logo", tint = MtaaOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mtaa Market", fontWeight = FontWeight.Bold, color = MtaaSlate)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MtaaCream),
                    actions = {
                        // Button to switch to Seller Mode
                        TextButton(onClick = {
                            scope.launch {
                                val user = MtaaSupabase.client.auth.currentUserOrNull()
                                if (user == null) {
                                    // 1. Not Logged In -> Go to Login
                                    navigator.push(LoginScreen())
                                } else {
                                    // 2. Logged In -> Check for Shop
                                    val hasShop = authRepo.hasBusiness()
                                    if (hasShop) {
                                        navigator.push(SellerDashboardScreen())
                                    } else {
                                        navigator.push(SellerOnboardingScreen())
                                    }
                                }
                            }
                        }) {
                            Text("Sell", color = MtaaOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MtaaCream)
                    .padding(padding)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MtaaOrange)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Hero Section (Span full width)
                        item(span = { GridItemSpan(2) }) {
                            HeroSection()
                        }

                        // 2. Section Title
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                "Fresh Arrivals",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MtaaSlate,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        // 3. Product Grid
                        items(products) { product ->
                            BuyerProductCard(product, navigator)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HeroSection() {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(180.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Gradient Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(MtaaOrange, Color(0xFFFFD180))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        "Trusted Shopping",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        "Verified sellers in top Nairobi buildings.",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }

    @Composable
    fun BuyerProductCard(product: Product, navigator: cafe.adriel.voyager.navigator.Navigator) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().clickable {
                // Navigate to Product Detail Screen
                navigator.push(ProductDetailScreen(product))
            }
        ) {
            Column {
                AsyncImage(
                    model = product.image_url,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
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
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Verified Seller ✓",
                        color = MtaaGreen,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}