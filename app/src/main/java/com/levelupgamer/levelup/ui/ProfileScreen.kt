package com.levelupgamer.app.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.repository.UserRepository
import com.levelupgamer.levelup.model.User
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileScreen(navController: NavController, onLogout: () -> Unit) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository((context.applicationContext as MyApp).database.userDao()) }
    var user by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", tmpFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                user?.let {
                    scope.launch {
                        val updatedUser = it.copy(profileImageUri = uri.toString())
                        userRepository.update(updatedUser)
                        user = updatedUser
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val newImageUri = getTmpFileUri()
            tempImageUri = newImageUri
            cameraLauncher.launch(newImageUri)
        }
    }

    LaunchedEffect(Unit) {
        val userId = UserManager.getLoggedInUserId(context)
        if (userId != null && userId != -1) {
            user = userRepository.getUserById(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        user?.let {
            UserInfoCard(user = it, onImageClick = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            })
            Spacer(Modifier.height(24.dp))
        }

        ProfileOption(navController, "myOrders", Icons.Default.ListAlt, "Mis Compras")
        ProfileOption(navController, "editProfile", Icons.Default.AccountCircle, "Editar Perfil")
        ProfileOption(navController, "address", Icons.Default.LocationOn, "Gestión de Dirección")

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                UserManager.logout(context)
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
            Spacer(Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun UserInfoCard(user: User, onImageClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable(onClick = onImageClick)
        ) {
            if (user.profileImageUri != null) {
                // 📸 Si tiene imagen, mostrarla
                AsyncImage(
                    model = Uri.parse(user.profileImageUri),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 👤 Si no tiene imagen, mostrar ícono de usuario
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Usuario sin foto",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // 🎥 Ícono de cámara pequeño en la esquina inferior derecha
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Cambiar foto",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(6.dp)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // 📋 Datos del usuario
        Text("Hola, ${user.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Divider(modifier = Modifier.padding(top = 8.dp))
        Text("Puntos Disponibles: ${user.points}", style = MaterialTheme.typography.titleMedium)
        Text("Tu Código de Referido: ${user.referralCode}", style = MaterialTheme.typography.bodyLarge)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileOption(
    navController: NavController,
    route: String,
    icon: ImageVector,
    text: String
) {
    Card(
        onClick = { navController.navigate(route) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = text)
            Spacer(Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
