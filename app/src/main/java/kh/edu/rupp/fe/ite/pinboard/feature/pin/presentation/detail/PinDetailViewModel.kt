package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Comment
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PinDetailUiState(
    val pin: Pin? = null,
    val isSaved: Boolean = false,
    val isLiked: Boolean = false,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val errorMessage: String? = null,
    val shareUrl: String? = null
)

@HiltViewModel
class PinDetailViewModel @Inject constructor(
    private val getPinByIdUseCase: GetPinByIdUseCase,
    private val savePinUseCase: SavePinUseCase,
    private val unsavePinUseCase: UnsavePinUseCase,
    private val togglePinLikeUseCase: TogglePinLikeUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val sharePinUseCase: SharePinUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()

    private val pinId: String? = savedStateHandle["pinId"]

    init {
        loadPinDetails()
        loadComments()
    }

    private fun loadPinDetails() {
        if (pinId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Invalid pin ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getPinByIdUseCase(pinId)) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pin = result.data,
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

    private fun loadComments() {
        if (pinId.isNullOrBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingComments = true) }

            when (val result = getCommentsUseCase(pinId)) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingComments = false,
                            comments = result.data.data,
                            commentsCount = result.data.data.size
                        )
                    }
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(isLoadingComments = false) }
                }
            }
        }
    }

    fun toggleSavePin() {
        val currentPin = _uiState.value.pin?._id ?: return

        viewModelScope.launch {
            val result = if (_uiState.value.isSaved) {
                unsavePinUseCase(currentPin)
            } else {
                savePinUseCase(currentPin)
            }

            when (result) {
                is PinResult.Success -> {
                    _uiState.update { it.copy(isSaved = !it.isSaved) }
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    fun toggleLike() {
        val currentPin = _uiState.value.pin?._id ?: return

        viewModelScope.launch {
            when (val result = togglePinLikeUseCase(currentPin)) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLiked = result.data.isLiked,
                            likesCount = result.data.likesCount
                        )
                    }
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    fun addComment(content: String, parentCommentId: String? = null) {
        if (pinId.isNullOrBlank() || content.isBlank()) return

        viewModelScope.launch {
            when (val result = createCommentUseCase(pinId, content, parentCommentId)) {
                is PinResult.Success -> {
                    // Reload comments to get the updated list
                    loadComments()
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    fun sharePin() {
        val currentPin = _uiState.value.pin?._id ?: return

        viewModelScope.launch {
            when (val result = sharePinUseCase(currentPin)) {
                is PinResult.Success -> {
                    // Share was tracked successfully
                    _uiState.update { it.copy(errorMessage = "Pin shared successfully!") }
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
        loadPinDetails()
        loadComments()
    }
}
