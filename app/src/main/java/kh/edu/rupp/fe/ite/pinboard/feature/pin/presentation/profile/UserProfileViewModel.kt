package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.AuthApi
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.PinResponse
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.PinApi
import javax.inject.Inject

data class UserProfileState(
    val username: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val pinsCount: Int = 0,
    val bio: String? = null,
    val profilePicture: String? = null,
    val isFollowing: Boolean = false,
    val pins: List<Pin> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingPins: Boolean = false,
    val isFollowLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val pinApi: PinApi
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = authApi.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    val profileData = response.body()!!.data
                    _state.update {
                        it.copy(
                            username = profileData.username,
                            followersCount = profileData.followersCount,
                            followingCount = profileData.followingCount,
                            pinsCount = profileData.pinsCount,
                            bio = profileData.bio,
                            profilePicture = profileData.profile_picture,
                            isFollowing = profileData.isFollowing,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load profile: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error loading profile: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadUserPins(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPins = true) }
            try {
                // Use getAllPins and filter by user, or create a new endpoint
                // For now, we'll use getAllPins and filter client-side
                val response = pinApi.getAllPins()
                val allPins = response.data
                val userPins = allPins.filter { pin ->
                    pin.user?._id == userId
                }
                _state.update {
                    it.copy(
                        pins = userPins,
                        isLoadingPins = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingPins = false) }
            }
        }
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isFollowLoading = true) }
            try {
                val response = authApi.followUser(
                    kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.FollowUserRequest(userId)
                )
                if (response.isSuccessful) {
                    _state.update {
                        it.copy(
                            isFollowing = true,
                            isFollowLoading = false,
                            followersCount = it.followersCount + 1
                        )
                    }
                } else {
                    _state.update { it.copy(isFollowLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isFollowLoading = false) }
            }
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isFollowLoading = true) }
            try {
                val response = authApi.unfollowUser(
                    kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.FollowUserRequest(userId)
                )
                if (response.isSuccessful) {
                    _state.update {
                        it.copy(
                            isFollowing = false,
                            isFollowLoading = false,
                            followersCount = maxOf(0, it.followersCount - 1)
                        )
                    }
                } else {
                    _state.update { it.copy(isFollowLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isFollowLoading = false) }
            }
        }
    }
}

