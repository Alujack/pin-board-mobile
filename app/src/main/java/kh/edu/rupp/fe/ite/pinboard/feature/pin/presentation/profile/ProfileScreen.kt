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
            username = "Your Username",
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
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
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile Picture", tint = Color.Gray)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(username, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(pinsCount, "Pins")
            StatItem(followersCount, "Followers")
            StatItem(followingCount, "Following")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onEditProfile,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60023))
        ) {
            Text("Edit Profile", color = Color.White)
        }
    }
}

@Composable
private fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.Gray, fontSize = 14.sp)
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
        placeholder = { Text("Search pins...") },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFE60023),
            unfocusedBorderColor = Color.LightGray
        )
    )
}

@Composable
private fun ProfileTabRow(selectedTab: ProfileTab, onTabSelected: (ProfileTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ProfileTab.values().forEach { tab ->
            TextButton(
                onClick = { onTabSelected(tab) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (selectedTab == tab) Color(0xFFE60023) else Color.Gray
                )
            ) {
                Text(
                    text = tab.displayName,
                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                )
            }
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
        EmptyPlaceholder("No media yet")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            AsyncImage(
                model = item.thumbnail_url ?: item.media_url,
                contentDescription = item.public_id,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(220.dp)
                    .fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ActionIcon(
                    icon = if (isSavedContext) Icons.Outlined.BookmarkRemove
                    else if (isSaved) Icons.Filled.Bookmark
                    else Icons.Outlined.BookmarkBorder,
                    onClick = onSaveOrUnsave
                )
                ActionIcon(icon = Icons.Outlined.Download, onClick = onDownload)
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
        EmptyPlaceholder("No pins found")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pins) { pin ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPinClick(pin) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box {
                        AsyncImage(
                            model = pin.imageUrl ?: pin.videoUrl,
                            contentDescription = pin.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ActionIcon(
                                icon = if (savedIds.contains(pin._id ?: "")) Icons.Filled.Bookmark
                                else Icons.Outlined.BookmarkBorder,
                                onClick = { onPinToggleSave(pin) }
                            )
                            ActionIcon(Icons.Outlined.Download, onClick = { onPinDownload(pin) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .background(Color.White.copy(alpha = 0.85f), CircleShape)
    ) {
        Icon(icon, contentDescription = null, tint = Color.Black)
    }
}

@Composable
private fun EmptyPlaceholder(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.Gray, fontSize = 16.sp)
    }
}

enum class ProfileTab(val displayName: String) {
    Created("Created"),
    Saved("Saved")
}
