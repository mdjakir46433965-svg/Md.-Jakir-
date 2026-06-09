package com.example.data.repository

import com.example.data.db.ChannelDao
import com.example.data.model.Channel
import kotlinx.coroutines.flow.Flow

class ChannelRepository(private val channelDao: ChannelDao) {

    val allChannels: Flow<List<Channel>> = channelDao.getAllChannelsFlow()
    val favoriteChannels: Flow<List<Channel>> = channelDao.getFavoriteChannelsFlow()

    fun getChannelsByCategory(category: String): Flow<List<Channel>> {
        return channelDao.getChannelsByCategoryFlow(category)
    }

    suspend fun insertChannel(channel: Channel) {
        channelDao.insertChannel(channel)
    }

    suspend fun insertChannels(channels: List<Channel>) {
        channelDao.insertChannels(channels)
    }

    suspend fun deleteChannel(channel: Channel) {
        channelDao.deleteChannel(channel)
    }

    suspend fun toggleFavorite(channelId: String, isCurrentlyFavorite: Boolean) {
        channelDao.updateFavoriteStatus(channelId, !isCurrentlyFavorite)
    }

    suspend fun getChannelCount(): Int {
        return channelDao.getChannelCount()
    }
}
