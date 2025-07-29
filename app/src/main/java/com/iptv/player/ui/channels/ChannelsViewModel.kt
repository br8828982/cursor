package com.iptv.player.ui.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.model.Channel
import com.iptv.player.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelsUiState(
    val isLoading: Boolean = false,
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val groups: List<String> = emptyList(),
    val selectedGroup: String? = null,
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val channelRepository: ChannelRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChannelsUiState())
    val uiState: StateFlow<ChannelsUiState> = _uiState.asStateFlow()
    
    init {
        loadChannels()
    }
    
    private fun loadChannels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                combine(
                    channelRepository.getAllChannels(),
                    channelRepository.getAllGroups()
                ) { channels, groups ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            channels = channels,
                            filteredChannels = channels,
                            groups = groups.sorted(),
                            error = null
                        )
                    }
                }.collect()
                
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
    
    fun searchChannels(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }
    
    fun filterByGroup(group: String?) {
        _uiState.update { it.copy(selectedGroup = group) }
        applyFilters()
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        val filteredChannels = currentState.channels.filter { channel ->
            val matchesSearch = if (currentState.searchQuery.isBlank()) {
                true
            } else {
                channel.name.contains(currentState.searchQuery, ignoreCase = true) ||
                channel.group?.contains(currentState.searchQuery, ignoreCase = true) == true
            }
            
            val matchesGroup = if (currentState.selectedGroup == null) {
                true
            } else {
                channel.group == currentState.selectedGroup
            }
            
            matchesSearch && matchesGroup
        }
        
        _uiState.update { it.copy(filteredChannels = filteredChannels) }
    }
    
    fun toggleFavorite(channelId: Long) {
        viewModelScope.launch {
            try {
                val channel = _uiState.value.channels.find { it.id == channelId }
                if (channel != null) {
                    channelRepository.updateFavoriteStatus(channelId, !channel.isFavorite)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
