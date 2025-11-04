package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val createdPins: List<Pin> = emptyList(),
    val savedPins: List<Pin> = emptyList(),
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
        loadCreatedPins()
        loadSavedPins()
    }

    fun loadCreatedPins() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = pinRepository.getCreatedPins()) {
                is PinResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            createdPins = result.data
                        )
                    }
                }
                is PinResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadSavedPins() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = pinRepository.getSavedPins()) {
                is PinResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            savedPins = result.data,
                            savedPinIds = result.data.mapNotNull { pin -> pin._id }.toSet()
                        )
                    }
                }
                is PinResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

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
                it.copy(
                    isSearching = true,
                    currentSearchQuery = query,
                    errorMessage = null
                )
            }

            when (val result = pinRepository.searchPins(query)) {
                is PinResult.Success -> {
                    _state.update {
                        it.copy(
                            isSearching = false,
                            searchResults = result.data
                        )
                    }
                }
                is PinResult.Error -> {
                    _state.update {
                        it.copy(
                            isSearching = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

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
                    // Optionally refresh saved list in background
                    loadSavedPins()
                }
                is PinResult.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

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
                        val updatedSaved = current.savedPins.filterNot { it._id == pinId }
                        current.copy(
                            isSaving = false,
                            errorMessage = null,
                            savedPinIds = updatedIds,
                            savedPins = updatedSaved
                        )
                    }
                    // Optionally refresh saved list in background
                    loadSavedPins()
                }
                is PinResult.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun downloadPin(pinId: String) {
        if (pinId.isBlank()) {
            _state.update { it.copy(errorMessage = "Invalid pin id") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isDownloading = true, errorMessage = null) }

            when (val result = pinRepository.downloadPin(pinId)) {
                is PinResult.Success -> {
                    _state.update {
                        it.copy(
                            isDownloading = false,
                            errorMessage = null
                        )
                    }
                }
                is PinResult.Error -> {
                    _state.update {
                        it.copy(
                            isDownloading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}