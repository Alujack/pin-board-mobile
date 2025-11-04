package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.MediaItem
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val createdMedia: List<MediaItem> = emptyList(),
    val savedMedia: List<MediaItem> = emptyList(),
    val searchResults: List<Pin> = emptyList(),
    val savedPinIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null,
    val currentSearchQuery: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        // Load initial data
        loadCreatedMedia()
        loadSavedMedia()
    }

    /** ----------------------- LOAD CREATED MEDIA ----------------------- */
    fun loadCreatedMedia() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = pinRepository.getCreatedImages()) {
                is PinResult.Success -> _state.update {
                    it.copy(isLoading = false, createdMedia = result.data)
                }
                is PinResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    /** ----------------------- LOAD SAVED MEDIA ----------------------- */
    fun loadSavedMedia() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = pinRepository.getSavedMedia()) {
                is PinResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        savedMedia = result.data,
                        savedPinIds = result.data.mapNotNull { it.pinId }.toSet()
                    )
                }
                is PinResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    /** ----------------------- SEARCH PINS ----------------------- */
    fun searchPins(query: String) {
        if (query.isBlank()) {
            _state.update {
                it.copy(
                    searchResults = emptyList(),
                    currentSearchQuery = "",
                    isSearching = false
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(isSearching = true, currentSearchQuery = query, errorMessage = null)
            }

            when (val result = pinRepository.searchPins(query)) {
                is PinResult.Success -> _state.update {
                    it.copy(isSearching = false, searchResults = result.data)
                }
                is PinResult.Error -> _state.update {
                    it.copy(isSearching = false, errorMessage = result.message)
                }
            }
        }
    }

    /** ----------------------- SAVE PIN ----------------------- */
    fun savePin(pinId: String) {
        if (pinId.isBlank()) {
            _state.update { it.copy(errorMessage = "Invalid pin id") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }

            when (val result = pinRepository.savePin(pinId)) {
                is PinResult.Success -> {
                    _state.update { current ->
                        val updatedIds = current.savedPinIds + pinId
                        current.copy(
                            isSaving = false,
                            errorMessage = null,
                            savedPinIds = updatedIds
                        )
                    }
                    // ✅ Refresh saved media after saving
                    loadSavedMedia()
                }
                is PinResult.Error -> _state.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    /** ----------------------- UNSAVE PIN ----------------------- */
    fun unsavePin(pinId: String) {
        if (pinId.isBlank()) {
            _state.update { it.copy(errorMessage = "Invalid pin id") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }

            when (val result = pinRepository.unsavePin(pinId)) {
                is PinResult.Success -> {
                    _state.update { current ->
                        val updatedIds = current.savedPinIds - pinId
                        val updatedSaved = current.savedMedia.filterNot { it.pinId == pinId }
                        current.copy(
                            isSaving = false,
                            errorMessage = null,
                            savedPinIds = updatedIds,
                            savedMedia = updatedSaved
                        )
                    }
                    // ✅ Refresh saved list after unsaving
                    loadSavedMedia()
                }
                is PinResult.Error -> _state.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    /** ----------------------- DOWNLOAD PIN ----------------------- */
    fun downloadPin(pinId: String) {
        if (pinId.isBlank()) {
            _state.update { it.copy(errorMessage = "Invalid pin id") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isDownloading = true, errorMessage = null) }

            when (val result = pinRepository.downloadPin(pinId)) {
                is PinResult.Success -> _state.update {
                    it.copy(isDownloading = false, errorMessage = null)
                }
                is PinResult.Error -> _state.update {
                    it.copy(isDownloading = false, errorMessage = result.message)
                }
            }
        }
    }

    /** ----------------------- CLEAR ERROR ----------------------- */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
