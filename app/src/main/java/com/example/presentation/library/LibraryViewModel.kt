package com.example.presentation.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MusicRepository
import com.example.domain.model.SongModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: MusicRepository) : ViewModel() {
    val userProfile = repository.userProfile.stateIn(
        viewModelScope, 
        SharingStarted.WhileSubscribed(5000), 
        null
    )
    
    val favorites = repository.favorites.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    val history = repository.history.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    val playlists = repository.playlists.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun createProfile(name: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, null)
        }
    }

    fun createNewPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun syncLocalMusic(context: Context) {
        viewModelScope.launch {
            repository.syncLocalMusic(context)
        }
    }
}
