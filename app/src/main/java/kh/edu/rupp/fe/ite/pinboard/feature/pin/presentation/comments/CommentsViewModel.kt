package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.comments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Comment
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.CreateCommentUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetCommentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val repository: PinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    private val pinId: String? = savedStateHandle["pinId"]

    init {
        loadComments()
    }

    private fun loadComments() {
        if (pinId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Invalid pin ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getCommentsUseCase(pinId)) {
                is PinResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            comments = result.data.data,
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

    fun addComment(content: String, parentCommentId: String? = null) {
        if (pinId.isNullOrBlank() || content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            when (val result = createCommentUseCase(pinId, content, parentCommentId)) {
                is PinResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    // Reload comments to get the updated list
                    loadComments()
                }
                is PinResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun toggleCommentLike(commentId: String) {
        viewModelScope.launch {
            when (val result = repository.toggleCommentLike(commentId)) {
                is PinResult.Success -> {
                    // Update the comment in the list
                    val updatedComments = _uiState.value.comments.map { comment ->
                        if (comment._id == commentId) {
                            comment.copy(
                                isLiked = result.data.isLiked,
                                likesCount = if (result.data.isLiked) comment.likesCount + 1 else comment.likesCount - 1
                            )
                        } else {
                            comment
                        }
                    }
                    _uiState.update { it.copy(comments = updatedComments) }
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            when (repository.deleteComment(commentId)) {
                is PinResult.Success -> {
                    // Remove comment from list
                    val updatedComments = _uiState.value.comments.filter { it._id != commentId }
                    _uiState.update { it.copy(comments = updatedComments) }
                }
                is PinResult.Error -> {
                    _uiState.update { it.copy(errorMessage = "Failed to delete comment") }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun refresh() {
        loadComments()
    }
}

