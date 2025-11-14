package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.board

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetBoardByIdUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetPinsByBoardUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.SavePinUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.UnsavePinUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BoardDetailUiState(
    val board: Board? = null,
    val pins: List<Pin> = emptyList(),
    val savedPinIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BoardDetailViewModel @Inject constructor(
    private val getBoardByIdUseCase: GetBoardByIdUseCase,
    private val getPinsByBoardUseCase: GetPinsByBoardUseCase,
    private val savePinUseCase: SavePinUseCase,
    private val unsavePinUseCase: UnsavePinUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardDetailUiState())
    val uiState: StateFlow<BoardDetailUiState> = _uiState.asStateFlow()

    private val boardId: String? = savedStateHandle["boardId"]

    init {
        loadBoardDetails()
    }

    private fun loadBoardDetails() {
        if (boardId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Invalid board ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Load board info
            when (val boardResult = getBoardByIdUseCase(boardId)) {
                is PinResult.Success -> {
                    _uiState.update { it.copy(board = boardResult.data) }
                }
                is PinResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = boardResult.message
                        )
                    }
                    return@launch
                }
            }

            // Load pins in board
            when (val pinsResult = getPinsByBoardUseCase(boardId)) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pins = pinsResult.data,
                            errorMessage = null
                        )
                    }
                }
                is PinResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = pinsResult.message
                        )
                    }
                }
            }
        }
    }

    fun refreshBoard() {
        if (boardId.isNullOrBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

            when (val result = getPinsByBoardUseCase(boardId)) {
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

    fun retry() {
        loadBoardDetails()
    }
}

