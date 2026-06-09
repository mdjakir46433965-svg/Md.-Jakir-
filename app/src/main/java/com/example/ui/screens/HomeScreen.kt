package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.ui.components.VideoPlayerView
import com.example.ui.viewmodel.SportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SportsViewModel,
    modifier: Modifier = Modifier
) {
    val filteredChannels by viewModel.filteredChannels.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activePlayingChannel by viewModel.activePlayingChannel.collectAsState()

    var showAddChannelDialog by remember { mutableStateOf(false) }

    // Professional Polish Light Theme Adaptations
    val stadiumDark = MaterialTheme.colorScheme.background // #FEF7FF
    val cardSurface = Color(0xFFF3EDF7) // Light-lavender card background
    val neonGreen = MaterialTheme.colorScheme.primary // Polish Primary Purple (#6750A4)
    val highContrastTurquoise = MaterialTheme.colorScheme.secondary // Polish Secondary Purple

    // Categories list pairing labels with helpful emojis
    val categories = listOf(
        Pair("ALL", "🌍"),
        Pair("FAVORITES", "⭐"),
        Pair("FOOTBALL", "⚽"),
        Pair("CRICKET", "🏏"),
        Pair("BASKETBALL", "🏀"),
        Pair("MOTORSPORTS", "🏎️"),
        Pair("TENNIS", "🎾"),
        Pair("NEWS", "📰"),
        Pair("CUSTOM", "📡")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = stadiumDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddChannelDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_custom_channel_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Live Channel")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Custom", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        topBar = {
            Column(
                modifier = Modifier
                    .background(stadiumDark)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("TV", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "SPORTS TV LIVE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "All sports live streams in one app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Ticker count
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${filteredChannels.size} CHANNELS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search matches, leagues, streams...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Carousel Categories scrolling bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("categories_carousel"),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (categoryToken, emoji) ->
                    val isActive = selectedCategory == categoryToken
                    val bgStyle = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    val textStyle = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 1.dp,
                                color = if (isActive) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .background(bgStyle)
                            .clickable { viewModel.selectCategory(categoryToken) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("category_pill_$categoryToken"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = categoryToken,
                                style = MaterialTheme.typography.bodySmall,
                                color = textStyle,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Real-time Video Player overlay wrapper
            AnimatedVisibility(
                visible = activePlayingChannel != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                activePlayingChannel?.let { targetChannel ->
                    VideoPlayerView(
                        channel = targetChannel,
                        onClosePlayer = { viewModel.playChannel(null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    )
                }
            }

            // Channel Selection Lists/Grids Space
            if (filteredChannels.isEmpty()) {
                // Empty view states with helpful tips
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📺",
                        fontSize = 54.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Live Broadcasts Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No channels match your active filtering preferences. Tap 'Add Custom' in the corner to spin up your own live streaming link!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("channels_grid"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredChannels, key = { it.id }) { channel ->
                        val isPlaying = activePlayingChannel?.id == channel.id
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardSurface)
                                .border(
                                    width = if (isPlaying) 2.dp else 1.dp,
                                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.playChannel(channel) }
                                .padding(12.dp)
                                .testTag("channel_card_${channel.id}")
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Logo icon or visual badge representation
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val catEmoji = when (channel.category) {
                                        "FOOTBALL" -> "⚽"
                                        "CRICKET" -> "🏏"
                                        "BASKETBALL" -> "🏀"
                                        "MOTORSPORTS" -> "🏎️"
                                        "TENNIS" -> "🎾"
                                        "NEWS" -> "📰"
                                        else -> "📡"
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(stadiumDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(catEmoji, fontSize = 14.sp)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Bookmark icon
                                        IconButton(
                                            onClick = { viewModel.toggleFavorite(channel) },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("toggle_fav_${channel.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite toggle",
                                                tint = if (channel.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Delete option for user cust channels
                                        if (!channel.isPreloaded) {
                                            IconButton(
                                                onClick = { viewModel.deleteChannel(channel) },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .testTag("delete_channel_${channel.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete custom channel",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Channel name & details
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = channel.description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Match Score/Live status footer badge
                                if (channel.liveScoreInfo.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = channel.liveScoreInfo,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = channel.liveStatusText,
                                                fontSize = 8.sp,
                                                color = if (channel.liveStatusText == "LIVE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive custom channel creation sheet dialog
    if (showAddChannelDialog) {
        var channelName by remember { mutableStateOf("") }
        var channelUrl by remember { mutableStateOf("") }
        var channelLogoUrl by remember { mutableStateOf("") }
        var channelCategory by remember { mutableStateOf("FOOTBALL") }

        val categoryOptions = listOf("FOOTBALL", "CRICKET", "BASKETBALL", "MOTORSPORTS", "TENNIS", "NEWS", "GENERAL")

        AlertDialog(
            onDismissRequest = { showAddChannelDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Add Custom IPTV Stream",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Configure a custom sports link stream (Supports adaptive live M3U8 IPTV feeds & progressive MP4 layouts).",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    OutlinedTextField(
                        value = channelName,
                        onValueChange = { channelName = it },
                        label = { Text("Channel Name (e.g. ESPN Live, Sky Sports)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_channel_name_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = channelUrl,
                        onValueChange = { channelUrl = it },
                        label = { Text("M3U8 / MP4 Broadcast URL") },
                        placeholder = { Text("https://example.com/stream.m3u8") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_channel_url_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Presets shortcut to simplify standard testing
                    Column {
                        Text(
                            text = "PRESET TESTING SAMPLES (TAP TO LOAD):",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    channelName = "Big Buck Bunny Stream"
                                    channelUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("M3U8 HLS", fontSize = 10.sp)
                            }
                            Button(
                                onClick = {
                                    channelName = "Sintel HLS Broadcast"
                                    channelUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Sintel HLS", fontSize = 10.sp)
                            }
                        }
                    }

                    // Category dropdown switcher
                    Column {
                        Text("SPORTS CATEGORY:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categoryOptions) { opt ->
                                val optActive = channelCategory == opt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (optActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { channelCategory = opt }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 10.sp,
                                        color = if (optActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (channelName.isNotBlank() && channelUrl.isNotBlank()) {
                            viewModel.addCustomChannel(
                                name = channelName,
                                url = channelUrl,
                                category = channelCategory,
                                logoUrl = channelLogoUrl
                            )
                            showAddChannelDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("dialog_submit_button")
                ) {
                    Text("Add Stream", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddChannelDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
