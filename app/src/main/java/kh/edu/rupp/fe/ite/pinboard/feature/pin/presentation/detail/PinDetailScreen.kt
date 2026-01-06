package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.detail

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Comment
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinDetailScreen(
    pinId: String? = null,
    openCommentsOnLoad: Boolean = false,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (String) -> Unit = {},
    onNavigateToPin: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    viewModel: PinDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCommentsSheet by remember { mutableStateOf(openCommentsOnLoad) }
    var commentText by remember { mutableStateOf("") }
    
    // Open comments if requested
    LaunchedEffect(openCommentsOnLoad) {
        if (openCommentsOnLoad) {
            showCommentsSheet = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            uiState.isLoading -> {
                LoadingView()
            }
            uiState.pin != null -> {
                PinDetailContent(
                    pin = uiState.pin!!,
                    isSaved = uiState.isSaved,
                    isLiked = uiState.isLiked,
                    likesCount = uiState.likesCount,
                    commentsCount = uiState.commentsCount,
                    comments = uiState.comments,
                    isFollowing = uiState.isFollowing,
                    isFollowLoading = uiState.isFollowLoading,
                    isCheckingFollow = uiState.isCheckingFollow,
                    isDownloading = uiState.isDownloading,
                    relatedPins = uiState.relatedPins,
                    showFollowButton = uiState.showFollowButton,
                    onNavigateBack = onNavigateBack,
                    onToggleSave = { viewModel.toggleSavePin() },
                    onToggleLike = { viewModel.toggleLike() },
                    onShare = { viewModel.onShareClicked() },
                    onDownload = { viewModel.onDownloadClicked() },
                    onFollow = { viewModel.onFollowClicked() },
                    onCommentClick = { showCommentsSheet = true },
                    onRelatedPinClick = onNavigateToPin,
                    onNavigateToUserProfile = onNavigateToUserProfile
                )
            }
            uiState.errorMessage != null -> {
                ErrorView(message = uiState.errorMessage!!, onRetry = { viewModel.retry() })
            }
        }

        // Comments Bottom Sheet
        if (showCommentsSheet && uiState.pin != null) {
            CommentsBottomSheet(
                pinId = uiState.pin!!._id ?: "",
                comments = uiState.comments,
                repliesMap = uiState.repliesMap,
                expandedReplies = uiState.expandedReplies,
                commentsCount = uiState.commentsCount,
                commentText = commentText,
                onCommentTextChange = { commentText = it },
                onDismiss = { showCommentsSheet = false },
                onSubmitComment = {
                    viewModel.addComment(it)
                    commentText = ""
                },
                onShare = { viewModel.onShareClicked() },
                onToggleCommentLike = { commentId ->
                    viewModel.toggleCommentLike(commentId)
                },
                onDeleteComment = { commentId ->
                    viewModel.deleteComment(commentId)
                },
                onAddReply = { replyText, parentCommentId ->
                    viewModel.addComment(replyText, parentCommentId)
                },
                onToggleRepliesExpanded = { commentId ->
                    viewModel.toggleRepliesExpanded(commentId)
                }
            )
        }

        // Success/Error Snackbar
        uiState.errorMessage?.let { error ->
            if (uiState.pin != null && error.contains("successfully")) {
                SuccessSnackbar(
                    message = error,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            } else if (uiState.pin != null) {
                ErrorSnackbar(
                    message = error,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
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
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinDetailContent(
    pin: Pin,
    isSaved: Boolean,
    isLiked: Boolean,
    likesCount: Int,
    commentsCount: Int,
    comments: List<Comment>,
    isFollowing: Boolean,
    isFollowLoading: Boolean,
    isCheckingFollow: Boolean,
    isDownloading: Boolean,
    relatedPins: List<Pin>,
    showFollowButton: Boolean,
    onNavigateBack: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleLike: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onFollow: () -> Unit,
    onCommentClick: () -> Unit,
    onRelatedPinClick: (String) -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {
        // Image Section with Overlay Back Button
        val mediaList = pin.media ?: emptyList()
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
        ) {
            if (mediaList.isNotEmpty()) {
                MediaCarousel(
                    media = mediaList,
                    modifier = Modifier.fillMaxSize(),
                    isSaved = isSaved,
                    onToggleSave = onToggleSave,
                    onShare = onShare,
                    onDownload = onDownload,
                    isDownloading = isDownloading,
                    onNavigateBack = onNavigateBack
                )
            } else {
                // Single image fallback
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = pin.firstMediaUrl ?: pin.imageUrl ?: pin.videoUrl,
                        contentDescription = pin.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Back button overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(40.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // Action buttons overlay (top right)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            IconButton(
                                onClick = onToggleSave,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = if (isSaved) "Unsave" else "Save",
                                    tint = if (isSaved) Color(0xFFE60023) else Color(0xFF1C1C1C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            IconButton(
                                onClick = onShare,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share",
                                    tint = Color(0xFF1C1C1C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            IconButton(
                                onClick = onDownload,
                                modifier = Modifier.fillMaxSize(),
                                enabled = !isDownloading
                            ) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFFE60023),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Download,
                                        contentDescription = "Download",
                                        tint = Color(0xFF1C1C1C),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            IconButton(
                                onClick = { /* TODO: More options */ },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = "More",
                                    tint = Color(0xFF1C1C1C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Engagement Section - Pinterest Style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Like, Comment, Share, More
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button
                Row(
                    modifier = Modifier.clickable { onToggleLike() },
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFE60023) else Color(0xFF1C1C1C),
                        modifier = Modifier.size(24.dp)
                    )
                    if (likesCount > 0) {
                        Text(
                            text = "$likesCount",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1C)
                        )
                    }
                }

                // Comment button
                Row(
                    modifier = Modifier.clickable { onCommentClick() },
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color(0xFF1C1C1C),
                        modifier = Modifier.size(24.dp)
                    )
                    if (commentsCount > 0) {
                        Text(
                            text = "$commentsCount",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1C)
                        )
                    }
                }

                // Share button
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowUpward,
                        contentDescription = "Share",
                        tint = Color(0xFF1C1C1C),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // More options
                IconButton(onClick = { /* TODO: Show more options */ }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = "More",
                        tint = Color(0xFF1C1C1C),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Right side: Save button (Pinterest red)
            Button(
                onClick = onToggleSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) Color(0xFFBDBDBD) else Color(0xFFE60023)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = if (isSaved) "Saved" else "Save",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

        // User Info Section
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(enabled = pin.user?._id != null) {
                    pin.user?._id?.let { userId ->
                        onNavigateToUserProfile(userId)
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            val user = pin.user
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                if (user != null && !user.username.isNullOrBlank()) {
                    Text(
                        text = user.username.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Username and Title
            Column(modifier = Modifier.weight(1f)) {
                if (user != null) {
                    Text(
                        text = user.username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1C)
                    )
                }
                Text(
                    text = pin.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1C),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add Comment Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile icon button
            IconButton(
                onClick = { /* TODO: Open profile */ },
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Comment input field
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Add a comment") },
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCommentClick() },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color(0xFFE60023)
                ),
                readOnly = true,
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // More to Explore Section
        if (relatedPins.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "More to explore",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1C)
                    )
                    TextButton(onClick = { /* TODO: See more */ }) {
                        Text(
                            text = "See more",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Related pins grid
                kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.components.RelatedPinsSection(
                    relatedPins = relatedPins,
                    onPinClick = { pinId -> pinId?.let { onRelatedPinClick(it) } },
                    onSaveClick = { /* TODO: Implement save for related pins */ },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheet(
    pinId: String,
    comments: List<Comment>,
    repliesMap: Map<String, List<Comment>>,
    expandedReplies: Set<String>,
    commentsCount: Int,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmitComment: (String) -> Unit,
    onShare: () -> Unit,
    onToggleCommentLike: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onAddReply: (String, String) -> Unit,
    onToggleRepliesExpanded: (String) -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE0E0E0))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF1C1C1C)
                    )
                }
                
                Text(
                    text = "$commentsCount comments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1C)
                )
                
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Outlined.ArrowUpward,
                        contentDescription = "Share",
                        tint = Color(0xFF1C1C1C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Reaction Buttons Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReactionButton(
                    text = "Love it ❤️",
                    onClick = { onSubmitComment("Love it ❤️") },
                    modifier = Modifier.weight(1f)
                )
                ReactionButton(
                    text = "Brilliant!",
                    onClick = { onSubmitComment("Brilliant!") },
                    modifier = Modifier.weight(1f)
                )
                ReactionButton(
                    text = "Obsessed 😍",
                    onClick = { onSubmitComment("Obsessed 😍") },
                    modifier = Modifier.weight(1f)
                )
                ReactionButton(
                    text = "Looks cool",
                    onClick = { onSubmitComment("Looks cool") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No comments yet",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    comments.forEach { comment ->
                        CommentItemWithReplies(
                            comment = comment,
                            replies = repliesMap[comment._id] ?: emptyList(),
                            isRepliesExpanded = comment._id in expandedReplies,
                            onToggleLike = { onToggleCommentLike(comment._id) },
                            onAddReply = { replyText -> onAddReply(replyText, comment._id) },
                            onNavigateToUserProfile = onNavigateToUserProfile,
                            onDelete = { onDeleteComment(comment._id) },
                            onToggleRepliesExpanded = { onToggleRepliesExpanded(comment._id) },
                            onToggleReplyLike = onToggleCommentLike,
                            onDeleteReply = onDeleteComment
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Comment Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* TODO: Add media */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChange,
                    placeholder = { Text("Add a comment") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFFE60023)
                    ),
                    trailingIcon = {
                        if (commentText.isNotBlank()) {
                            IconButton(onClick = { onSubmitComment(commentText) }) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color(0xFFE60023)
                                )
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReactionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF5F5F5),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1C)
            )
        }
    }
}

@Composable
private fun CommentItemWithReplies(
    comment: Comment,
    replies: List<Comment>,
    isRepliesExpanded: Boolean,
    onToggleLike: () -> Unit,
    onAddReply: (String) -> Unit,
    onDelete: () -> Unit,
    onToggleRepliesExpanded: () -> Unit,
    onToggleReplyLike: (String) -> Unit,
    onDeleteReply: (String) -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {},
    isReply: Boolean = false
) {
    var replyText by remember { mutableStateOf("") }
    var showReplyInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Main Comment
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Indentation for replies
            if (isReply) {
                Spacer(modifier = Modifier.width(24.dp))
            }

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(40.dp)
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
                Text(
                    text = comment.user.username.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Username and timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = comment.user.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1C)
                    )
                    Text(
                        text = formatTimestamp(comment.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Comment content
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = Color(0xFF1C1C1C),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions: Reply, Like, More
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showReplyInput = !showReplyInput },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Reply",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }

                    Row(
                        modifier = Modifier.clickable { onToggleLike() },
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (comment.isLiked) Color(0xFFE60023) else Color(0xFF757575),
                            modifier = Modifier.size(16.dp)
                        )
                        if (comment.likesCount > 0) {
                            Text(
                                text = "${comment.likesCount}",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }

                    // More options dropdown
                    var showMoreMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.MoreHoriz,
                                contentDescription = "More",
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color(0xFFD32F2F)) },
                                onClick = {
                                    onDelete()
                                    showMoreMenu = false
                                }
                            )
                        }
                    }
                }

                // Inline Reply Input
                if (showReplyInput) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Write a reply...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color(0xFFE60023)
                            ),
                            maxLines = 3,
                            trailingIcon = {
                                if (replyText.isNotBlank()) {
                                    IconButton(onClick = {
                                        onAddReply(replyText)
                                        replyText = ""
                                        showReplyInput = false
                                    }) {
                                        Icon(
                                            Icons.Default.Send,
                                            contentDescription = "Send",
                                            tint = Color(0xFFE60023),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // View Replies Button
                if (comment.repliesCount > 0 && !isReply) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onToggleRepliesExpanded,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (isRepliesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRepliesExpanded) "Hide ${comment.repliesCount} replies" else "View ${comment.repliesCount} replies",
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Nested Replies
        if (isRepliesExpanded && replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                replies.forEach { reply ->
                    CommentItemWithReplies(
                        comment = reply,
                        replies = emptyList(), // Replies don't have nested replies
                        isRepliesExpanded = false,
                        onToggleLike = { onToggleReplyLike(reply._id) },
                        onAddReply = { /* Replies can't have replies */ },
                        onDelete = { onDeleteReply(reply._id) },
                        onToggleRepliesExpanded = { },
                        onToggleReplyLike = onToggleReplyLike,
                        onNavigateToUserProfile = onNavigateToUserProfile,
                        onDeleteReply = onDeleteReply,
                        isReply = true
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = sdf.parse(timestamp)
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        when {
            diff < 60_000 -> "now"
            diff < 3_600_000 -> "${diff / 60_000}m"
            diff < 86_400_000 -> "${diff / 3_600_000}h"
            diff < 2_592_000_000 -> "${diff / 86_400_000}d"
            diff < 31_536_000_000 -> "${diff / 2_592_000_000}mo"
            else -> "${diff / 31_536_000_000}y"
        }
    } catch (e: Exception) {
        timestamp
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaCarousel(
    media: List<kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Media>,
    modifier: Modifier = Modifier,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    isDownloading: Boolean,
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { media.size })
    
    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val mediaItem = media[page]
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = mediaItem.mediaUrl,
                    contentDescription = "Media ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Media type indicator (image/video)
                if (mediaItem.resourceType == "video") {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
        
        // Back button overlay
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 4.dp
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Page indicator
        if (media.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(media.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) 
                                    Color.White 
                                else 
                                    Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
        
        // Action buttons overlay (top right)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) "Unsave" else "Save",
                        tint = if (isSaved) Color(0xFFE60023) else Color(0xFF1C1C1C),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF1C1C1C),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxSize(),
                    enabled = !isDownloading
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFE60023),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Download",
                            tint = Color(0xFF1C1C1C),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = { /* TODO: More options */ },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFF1C1C1C),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Color(0xFFE60023), strokeWidth = 3.dp)
            Text(text = "Loading pin details...", fontSize = 14.sp, color = Color(0xFF757575))
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60023)),
                shape = RoundedCornerShape(24.dp)
            ) { Text("Retry") }
        }
    }
}

@Composable
private fun ErrorSnackbar(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
private fun SuccessSnackbar(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF388E3C))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF2E7D32))
            }
        }
    }
}
