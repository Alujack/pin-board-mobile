package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header with Mark All as Read
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1C)
                )
                TextButton(
                    onClick = { viewModel.markAllAsRead() }
                ) {
                    Text(
                        text = "Mark all as read",
                        color = Color(0xFFE60023)
                    )
                }
            }
        }

        when {
            uiState.isLoading -> {
                LoadingView()
            }
            uiState.notifications.isEmpty() -> {
                EmptyStateView()
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = { viewModel.markAsRead(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on notification type
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color(0xFF1C1C1C),
                    fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.timestamp,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE60023))
                )
            }
        }
    }
}

@Composable
private fun getNotificationIcon(type: NotificationType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        NotificationType.LIKE -> Icons.Filled.Favorite
        NotificationType.COMMENT -> Icons.Filled.ChatBubble
        NotificationType.FOLLOW -> Icons.Filled.PersonAdd
        NotificationType.SAVE -> Icons.Filled.Bookmark
        NotificationType.SYSTEM -> Icons.Filled.Notifications
    }
}

@Composable
private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.LIKE -> Color(0xFFE91E63)
        NotificationType.COMMENT -> Color(0xFF2196F3)
        NotificationType.FOLLOW -> Color(0xFF4CAF50)
        NotificationType.SAVE -> Color(0xFFE60023)
        NotificationType.SYSTEM -> Color(0xFF9E9E9E)
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFE60023))
    }
}

@Composable
private fun EmptyStateView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF9E9E9E)
            )
            Text(
                text = "No notifications yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Text(
                text = "When you get notifications, they'll show up here",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

