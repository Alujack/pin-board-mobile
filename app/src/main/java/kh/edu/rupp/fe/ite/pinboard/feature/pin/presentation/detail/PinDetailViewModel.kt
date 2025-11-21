package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Comment
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.DownloadPinUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetPinByIdUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.SavePinUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.UnsavePinUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PinDetailUiState(
        val pin: Pin? = null,
        val isSaved: Boolean = false,
        val isLiked: Boolean = false,
        val likesCount: Int = 0,
        val commentsCount: Int = 0,
        val comments: List<Comment> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isDownloading: Boolean = false,
        val downloadMessage: String? = null,
        val isFollowing: Boolean = false,
        val isFollowLoading: Boolean = false,
        val pendingShareUrl: String? = null
)

@HiltViewModel
class PinDetailViewModel
@Inject
constructor(
        private val getPinByIdUseCase: GetPinByIdUseCase,
        private val savePinUseCase: SavePinUseCase,
        private val unsavePinUseCase: UnsavePinUseCase,
        private val downloadPinUseCase: DownloadPinUseCase,
        savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()

    private val pinId: String? = savedStateHandle["pinId"]

    init {
        loadPinDetails()
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
                        it.copy(isLoading = false, pin = result.data, errorMessage = null)
                    }
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onShareClicked() {
        val pin = _uiState.value.pin ?: return

        // Prefer explicit link if available, otherwise fall back to media URL or id
        val shareUrl =
                when {
                    !pin.link.isNullOrBlank() -> pin.link
                    !pin.firstMediaUrl.isNullOrBlank() -> pin.firstMediaUrl
                    !pin._id.isNullOrBlank() -> "Pin: ${pin._id}"
                    else -> null
                }
                        ?: return

        _uiState.update { it.copy(pendingShareUrl = shareUrl) }
    }

    fun onShareHandled() {
        _uiState.update { it.copy(pendingShareUrl = null) }
    }

    fun onFollowClicked() {
        val current = _uiState.value
        val user = current.pin?.user ?: return

        // UI-only toggle for now; can be replaced with real follow/unfollow API
        _uiState.update { it.copy(isFollowLoading = true) }

        viewModelScope.launch {
            // Simulate immediate completion; hook API here later
            _uiState.update { it.copy(isFollowing = !it.isFollowing, isFollowLoading = false) }
        }
    }

    fun onDownloadClicked() {
        val pinId = _uiState.value.pin?._id ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isDownloading = true, downloadMessage = null, errorMessage = null)
            }

            when (val result = downloadPinUseCase(pinId)) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(isDownloading = false, downloadMessage = "Saved to gallery")
                    }
                }
                is PinResult.Error -> {
                    _uiState.update {
                        it.copy(isDownloading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun clearDownloadMessage() {
        _uiState.update { it.copy(downloadMessage = null) }
    }

    fun toggleSavePin() {
        val currentPin = _uiState.value.pin?._id ?: return

        viewModelScope.launch {
            val result =
                    if (_uiState.value.isSaved) {
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

    fun addComment(content: String) {
        if (pinId.isNullOrBlank() || content.isBlank()) return

        viewModelScope.launch {
            // TODO: Implement comment creation when API is ready
            _uiState.update { it.copy(errorMessage = "Comment feature coming soon!") }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun retry() {
        loadPinDetails()
    }
}
