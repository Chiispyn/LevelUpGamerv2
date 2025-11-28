package com.levelupgamer.levelup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.levelupgamer.levelup.ui.AdminFeature
import com.levelupgamer.levelup.ui.MainScreen
import com.levelupgamer.levelup.ui.auth.LoginScreen
import com.levelupgamer.levelup.ui.auth.RegisterScreen
import com.levelupgamer.levelup.ui.theme.LevelUpGamerTheme
import com.levelupgamer.levelup.util.UserManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LevelUpGamerTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val startDestination = when {
        UserManager.isLoggedIn(navController.context) -> {
            if (UserManager.isAdmin(navController.context)) "admin" else "main"
        }
        else -> "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("main") { MainScreen(navController) }
        composable("admin") { AdminFeature(mainNavController = navController) }
    }
}
