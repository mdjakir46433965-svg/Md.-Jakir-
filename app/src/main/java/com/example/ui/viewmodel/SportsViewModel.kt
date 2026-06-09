package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Channel
import com.example.data.repository.ChannelRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SportsViewModel(private val repository: ChannelRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activePlayingChannel = MutableStateFlow<Channel?>(null)
    val activePlayingChannel: StateFlow<Channel?> = _activePlayingChannel.asStateFlow()

    // Dynamically filter all channels based on selected category pill and search queries
    val filteredChannels: StateFlow<List<Channel>> = combine(
        repository.allChannels,
        _selectedCategory,
        _searchQuery
    ) { channels, category, query ->
        channels.filter { channel ->
            val matchesCategory = when (category) {
                "ALL" -> true
                "FAVORITES" -> channel.isFavorite
                "CUSTOM" -> !channel.isPreloaded
                else -> channel.category.equals(category, ignoreCase = true)
            }
            val matchesQuery = channel.name.contains(query, ignoreCase = true) ||
                               channel.category.contains(query, ignoreCase = true) ||
                               channel.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        prepopulateDefaultChannelsIfRequired()
    }

    private fun prepopulateDefaultChannelsIfRequired() {
        viewModelScope.launch {
            if (repository.getChannelCount() == 0) {
                val defaults = listOf(
                    Channel(
                        id = "pre_foot_1",
                        name = "Red Dev Football Pro",
                        url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                        category = "FOOTBALL",
                        logoUrl = "soccer",
                        isPreloaded = true,
                        isFavorite = true,
                        description = "UEFA Champions League highlights & Live Classic Matches.",
                        liveScoreInfo = "MUN 3 - 2 CHE (84')",
                        liveStatusText = "LIVE"
                    ),
                    Channel(
                        id = "pre_cric_1",
                        name = "T20 Cricket World TV",
                        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        category = "CRICKET",
                        logoUrl = "sports_cricket",
                        isPreloaded = true,
                        isFavorite = false,
                        description = "Continuous live action of Global T20 championships & big knocks.",
                        liveScoreInfo = "IND 184/3 vs PAK (16.4 ov)",
                        liveStatusText = "Live-Overs"
                    ),
                    Channel(
                        id = "pre_moto_1",
                        name = "Formula Prime Speed",
                        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                        category = "MOTORSPORTS",
                        logoUrl = "racing",
                        isPreloaded = true,
                        isFavorite = false,
                        description = "Apex updates, live qualification analysis & trackside records.",
                        liveScoreInfo = "VER • HAM • LEC (+0.12s)",
                        liveStatusText = "Lap 54/56"
                    ),
                    Channel(
                        id = "pre_ten_1",
                        name = "Slam Center Tennis",
                        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                        category = "TENNIS",
                        logoUrl = "sports_tennis",
                        isPreloaded = true,
                        isFavorite = false,
                        description = "International Singles & Doubles tournament relays.",
                        liveScoreInfo = "DJOKOVIC 6 5 | NADAL 4 4",
                        liveStatusText = "Set 2 • 30-30"
                    ),
                    Channel(
                        id = "pre_bask_1",
                        name = "Hoops Street Nation",
                        url = "https://developer.apple.com/streams/sintel/master.m3u8",
                        category = "BASKETBALL",
                        logoUrl = "sports_basketball",
                        isPreloaded = true,
                        isFavorite = false,
                        description = "Pro league exhibition and live street slam reels.",
                        liveScoreInfo = "LAL 102 - 98 BOS",
                        liveStatusText = "Q4 • 0:12"
                    ),
                    Channel(
                        id = "pre_news_1",
                        name = "Apex Sports News Live",
                        url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                        category = "NEWS",
                        logoUrl = "tv",
                        isPreloaded = true,
                        isFavorite = true,
                        description = "24/7 breaking news, league negotiations and player reports.",
                        liveScoreInfo = "BREAKING DEAL • IN PROGRESS",
                        liveStatusText = "HOT"
                    )
                )
                repository.insertChannels(defaults)
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playChannel(channel: Channel?) {
        _activePlayingChannel.value = channel
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            repository.toggleFavorite(channel.id, channel.isFavorite)
            // If the currently playing channel favorite status changes, update the playing cache
            if (_activePlayingChannel.value?.id == channel.id) {
                _activePlayingChannel.value = _activePlayingChannel.value?.copy(isFavorite = !channel.isFavorite)
            }
        }
    }

    fun addCustomChannel(name: String, url: String, category: String, logoUrl: String) {
        viewModelScope.launch {
            val validatedUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                url
            } else {
                "https://localhost/$url"
            }
            val newChannel = Channel(
                id = "custom_" + System.currentTimeMillis(),
                name = name.ifBlank { "Custom Channel" },
                url = validatedUrl,
                category = category.uppercase(),
                logoUrl = logoUrl.ifBlank { "custom_stream" },
                isPreloaded = false,
                isFavorite = false,
                description = "User Provided IPTV Live Link.",
                liveScoreInfo = "MANUAL LIVE BCAST",
                liveStatusText = "CUSTOM"
            )
            repository.insertChannel(newChannel)
        }
    }

    fun deleteChannel(channel: Channel) {
        viewModelScope.launch {
            if (activePlayingChannel.value?.id == channel.id) {
                _activePlayingChannel.value = null
            }
            repository.deleteChannel(channel)
        }
    }
}

class SportsViewModelFactory(private val repository: ChannelRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SportsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
