package kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthRepository
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSuccessful: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onUsernameChange(username: String) {
        _state.value = _state.value.copy(username = username, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, errorMessage = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword, errorMessage = null)
    }

    fun register() {
        val currentState = _state.value

        // Validation
        when {
            currentState.username.isBlank() -> {
                _state.value = currentState.copy(errorMessage = "Username is required")
                return
            }
            currentState.username.length < 3 -> {
                _state.value = currentState.copy(errorMessage = "Username must be at least 3 characters")
                return
            }
            currentState.password.isBlank() -> {
                _state.value = currentState.copy(errorMessage = "Password is required")
                return
            }
            currentState.password.length < 6 -> {
                _state.value = currentState.copy(errorMessage = "Password must be at least 6 characters")
                return
            }
            currentState.password != currentState.confirmPassword -> {
                _state.value = currentState.copy(errorMessage = "Passwords do not match")
                return
            }
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, errorMessage = null)

            when (val result = authRepository.register(
                currentState.username,
                currentState.password,
                null // profile picture can be added later
            )) {
                is AuthResult.Success -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        isRegistrationSuccessful = true
                    )
                }
                is AuthResult.Error -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}