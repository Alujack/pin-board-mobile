package kh.edu.rupp.fe.ite.pinboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import kh.edu.rupp.fe.ite.pinboard.app.navigation.AppNavigation
import kh.edu.rupp.fe.ite.pinboard.ui.theme.PinBoardTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.usecase.GetAuthStateUseCase
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PinBoardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = hiltViewModel()
                    val isAuthenticated by viewModel.isAuthenticated.collectAsState(initial = true)
                    AppNavigation(isAuthenticated = isAuthenticated)
                }
            }
        }
    }
}


@HiltViewModel
class MainViewModel @Inject constructor(
    getAuthStateUseCase: GetAuthStateUseCase
) : ViewModel() {
    val isAuthenticated = getAuthStateUseCase()
}