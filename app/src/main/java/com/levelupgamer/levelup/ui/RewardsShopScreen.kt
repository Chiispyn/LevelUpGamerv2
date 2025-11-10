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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.levelupgamer.levelup.model.Reward
import com.levelupgamer.levelup.ui.rewardsshop.RewardsShopViewModel
import com.levelupgamer.levelup.ui.viewmodel.ViewModelFactory

@Composable
fun RewardsShopScreen() {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory(context) }
    val viewModel: RewardsShopViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    RewardsShopContent(
        rewards = uiState.rewards,
        userPoints = uiState.userPoints,
        onRedeem = viewModel::purchaseReward
    )
}

@Composable
fun RewardsShopContent(rewards: List<Reward>, userPoints: Int, onRedeem: (Reward) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tienda de Canje", style = MaterialTheme.typography.headlineLarge)
        Text("Tus Puntos: $userPoints", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))

        if (rewards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay recompensas disponibles en este momento.")
            }
        } else {
            LazyColumn {
                items(rewards) { reward ->
                    RewardItem(reward = reward, userPoints = userPoints, onRedeem = { onRedeem(reward) })
                }
            }
        }
    }
}

@Composable
fun RewardItem(reward: Reward, userPoints: Int, onRedeem: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reward.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(reward.description, style = MaterialTheme.typography.bodySmall)
                Text("Costo: ${reward.pointsCost} puntos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            Button(
                onClick = onRedeem,
                enabled = userPoints >= reward.pointsCost
            ) {
                Text("Canjear")
            }
        }
    }
}
