package com.levelupgamer.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.repository.OrderRepository
import com.levelupgamer.levelup.model.OrderWithItems
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyOrdersScreen(navController: NavController) {
    val context = LocalContext.current
    val orderRepository = remember { OrderRepository((context.applicationContext as MyApp).database.orderDao()) }
    val userId = remember { UserManager.getLoggedInUserId(context) }
    
    val orders by (userId?.let { orderRepository.getOrdersForUser(it) } ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Aún no has realizado ningún pedido.")
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(orders) { orderWithItems ->
                MyOrderItem(orderWithItems = orderWithItems, navController = navController)
            }
        }
    }
}

@Composable
fun MyOrderItem(orderWithItems: OrderWithItems, navController: NavController) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val order = orderWithItems.order

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { navController.navigate("orderDetail/${order.id}") }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pedido: #${order.id.take(8).uppercase(Locale.ROOT)}", fontWeight = FontWeight.Bold)
            Text("Fecha: ${dateFormatter.format(order.date)}")
            Text("Total: $${order.total}")
        }
    }
}
