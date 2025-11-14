package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetAllPinsUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.SavePinUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.UnsavePinUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val pins: List<Pin> = emptyList(),
    val savedPinIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPinsUseCase: GetAllPinsUseCase,
    private val savePinUseCase: SavePinUseCase,
    private val unsavePinUseCase: UnsavePinUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPins()
    }

    fun loadPins() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getAllPinsUseCase()) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pins = result.data,
                            errorMessage = null
                        )
                    }
                }
                is PinResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun refreshPins() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

            when (val result = getAllPinsUseCase()) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            pins = result.data,
                            errorMessage = null
                        )
                    }
                }
                is PinResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun toggleSavePin(pinId: String) {
        if (pinId.isBlank()) return

        viewModelScope.launch {
            val isSaved = _uiState.value.savedPinIds.contains(pinId)
            
            val result = if (isSaved) {
                unsavePinUseCase(pinId)
            } else {
                savePinUseCase(pinId)
            }

            when (result) {
                is PinResult.Success -> {
                    _uiState.update { currentState ->
                        val updatedSavedIds = if (isSaved) {
                            currentState.savedPinIds - pinId
                        } else {
                            currentState.savedPinIds + pinId
                        }
                        currentState.copy(savedPinIds = updatedSavedIds)
                    }
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

