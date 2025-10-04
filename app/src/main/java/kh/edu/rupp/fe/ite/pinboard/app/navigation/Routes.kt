package kh.edu.rupp.fe.ite.pinboard.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.login.LoginScreen
import kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.register.RegisterScreen

sealed class Route(val route: String) {
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")
}

// NavHost setup
@Composable
fun AppNavigation(
    isAuthenticated: Boolean
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) Route.Login.route else Route.Login.route // force Login for now
    ) {
        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Just stay on login or navigate to register for now
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Register.route)
                }
            )
        }

        composable(Route.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack(Route.Login.route, inclusive = false)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}