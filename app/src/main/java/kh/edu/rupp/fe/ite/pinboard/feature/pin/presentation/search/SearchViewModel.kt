package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val searchResults: List<Pin> = emptyList(),
    val allPins: List<Pin> = emptyList(), // Store all pins
    val savedPinIds: Set<String> = emptySet(),
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null,
    val currentSearchQuery: String = ""
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    init {
        // Load saved pins on initialization
        loadSavedPins()
    }

    private fun loadSavedPins() {
        viewModelScope.launch {
            when (val saved = pinRepository.getSavedMedia()) {
                is PinResult.Success -> {
                    _state.value = _state.value.copy(
                        savedPinIds = saved.data.mapNotNull { it.pinId }.toSet()
                    )
                }
                else -> {}
            }
        }
    }

    // Load all pins (when screen opens or search is cleared)
    fun loadAllPins() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSearching = true,
                errorMessage = null
            )

            when (val result = pinRepository.getAllPins()) {
                is PinResult.Success -> {
                    _state.value = _state.value.copy(
                        isSearching = false,
                        searchResults = result.data,
                        allPins = result.data
                    )
                    loadSavedPins()
                }
                is PinResult.Error -> {
                    _state.value = _state.value.copy(
                        isSearching = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    // Search pins by query
    fun searchPins(query: String) {
        if (query.isBlank()) {
            _state.value = _state.value.copy(
                searchResults = _state.value.allPins,
                currentSearchQuery = "",
                isSearching = false
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSearching = true,
                currentSearchQuery = query,
                errorMessage = null
            )

            when (val result = pinRepository.searchPins(query)) {
                is PinResult.Success -> {
                    _state.value = _state.value.copy(
                        isSearching = false,
                        searchResults = result.data
                    )
                    loadSavedPins()
                }
                is PinResult.Error -> {
                    _state.value = _state.value.copy(
                        isSearching = false,
                        searchResults = emptyList(),
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun savePin(pinId: String) {
        if (pinId.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Invalid pin id")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)

            when (val result = pinRepository.savePin(pinId)) {
                is PinResult.Success -> {
                    val updated = _state.value.savedPinIds + pinId
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = null,
                        savedPinIds = updated
                    )
                }
                is PinResult.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun unsavePin(pinId: String) {
        if (pinId.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Invalid pin id")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)

            when (val result = pinRepository.unsavePin(pinId)) {
                is PinResult.Success -> {
                    val updated = _state.value.savedPinIds - pinId
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = null,
                        savedPinIds = updated
                    )
                }
                is PinResult.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun downloadPin(pinId: String?) {
        if (pinId.isNullOrBlank()) {
            _state.value = _state.value.copy(errorMessage = "Invalid pin id")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isDownloading = true, errorMessage = null)

            when (val result = pinRepository.downloadPin(pinId)) {
                is PinResult.Success -> {
                    _state.value = _state.value.copy(
                        isDownloading = false,
                        errorMessage = null
                    )
                }
                is PinResult.Error -> {
                    _state.value = _state.value.copy(
                        isDownloading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}