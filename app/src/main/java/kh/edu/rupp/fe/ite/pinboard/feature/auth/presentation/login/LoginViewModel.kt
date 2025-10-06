package kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class LoginState(
    val message: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) {
        // Simple logic for testing
        if (email.isEmpty() || password.isEmpty()) {
            _state.value = LoginState("Email or password cannot be empty")
        } else {
            _state.value = LoginState("Login successful (dummy)")
        }
    }
}
