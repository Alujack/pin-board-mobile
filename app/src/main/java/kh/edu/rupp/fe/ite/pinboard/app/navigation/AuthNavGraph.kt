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
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.TokenManager
import kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.login.LoginScreen
import kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.register.RegisterScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.create.CreatePinScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.profile.ProfileScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.search.SearchScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.home.HomeScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.detail.PinDetailScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.board.BoardDetailScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.notifications.NotificationsScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.services.FCMTokenManager

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreatePin : Screen("create_pin")
    object Profile : Screen("profile")
    object Search : Screen("search")
    object PinDetail : Screen("pin_detail/{pinId}") {
        fun createRoute(pinId: String) = "pin_detail/$pinId"
    }
    object BoardDetail : Screen("board_detail/{boardId}") {
        fun createRoute(boardId: String) = "board_detail/$boardId"
    }
    object Notifications : Screen("notifications")
}

/**
 * Root navigation graph with auto-login handling
 */
@Composable
fun AuthNavGraph(
    navController: NavHostController,
    tokenManager: TokenManager,
    fcmTokenManager: FCMTokenManager
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
            MainHomeScreen(
                navController = navController,
                onLogout = {
                    scope.launch {
                        // Remove FCM token from backend
                        fcmTokenManager.removeFCMToken()
                        
                        // Clear auth tokens
                        tokenManager.clearAllTokens()
                        
                        // Navigate to login
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.CreatePin.route) {
            CreatePinScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPinCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenPinDetail = { pinId ->
                    navController.navigate(Screen.PinDetail.createRoute(pinId))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PinDetail.route) {
            PinDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.BoardDetail.route) {
            BoardDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPinClick = { pinId ->
                    navController.navigate(Screen.PinDetail.createRoute(pinId))
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen()
        }
    }
}

/**
 * Pinterest-style Home screen scaffold with top app bar and bottom tabs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val tabs = remember {
        listOf(
            BottomTab.Home,
            BottomTab.Search,
            BottomTab.Messages,
            BottomTab.Profile
        )
    }
    var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Home) }

    // Colors for active/inactive icons
    val activeColor = Color(0xFFE60023)
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

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
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                actions = {
                    IconButton(onClick = { selectedTab = BottomTab.Home }) {
                        Icon(
                            Icons.Outlined.Home,
                            contentDescription = "Home",
                            tint = if (selectedTab == BottomTab.Home) activeColor else inactiveColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedTab = BottomTab.Search }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = if (selectedTab == BottomTab.Search) activeColor else inactiveColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedTab = BottomTab.Messages }) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notification",
                            tint = if (selectedTab == BottomTab.Messages) activeColor else inactiveColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedTab = BottomTab.Profile }) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "Profile",
                            tint = if (selectedTab == BottomTab.Profile) activeColor else inactiveColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.CreatePin.route) },
                        containerColor = activeColor,
                        contentColor = Color.White,
                        modifier = Modifier.offset(y = (-1).dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Create", modifier = Modifier.size(28.dp))
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                BottomTab.Home -> HomeScreen(
                    onPinClick = { pinId ->
                        navController.navigate(Screen.PinDetail.createRoute(pinId))
                    }
                )
                BottomTab.Search -> SearchScreen(onNavigateBack = { selectedTab = BottomTab.Home })
                BottomTab.Messages -> NotificationsScreen()
                BottomTab.Profile -> ProfileScreen(
                    onNavigateBack = { selectedTab = BottomTab.Home },
                    onOpenPinDetail = { pinId ->
                        navController.navigate(Screen.PinDetail.createRoute(pinId))
                    }
                )
            }
        }
    }
}

private enum class BottomTab(val label: String) {
    Home("Home"),
    Search("Search"),
    Messages("Messages"),
    Profile("Profile")
}

