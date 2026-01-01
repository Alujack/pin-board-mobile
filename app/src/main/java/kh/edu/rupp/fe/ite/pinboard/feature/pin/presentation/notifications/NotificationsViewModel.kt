package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.NotificationItem
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.usecase.GetNotificationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Notification(
    val id: String,
    val type: NotificationType,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val fromUser: String? = null,
    val metadata: Map<String, String>? = null
)

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    SAVE,
    SYSTEM
}

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val repository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                Log.d("NotificationsViewModel", "Loading notifications...")
                when (val result = getNotificationsUseCase()) {
                    is PinResult.Success -> {
                        Log.d("NotificationsViewModel", "Notifications loaded successfully. Count: ${result.data.data.size}")
                        Log.d("NotificationsViewModel", "Response success: ${result.data.success}, message: ${result.data.message}")
                        
                        val notifications = result.data.data.map { item ->
                            Log.d("NotificationsViewModel", "Processing notification: ${item._id}, type: ${item.type}, content: ${item.content}")
                            Notification(
                                id = item._id,
                                type = mapNotificationType(item.type),
                                message = item.content,
                                timestamp = formatTimestamp(item.created_at),
                                isRead = item.is_read,
                                fromUser = item.from_user?.username,
                                metadata = item.metadata?.let { meta ->
                                    mapOf(
                                        "pin_id" to (meta.pin_id ?: ""),
                                        "board_id" to (meta.board_id ?: ""),
                                        "user_id" to (meta.user_id ?: ""),
                                        "comment_id" to (meta.comment_id ?: "")
                                    ).filterValues { it.isNotEmpty() }
                                }
                            )
                        }
                        Log.d("NotificationsViewModel", "Mapped ${notifications.size} notifications")
                        _uiState.update {
                            it.copy(
                                notifications = notifications,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is PinResult.Error -> {
                        Log.e("NotificationsViewModel", "Error loading notifications: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Exception loading notifications", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load notifications: ${e.message}"
                    )
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            when (repository.markNotificationAsRead(notificationId)) {
                is PinResult.Success -> {
                    val updatedNotifications = _uiState.value.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _uiState.update { it.copy(notifications = updatedNotifications) }
                }
                is PinResult.Error -> {
                    // Silently fail or show error
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            when (repository.markAllNotificationsAsRead()) {
                is PinResult.Success -> {
                    val updatedNotifications = _uiState.value.notifications.map {
                        it.copy(isRead = true)
                    }
                    _uiState.update { it.copy(notifications = updatedNotifications) }
                }
                is PinResult.Error -> {
                    // Silently fail or show error
                }
            }
        }
    }

    fun refresh() {
        loadNotifications()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun mapNotificationType(type: String): NotificationType {
        return when (type.uppercase()) {
            "PIN_LIKED" -> NotificationType.LIKE
            "PIN_COMMENTED", "COMMENT_REPLIED" -> NotificationType.COMMENT
            "NEW_FOLLOWER" -> NotificationType.FOLLOW
            "PIN_SAVED" -> NotificationType.SAVE
            else -> NotificationType.SYSTEM
        }
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = sdf.parse(timestamp)
            val now = java.util.Date()
            val diff = now.time - (date?.time ?: 0)

            when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                diff < 604800000 -> "${diff / 86400000}d ago"
                else -> "${diff / 604800000}w ago"
            }
        } catch (e: Exception) {
            "Recently"
        }
    }
}
