package kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kh.edu.rupp.fe.ite.pinboard.core.utils.Resource
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.usecase.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onUsernameChange(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun login() {
        // Fake login
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(1000)
            if (_state.value.username == "admin" && _state.value.password == "1234") {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = "Invalid credentials") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

