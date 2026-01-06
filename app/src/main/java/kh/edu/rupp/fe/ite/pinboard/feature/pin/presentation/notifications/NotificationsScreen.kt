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
    onNavigateToPin: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToPinWithComments: (String) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Refresh notifications when screen is focused
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

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
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Refresh button
                        IconButton(
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color(0xFFE60023)
                            )
                        }
                        
                        // Mark all read button
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
        }

        when {
            uiState.isLoading && uiState.notifications.isEmpty() -> {
                LoadingView()
            }
            uiState.errorMessage != null -> {
                ErrorView(
                    message = uiState.errorMessage ?: "Unknown error",
                    onRetry = { viewModel.refresh() },
                    onDismiss = { viewModel.clearError() }
                )
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
                                onClick = { 
                                    android.util.Log.d("NotificationsScreen", "Notification clicked: id=${notification.id}, type=${notification.type}")
                                    // Mark as read first (non-blocking)
                                    viewModel.markAsRead(notification.id)
                                    // Navigate based on notification type
                                    android.util.Log.d("NotificationsScreen", "Calling handleNotificationClick")
                                    handleNotificationClick(
                                        notification = notification,
                                        onNavigateToPin = onNavigateToPin,
                                        onNavigateToUserProfile = onNavigateToUserProfile,
                                        onNavigateToPinWithComments = onNavigateToPinWithComments
                                    )
                                    android.util.Log.d("NotificationsScreen", "handleNotificationClick completed")
                                }
                            )
                    }
                }
            }
        }
    }
}

private fun handleNotificationClick(
    notification: Notification,
    onNavigateToPin: (String) -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToPinWithComments: (String) -> Unit
) {
    android.util.Log.d("NotificationsScreen", "=== handleNotificationClick START ===")
    android.util.Log.d("NotificationsScreen", "Notification type: ${notification.type}")
    android.util.Log.d("NotificationsScreen", "Metadata: ${notification.metadata}")
    android.util.Log.d("NotificationsScreen", "Metadata keys: ${notification.metadata?.keys}")
    android.util.Log.d("NotificationsScreen", "From user: ${notification.fromUser}")
    
    val metadata = notification.metadata
    
    try {
        when (notification.type) {
            NotificationType.COMMENT -> {
                // Navigate to pin detail and open comments
                val pinId = metadata?.get("pin_id")
                android.util.Log.d("NotificationsScreen", "COMMENT - pinId from metadata: $pinId")
                android.util.Log.d("NotificationsScreen", "COMMENT - pinId isNullOrEmpty: ${pinId.isNullOrEmpty()}")
                
                if (!pinId.isNullOrEmpty() && pinId.isNotBlank()) {
                    android.util.Log.d("NotificationsScreen", "COMMENT - Calling onNavigateToPinWithComments with: $pinId")
                    onNavigateToPinWithComments(pinId)
                    android.util.Log.d("NotificationsScreen", "COMMENT - Navigation callback executed")
                } else {
                    android.util.Log.e("NotificationsScreen", "COMMENT - No valid pin_id found. Metadata: $metadata")
                }
            }
            NotificationType.LIKE, NotificationType.SAVE -> {
                // Navigate to pin detail
                val pinId = metadata?.get("pin_id")
                android.util.Log.d("NotificationsScreen", "LIKE/SAVE - pinId from metadata: $pinId")
                
                if (!pinId.isNullOrEmpty() && pinId.isNotBlank()) {
                    android.util.Log.d("NotificationsScreen", "LIKE/SAVE - Calling onNavigateToPin with: $pinId")
                    onNavigateToPin(pinId)
                    android.util.Log.d("NotificationsScreen", "LIKE/SAVE - Navigation callback executed")
                } else {
                    android.util.Log.e("NotificationsScreen", "LIKE/SAVE - No valid pin_id found. Metadata: $metadata")
                }
            }
            NotificationType.FOLLOW -> {
                // Navigate to user profile
                // Try metadata first, then fallback to from_user if available
                var userId = metadata?.get("user_id")
                android.util.Log.d("NotificationsScreen", "FOLLOW - userId from metadata: $userId")
                
                // If no userId in metadata, try to get from fromUser (though we'd need the actual ID)
                if (userId.isNullOrEmpty() || userId.isBlank()) {
                    android.util.Log.w("NotificationsScreen", "FOLLOW - No user_id in metadata, cannot navigate")
                } else {
                    android.util.Log.d("NotificationsScreen", "FOLLOW - Calling onNavigateToUserProfile with: $userId")
                    onNavigateToUserProfile(userId)
                    android.util.Log.d("NotificationsScreen", "FOLLOW - Navigation callback executed")
                }
            }
            NotificationType.SYSTEM -> {
                // Try to navigate to pin if available
                val pinId = metadata?.get("pin_id")
                if (!pinId.isNullOrEmpty() && pinId.isNotBlank()) {
                    android.util.Log.d("NotificationsScreen", "SYSTEM - Navigating to pin: $pinId")
                    onNavigateToPin(pinId)
                } else {
                    android.util.Log.w("NotificationsScreen", "SYSTEM - No pin_id found")
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("NotificationsScreen", "Error in handleNotificationClick", e)
    }
    
    android.util.Log.d("NotificationsScreen", "=== handleNotificationClick END ===")
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

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
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
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFD32F2F)
            )
            Text(
                text = "Error loading notifications",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1C)
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE60023)
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}
