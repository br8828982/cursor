package com.iptv.player.data.repository

import android.content.Context
import android.net.Uri
import com.iptv.player.data.database.PlaylistDao
import com.iptv.player.data.model.Channel
import com.iptv.player.data.model.Playlist
import com.iptv.player.parser.M3UParser
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun insertPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun deleteAllPlaylists()
    suspend fun loadPlaylistChannels(playlist: Playlist): List<Channel>
    suspend fun importPlaylistFromUrl(url: String, name: String, headers: Map<String, String>? = null): List<Channel>
    suspend fun importPlaylistFromUri(context: Context, uri: Uri, name: String): List<Channel>
    suspend fun importPlaylistFromFile(filePath: String, name: String): List<Channel>
}

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val m3uParser: M3UParser,
    private val okHttpClient: OkHttpClient
) : PlaylistRepository {
    
    override fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    
    override suspend fun getPlaylistById(id: Long): Playlist? = playlistDao.getPlaylistById(id)
    
    override suspend fun insertPlaylist(playlist: Playlist): Long = playlistDao.insertPlaylist(playlist)
    
    override suspend fun updatePlaylist(playlist: Playlist) = playlistDao.updatePlaylist(playlist)
    
    override suspend fun deletePlaylist(playlist: Playlist) = playlistDao.deletePlaylist(playlist)
    
    override suspend fun deleteAllPlaylists() = playlistDao.deleteAllPlaylists()
    
    override suspend fun loadPlaylistChannels(playlist: Playlist): List<Channel> {
        return when {
            playlist.isLocal && playlist.filePath != null -> {
                importPlaylistFromFile(playlist.filePath, playlist.name)
            }
            playlist.url != null -> {
                importPlaylistFromUrl(playlist.url, playlist.name, playlist.headers)
            }
            else -> emptyList()
        }
    }
    
    override suspend fun importPlaylistFromUrl(
        url: String, 
        name: String, 
        headers: Map<String, String>?
    ): List<Channel> {
        return try {
            val requestBuilder = Request.Builder().url(url)
            
            // Add custom headers
            headers?.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            
            val request = requestBuilder.build()
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                response.body?.byteStream()?.use { inputStream ->
                    m3uParser.parseM3U(inputStream)
                } ?: emptyList()
            } else {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to load playlist from URL: ${e.message}", e)
        }
    }
    
    override suspend fun importPlaylistFromUri(
        context: Context, 
        uri: Uri, 
        name: String
    ): List<Channel> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                m3uParser.parseM3U(inputStream)
            } ?: emptyList()
        } catch (e: Exception) {
            throw Exception("Failed to load playlist from URI: ${e.message}", e)
        }
    }
    
    override suspend fun importPlaylistFromFile(filePath: String, name: String): List<Channel> {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                throw Exception("File does not exist: $filePath")
            }
            
            FileInputStream(file).use { inputStream ->
                m3uParser.parseM3U(inputStream)
            }
        } catch (e: Exception) {
            throw Exception("Failed to load playlist from file: ${e.message}", e)
        }
    }
}
