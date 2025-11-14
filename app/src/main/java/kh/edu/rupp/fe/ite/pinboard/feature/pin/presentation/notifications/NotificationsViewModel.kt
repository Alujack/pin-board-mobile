package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.notifications

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class Notification(
    val id: String,
    val type: NotificationType,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false
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
class NotificationsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        // TODO: Implement actual API call when backend is ready
        // For now, showing placeholder data
        val mockNotifications = listOf(
            Notification(
                id = "1",
                type = NotificationType.LIKE,
                message = "John liked your pin \"Beautiful Sunset\"",
                timestamp = "2 hours ago",
                isRead = false
            ),
            Notification(
                id = "2",
                type = NotificationType.FOLLOW,
                message = "Sarah started following you",
                timestamp = "5 hours ago",
                isRead = false
            ),
            Notification(
                id = "3",
                type = NotificationType.SAVE,
                message = "Mike saved your pin to \"Travel Ideas\"",
                timestamp = "1 day ago",
                isRead = true
            ),
            Notification(
                id = "4",
                type = NotificationType.COMMENT,
                message = "Emma commented on your pin",
                timestamp = "2 days ago",
                isRead = true
            )
        )

        _uiState.value = _uiState.value.copy(
            notifications = mockNotifications,
            isLoading = false
        )
    }

    fun markAsRead(notificationId: String) {
        val updatedNotifications = _uiState.value.notifications.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }
        _uiState.value = _uiState.value.copy(notifications = updatedNotifications)
    }

    fun markAllAsRead() {
        val updatedNotifications = _uiState.value.notifications.map { it.copy(isRead = true) }
        _uiState.value = _uiState.value.copy(notifications = updatedNotifications)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

