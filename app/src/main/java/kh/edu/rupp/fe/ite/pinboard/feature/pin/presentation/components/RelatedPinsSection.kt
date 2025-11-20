package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import coil.compose.AsyncImage
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.model.Pin

@Composable
fun RelatedPinsSection(
    relatedPins: List<Pin>,
    onPinClick: (String) -> Unit,
    onSaveClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8))
            .padding(vertical = 16.dp)
    ) {
        // Section Header
        Text(
            text = "More like this",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1C),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal scrolling pins
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(relatedPins) { pin ->
                RelatedPinCard(
                    pin = pin,
                    onClick = { onPinClick(pin._id) },
                    onSaveClick = { onSaveClick(pin._id) }
                )
            }
        }
    }
}

private fun LazyItemScope.onPinClick(p1: String?) {}

private fun LazyItemScope.onSaveClick(p1: String?) {}

@Composable
fun RelatedPinCard(
    pin: Pin,
    onClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSaved by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Pin Image
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

                // Save button overlay
                IconButton(
                    onClick = {
                        isSaved = !isSaved
                        onSaveClick()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) Color(0xFFE60023) else Color(0xFF1C1C1C),
                        modifier = Modifier.size(20.dp)
                    )
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

                pin.user?.let { user ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.username,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun RelatedPinsGrid(
    relatedPins: List<Pin>,
    onPinClick: (String) -> Unit,
    onSaveClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp)
    ) {
        // Section Header
        Text(
            text = "More like this",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1C),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Grid of related pins
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            modifier = Modifier.height(600.dp) // Fixed height for nested scroll
        ) {
            items(relatedPins) { pin ->
                RelatedPinGridItem(
                    pin = pin,
                    onClick = { onPinClick(pin._id) },
                    onSaveClick = { onSaveClick(pin._id) }
                )
            }
        }
    }
}

private fun LazyStaggeredGridItemScope.onPinClick(p1: String?) {}
private fun LazyStaggeredGridItemScope.onSaveClick(p1: String?) {}

@Composable
fun RelatedPinGridItem(
    pin: Pin,
    onClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSaved by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Pin Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
            ) {
                AsyncImage(
                    model = pin.firstMediaUrl ?: pin.imageUrl ?: pin.videoUrl,
                    contentDescription = pin.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Save button overlay
                IconButton(
                    onClick = {
                        isSaved = !isSaved
                        onSaveClick()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) Color(0xFFE60023) else Color(0xFF1C1C1C),
                        modifier = Modifier.size(20.dp)
                    )
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
            }
        }
    }
}

