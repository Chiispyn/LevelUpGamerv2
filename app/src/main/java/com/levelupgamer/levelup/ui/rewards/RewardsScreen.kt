package com.levelupgamer.levelup.ui.rewards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.levelupgamer.levelup.model.Event
import com.levelupgamer.levelup.model.UserLevel
import com.levelupgamer.levelup.ui.viewmodel.ViewModelFactory

@Composable
fun RewardsScreen(navController: NavController) {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory(context) }
    val viewModel: RewardsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    val userLevel = remember(uiState.userPoints) {
        UserLevel.fromPoints(uiState.userPoints)
    }
    val allLevels = remember { UserLevel.values() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Programa de Recompensas", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            Text("Tu Nivel: ${userLevel.levelName}", style = MaterialTheme.typography.titleLarge, color = userLevel.color)
            Text("Puntos Actuales: ${uiState.userPoints}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigate("rewardsShop") }) {
                Text("Ir a la Tienda de Recompensas")
            }
            Spacer(Modifier.height(24.dp))
            Text("Niveles Disponibles", style = MaterialTheme.typography.headlineSmall)
        }

        items(allLevels) { level ->
            LevelCard(level = level)
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text("Eventos Disponibles", style = MaterialTheme.typography.headlineSmall)
        }

        if (uiState.events.isEmpty()) {
            item {
                Text("No hay eventos disponibles en este momento.", modifier = Modifier.padding(16.dp))
            }
        } else {
            items(uiState.events) { event ->
                EventCard(event = event, onClick = {
                    navController.navigate("eventDetail/${event.id}")
                })
            }
        }
    }
}

@Composable
fun LevelCard(level: UserLevel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = level.color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(level.levelName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = level.color)
            Text("Puntos Requeridos: ${level.requiredPoints}")
            Spacer(Modifier.height(8.dp))
            Text("Beneficios:", fontWeight = FontWeight.Bold)
            when (level) {
                UserLevel.BRONZE -> Text("Acceso al programa de puntos.")
                UserLevel.SILVER -> Text("Descuento del 5% en todos los pedidos.")
                UserLevel.GOLD -> Text("Descuento del 10% y soporte prioritario.")
                UserLevel.DIAMOND -> Text("Descuento del 15%, regalo de cumpleaños y acceso a eventos VIP.")
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(event.description)
            Spacer(Modifier.height(8.dp))
            Text("Ubicación: ${event.locationName}", style = MaterialTheme.typography.bodySmall)
            Text("Fecha: ${event.date} a las ${event.time}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Puntos por inscribirse: ${event.inscriptionPoints}", fontWeight = FontWeight.Bold)
                Text("Premio: ${event.prizePoints} Puntos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
