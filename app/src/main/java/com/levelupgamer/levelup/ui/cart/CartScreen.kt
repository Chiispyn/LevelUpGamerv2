package com.levelupgamer.levelup.ui.cart

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.levelupgamer.levelup.R
import com.levelupgamer.levelup.model.Product
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CartScreen(navController: NavController, uiState: CartScreenUiState, viewModel: CartViewModel) {
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val subtotal = uiState.cartItems.sumOf { (product, quantity) -> product.price * quantity }
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (uiState.cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.cartItems, key = { it.first.code }) { item ->
                    CartItemRow(
                        item = item,
                        onRemove = { viewModel.onProductRemoved(item.first.code) },
                        onQuantityChanged = { newQuantity -> viewModel.onQuantityChanged(item.first.code, newQuantity) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Subtotal: ${formatter.format(subtotal)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigate("checkout") }, modifier = Modifier.fillMaxWidth()) {
                Text("Finalizar Compra")
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: Pair<Product, Int>,
    onRemove: () -> Unit,
    onQuantityChanged: (Int) -> Unit
) {
    val (product, quantity) = item
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = if (product.imageUrl != null) product.imageUrl else product.imageResId,
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = product.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text(formatter.format(product.price), style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChanged(quantity - 1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Quitar uno")
                }
                Text("$quantity", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(
                    onClick = { onQuantityChanged(quantity + 1) },
                    enabled = quantity < product.quantity
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir uno")
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar del carrito")
            }
        }
    }
}
