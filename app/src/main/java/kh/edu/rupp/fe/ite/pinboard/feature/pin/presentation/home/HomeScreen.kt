package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onPinClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshPins() }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .pullRefresh(pullRefreshState)
    ) {
        when {
            uiState.isLoading && uiState.pins.isEmpty() -> {
                // Initial loading state
                LoadingView()
            }
            uiState.pins.isEmpty() && !uiState.isLoading -> {
                // Empty state
                EmptyStateView(
                    onRetry = { viewModel.loadPins() }
                )
            }
            else -> {
                // Pin grid
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    items(uiState.pins) { pin ->
                        PinCard(
                            pin = pin,
                            isSaved = uiState.savedPinIds.contains(pin._id ?: ""),
                            onClick = { onPinClick(pin._id ?: "") },
                            onToggleSave = { viewModel.toggleSavePin(pin._id ?: "") }
                        )
                    }
                }
            }
        }

        // Pull refresh indicator
        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = Color(0xFFE60023)
        )

        // Error message
        uiState.errorMessage?.let { error ->
            ErrorSnackbar(
                message = error,
                onDismiss = { viewModel.clearError() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun PinCard(
    pin: Pin,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Column {
                // Pin Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = pin.firstMediaUrl ?: pin.imageUrl ?: pin.videoUrl,
                        contentDescription = pin.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Save button overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 2.dp
                    ) {
                        IconButton(
                            onClick = onToggleSave,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (isSaved) "Unsave" else "Save",
                                tint = if (isSaved) Color(0xFFE60023) else Color(0xFF1C1C1C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Pin Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = pin.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1C),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    pin.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                color = Color(0xFF757575),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    pin.user?.let { user ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF9E9E9E)
                            )
                            Text(
                                text = user.username,
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
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
                color = Color(0xFFE60023)
            )
            Text(
                text = "Loading pins...",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun EmptyStateView(onRetry: () -> Unit) {
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
            Text(
                text = "No pins yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Text(
                text = "Create your first pin or explore others",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
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

@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(8.dp)
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

