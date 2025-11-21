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
import androidx.compose.ui.graphics.Brush
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
            .background(Color(0xFFF8F8F8))
    ) {
        // Modern Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Notifications",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1C)
                        )
                        val unreadCount = uiState.notifications.count { !it.isRead }
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread",
                                fontSize = 14.sp,
                                color = Color(0xFFE60023),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    if (uiState.notifications.any { !it.isRead }) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFE60023)
                            )
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mark all read",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        when {
            uiState.isLoading && uiState.notifications.isEmpty() -> {
                LoadingView()
            }
            uiState.notifications.isEmpty() -> {
                EmptyStateView()
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notifications) { notification ->
                        ModernNotificationItem(
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
private fun ModernNotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = getNotificationGradient(notification.type)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.message,
                    fontSize = 15.sp,
                    color = Color(0xFF1C1C1C),
                    fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = notification.timestamp,
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
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
private fun getNotificationGradient(type: NotificationType): List<Color> {
    return when (type) {
        NotificationType.LIKE -> listOf(Color(0xFFE91E63), Color(0xFFF06292))
        NotificationType.COMMENT -> listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
        NotificationType.FOLLOW -> listOf(Color(0xFF4CAF50), Color(0xFF81C784))
        NotificationType.SAVE -> listOf(Color(0xFFE60023), Color(0xFFFF6B6B))
        NotificationType.SYSTEM -> listOf(Color(0xFF9E9E9E), Color(0xFFBDBDBD))
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFFE60023),
                strokeWidth = 3.dp
            )
            Text(
                text = "Loading notifications...",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        }
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE60023).copy(alpha = 0.1f),
                                Color(0xFFFF6B6B).copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFE60023)
                )
            }
            Text(
                text = "No notifications yet",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1C)
            )
            Text(
                text = "When you get notifications, they'll show up here",
                fontSize = 15.sp,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
