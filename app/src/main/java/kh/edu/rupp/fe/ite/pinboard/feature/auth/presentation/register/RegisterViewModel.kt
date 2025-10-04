package kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kh.edu.rupp.fe.ite.pinboard.core.utils.Resource
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterState(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name, error = null) }
    }

    fun onUsernameChange(username: String) {
        _state.update { it.copy(username = username, error = null) }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    fun register() {
        viewModelScope.launch {
            if (_state.value.password != _state.value.confirmPassword) {
                _state.update { it.copy(error = "Passwords do not match") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = registerUserUseCase(
                _state.value.name,
                _state.value.username,
                _state.value.email,
                _state.value.password
            )) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}