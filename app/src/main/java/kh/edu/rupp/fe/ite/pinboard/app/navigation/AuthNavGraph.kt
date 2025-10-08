// navigation/AuthNavGraph.kt
package kh.edu.rupp.fe.ite.pinboard.app.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.TokenManager
import kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.login.LoginScreen
import kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.register.RegisterScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
}

/**
 * Root navigation graph with auto-login handling
 */
@Composable
fun AuthNavGraph(
    navController: NavHostController,
    tokenManager: TokenManager
) {
    val context = LocalContext.current
    var startDestination by remember { mutableStateOf(Screen.Login.route) }
    val scope = rememberCoroutineScope()

    // Check token on app start
    LaunchedEffect(Unit) {
        scope.launch {
            val token = tokenManager.getToken()
            Log.d("AuthNavGraph", "Loaded token: $token")
            if (!token.isNullOrEmpty()) {
                startDestination = Screen.Home.route
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    scope.launch {
                        tokenManager.clearAllTokens()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Simple but nicer Home screen with logout button
 */
@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Home 👋",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You are now logged in.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = "Logout")
            }
        }
    }
}
