package com.levelupgamer.levelup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.levelupgamer.levelup.data.repository.*
import com.levelupgamer.levelup.model.*
import com.levelupgamer.levelup.util.ShippingManager
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

@Composable
fun CheckoutScreen(navController: NavController, cartItems: List<Pair<Product, Int>>, onCartCleared: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Repositories
    val orderRepository = remember { OrderRepository((context.applicationContext as MyApp).database.orderDao()) }
    val productRepository = remember { ProductRepository((context.applicationContext as MyApp).database.productDao(), RetrofitInstance.api) }
    val addressRepository = remember { AddressRepository((context.applicationContext as MyApp).database.addressDao()) }
    val rewardRepository = remember { RewardRepository((context.applicationContext as MyApp).database.rewardDao()) }
    val userRewardRepository = remember { UserRewardRepository((context.applicationContext as MyApp).database.userRewardDao()) }
    val userRepository = remember { UserRepository((context.applicationContext as MyApp).database.userDao()) }

    // State
    val userId = remember { UserManager.getLoggedInUserId(context) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    val addresses by (userId?.let { addressRepository.getAddressesForUser(it) } ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val activeRewards by remember(userId) {
        if (userId != null) {
            userRewardRepository.getUserRewards(userId).combine(rewardRepository.getAllRewards()) { userRewards, allRewards ->
                val userRewardIds = userRewards.map { it.rewardId }.toSet()
                allRewards.filter { it.id in userRewardIds }
            }
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())
    
    LaunchedEffect(userId) {
        if (userId != null) {
            currentUser = userRepository.getUserById(userId)
        }
    }

    var selectedAddress by remember(addresses) { mutableStateOf(addresses.find { it.isPrimary }) }
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var selectedReward by remember { mutableStateOf<Reward?>(null) }

    // --- LÓGICA DE CÁLCULO DINÁMICO ---
    val subtotal = remember(cartItems) { cartItems.sumOf { (product, quantity) -> product.price * quantity }.toDouble() }
    val shippingInfo = selectedAddress?.let { ShippingManager.getShippingInfo(it.region) }
    val baseShippingCost = shippingInfo?.cost?.toDouble() ?: 0.0

    val discountAmount = when (selectedReward?.type) {
        RewardType.DISCOUNT_PERCENTAGE.name -> subtotal * ((selectedReward?.value ?: 0.0) / 100.0)
        RewardType.DISCOUNT_AMOUNT.name -> selectedReward?.value ?: 0.0
        else -> 0.0
    }

    val finalShippingCost = if (selectedReward?.type == RewardType.FREE_SHIPPING.name) 0.0 else baseShippingCost

    val total = (subtotal - discountAmount).coerceAtLeast(0.0) + finalShippingCost
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    
    val canCheckout = selectedAddress != null && selectedPaymentMethod != null && currentUser?.isActive == true

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Finalizar Compra", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        // Sección de Dirección
        Text("1. Selecciona una dirección de envío", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
            items(addresses, key = { it.id }) { address ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { selectedAddress = address }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedAddress?.id == address.id, onClick = { selectedAddress = address })
                    Column {
                        Text("${address.street} ${address.numberOrApt}", fontWeight = FontWeight.Bold)
                        Text("${address.commune}, ${address.region}")
                    }
                }
            }
        }
        Button(onClick = { navController.navigate("address") }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Gestionar Direcciones")
        }

        if (shippingInfo != null && selectedAddress != null) {
            Text("Costo de Envío: ${formatter.format(baseShippingCost)} (Estimado: ${shippingInfo.estimatedDays})", style = MaterialTheme.typography.bodyMedium)
        }
        
        // Sección de Recompensas
        if (activeRewards.isNotEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Text("2. Aplica una recompensa (Opcional)", style = MaterialTheme.typography.titleMedium)
            activeRewards.forEach { reward ->
                Row(modifier = Modifier.clickable { selectedReward = if (selectedReward == reward) null else reward }.padding(vertical = 4.dp)) {
                    RadioButton(selected = selectedReward?.id == reward.id, onClick = { selectedReward = if (selectedReward == reward) null else reward })
                    Text(reward.title)
                }
            }
        }

        // Sección de Pago
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Text("3. Selecciona un método de pago", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { selectedPaymentMethod = "WebPay" }) { Text("WebPay") }
            Button(onClick = { selectedPaymentMethod = "MercadoPago" }) { Text("MercadoPago") }
        }

        Spacer(Modifier.weight(1f))

        // --- RESUMEN DE COMPRA DINÁMICO ---
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            Text("Subtotal: ${formatter.format(subtotal)}")
            Text("Envío: ${formatter.format(finalShippingCost)}")
            if (discountAmount > 0) {
                Text(
                    text = "Descuento (${selectedReward?.title ?: ""}): -${formatter.format(discountAmount)}", 
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Text("Total: ${formatter.format(total)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        if (currentUser?.isActive == false) {
            Text(
                text = "Tu cuenta está suspendida. No puedes realizar compras.", 
                color = MaterialTheme.colorScheme.error, 
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
            )
        }

        // Botón de Pagar
        Button(
            onClick = {
                scope.launch {
                    if (userId != null && selectedAddress != null) {
                        val newOrderId = UUID.randomUUID().toString()
                        val newOrder = Order(
                            id = newOrderId,
                            userId = userId,
                            subtotal = subtotal,
                            shippingCost = finalShippingCost,
                            total = total
                        )
                        val orderItems = cartItems.map { (product, quantity) ->
                            OrderItem(orderId = newOrderId, productCode = product.code, quantity = quantity)
                        }
                        
                        orderRepository.addOrder(newOrder, orderItems)

                        cartItems.forEach { (product, quantity) ->
                            val newStock = product.quantity - quantity
                            productRepository.update(product.copy(quantity = newStock))
                        }

                        selectedReward?.let { userRewardRepository.delete(UserReward(userId, it.id)) }
                        
                        val pointsEarned = (subtotal / 1000).toInt() * 10
                        if (currentUser != null) {
                            val updatedUser = currentUser!!.copy(points = currentUser!!.points + pointsEarned)
                            userRepository.update(updatedUser)
                        }
                        
                        onCartCleared()
                        navController.navigate("orderConfirmation/${newOrder.id}/$pointsEarned") { 
                            popUpTo(navController.graph.startDestinationId) { inclusive = true } 
                        }
                    }
                }
            },
            enabled = canCheckout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pagar y Finalizar Pedido")
        }
    }
}
