package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.profile

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.MediaItem
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(ProfileTab.Created) }
    var searchQuery by remember { mutableStateOf("") }

    // Load data on initial load
    LaunchedEffect(Unit) {
        viewModel.loadCreatedMedia()
    }

    // Auto search when typing
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.searchPins(searchQuery)
        }
    }

    // Main content (no Scaffold here → bottom bar remains visible)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Profile Header ---
        ProfileHeader(
            username = state.username.ifBlank { "" },
            followersCount = 1250,
            followingCount = 89,
            pinsCount = state.createdMedia.size,
            onEditProfile = {}
        )

        // --- Search bar ---
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { viewModel.searchPins(it) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // --- Tabs (Created / Saved) ---
        ProfileTabRow(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
                if (tab == ProfileTab.Created) viewModel.loadCreatedMedia()
                else viewModel.loadSavedMedia()
            }
        )

        // --- Content Switcher ---
        when {
            searchQuery.isNotBlank() -> {
                if (state.isSearching) LoadingView()
                else PinGrid(
                    pins = state.searchResults,
                    savedIds = state.savedPinIds,
                    onPinClick = {},
                    onPinToggleSave = { pin ->
                        val id = pin._id ?: return@PinGrid
                        if (state.savedPinIds.contains(id)) viewModel.unsavePin(id)
                        else viewModel.savePin(id)
                    },
                    onPinDownload = { pin -> viewModel.downloadPin(pin._id ?: "") }
                )
            }

            selectedTab == ProfileTab.Created -> {
                if (state.isLoading) LoadingView()
                else MediaGrid(
                    media = state.createdMedia,
                    savedIds = state.savedPinIds,
                    isSavedContext = false,
                    onToggleSave = { media ->
                        val id = media.pinId ?: return@MediaGrid
                        if (state.savedPinIds.contains(id)) viewModel.unsavePin(id)
                        else viewModel.savePin(id)
                    },
                    onDownload = { media -> viewModel.downloadPin(media.pinId ?: "") }
                )
            }

            selectedTab == ProfileTab.Saved -> {
                if (state.isLoading) LoadingView()
                else MediaGrid(
                    media = state.savedMedia,
                    savedIds = state.savedPinIds,
                    isSavedContext = true,
                    onToggleSave = { media -> viewModel.unsavePin(media.pinId ?: "") },
                    onDownload = { media -> viewModel.downloadPin(media.pinId ?: "") }
                )
            }
        }

        // --- Error Message ---
        state.errorMessage?.let { message ->
            ErrorMessageCard(message = message, onDismiss = { viewModel.clearError() })
        }
    }
}

// ------------------------------------------------------------------
// Components
// ------------------------------------------------------------------

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFE60023))
    }
}

@Composable
private fun ErrorMessageCard(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFD32F2F))
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
private fun ProfileHeader(
    username: String,
    followersCount: Int,
    followingCount: Int,
    pinsCount: Int,
    onEditProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile Picture",
                tint = Color.Gray,
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            username,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF1C1C1C)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(pinsCount, "Pins")
            StatItem(followersCount, "Followers")
            StatItem(followingCount, "Following")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onEditProfile,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60023)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .height(44.dp)
        ) {
            Text("Edit Profile", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$count",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF1C1C1C)
        )
        Text(
            label,
            color = Color(0xFF757575),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search your pins...", color = Color(0xFF9E9E9E)) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "Search",
                tint = Color(0xFF757575)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color(0xFF757575)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFE60023),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            cursorColor = Color(0xFFE60023)
        )
    )
}

@Composable
private fun ProfileTabRow(selectedTab: ProfileTab, onTabSelected: (ProfileTab) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Color.White,
        contentColor = Color(0xFFE60023),
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = Color(0xFFE60023),
                height = 3.dp
            )
        },
        divider = {}
    ) {
        ProfileTab.values().forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.displayName,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                },
                selectedContentColor = Color(0xFF1C1C1C),
                unselectedContentColor = Color(0xFF757575)
            )
        }
    }
}

// ------------------------------------------------------------------
// Media Grid + Pin Grid (Pinterest style)
// ------------------------------------------------------------------

@Composable
private fun MediaGrid(
    media: List<MediaItem>,
    savedIds: Set<String>,
    isSavedContext: Boolean,
    onToggleSave: (MediaItem) -> Unit,
    onDownload: (MediaItem) -> Unit
) {
    if (media.isEmpty()) {
        EmptyPlaceholder(
            icon = if (isSavedContext) Icons.Outlined.BookmarkBorder else Icons.Outlined.AddCircleOutline,
            text = if (isSavedContext) "No saved pins yet" else "No pins created yet",
            subtext = if (isSavedContext) "Start saving pins you love" else "Create your first pin"
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(media) { item ->
                MediaItemCard(
                    item = item,
                    isSaved = savedIds.contains(item.pinId ?: ""),
                    isSavedContext = isSavedContext,
                    onSaveOrUnsave = { onToggleSave(item) },
                    onDownload = { onDownload(item) }
                )
            }
        }
    }
}

@Composable
private fun MediaItemCard(
    item: MediaItem,
    isSavedContext: Boolean,
    isSaved: Boolean,
    onSaveOrUnsave: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Show loading placeholder if no image
            val imageUrl = item.media_url ?: item.thumbnail_url
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.public_id,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder when no image URL
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.BrokenImage,
                        contentDescription = "No image",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF9E9E9E)
                    )
                }
            }

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            ),
                            startY = 100f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Action buttons
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    icon = if (isSavedContext) Icons.Outlined.BookmarkRemove
                    else if (isSaved) Icons.Filled.Bookmark
                    else Icons.Outlined.BookmarkBorder,
                    tint = if (isSaved && !isSavedContext) Color(0xFFE60023) else Color(0xFF1C1C1C),
                    onClick = onSaveOrUnsave
                )
                ActionButton(
                    icon = Icons.Outlined.Download,
                    onClick = onDownload
                )
            }
        }
    }
}

@Composable
private fun PinGrid(
    pins: List<Pin>,
    savedIds: Set<String>,
    onPinClick: (Pin) -> Unit,
    onPinToggleSave: (Pin) -> Unit,
    onPinDownload: (Pin) -> Unit
) {
    if (pins.isEmpty()) {
        EmptyPlaceholder(
            icon = Icons.Outlined.SearchOff,
            text = "No pins found",
            subtext = "Try a different search term"
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pins) { pin ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPinClick(pin) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        AsyncImage(
                            model = pin.firstMediaUrl ?: pin.imageUrl ?: pin.videoUrl,
                            contentDescription = pin.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f)
                                        ),
                                        startY = 100f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )

                        // Action buttons
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isSaved = savedIds.contains(pin._id ?: "")
                            ActionButton(
                                icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                tint = if (isSaved) Color(0xFFE60023) else Color(0xFF1C1C1C),
                                onClick = { onPinToggleSave(pin) }
                            )
                            ActionButton(
                                icon = Icons.Outlined.Download,
                                onClick = { onPinDownload(pin) }
                            )
                        }

                        // Pin title at bottom
                        pin.title.let { title ->
                            if (title.isNotBlank()) {
                                Text(
                                    text = title,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tint: Color = Color(0xFF1C1C1C)
) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 2.dp
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EmptyPlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    subtext: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                color = Color(0xFF424242),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            subtext?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = Color(0xFF757575),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

enum class ProfileTab(val displayName: String) {
    Created("Created"),
    Saved("Saved")
}