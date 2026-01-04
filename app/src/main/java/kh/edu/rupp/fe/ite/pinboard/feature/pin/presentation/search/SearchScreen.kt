package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Board
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onOpenPinDetail: (String) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    // Load all pins when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadAllPins()
    }

    // Search when query changes (only if no board is selected)
    LaunchedEffect(searchQuery) {
        if (state.selectedBoard == null) {
            if (searchQuery.isNotBlank()) {
                viewModel.searchPins(searchQuery)
            } else {
                // Show all pins when search is empty
                viewModel.loadAllPins()
            }
        }
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { viewModel.searchPins(it) },
                modifier = Modifier.padding(16.dp)
            )

            // Explore Boards Section - Horizontal Scrollable Chips
            BoardChipsSection(
                publicBoards = state.publicBoards,
                selectedBoard = state.selectedBoard,
                isLoadingBoards = state.isLoadingBoards,
                onBoardSelected = { viewModel.selectBoard(it) },
                onClearSelection = { viewModel.clearBoardSelection() }
            )

            // Search Results
            when {
                state.isLoadingBoardPins -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFE60023))
                    }
                }
                state.isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFE60023))
                    }
                }

                state.searchResults.isNotEmpty() -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 16.dp
                    ) {
                        items(state.searchResults) { pin ->
                            val id = pin._id ?: ""
                            val isSaved = state.savedPinIds.contains(id)
                            PinItem(
                                pin = pin,
                                isSaved = isSaved,
                                onClick = {
                                    if (id.isNotBlank()) {
                                        onOpenPinDetail(id)
                                    }
                                },
                                onToggleSave = {
                                    if (id.isNotBlank()) {
                                        if (isSaved) {
                                            viewModel.unsavePin(id)
                                        } else {
                                            viewModel.savePin(id)
                                        }
                                    }
                                },
                                onDownload = {
                                    pin._id?.let { viewModel.downloadPin(it) }
                                }
                            )
                        }
                    }
                }

                state.selectedBoard != null && state.searchResults.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFF9E9E9E)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No pins in this board",
                                color = Color(0xFF424242),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This board doesn't have any pins yet",
                                color = Color(0xFF757575),
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                searchQuery.isNotBlank() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFF9E9E9E)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No results found",
                                color = Color(0xFF424242),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try searching for something else",
                                color = Color(0xFF757575),
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFF9E9E9E)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Search for pins",
                                color = Color(0xFF424242),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Discover ideas and inspiration",
                                color = Color(0xFF757575),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Error Message
            state.errorMessage?.let { errorMsg ->
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
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMsg,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
        }
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
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search pins...") },
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
private fun PinItem(
    pin: Pin,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    onDownload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(getPinAspectRatio(pin))
                .clip(RoundedCornerShape(20.dp))
        ) {
            // Pin Image
            AsyncImage(
                model = pin.firstMediaUrl ?: pin.imageUrl ?: pin.videoUrl,
                contentDescription = pin.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay at bottom for text readability
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            // Top actions
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save button
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isSaved) {
                                Icons.Filled.Bookmark
                            } else {
                                Icons.Outlined.BookmarkBorder
                            },
                            contentDescription = if (isSaved) "Saved" else "Save",
                            modifier = Modifier.size(20.dp),
                            tint = if (isSaved) Color(0xFFE60023) else Color(0xFF1C1C1C)
                        )
                    }
                }

                // Download button
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Outlined.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF1C1C1C)
                        )
                    }
                }
            }

            // Bottom info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = pin.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                pin.user?.let { user ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = user.username,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// Helper function to get varying aspect ratios for Pinterest-style staggered grid
private fun getPinAspectRatio(pin: Pin): Float {
    // Use pin ID hash to create variety in aspect ratios
    val hash = abs((pin._id ?: pin.id ?: "").hashCode())
    // Aspect ratios between 0.6 (tall) and 1.4 (wide) for variety
    return 0.6f + (hash % 80) / 100f // Range: 0.6 to 1.4
}

@Composable
fun BoardChipsSection(
    publicBoards: List<Board>,
    selectedBoard: Board?,
    isLoadingBoards: Boolean,
    onBoardSelected: (Board) -> Unit,
    onClearSelection: () -> Unit
) {
    if (isLoadingBoards) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFFE60023),
                strokeWidth = 2.dp
            )
        }
    } else if (publicBoards.isNotEmpty()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clear selection chip (if a board is selected)
            if (selectedBoard != null) {
                item {
                    BoardChip(
                        text = "All Boards",
                        isSelected = false,
                        onClick = onClearSelection,
                        showIcon = false
                    )
                }
            }
            
            // Board chips
            items(publicBoards) { board ->
                BoardChip(
                    text = board.name,
                    isSelected = selectedBoard?._id == board._id,
                    onClick = { onBoardSelected(board) },
                    showIcon = true
                )
            }
        }
    }
}

@Composable
fun BoardChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showIcon: Boolean = true
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFFE60023) else Color(0xFFF5F5F5),
        border = if (!isSelected) 
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) Color.White else Color(0xFF6B6B6B)
                )
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF1C1C1C),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SelectedBoardChip(
    board: Board,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFE60023).copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE60023))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFFE60023)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Viewing: ${board.name}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE60023)
                )
                Text(
                    text = "${board.pinCount} pins",
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B6B)
                )
            }
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Clear",
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFE60023)
                )
            }
        }
    }
}