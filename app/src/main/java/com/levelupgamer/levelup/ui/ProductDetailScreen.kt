package com.levelupgamer.levelup.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.R
import com.levelupgamer.levelup.data.remote.RetrofitInstance
import com.levelupgamer.levelup.data.repository.ProductRepository
import com.levelupgamer.levelup.model.Product
import com.levelupgamer.levelup.ui.cart.CartViewModel
import com.levelupgamer.levelup.ui.reviews.ReviewSection
import com.levelupgamer.levelup.ui.reviews.ReviewViewModel
import com.levelupgamer.levelup.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductDetailScreen(productCode: String, navController: NavController) {
    val context = LocalContext.current
    val productRepository = remember { ProductRepository((context.applicationContext as MyApp).database.productDao(), RetrofitInstance.api) }
    val factory = ViewModelFactory(context)
    val reviewViewModel: ReviewViewModel = viewModel(factory = factory)
    val cartViewModel: CartViewModel = viewModel(factory = factory)

    var product by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(productCode) {
        launch {
            product = productRepository.getProductByCode(productCode)
        }
    }

    LaunchedEffect(Unit) {
        reviewViewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        cartViewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val p = product!!
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            AsyncImage(
                model = if (p.imageUrl != null) p.imageUrl else p.imageResId,
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = p.name,
                modifier = Modifier.height(300.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(p.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(formatter.format(p.price), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("Stock: ${p.quantity} unidades", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { cartViewModel.onProductAdded(p.code) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = p.quantity > 0
                ) {
                    Text("Añadir al carrito")
                }

                Spacer(Modifier.height(16.dp))
                Text("Descripción", style = MaterialTheme.typography.titleLarge)
                Text(p.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                Divider()
                ReviewSection(productCode = p.code, viewModel = reviewViewModel)
            }
        }
    }
}
