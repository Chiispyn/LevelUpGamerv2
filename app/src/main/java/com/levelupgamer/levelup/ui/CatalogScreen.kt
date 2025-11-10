package com.levelupgamer.levelup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.data.repository.ReviewRepository
import com.levelupgamer.levelup.model.Product
import com.levelupgamer.levelup.ui.theme.BlueElectric
import com.levelupgamer.levelup.ui.theme.GreenLime
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    navController: NavController,
    products: List<Product>,
    cartProductCodes: List<String>,
    onProductAdded: (String) -> Unit
) {
    val context = LocalContext.current
    val reviewRepository = remember { ReviewRepository((context.applicationContext as MyApp).database.reviewDao()) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = remember(products) {
        listOf("Todos") + products.map { it.category }.distinct().sorted()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { product ->
            val matchesCategory = selectedCategory == "Todos" || product.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar producto...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducts, key = { it.code }) { product ->
                val averageRating by reviewRepository.getAverageRatingForProduct(product.code).collectAsState(initial = null)

                ProductCard(
                    product = product,
                    navController = navController,
                    isInCart = cartProductCodes.contains(product.code),
                    onProductAdded = { onProductAdded(product.code) },
                    averageRating = averageRating
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    navController: NavController,
    isInCart: Boolean,
    onProductAdded: () -> Unit,
    averageRating: Double?
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("productDetail/${product.code}") },
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column {
            Image(
                painter = painterResource(id = product.imageResId),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1)

                RatingBar(rating = averageRating ?: 0.0)

                if (product.quantity in 1..5) {
                    Text("¡Quedan solo ${product.quantity}!", style = MaterialTheme.typography.bodySmall, color = Color.Yellow)
                }
                Text(formatter.format(product.price), fontWeight = FontWeight.Bold, color = BlueElectric)
                Spacer(Modifier.height(8.dp))

                if (isInCart) {
                    Button(
                        onClick = { navController.navigate("cart") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ir al Carrito")
                    }
                } else {
                    Button(
                        onClick = onProductAdded,
                        enabled = product.quantity > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (product.quantity > 0) GreenLime else Color.Gray,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (product.quantity > 0) "Añadir al Carrito" else "Sin Stock")
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBar(rating: Double, maxRating: Int = 5) {
    Row {
        for (i in 1..maxRating) {
            val icon = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Yellow,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
