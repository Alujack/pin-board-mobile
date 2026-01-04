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
) {
    val unreadCount: Int
        get() = notifications.count { !it.isRead }
}

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
                            Log.d("NotificationsViewModel", "Raw metadata: ${item.metadata}")
                            Log.d("NotificationsViewModel", "Metadata pin_id: ${item.metadata?.pin_id}, board_id: ${item.metadata?.board_id}, user_id: ${item.metadata?.user_id}")
                            
                            Notification(
                                id = item._id,
                                type = mapNotificationType(item.type),
                                message = item.content,
                                timestamp = formatTimestamp(item.created_at),
                                isRead = item.is_read,
                                fromUser = item.from_user?.username,
                                metadata = item.metadata?.let { meta ->
                                    val metadataMap = mutableMapOf<String, String>()
                                    // Handle both snake_case (pin_id) and camelCase (pinId) from backend
                                    (meta.pin_id ?: meta.pinId)?.takeIf { it.isNotEmpty() }?.let { 
                                        metadataMap["pin_id"] = it 
                                    }
                                    (meta.board_id ?: meta.boardId)?.takeIf { it.isNotEmpty() }?.let { 
                                        metadataMap["board_id"] = it 
                                    }
                                    (meta.user_id ?: meta.userId)?.takeIf { it.isNotEmpty() }?.let { 
                                        metadataMap["user_id"] = it 
                                    }
                                    (meta.comment_id ?: meta.commentId)?.takeIf { it.isNotEmpty() }?.let { 
                                        metadataMap["comment_id"] = it 
                                    }
                                    Log.d("NotificationsViewModel", "Processed metadata map: $metadataMap")
                                    if (metadataMap.isNotEmpty()) metadataMap else null
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
            // Try multiple date formats
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss"
            )
            
            var date: java.util.Date? = null
            for (format in formats) {
                try {
                    val sdf = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    date = sdf.parse(timestamp)
                    if (date != null) break
                } catch (e: Exception) {
                    // Try next format
                }
            }
            
            if (date == null) {
                Log.w("NotificationsViewModel", "Could not parse timestamp: $timestamp")
                return "Recently"
            }
            
            val now = java.util.Date()
            val diff = now.time - date.time

            when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                diff < 604800000 -> "${diff / 86400000}d ago"
                else -> "${diff / 604800000}w ago"
            }
        } catch (e: Exception) {
            Log.e("NotificationsViewModel", "Error formatting timestamp: $timestamp", e)
            "Recently"
        }
    }
}
