// navigation/AuthNavGraph.kt
package kh.edu.rupp.fe.ite.pinboard.app.navigation

import android.util.Log
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.messaging.FirebaseMessaging
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
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
import androidx.compose.ui.graphics.Color
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
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.create.CreatePinScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.profile.ProfileScreen
import kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.search.SearchScreen
import kh.edu.rupp.fe.ite.pinboard.feature.notification.data.NotificationPrefs

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreatePin : Screen("create_pin")
    object Profile : Screen("profile")
    object Search : Screen("search")
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
                },
                onNavigateToCreatePin = {
                    navController.navigate(Screen.CreatePin.route)
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
    }
}

/**
 * Pinterest-style Home screen scaffold with top app bar and bottom tabs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToCreatePin: () -> Unit
) {
    val context = LocalContext.current
    val notificationPrefs = remember { NotificationPrefs(context) }
    val optInDone by notificationPrefs.optInDone.collectAsState(initial = false)
    var showNotifOptIn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("HomeScreen", "FCM token: $token")
                    // TODO: send token to backend
                }
            }
            scope.launch { notificationPrefs.setOptInDone(true) }
        } else {
            Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
            scope.launch { notificationPrefs.setOptInDone(true) } // user decided; don't nag again
        }
    }

    LaunchedEffect(optInDone) {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED

        if (!optInDone) {
            if (needsPermission) {
                showNotifOptIn = true
            } else {
                // Permission not required or already granted; mark done and fetch token once
                showNotifOptIn = false
                scope.launch { notificationPrefs.setOptInDone(true) }
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d("HomeScreen", "FCM token: $token")
                        // TODO: send token to backend
                    }
                }
            }
        } else {
            showNotifOptIn = false
        }
    }
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
                        onClick = onNavigateToCreatePin,
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
            if (showNotifOptIn) {
                AlertDialog(
                    onDismissRequest = { showNotifOptIn = false },
                    title = { Text(text = "Enable notifications?") },
                    text = { Text(text = "Allow push notifications to get updates.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showNotifOptIn = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val token = task.result
                                        Log.d("HomeScreen", "FCM token: $token")
                                        // TODO: send token to backend
                                    }
                                }
                                scope.launch { notificationPrefs.setOptInDone(true) }
                            }
                        }) { Text("Allow") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNotifOptIn = false; scope.launch { notificationPrefs.setOptInDone(true) } }) { Text("Not now") }
                    }
                )
            }
            when (selectedTab) {
                BottomTab.Home -> TabPlaceholderContent("Home Feed")
                BottomTab.Search -> SearchScreen(onNavigateBack = { selectedTab = BottomTab.Home })
                BottomTab.Messages -> TabPlaceholderContent("Messages")
                BottomTab.Profile -> ProfileScreen(onNavigateBack = { selectedTab = BottomTab.Home })
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

 
