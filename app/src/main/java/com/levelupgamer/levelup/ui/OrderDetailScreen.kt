package com.levelupgamer.levelup.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.data.remote.RetrofitInstance
import com.levelupgamer.levelup.data.repository.OrderRepository
import com.levelupgamer.levelup.data.repository.ProductRepository
import com.levelupgamer.levelup.model.OrderWithItems
import com.levelupgamer.levelup.model.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrderDetailScreen(orderId: String, navController: NavController) {
    val context = LocalContext.current
    val orderRepository = remember { OrderRepository((context.applicationContext as MyApp).database.orderDao()) }
    val productRepository = remember { ProductRepository((context.applicationContext as MyApp).database.productDao(), RetrofitInstance.api) }

    var orderWithItems by remember { mutableStateOf<OrderWithItems?>(null) }
    var productsInOrder by remember { mutableStateOf<List<Pair<Product, Int>>>(emptyList()) }

    LaunchedEffect(orderId) {
        launch {
            val orderDetails = orderRepository.getOrderById(orderId)
            if (orderDetails != null) {
                orderWithItems = orderDetails
                val allProducts = productRepository.getAllProducts().first()
                val productMap = allProducts.associateBy { it.code }
                
                productsInOrder = orderDetails.items.mapNotNull { orderItem ->
                    productMap[orderItem.productCode]?.let { product ->
                        Pair(product, orderItem.quantity)
                    }
                }
            }
        }
    }

    orderWithItems?.let { details ->
        val order = details.order
        val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }
        val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Text("Detalle del Pedido", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                Text("Pedido: #${order.id.take(8).uppercase(Locale.ROOT)}", style = MaterialTheme.typography.titleMedium)
                Text("Fecha: ${dateFormatter.format(order.date)}", style = MaterialTheme.typography.bodyMedium)
                Text("Estado: ${order.status}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                Text("Productos", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
            }

            items(productsInOrder) { (product, quantity) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text("Cantidad: $quantity")
                    }
                    Text(formatter.format(product.price * quantity))
                }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text("Subtotal: ${formatter.format(order.subtotal)}")
                    Text("Costo de Envío: ${formatter.format(order.shippingCost)}")
                    Text("Total: ${formatter.format(order.total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
