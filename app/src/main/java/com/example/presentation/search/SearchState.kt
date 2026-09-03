package com.example.presentation.search

import com.example.domain.model.SongModel

sealed interface SearchState {
    object Idle : SearchState
    object Loading : SearchState
    data class Success(val results: List<SongModel>) : SearchState
    object Empty : SearchState
    data class Error(val message: String) : SearchState
}
