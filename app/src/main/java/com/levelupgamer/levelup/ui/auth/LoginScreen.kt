package com.levelupgamer.levelup.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.levelupgamer.levelup.model.UserAddress
import com.levelupgamer.levelup.ui.viewmodel.ViewModelFactory
import com.levelupgamer.levelup.R // 👈 Asegúrate de importar tu archivo R
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory(context) }
    val viewModel: LoginViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    // Navegación basada en el estado del ViewModel
    LaunchedEffect(uiState.navigateTo) {
        when (uiState.navigateTo) {
            LoginDestination.MAIN -> navController.navigate("main") { popUpTo("login") { inclusive = true } }
            LoginDestination.ADMIN -> navController.navigate("admin") { popUpTo("login") { inclusive = true } }
            LoginDestination.NONE -> { /* No hacer nada */ }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🖼️ Imagen del logo antes del texto
            Image(
                painter = painterResource(id = R.drawable.logo), // 🔹de tu imagen🔹
                contentDescription = "Logo Level-Up Gamer",
                modifier = Modifier
                    .size(160.dp) // Ajusta el tamaño según lo necesites
                    .padding(bottom = 16.dp)
            )

            Text("Level-Up Gamer", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.error != null
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.error != null
            )

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onLoginClick() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ingresar") }

            TextButton(onClick = { navController.navigate("register") }) {
                Text("¿No tienes cuenta? Regístrate")
            }

            Spacer(Modifier.height(24.dp))
            Text("Atajos de Desarrollo", style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.onAdminShortcutClick() }) { Text("Admin") }
                Button(onClick = {
                    viewModel.onTestUserClick(
                        email = "cliente@ejemplo.com",
                        name = "Cliente de Prueba",
                        addresses = listOf(
                            UserAddress(id = UUID.randomUUID().toString(), userId = 1, street = "Av. Siempre Viva", numberOrApt = "742", commune = "Santiago", region = "Metropolitana de Santiago", isPrimary = true),
                            UserAddress(id = UUID.randomUUID().toString(), userId = 1, street = "Calle Falsa", numberOrApt = "123", commune = "Maipú", region = "Metropolitana de Santiago")
                        )
                    )
                }) { Text("Cliente") }
                Button(onClick = {
                    viewModel.onTestUserClick(
                        email = "cliente@duocuc.cl",
                        name = "Cliente Duoc",
                        addresses = listOf(
                            UserAddress(id = UUID.randomUUID().toString(), userId = 2, street = "Av. Costanera", numberOrApt = "123", commune = "Lota", region = "Biobío", isPrimary = true),
                            UserAddress(id = UUID.randomUUID().toString(), userId = 2, street = "Plaza de Armas", numberOrApt = "456", commune = "Concepción", region = "Biobío")
                        )
                    )
                }) { Text("Cliente Duoc") }
            }
        }
    }
}
