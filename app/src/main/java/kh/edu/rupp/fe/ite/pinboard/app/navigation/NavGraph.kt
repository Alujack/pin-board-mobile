//package kh.edu.rupp.fe.ite.pinboard.app.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.hilt.navigation.compose.hiltViewModel
//import kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.login.LoginScreen
//import kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.login.LoginViewModel
//
//@Composable
//fun NavGraph(navController: NavHostController) {
//    NavHost(navController = navController, startDestination = "login") {
//        composable("login") {
//            val viewModel: LoginViewModel = hiltViewModel()
//            LoginScreen(viewModel = viewModel, onNavigateToRegister = {})
//        }
//    }
//}
