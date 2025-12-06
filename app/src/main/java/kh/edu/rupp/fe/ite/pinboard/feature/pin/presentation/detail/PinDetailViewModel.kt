package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.AuthApi
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Comment
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.CreateCommentUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.DownloadPinUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetCommentsUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetPinByIdUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.SavePinUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.SharePinUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.TogglePinLikeUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.UnsavePinUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.FollowUserUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.UnfollowUserUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.UserActionResult
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
    val relatedPins: List<Pin> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDownloading: Boolean = false,
    val downloadMessage: String? = null,
    val isFollowing: Boolean = false,
    val isFollowLoading: Boolean = false,
    val showFollowButton: Boolean = true,
    val pendingShareUrl: String? = null,
    val isCheckingFollow: Boolean = false
)

@HiltViewModel
class PinDetailViewModel
@Inject
constructor(
    private val getPinByIdUseCase: GetPinByIdUseCase,
    private val savePinUseCase: SavePinUseCase,
    private val unsavePinUseCase: UnsavePinUseCase,
    private val downloadPinUseCase: DownloadPinUseCase,
        private val togglePinLikeUseCase: TogglePinLikeUseCase,
        private val createCommentUseCase: CreateCommentUseCase,
        private val getCommentsUseCase: GetCommentsUseCase,
        private val sharePinUseCase: SharePinUseCase,
    private val getAllPinsUseCase: kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetAllPinsUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val authApi: AuthApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinDetailUiState())
    val uiState: StateFlow<PinDetailUiState> = _uiState.asStateFlow()

    private val pinId: String? = savedStateHandle["pinId"]

    init {
        loadPinDetails()
        loadComments()
        loadRelatedPins()
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
                    val pin = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false, 
                            pin = pin, 
                            errorMessage = null,
                            isLiked = pin.isLiked,
                            likesCount = pin.likesCount
                        )
                    }
                    loadFollowState(pin)
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun loadFollowState(pin: Pin) {
        val ownerId = pin.user?._id ?: return

        _uiState.update { it.copy(isCheckingFollow = true) }

        viewModelScope.launch {
            try {
                // Check follow status
                val response = authApi.checkFollowing(ownerId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        val status = body.data.status
                        _uiState.update {
                            it.copy(
                                isFollowing = status == "following",
                                showFollowButton = status != "self",
                                isCheckingFollow = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isCheckingFollow = false) }
                    }
                } else {
                    _uiState.update { it.copy(isCheckingFollow = false) }
                }
            } catch (e: Exception) {
                // Ignore follow state errors for now
                _uiState.update { it.copy(isCheckingFollow = false) }
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
        val userId = _uiState.value.pin?.user?._id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isFollowLoading = true, errorMessage = null) }

            val currentlyFollowing = _uiState.value.isFollowing

            val result = if (currentlyFollowing) {
                unfollowUserUseCase(userId)
            } else {
                followUserUseCase(userId)
            }

            when (result) {
                is UserActionResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isFollowing = !currentlyFollowing,
                            isFollowLoading = false
                        )
                    }
                }
                is UserActionResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isFollowLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
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

    private fun loadComments() {
        if (pinId.isNullOrBlank()) return

        viewModelScope.launch {
            when (val result = getCommentsUseCase(pinId)) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            comments = result.data.data,
                            commentsCount = result.data.data.size
                        )
                    }
                }
                is PinResult.Error -> {
                    // Silently fail for comments
                }
            }
        }
    }
    
    private fun loadRelatedPins() {
        viewModelScope.launch {
            when (val result = getAllPinsUseCase()) {
                is PinResult.Success -> {
                    // Filter out current pin and take random 10 pins as related
                    val related = result.data
                        .filter { it._id != pinId }
                        .shuffled()
                        .take(10)
                    _uiState.update { it.copy(relatedPins = related) }
                }
                is PinResult.Error -> {
                    // Silently fail for related pins
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

    fun addComment(content: String) {
        if (pinId.isNullOrBlank() || content.isBlank()) return

        viewModelScope.launch {
            when (val result = createCommentUseCase(pinId, content)) {
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
