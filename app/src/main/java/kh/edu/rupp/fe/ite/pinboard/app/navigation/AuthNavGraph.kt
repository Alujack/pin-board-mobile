// navigation/AuthNavGraph.kt
package kh.edu.rupp.fe.ite.pinboard.app.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
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
 * Pinterest-style Home screen scaffold with top app bar and bottom tabs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    val tabs = remember {
        listOf(
            BottomTab.Home,
            BottomTab.Search,
            BottomTab.Create,
            BottomTab.Messages,
            BottomTab.Profile
        )
    }
    var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Home) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pinterest",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text(text = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            when (tab) {
                                BottomTab.Home -> Icon(Icons.Outlined.Home, contentDescription = "Home")
                                BottomTab.Search -> Icon(Icons.Outlined.Search, contentDescription = "Search")
                                BottomTab.Create -> Icon(Icons.Filled.Add, contentDescription = "Create")
                                BottomTab.Messages -> Icon(Icons.Outlined.Notifications, contentDescription = "Notification")
                                BottomTab.Profile -> Icon(Icons.Outlined.Person, contentDescription = "Profile")
                            }
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                BottomTab.Home -> TabPlaceholderContent("Home Feed")
                BottomTab.Search -> TabPlaceholderContent("Search")
                BottomTab.Create -> TabPlaceholderContent("Create Pin")
                BottomTab.Messages -> TabPlaceholderContent("Messages")
                BottomTab.Profile -> TabPlaceholderContent("Profile")
            }
        }
    }
}

private enum class BottomTab(val label: String) {
    Home("Home"),
    Search("Search"),
    Create("Create"),
    Messages("Messages"),
    Profile("Profile")
}

@Composable
private fun TabPlaceholderContent(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Simple clean layout",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
