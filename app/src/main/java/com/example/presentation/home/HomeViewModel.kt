package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MusicRepository
import com.example.domain.model.SongModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MusicRepository) : ViewModel() {
    val history = repository.history.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val mostPlayed = repository.mostPlayed.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val userProfile = repository.userProfile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun updateUserProfile(name: String, avatarUrl: String?) {
        viewModelScope.launch {
            repository.updateUserProfile(name, avatarUrl)
        }
    }

    private val _recommendations = MutableStateFlow<List<SongModel>>(emptyList())
    val recommendations: StateFlow<List<SongModel>> = _recommendations

    init {
        loadRecommendations()
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            val result = repository.getPersonalizedRecommendations()
            if (result.isSuccess && !result.getOrNull().isNullOrEmpty()) {
                _recommendations.value = result.getOrNull() ?: emptyList()
            } else {
                // Fallback to trending hits
                val fallbackResult = repository.search("trending")
                if (fallbackResult.isSuccess && !fallbackResult.getOrNull().isNullOrEmpty()) {
                    _recommendations.value = fallbackResult.getOrNull() ?: emptyList()
                }
            }
        }
    }
}
