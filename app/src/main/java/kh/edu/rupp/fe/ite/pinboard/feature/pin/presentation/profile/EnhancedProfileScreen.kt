package kh.edu.rupp.fe.ite.pinboard.feature.pin.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProfileScreen(
    userId: String?,
    isCurrentUser: Boolean = true,
    onNavigateBack: () -> Unit = {},
    onPinClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var isFollowing by remember { mutableStateOf(false) }
    
    // Mock data - replace with actual data from ViewModel
    val user = remember {
        ProfileUser(
            id = userId ?: "",
            username = "johndoe",
            fullName = "John Doe",
            bio = "Digital artist and designer. Love creating beautiful pins! 🎨",
            website = "https://johndoe.com",
            location = "San Francisco, CA",
            profilePicture = null,
            followersCount = 1234,
            followingCount = 567,
            pinsCount = 89
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user.username) },
                navigationIcon = {
                    if (!isCurrentUser) {
                        @androidx.compose.runtime.Composable {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    } else null
                },
                actions = {
                    if (isCurrentUser) {
                        IconButton(onClick = { /* TODO: Settings */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    } else {
                        IconButton(onClick = { /* TODO: Share profile */ }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { /* TODO: More options */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Profile Header
            item {
                ProfileHeader(
                    user = user,
                    isCurrentUser = isCurrentUser,
                    isFollowing = isFollowing,
                    onFollowClick = { isFollowing = !isFollowing },
                    onEditProfile = { /* TODO: Edit profile */ }
                )
            }

            // Stats Row
            item {
                StatsRow(
                    followersCount = user.followersCount,
                    followingCount = user.followingCount,
                    pinsCount = user.pinsCount,
                    onFollowersClick = { /* TODO: Show followers */ },
                    onFollowingClick = { /* TODO: Show following */ }
                )
            }

            // Tabs
            item {
                ProfileTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // Content based on selected tab
            item {
                when (selectedTab) {
                    0 -> PinsGrid(onPinClick = onPinClick)
                    1 -> BoardsGrid()
                    2 -> SavedPinsGrid(onPinClick = onPinClick)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    user: ProfileUser,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onEditProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (user.profilePicture != null) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Name
        Text(
            text = user.fullName ?: user.username,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1C)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Username
        Text(
            text = "@${user.username}",
            fontSize = 16.sp,
            color = Color(0xFF757575)
        )

        // Bio
        user.bio?.let { bio ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = bio,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Website
        user.website?.let { website ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* TODO: Open website */ }
            ) {
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = "Website",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = website,
                    fontSize = 14.sp,
                    color = Color(0xFF1976D2)
                )
            }
        }

        // Location
        user.location?.let { location ->
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = location,
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button
        if (isCurrentUser) {
            OutlinedButton(
                onClick = onEditProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1C1C1C)
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            Button(
                onClick = onFollowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color(0xFFF5F5F5) else Color(0xFFE60023),
                    contentColor = if (isFollowing) Color(0xFF1C1C1C) else Color.White
                )
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatsRow(
    followersCount: Int,
    followingCount: Int,
    pinsCount: Int,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            count = followersCount,
            label = "Followers",
            onClick = onFollowersClick
        )
        Divider(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp),
            color = Color(0xFFE0E0E0)
        )
        StatItem(
            count = followingCount,
            label = "Following",
            onClick = onFollowingClick
        )
        Divider(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp),
            color = Color(0xFFE0E0E0)
        )
        StatItem(
            count = pinsCount,
            label = "Pins",
            onClick = {}
        )
    }
}

@Composable
private fun StatItem(
    count: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = formatCount(count),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
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
private fun ProfileTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.White,
        contentColor = Color(0xFFE60023),
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = Color(0xFFE60023)
            )
        }
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = { Text("Pins") }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = { Text("Boards") }
        )
        Tab(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            text = { Text("Saved") }
        )
    }
}

@Composable
private fun PinsGrid(onPinClick: (String) -> Unit) {
    // TODO: Replace with actual pins data
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Pins will be displayed here", color = Color(0xFF999999))
    }
}

@Composable
private fun BoardsGrid() {
    // TODO: Replace with actual boards data
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Boards will be displayed here", color = Color(0xFF999999))
    }
}

@Composable
private fun SavedPinsGrid(onPinClick: (String) -> Unit) {
    // TODO: Replace with actual saved pins data
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Saved pins will be displayed here", color = Color(0xFF999999))
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}

data class ProfileUser(
    val id: String,
    val username: String,
    val fullName: String?,
    val bio: String?,
    val website: String?,
    val location: String?,
    val profilePicture: String?,
    val followersCount: Int,
    val followingCount: Int,
    val pinsCount: Int
)

