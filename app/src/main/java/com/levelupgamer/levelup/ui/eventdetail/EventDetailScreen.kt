package com.levelupgamer.levelup.ui.eventdetail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.levelupgamer.levelup.model.Event

@Composable
fun EventDetailScreen(eventId: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val factory = remember(eventId) { EventDetailViewModelFactory(eventId, context) }
    val viewModel: EventDetailViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold {
        Column(modifier = Modifier.padding(it)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.event == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.userMessage ?: "El evento no está disponible.")
                }
            } else {
                EventDetailContent(
                    event = uiState.event!!,
                    isAlreadyInscribed = uiState.isUserInscribed,
                    onInscribeClick = { viewModel.inscribeToEvent() },
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    isAlreadyInscribed: Boolean,
    onInscribeClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(event.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(event.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))

        InfoRow("Ubicación:", event.locationName)
        InfoRow("Fecha:", "${event.date} a las ${event.time}")
        InfoRow("Puntos por Participar:", "+${event.inscriptionPoints}", isHighlight = true)
        InfoRow("Premio:", "${event.prizePoints} Puntos")

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onInscribeClick,
            enabled = !isAlreadyInscribed,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isAlreadyInscribed) "Ya estás inscrito" else "Participar (+${event.inscriptionPoints} puntos)")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Bold)
        Text(value, color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}
