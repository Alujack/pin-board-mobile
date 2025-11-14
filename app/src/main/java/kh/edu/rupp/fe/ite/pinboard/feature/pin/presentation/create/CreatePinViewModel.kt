package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.CreatePinUseCase
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetBoardsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class CreatePinState(
    val title: String = "",
    val description: String = "",
    val link: String = "",
    val selectedBoard: Board? = null,
    val selectedFiles: List<File> = emptyList(),
    val boards: List<Board> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    val isPinCreated: Boolean = false,
    val step: CreatePinStep = CreatePinStep.MEDIA
)

enum class CreatePinStep { MEDIA, DETAILS, BOARD }

@HiltViewModel
class CreatePinViewModel @Inject constructor(
    private val createPinUseCase: CreatePinUseCase,
    private val getBoardsUseCase: GetBoardsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePinState())
    val state: StateFlow<CreatePinState> = _state.asStateFlow()

    init {
        loadBoards()
    }

    fun nextStep() {
        val s = _state.value
        when (s.step) {
            CreatePinStep.MEDIA -> {
                if (s.selectedFiles.isEmpty()) {
                    _state.value = s.copy(errorMessage = "Please select at least one file")
                } else {
                    _state.value = s.copy(step = CreatePinStep.DETAILS, errorMessage = null)
                }
            }
            CreatePinStep.DETAILS -> {
                if (s.title.isBlank() || s.description.isBlank()) {
                    _state.value = s.copy(errorMessage = "Title and description are required")
                } else {
                    _state.value = s.copy(step = CreatePinStep.BOARD, errorMessage = null)
                }
            }
            CreatePinStep.BOARD -> Unit
        }
    }

    fun prevStep() {
        val s = _state.value
        when (s.step) {
            CreatePinStep.MEDIA -> Unit
            CreatePinStep.DETAILS -> _state.value = s.copy(step = CreatePinStep.MEDIA)
            CreatePinStep.BOARD -> _state.value = s.copy(step = CreatePinStep.DETAILS)
        }
    }

    fun onTitleChange(title: String) {
        _state.value = _state.value.copy(title = title, errorMessage = null)
    }

    fun onDescriptionChange(description: String) {
        _state.value = _state.value.copy(description = description, errorMessage = null)
    }

    fun onLinkChange(link: String) {
        _state.value = _state.value.copy(link = link, errorMessage = null)
    }

    fun onBoardSelected(board: Board) {
        _state.value = _state.value.copy(selectedBoard = board, errorMessage = null)
    }

    fun onFilesSelected(files: List<File>) {
        val current = _state.value.selectedFiles
        val merged = (current + files).distinctBy { it.absolutePath }
        _state.value = _state.value.copy(selectedFiles = merged, errorMessage = null)
    }

    fun removeFile(file: File) {
        val updated = _state.value.selectedFiles.filterNot { it.absolutePath == file.absolutePath }
        _state.value = _state.value.copy(selectedFiles = updated)
    }

    fun createPin() {
        val currentState = _state.value

        // Basic validation (use case will do more)
        if (currentState.selectedBoard == null) {
            _state.value = currentState.copy(errorMessage = "Please select a board")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isCreating = true, errorMessage = null)

            when (val result = createPinUseCase(
                title = currentState.title,
                board = currentState.selectedBoard!!._id,
                description = currentState.description,
                link = currentState.link.takeIf { it.isNotBlank() },
                media = currentState.selectedFiles
            )) {
                is PinResult.Success -> {
                    _state.value = currentState.copy(
                        isCreating = false,
                        isPinCreated = true
                    )
                }
                is PinResult.Error -> {
                    _state.value = currentState.copy(
                        isCreating = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun loadBoards() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = getBoardsUseCase()) {
                is PinResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        boards = result.data
                    )
                }
                is PinResult.Error -> {
                    _state.value = _state.value.copy(
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

    fun resetState() {
        _state.value = CreatePinState()
        loadBoards()
    }
}
