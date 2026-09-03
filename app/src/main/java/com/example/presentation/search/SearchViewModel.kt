package com.example.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: MusicRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state = _state.asStateFlow()

    init {
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _query.debounce(500)
                .distinctUntilChanged()
                .collectLatest { q ->
                    if (q.isBlank()) {
                        _state.value = SearchState.Idle
                    } else if (q.length >= 2) {
                        performSearch(q)
                    }
                }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    private suspend fun performSearch(q: String) {
        _state.value = SearchState.Loading
        val result = repository.search(q)
        result.fold(
            onSuccess = { songs ->
                if (songs.isEmpty()) _state.value = SearchState.Empty
                else _state.value = SearchState.Success(songs)
            },
            onFailure = { e ->
                _state.value = SearchState.Error(e.message ?: "Bilinmeyen bir hata oluştu")
            }
        )
    }
}
