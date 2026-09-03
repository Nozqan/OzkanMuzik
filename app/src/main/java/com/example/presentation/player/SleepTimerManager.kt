package com.example.presentation.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SleepTimerManager(
    private val coroutineScope: CoroutineScope,
    private val onTimerExpired: () -> Unit
) {
    private var timerJob: Job? = null
    
    private val _timeRemainingMillis = MutableStateFlow<Long>(0L)
    val timeRemainingMillis: StateFlow<Long> = _timeRemainingMillis.asStateFlow()

    fun startTimer(minutes: Int) {
        cancelTimer()
        timerJob = coroutineScope.launch {
            var remaining = minutes * 60 * 1000L
            while (remaining > 0) {
                _timeRemainingMillis.value = remaining
                delay(1000)
                remaining -= 1000
            }
            _timeRemainingMillis.value = 0
            onTimerExpired()
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _timeRemainingMillis.value = 0
    }
}
