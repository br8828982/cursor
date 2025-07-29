package com.iptv.player.data.repository

import com.iptv.player.data.database.ChannelDao
import com.iptv.player.data.model.Channel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface ChannelRepository {
    fun getAllChannels(): Flow<List<Channel>>
    fun getChannelsByGroup(group: String): Flow<List<Channel>>
    fun getFavoriteChannels(): Flow<List<Channel>>
    fun getAllGroups(): Flow<List<String>>
    suspend fun getChannelById(id: Long): Channel?
    suspend fun insertChannel(channel: Channel): Long
    suspend fun insertChannels(channels: List<Channel>)
    suspend fun updateChannel(channel: Channel)
    suspend fun deleteChannel(channel: Channel)
    suspend fun deleteAllChannels()
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    suspend fun updatePlayStats(id: Long, timestamp: Long)
}

@Singleton
class ChannelRepositoryImpl @Inject constructor(
    private val channelDao: ChannelDao
) : ChannelRepository {
    
    override fun getAllChannels(): Flow<List<Channel>> = channelDao.getAllChannels()
    
    override fun getChannelsByGroup(group: String): Flow<List<Channel>> = 
        channelDao.getChannelsByGroup(group)
    
    override fun getFavoriteChannels(): Flow<List<Channel>> = channelDao.getFavoriteChannels()
    
    override fun getAllGroups(): Flow<List<String>> = channelDao.getAllGroups()
    
    override suspend fun getChannelById(id: Long): Channel? = channelDao.getChannelById(id)
    
    override suspend fun insertChannel(channel: Channel): Long = channelDao.insertChannel(channel)
    
    override suspend fun insertChannels(channels: List<Channel>) = channelDao.insertChannels(channels)
    
    override suspend fun updateChannel(channel: Channel) = channelDao.updateChannel(channel)
    
    override suspend fun deleteChannel(channel: Channel) = channelDao.deleteChannel(channel)
    
    override suspend fun deleteAllChannels() = channelDao.deleteAllChannels()
    
    override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) = 
        channelDao.updateFavoriteStatus(id, isFavorite)
    
    override suspend fun updatePlayStats(id: Long, timestamp: Long) = 
        channelDao.updatePlayStats(id, timestamp)
}
