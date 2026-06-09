package com.example.data.db

import androidx.room.*
import com.example.data.model.Channel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY isFavorite DESC, name ASC")
    fun getAllChannelsFlow(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE category = :category ORDER BY isFavorite DESC, name ASC")
    fun getChannelsByCategoryFlow(category: String): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannelsFlow(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE id = :channelId")
    suspend fun getChannelById(channelId: String): Channel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Delete
    suspend fun deleteChannel(channel: Channel)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun updateFavoriteStatus(channelId: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun getChannelCount(): Int
}
