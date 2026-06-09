package com.example.ui.components

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Structure for dynamic simulated fan comments
data class LiveChatMessage(
    val username: String,
    val text: String,
    val avatarColor: Color,
    val isSystem: Boolean = false
)

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerView(
    channel: Channel,
    onClosePlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlayerLoading by remember { mutableStateOf(true) }
    var hasPlayerError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isVideoMuted by remember { mutableStateOf(false) }
    var isVideoPaused by remember { mutableStateOf(false) }
    var currentAspectRatioMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) } // Default FIT

    // Create and remember ExoPlayer
    val exoPlayer = remember(channel.url) {
        try {
            ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ONE
                
                // Add listener to manage loading state and error handling
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        isPlayerLoading = state == Player.STATE_BUFFERING
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        hasPlayerError = true
                        errorMessage = "Stream unplayable: ${error.localizedMessage ?: "Invalid source"}"
                        isPlayerLoading = false
                    }
                })
            }
        } catch (e: Exception) {
            null
        }
    }

    // Load Stream Source Safely
    LaunchedEffect(exoPlayer, channel.url) {
        exoPlayer?.let { player ->
            try {
                hasPlayerError = false
                isPlayerLoading = true
                
                val sourceUri = channel.url
                val mediaItem = if (sourceUri.contains(".m3u8") || sourceUri.contains("m3u8")) {
                    MediaItem.Builder()
                        .setUri(sourceUri)
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()
                } else {
                    MediaItem.fromUri(sourceUri)
                }
                
                player.setMediaItem(mediaItem)
                player.prepare()
            } catch (ex: Exception) {
                hasPlayerError = true
                errorMessage = "Source preparation error"
                isPlayerLoading = false
            }
        }
    }

    // Set volume relative to mute button
    LaunchedEffect(isVideoMuted, exoPlayer) {
        exoPlayer?.volume = if (isVideoMuted) 0f else 1f
    }

    // Direct toggle for pause/play
    LaunchedEffect(isVideoPaused, exoPlayer) {
        if (isVideoPaused) {
            exoPlayer?.pause()
        } else {
            exoPlayer?.play()
        }
    }

    // Manage Activity / Screen Lifecycle (Pause when backgrounded, release on dispose)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer?.pause()
                isVideoPaused = true
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                exoPlayer?.release()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.release()
        }
    }

    // Chat simulation variables
    val chatMessages = remember { mutableStateListOf<LiveChatMessage>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Seed initial messages
    LaunchedEffect(channel.id) {
        chatMessages.clear()
        chatMessages.add(LiveChatMessage("StreamBot", "Welcome to ${channel.name} live discussion! Join the chat.", Color(0xFF4CAF50), isSystem = true))
        chatMessages.add(LiveChatMessage("LiveViewer", "Hype is real, stream is working flawlessly! 🔥", Color(0xFF00BCD4)))
    }

    // Loop simulated messages at dynamic intervals
    val usersList = listOf(
        "MessiFan10", "GoalGetter", "CricketLover99", "SuperSpiker", "FormulaFanatic", "BaseBallAce",
        "WicketWizard", "SlamDunker", "SportsGeek", "AeroSpeed", "FieldMarshal", "NetSmasher"
    )
    val sportsHypeQuotes = listOf(
        "OMG what a run! 🤯", "That is an absolute masterclass of a play right there!", "Is there anything this team can't do?!",
        "Absolute stunner! Let's goooo!", "Defense was sleeping, brilliant goal/hit!", "Ref needs glasses! Red Card!",
        "OMG this is the best stream! Ultra HD quality with no buffering!", "What a clutch moment! Unbelievable!",
        "Who else is watching this match live from their couch? 📺", "This game is legendary, my nerves are fried!",
        "Defending champions are under intense pressure here!", "Amazing tactics! Tactical brilliant of the coach."
    )
    val colorsList = listOf(
        Color(0xFFFFADAD), Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF),
        Color(0xFF9BF6FF), Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFC6FF)
    )

    LaunchedEffect(channel.id) {
        while (true) {
            delay(Random.nextLong(2000, 5000))
            val randomUser = usersList.random()
            val randomQuote = sportsHypeQuotes.random()
            val randomColor = colorsList.random()
            chatMessages.add(LiveChatMessage(randomUser, randomQuote, randomColor))
            if (chatMessages.size > 40) {
                chatMessages.removeAt(0)
            }
            scope.launch {
                if (chatMessages.isNotEmpty()) {
                    listState.animateScrollToItem(chatMessages.size - 1)
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // Deep Slate background
        color = Color(0xFF0F172A)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Video Frame (Top Section)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                if (hasPlayerError) {
                    // Refined Error Screen with Explanation
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Unplayable Stream",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Failed to load Live Broadcast",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (channel.isPreloaded) 
                                "Public streaming URL is temporarily offline, geoblocked, or busy. Feel free to use 'Add Custom' with your own M3U8 IPTV URL!"
                            else 
                                errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { hasPlayerError = false; isVideoPaused = false; exoPlayer?.seekTo(0) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Connection")
                        }
                    }
                } else {
                    // AndroidView with Media3 Player
                    if (exoPlayer != null) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false // We provide premium custom overlays
                                    // Set scale-mode
                                    resizeMode = currentAspectRatioMode
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            update = { view ->
                                view.resizeMode = currentAspectRatioMode
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Simple overlay gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )

                    // Loading Spinner Overlay
                    if (isPlayerLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                        )
                    }

                    // LIVE indicator sticker
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEF4444)) // Vivid Red
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bottom Player Controls (Mute, Play, Scaling Indicators)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isVideoPaused = !isVideoPaused },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .size(36.dp)
                                    .testTag("player_play_pause_button")
                            ) {
                                if (isVideoPaused) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(width = 4.dp, height = 12.dp).background(Color.White))
                                        Box(modifier = Modifier.size(width = 4.dp, height = 12.dp).background(Color.White))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { isVideoMuted = !isVideoMuted },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .size(36.dp)
                                    .testTag("player_mute_button")
                            ) {
                                Text(
                                    text = if (isVideoMuted) "🔇" else "🔊",
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // Right buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Scale mode switcher
                            TextButton(
                                onClick = {
                                    currentAspectRatioMode = when (currentAspectRatioMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .height(36.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                val scaleText = when (currentAspectRatioMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> "Fit"
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Zoom"
                                    AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Fill"
                                    else -> "Fit"
                                }
                                Text(
                                    text = "📺 $scaleText",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Live Match Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B)) // Slate 800
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${channel.category} Broadcast • " + (if (channel.isPreloaded) "Official Feed" else "User IPTV"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8) // Slate 400
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onClosePlayer,
                            modifier = Modifier
                                .testTag("close_player_button")
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Broadcast",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Simulated Interactive Sports Score Widget if active
                if (channel.liveScoreInfo.isNotEmpty() || channel.liveStatusText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E)) // Neon Green pulse
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MATCH TICKER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22C55E),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channel.liveScoreInfo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            if (channel.liveStatusText.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${channel.liveStatusText})",
                                    fontSize = 11.sp,
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Live Fan Discussion Stream (Chat Panel Client-Side)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A)) // Slate 900
            ) {
                // Chat header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131B2E))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💬",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE FAN CHAT",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Simulated views
                    Text(
                        text = "1.2K watching",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Chat Messages List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages) { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                if (msg.isSystem) {
                                    // System notification
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = msg.avatarColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                } else {
                                    // Standard User Message
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(msg.avatarColor)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = msg.username.take(1).uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = msg.username,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8) // Sky blue username
                                        )
                                        Text(
                                            text = msg.text,
                                            fontSize = 12.sp,
                                            color = Color.White
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
