package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onOpenPinDetail: (String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load user profile and pins
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
        viewModel.loadUserPins(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.username.ifBlank { "Profile" }) },
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
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE60023))
                        }
                    }
                    state.errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = state.errorMessage ?: "Error loading profile",
                                    color = Color(0xFFD32F2F),
                                    fontSize = 16.sp
                                )
                                Button(
                                    onClick = {
                                        viewModel.loadUserProfile(userId)
                                        viewModel.loadUserPins(userId)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE60023)
                                    )
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    else -> {
                        // Profile Header
                        UserProfileHeader(
                            username = state.username,
                            followersCount = state.followersCount,
                            followingCount = state.followingCount,
                            pinsCount = state.pinsCount,
                            bio = state.bio,
                            profilePicture = state.profilePicture,
                            isFollowing = state.isFollowing,
                            isFollowLoading = state.isFollowLoading,
                            onFollowClick = {
                                if (state.isFollowing) {
                                    viewModel.unfollowUser(userId)
                                } else {
                                    viewModel.followUser(userId)
                                }
                            }
                        )

                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                        // Pins Grid
                        if (state.pins.isEmpty() && !state.isLoadingPins) {
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
                                        Icons.Outlined.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color(0xFF9E9E9E)
                                    )
                                    Text(
                                        text = "No pins yet",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.pins) { pin ->
                                    PinItem(
                                        pin = pin,
                                        onClick = {
                                            pin._id?.let { onOpenPinDetail(it) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Follow/Unfollow Error Snackbar
            state.followErrorMessage?.let { errorMsg ->
                ErrorSnackbar(
                    message = errorMsg,
                    onDismiss = { viewModel.clearFollowError() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
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
private fun UserProfileHeader(
    username: String,
    followersCount: Int,
    followingCount: Int,
    pinsCount: Int,
    bio: String?,
    profilePicture: String?,
    isFollowing: Boolean,
    isFollowLoading: Boolean,
    onFollowClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        Box(
            modifier = Modifier
                .size(100.dp)
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
            if (profilePicture != null && profilePicture.isNotBlank()) {
                AsyncImage(
                    model = profilePicture,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = username.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Username
        Text(
            text = username,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF1C1C1C)
        )

        // Bio
        if (!bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = bio,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(pinsCount, "Pins")
            StatItem(followersCount, "Followers")
            StatItem(followingCount, "Following")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Follow/Unfollow Button
        Button(
            onClick = onFollowClick,
            enabled = !isFollowLoading,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFollowing) Color(0xFFBDBDBD) else Color(0xFFE60023)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .height(44.dp)
        ) {
            if (isFollowLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1C1C1C)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun PinItem(
    pin: Pin,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = pin.firstMediaUrl
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = pin.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF9E9E9E)
                    )
                }
            }

            // Title overlay
            if (!pin.title.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = pin.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

