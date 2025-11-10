package com.levelupgamer.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.data.repository.ProductRepository
import com.levelupgamer.levelup.ui.CatalogScreen
import com.levelupgamer.levelup.ui.HomeScreen
import com.levelupgamer.levelup.ui.address.AddressScreen
import com.levelupgamer.levelup.ui.cart.CartScreen
import com.levelupgamer.levelup.ui.cart.CartViewModel
import com.levelupgamer.levelup.ui.navigation.NavScreen
import com.levelupgamer.levelup.ui.rewardsshop.RewardsShopScreen
import com.levelupgamer.levelup.ui.viewmodel.ViewModelFactory
import com.levelupgamer.levelup.ui.ProductDetailScreen
import com.levelupgamer.levelup.ui.eventdetail.EventDetailScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainNavController: NavController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val factory = ViewModelFactory(context)
    val cartViewModel: CartViewModel = viewModel(factory = factory)
    val productRepository = remember { ProductRepository((context.applicationContext as MyApp).database.productDao()) }
    
    val products by productRepository.getAllProducts().collectAsState(initial = emptyList())
    val cartState by cartViewModel.uiState.collectAsState()

    val bottomNavScreens = listOf(NavScreen.Home, NavScreen.Store, NavScreen.Redeem, NavScreen.Community, NavScreen.Profile)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(getScreenTitle(currentRoute, bottomNavScreens)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    if (currentRoute != null && currentRoute !in bottomNavScreens.map { it.route }) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("cart") }) {
                        BadgedBox(badge = { 
                            if (cartState.cartItems.isNotEmpty()) {
                                Badge { Text("${cartState.cartItems.sumOf { it.second }}") }
                            }
                        }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito de Compras")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = NavScreen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(NavScreen.Home.route) { HomeScreen(navController, products, cartState.cartItems.map { it.first }, cartViewModel::onProductAdded) }
            composable(NavScreen.Store.route) { CatalogScreen(navController, products, cartState.cartItems.map { it.first.code }, cartViewModel::onProductAdded) }
            composable(NavScreen.Redeem.route) { RewardsShopScreen() }
            composable(NavScreen.Community.route) { CommunityScreen(navController) }
            composable(NavScreen.Profile.route) { 
                ProfileScreen(navController = navController, onLogout = { 
                    mainNavController.navigate("login") {
                        popUpTo(mainNavController.graph.id) { inclusive = true }
                    }
                })
            }
            composable("cart") { CartScreen(navController, cartState, cartViewModel) }
            composable("checkout") { CheckoutScreen(navController, cartState.cartItems, cartViewModel::onCartCleared) }
            composable("myOrders") { MyOrdersScreen(navController) }
            composable("editProfile") { EditProfileScreen(navController) }
            composable("address") { AddressScreen(navController) }
            composable("orderConfirmation/{orderId}/{pointsEarned}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val pointsEarned = backStackEntry.arguments?.getString("pointsEarned")?.toInt() ?: 0
                OrderConfirmationScreen(navController, orderId, pointsEarned)
            }
            composable(
                route = "productDetail/{productCode}",
                arguments = listOf(navArgument("productCode") { type = NavType.StringType })
            ) { backStackEntry ->
                val productCode = backStackEntry.arguments?.getString("productCode")
                if (productCode != null) {
                    ProductDetailScreen(productCode = productCode, navController = navController)
                } else {
                    navController.popBackStack()
                }
            }
            composable(
                route = "orderDetail/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId")
                if (orderId != null) {
                    OrderDetailScreen(orderId = orderId, navController = navController)
                } else {
                    navController.popBackStack()
                }
            }
            composable(
                route = "event/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                if (eventId != null) {
                    EventDetailScreen(eventId = eventId, onNavigateBack = { navController.navigateUp() })
                } else {
                    navController.popBackStack()
                }
            }
        }
    }
}

private fun getScreenTitle(route: String?, bottomNavScreens: List<NavScreen>): String {
    val mainScreen = bottomNavScreens.find { it.route == route }
    if (mainScreen != null) return mainScreen.title

    return when {
        route == "cart" -> "Carrito"
        route == "checkout" -> "Finalizar Compra"
        route == "myOrders" -> "Mis Compras"
        route == "editProfile" -> "Editar Perfil"
        route == "address" -> "Gestión de Dirección"
        route?.startsWith("orderConfirmation") == true -> "Compra Finalizada"
        route?.startsWith("productDetail") == true -> "Detalle del Producto"
        route?.startsWith("orderDetail") == true -> "Detalle del Pedido"
        route?.startsWith("event/") == true -> "Detalle del Evento" 
        else -> "Level-Up Gamer"
    }
}
