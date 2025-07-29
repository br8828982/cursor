package com.iptv.player.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.model.Channel
import com.iptv.player.data.model.Playlist
import com.iptv.player.data.repository.ChannelRepository
import com.iptv.player.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = false,
    val recentChannels: List<Channel> = emptyList(),
    val favoriteChannels: List<Channel> = emptyList(),
    val totalChannels: Int = 0,
    val totalPlaylists: Int = 0,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Combine all data streams
                combine(
                    channelRepository.getAllChannels(),
                    channelRepository.getFavoriteChannels(),
                    playlistRepository.getAllPlaylists()
                ) { allChannels, favoriteChannels, playlists ->
                    
                    // Get recent channels (last 5 played channels)
                    val recentChannels = allChannels
                        .filter { it.lastPlayed > 0 }
                        .sortedByDescending { it.lastPlayed }
                        .take(5)
                    
                    MainUiState(
                        isLoading = false,
                        recentChannels = recentChannels,
                        favoriteChannels = favoriteChannels.take(10),
                        totalChannels = allChannels.size,
                        totalPlaylists = playlists.size
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun refreshData() {
        loadData()
    }
}
