package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Comment
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToComments: (String) -> Unit = {},
    viewModel: PinDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCommentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pin Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1C1C1C)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView()
                }
                uiState.pin != null -> {
                    PinDetailContent(
                        pin = uiState.pin!!,
                        isSaved = uiState.isSaved,
                        isFollowing = uiState.isFollowing,
                        isFollowLoading = uiState.isFollowLoading,
                        isDownloading = uiState.isDownloading,
                        onToggleSave = { viewModel.toggleSavePin() },
                        onShare = { viewModel.onShareClicked() },
                        onDownload = { viewModel.onDownloadClicked() },
                        onFollow = { viewModel.onFollowClicked() },
                        onCommentClick = { showCommentDialog = true }
                    )
                }
                uiState.errorMessage != null -> {
                    ErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.retry() }
                    )
                }
            }

            // Success/Error Snackbar
            uiState.errorMessage?.let { error ->
                if (uiState.pin != null && error.contains("successfully")) {
                    SuccessSnackbar(
                        message = error,
                        onDismiss = { viewModel.clearError() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                } else if (uiState.pin != null) {
                    ErrorSnackbar(
                        message = error,
                        onDismiss = { viewModel.clearError() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }

            // Share event: open system share sheet when pendingShareUrl is set
            uiState.pendingShareUrl?.let { shareUrl ->
                LaunchedEffect(shareUrl) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareUrl)
                    }
                    val chooser = Intent.createChooser(intent, "Share pin")
                    context.startActivity(chooser)
                    viewModel.onShareHandled()
                }
            }

            // Download success message
            uiState.downloadMessage?.let { msg ->
                if (uiState.pin != null) {
                    SuccessSnackbar(
                        message = msg,
                        onDismiss = { viewModel.clearDownloadMessage() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
        }

        // Comment Dialog
        if (showCommentDialog) {
            CommentDialog(
                onDismiss = { showCommentDialog = false },
                onSubmit = { comment ->
                    viewModel.addComment(comment)
                    showCommentDialog = false
                }
            )
        }
    }
}

@Composable
private fun PinDetailContent(
    pin: Pin,
    isSaved: Boolean,
    isFollowing: Boolean,
    isFollowLoading: Boolean,
    isDownloading: Boolean,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onFollow: () -> Unit,
    onCommentClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F8F8))
    ) {
        // Pin Image with gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
        ) {
            AsyncImage(
                model = pin.firstMediaUrl ?: pin.imageUrl ?: pin.videoUrl,
                contentDescription = pin.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Action buttons overlay
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModernActionButton(
                    icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    onClick = onToggleSave,
                    tint = if (isSaved) Color(0xFFE60023) else Color.White
                )
                ModernActionButton(
                    icon = Icons.Outlined.Share,
                    onClick = onShare
                )
                ModernActionButton(
                    icon = if (isDownloading) Icons.Filled.Download else Icons.Outlined.Download,
                    onClick = onDownload
                )
                ModernActionButton(
                    icon = Icons.Outlined.MoreVert,
                    onClick = { /* TODO: Implement more options */ }
                )
            }
        }

        // Content Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-30).dp),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = pin.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1C),
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interaction Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernInteractionButton(
                        icon = Icons.Outlined.FavoriteBorder,
                        text = "Like",
                        isActive = false,
                        onClick = { /* TODO: Implement like */ },
                        modifier = Modifier.weight(1f)
                    )

                    ModernInteractionButton(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        text = "Comment",
                        isActive = false,
                        onClick = onCommentClick,
                        modifier = Modifier.weight(1f)
                    )

                    ModernInteractionButton(
                        icon = Icons.Outlined.Share,
                        text = "Share",
                        isActive = false,
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                pin.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Text(
                            text = "Description",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = desc,
                            fontSize = 16.sp,
                            color = Color(0xFF424242),
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Link
                pin.link?.let { link ->
                    if (link.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF0F7FF)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Link,
                                    contentDescription = "Link",
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = link,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1976D2),
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // User Information
                val user = pin.user
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE60023),
                                        Color(0xFFFF6B6B)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        if (user != null) {
                            Text(
                                text = user.username,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1C)
                            )
                            if (!user.email.isNullOrBlank()) {
                                Text(
                                    text = user.email,
                                    fontSize = 14.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        } else {
                            Text(
                                text = "Unknown User",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1C)
                            )
                        }
                    }

                    Button(
                        onClick = onFollow,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color(0xFFBDBDBD) else Color(0xFFE60023)
                        )
                    ) {
                        Text(
                            text = if (isFollowing) "Following" else "Follow"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CommentItemCompact(comment: Comment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.user.username,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF1C1C1C)
            )
            Text(
                text = comment.content,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ModernActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.3f)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ModernInteractionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color(0xFFFFEBEE) else Color(0xFFF5F5F5),
            contentColor = if (isActive) Color(0xFFE60023) else Color(0xFF666666)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Icon(
            icon,
            contentDescription = text,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CommentDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Comment",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            TextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Write your comment...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSubmit(commentText)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE60023)
                ),
                enabled = commentText.isNotBlank()
            ) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF666666))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
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
                text = "Loading pin details...",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
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
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color(0xFF424242),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE60023)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
private fun SuccessSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFF2E7D32)
                )
            }
        }
    }
}

