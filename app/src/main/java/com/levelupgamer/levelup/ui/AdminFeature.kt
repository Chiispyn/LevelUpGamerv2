package com.levelupgamer.app.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.R
import com.levelupgamer.levelup.model.*
import com.levelupgamer.levelup.repository.EventRepository
import com.levelupgamer.levelup.repository.OrderRepository
import com.levelupgamer.levelup.repository.ProductRepository
import com.levelupgamer.levelup.repository.RewardRepository
import com.levelupgamer.levelup.repository.UserRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

// --- NAVIGATION ROOT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeature(mainNavController: NavHostController) {
    val adminNavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(getAdminScreenTitle(currentRoute)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    if (adminNavController.previousBackStackEntry != null) {
                        IconButton(onClick = { adminNavController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
             if (currentRoute == "adminProducts" || currentRoute == "adminEvents" || currentRoute == "adminRewards") {
                FloatingActionButton(onClick = {
                    when (currentRoute) {
                        "adminProducts" -> adminNavController.navigate("productEdit")
                        "adminEvents" -> adminNavController.navigate("eventEdit")
                        "adminRewards" -> adminNavController.navigate("rewardEdit")
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = adminNavController, startDestination = "adminHome", modifier = Modifier.padding(innerPadding)) {
            composable("adminHome") { AdminHomeScreen(adminNavController, mainNavController) }
            composable("adminProducts") { AdminProductsScreen(adminNavController) }
            composable("adminEvents") { AdminEventsScreen(navController = adminNavController) }
            composable("adminRewards") { AdminRewardsScreen(navController = adminNavController) }
            composable("adminOrders") { AdminOrdersScreen(navController = adminNavController) }
            composable("adminUsers") { AdminUsersScreen(navController = adminNavController) }
            composable("productEdit?productCode={productCode}", arguments = listOf(navArgument("productCode") { type = NavType.StringType; nullable = true })) {
                ProductEditScreen(adminNavController, it.arguments?.getString("productCode"), snackbarHostState)
            }
            composable("eventEdit?eventId={eventId}", arguments = listOf(navArgument("eventId") { type = NavType.StringType; nullable = true })) {
                EventEditScreen(adminNavController, it.arguments?.getString("eventId"), snackbarHostState)
            }
            composable("rewardEdit?rewardId={rewardId}", arguments = listOf(navArgument("rewardId") { type = NavType.StringType; nullable = true })) {
                RewardEditScreen(adminNavController, it.arguments?.getString("rewardId"), snackbarHostState)
            }
            composable("adminOrderDetail/{orderId}", arguments = listOf(navArgument("orderId") { type = NavType.StringType })) {
                AdminOrderDetailScreen(it.arguments?.getString("orderId") ?: "")
            }
        }
    }
}

// --- ADMIN HOME SCREEN ---
@Composable
private fun AdminHomeScreen(navController: NavController, mainNavController: NavHostController) {
    val context = LocalContext.current
    val orderRepository = remember { OrderRepository((context.applicationContext as MyApp).database.orderDao()) }
    val orders by orderRepository.getAllOrders().collectAsState(initial = emptyList())

    val (totalSales, pendingOrders) = remember(orders) {
        val total = orders.sumOf { it.order.total }
        val pending = orders.count { it.order.status != OrderStatus.ENTREGADO.name }
        Pair(total, pending)
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Panel de Administración", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(24.dp))

        SalesSummaryCard(totalSales = totalSales, pendingOrders = pendingOrders)

        Spacer(Modifier.height(32.dp))

        Button(onClick = { navController.navigate("adminProducts") }, modifier = Modifier.fillMaxWidth()) { Text("Gestionar Productos") }
        Button(onClick = { navController.navigate("adminEvents") }, modifier = Modifier.fillMaxWidth()) { Text("Gestionar Eventos") }
        Button(onClick = { navController.navigate("adminRewards") }, modifier = Modifier.fillMaxWidth()) { Text("Gestionar Recompensas") }
        Button(onClick = { navController.navigate("adminOrders") }, modifier = Modifier.fillMaxWidth()) { Text("Gestionar Pedidos") }
        Button(onClick = { navController.navigate("adminUsers") }, modifier = Modifier.fillMaxWidth()) { Text("Gestionar Usuarios") }

        Spacer(Modifier.weight(1f))

        Button(onClick = {
            context.getSharedPreferences("user", Context.MODE_PRIVATE).edit().clear().apply()
            mainNavController.navigate("login") { popUpTo(mainNavController.graph.id) { inclusive = true } }
        }, modifier = Modifier.fillMaxWidth()) { Text("Cerrar Sesión") }
    }
}

@Composable
private fun SalesSummaryCard(totalSales: Double, pendingOrders: Int) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen de Ventas", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Total Vendido: ${formatter.format(totalSales)}", style = MaterialTheme.typography.bodyLarge)
            Text("Pedidos Pendientes: $pendingOrders", style = MaterialTheme.typography.bodyLarge)
        }
    }
}


// --- TITLE HELPER ---
private fun getAdminScreenTitle(route: String?): String {
    return when {
        route == "adminHome" -> "Panel de Admin"
        route == "adminProducts" -> "Gestionar Productos"
        route?.startsWith("productEdit") == true -> "Editar Producto"
        route == "adminEvents" -> "Gestionar Eventos"
        route?.startsWith("eventEdit") == true -> "Editar Evento"
        route == "adminRewards" -> "Gestionar Recompensas"
        route?.startsWith("rewardEdit") == true -> "Editar Recompensa"
        route == "adminOrders" -> "Gestionar Pedidos"
        route?.startsWith("adminOrderDetail") == true -> "Detalle del Pedido"
        route == "adminUsers" -> "Gestionar Usuarios"
        else -> "Admin"
    }
}

// --- ADMIN PRODUCTS ---
@Composable
private fun AdminProductsScreen(navController: NavController) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository((context.applicationContext as MyApp).database.productDao()) }
    val products by productRepository.getAllProducts().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(products, key = { it.code }) { product ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text("Stock: ${product.quantity}")
                    }
                    IconButton(onClick = { navController.navigate("productEdit?productCode=${product.code}") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { scope.launch { productRepository.delete(product) } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditScreen(navController: NavController, productCode: String?, snackbarHostState: SnackbarHostState) {
    val isEditing = productCode != null
    val context = LocalContext.current
    val productRepository = remember { ProductRepository((context.applicationContext as MyApp).database.productDao()) }
    val scope = rememberCoroutineScope()

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val productCategories = listOf("Consolas", "Videojuegos", "Accesorios", "Poleras", "Figuras")
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(productCode) {
        if (isEditing) {
            scope.launch {
                productToEdit = productRepository.getProductByCode(productCode!!)
                productToEdit?.let {
                    code = it.code; name = it.name; category = it.category; price = it.price.toString(); quantity = it.quantity.toString(); description = it.description
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código (SKU)") }, enabled = !isEditing, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenuBox(expanded = isCategoryMenuExpanded, onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }, modifier = Modifier.padding(vertical = 8.dp)) {
            OutlinedTextField(value = category, onValueChange = {}, label = { Text("Categoría") }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = isCategoryMenuExpanded, onDismissRequest = { isCategoryMenuExpanded = false }) {
                productCategories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; isCategoryMenuExpanded = false }) }
            }
        }

        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth().height(100.dp))

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            val newOrUpdatedProduct = Product(
                code = code,
                name = name,
                category = category,
                price = price.toIntOrNull() ?: 0,
                quantity = quantity.toIntOrNull() ?: 0,
                description = description,
                imageResId = productToEdit?.imageResId ?: R.drawable.ic_launcher_foreground, // Conservar imagen existente o usar una por defecto
                averageRating = productToEdit?.averageRating ?: 0f
            )

            scope.launch {
                if (isEditing) productRepository.update(newOrUpdatedProduct) else productRepository.insert(newOrUpdatedProduct)
                snackbarHostState.showSnackbar("Producto guardado con éxito")
                navController.popBackStack()
            }
        }, modifier = Modifier.fillMaxWidth()) { Text(if (isEditing) "Guardar Cambios" else "Añadir Producto") }
    }
}

// --- ADMIN EVENTS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminEventsScreen(navController: NavController) {
    val context = LocalContext.current
    val eventRepository = remember { EventRepository((context.applicationContext as MyApp).database.eventDao()) }
    val events by eventRepository.getAllEvents().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(events, key = { it.id }) { event ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.name, fontWeight = FontWeight.Bold)
                        Text(event.date)
                    }
                    IconButton(onClick = { navController.navigate("eventEdit?eventId=${event.id}") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { scope.launch { eventRepository.delete(event) } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventEditScreen(navController: NavController, eventId: String?, snackbarHostState: SnackbarHostState) {
    val isEditing = eventId != null
    val context = LocalContext.current
    val eventRepository = remember { EventRepository((context.applicationContext as MyApp).database.eventDao()) }
    val scope = rememberCoroutineScope()

    var eventToEdit by remember { mutableStateOf<Event?>(null) }
    var id by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var inscriptionPoints by remember { mutableStateOf("") }
    var prizePoints by remember { mutableStateOf("") }

    LaunchedEffect(eventId) {
        if (isEditing) {
            scope.launch {
                eventToEdit = eventRepository.getEventById(eventId!!)
                eventToEdit?.let {
                    id = it.id
                    name = it.name
                    description = it.description
                    date = it.date
                    time = it.time
                    locationName = it.locationName
                    inscriptionPoints = it.inscriptionPoints.toString()
                    prizePoints = it.prizePoints.toString()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del Evento") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Fecha (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Hora (HH:MM)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = locationName, onValueChange = { locationName = it }, label = { Text("Lugar") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = inscriptionPoints, onValueChange = { inscriptionPoints = it }, label = { Text("Puntos de Inscripción") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = prizePoints, onValueChange = { prizePoints = it }, label = { Text("Puntos de Premio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            val newOrUpdatedEvent = Event(
                id = id,
                name = name,
                description = description,
                date = date,
                time = time,
                locationName = locationName,
                inscriptionPoints = inscriptionPoints.toIntOrNull() ?: 0,
                prizePoints = prizePoints.toIntOrNull() ?: 0
            )

            scope.launch {
                if (isEditing) {
                    eventRepository.updateEvent(newOrUpdatedEvent)
                } else {
                    eventRepository.insertEvent(newOrUpdatedEvent)
                }
                snackbarHostState.showSnackbar("Evento guardado con éxito")
                navController.popBackStack()
            }
        }, modifier = Modifier.fillMaxWidth()) { Text(if (isEditing) "Guardar Cambios" else "Añadir Evento") }
    }
}


// --- ADMIN REWARDS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminRewardsScreen(navController: NavController) {
    val context = LocalContext.current
    val rewardRepository = remember { RewardRepository((context.applicationContext as MyApp).database.rewardDao()) }
    val rewards by rewardRepository.getAllRewards().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(rewards, key = { it.id }) { reward ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reward.title, fontWeight = FontWeight.Bold)
                        Text("Costo: ${reward.pointsCost} puntos. Stock: ${reward.stock ?: "Ilimitado"}")
                    }
                    IconButton(onClick = { navController.navigate("rewardEdit?rewardId=${reward.id}") }) { Icon(Icons.Default.Edit, "Editar") }
                    IconButton(onClick = { scope.launch { rewardRepository.deleteReward(reward) } }) { Icon(Icons.Default.Delete, "Eliminar") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardEditScreen(navController: NavController, rewardId: String?, snackbarHostState: SnackbarHostState) {
    val isEditing = rewardId != null
    val context = LocalContext.current
    val rewardRepository = remember { RewardRepository((context.applicationContext as MyApp).database.rewardDao()) }
    val scope = rememberCoroutineScope()

    var rewardToEdit by remember { mutableStateOf<Reward?>(null) }
    var id by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pointsCost by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(RewardType.DISCOUNT_AMOUNT) }
    var value by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var productCode by remember { mutableStateOf("") }
    var isTypeMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(rewardId) {
        if (isEditing) {
            scope.launch {
                rewardToEdit = rewardRepository.getReward(rewardId!!)
                rewardToEdit?.let { r ->
                    id = r.id; title = r.title; description = r.description; pointsCost = r.pointsCost.toString()
                    type = RewardType.valueOf(r.type); value = r.value?.toString() ?: ""; stock = r.stock?.toString() ?: ""; productCode = r.productCode ?: ""
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("ID Recompensa") }, enabled = !isEditing, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth().height(100.dp))
        OutlinedTextField(value = pointsCost, onValueChange = { pointsCost = it }, label = { Text("Costo en Puntos") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenuBox(expanded = isTypeMenuExpanded, onExpandedChange = { isTypeMenuExpanded = it }) {
             OutlinedTextField(value = type.name, onValueChange = {}, label = { Text("Tipo de Recompensa") }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeMenuExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = isTypeMenuExpanded, onDismissRequest = { isTypeMenuExpanded = false }) {
                RewardType.values().forEach { rt -> DropdownMenuItem(text = { Text(rt.name) }, onClick = { type = rt; isTypeMenuExpanded = false }) }
            }
        }

        when (type) {
            RewardType.DISCOUNT_PERCENTAGE ->
                OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Porcentaje de Descuento (ej: 10)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            RewardType.DISCOUNT_AMOUNT ->
                OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Monto de Descuento (ej: 5000)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            RewardType.FREE_PRODUCT ->
                OutlinedTextField(value = productCode, onValueChange = { productCode = it }, label = { Text("Código de Producto (SKU)") }, modifier = Modifier.fillMaxWidth())
            RewardType.FREE_SHIPPING -> {}
        }

        OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock (dejar en blanco para ilimitado)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            val finalStock = if (stock.isBlank()) null else stock.toIntOrNull()
            val finalValue = if (value.isBlank()) null else value.toDoubleOrNull()
            val finalProductCode = if (productCode.isBlank()) null else productCode

            val reward = Reward(id, title, description, pointsCost.toIntOrNull() ?: 0, type.name, finalValue, finalStock, finalProductCode)
            scope.launch {
                if (isEditing) rewardRepository.updateReward(reward) else rewardRepository.addReward(reward)
                snackbarHostState.showSnackbar("Recompensa guardada"); navController.popBackStack()
            }
        }, modifier = Modifier.fillMaxWidth()) { Text(if (isEditing) "Guardar Cambios" else "Añadir Recompensa") }
    }
}

// --- ADMIN ORDERS & USERS ---
private data class EnrichedOrderItem(
    val quantity: Int,
    val productName: String,
    val price: Int
)

@Composable
private fun AdminOrdersScreen(navController: NavController) {
    val context = LocalContext.current
    val orderRepository = remember { OrderRepository((context.applicationContext as MyApp).database.orderDao()) }
    val orders by orderRepository.getAllOrders().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("No hay pedidos para mostrar.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(orders, key = { it.order.id }) { orderWithItems ->
                AdminOrderItem(
                    orderWithItems = orderWithItems,
                    onClick = { navController.navigate("adminOrderDetail/${orderWithItems.order.id}") },
                    onStatusChange = { newStatus ->
                        scope.launch {
                            orderRepository.update(orderWithItems.order.copy(status = newStatus.name))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminOrderDetailScreen(orderId: String) {
    val context = LocalContext.current
    val orderRepository = remember { OrderRepository((context.applicationContext as MyApp).database.orderDao()) }
    val productRepository = remember { ProductRepository((context.applicationContext as MyApp).database.productDao()) }
    var orderWithItems by remember { mutableStateOf<OrderWithItems?>(null) }
    var enrichedItems by remember { mutableStateOf<List<EnrichedOrderItem>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(orderId) {
        scope.launch {
            val fetchedOrder = orderRepository.getOrderById(orderId)
            orderWithItems = fetchedOrder

            fetchedOrder?.let { details ->
                enrichedItems = details.items.mapNotNull { item ->
                    productRepository.getProductByCode(item.productCode)?.let { product ->
                        EnrichedOrderItem(
                            quantity = item.quantity,
                            productName = product.name,
                            price = product.price
                        )
                    }
                }
            }
        }
    }

    orderWithItems?.let { details ->
        val order = details.order
        var currentStatus by remember(order.status) { mutableStateOf(OrderStatus.valueOf(order.status)) }
        var isStatusMenuExpanded by remember { mutableStateOf(false) }
        val isEnabled = currentStatus != OrderStatus.ENTREGADO

        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Pedido #${order.id.take(8).uppercase(Locale.ROOT)}", style = MaterialTheme.typography.headlineMedium)
            Text("Fecha: ${dateFormatter.format(order.date)}", style = MaterialTheme.typography.bodyMedium)
            Text("Cliente ID: ${order.userId}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Estado del Pedido", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = isStatusMenuExpanded,
                onExpandedChange = { if (isEnabled) isStatusMenuExpanded = !isStatusMenuExpanded },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = currentStatus.name,
                    onValueChange = {},
                    label = { Text("Estado") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = isEnabled
                )
                ExposedDropdownMenu(
                    expanded = isStatusMenuExpanded,
                    onDismissRequest = { isStatusMenuExpanded = false }
                ) {
                    OrderStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                                currentStatus = status
                                isStatusMenuExpanded = false
                                scope.launch {
                                    orderRepository.update(order.copy(status = status.name))
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Items del Pedido", style = MaterialTheme.typography.titleMedium)
            if (enrichedItems.isEmpty() && details.items.isNotEmpty()) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
            } else {
                enrichedItems.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("${item.quantity}x ${item.productName}", modifier = Modifier.weight(1f))
                        Text(numberFormat.format(item.price))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Total: ${numberFormat.format(order.total)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
@Composable
private fun AdminUsersScreen(navController: NavController) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository((context.applicationContext as MyApp).database.userDao()) }
    val users by userRepository.getAllUsers().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    if (users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("No hay usuarios para mostrar.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users, key = { it.id }) { user ->
                UserListItem(user = user) {
                    scope.launch {
                        userRepository.update(user.copy(isActive = !user.isActive))
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(user: User, onToggleStatus: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.email)
            }
            Switch(
                checked = user.isActive,
                onCheckedChange = { onToggleStatus() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminOrderItem(orderWithItems: OrderWithItems, onClick: () -> Unit, onStatusChange: (OrderStatus) -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val order = orderWithItems.order
    var isStatusMenuExpanded by remember { mutableStateOf(false) }
    var currentStatus by remember(order.status) { mutableStateOf(OrderStatus.valueOf(order.status)) }
    val isEnabled = currentStatus != OrderStatus.ENTREGADO

    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pedido: #${order.id.take(8).uppercase(Locale.ROOT)}", fontWeight = FontWeight.Bold)
            Text("Fecha: ${dateFormatter.format(order.date)}")
            Text("Total: $${order.total}")
            Text("Usuario ID: ${order.userId}")
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = isStatusMenuExpanded,
                onExpandedChange = { if (isEnabled) isStatusMenuExpanded = !isStatusMenuExpanded }
            ) {
                OutlinedTextField(
                    value = currentStatus.name,
                    onValueChange = {},
                    label = { Text("Estado") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = isEnabled
                )
                ExposedDropdownMenu(
                    expanded = isStatusMenuExpanded,
                    onDismissRequest = { isStatusMenuExpanded = false }
                ) {
                    OrderStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                                currentStatus = status
                                isStatusMenuExpanded = false
                                onStatusChange(status)
                            }
                        )
                    }
                }
            }
        }
    }
}
