package com.example.androidgamekt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    // Persisted game state
    private val _timeElapsedMs = MutableLiveData(0L)
    val timeElapsedMs: LiveData<Long> = _timeElapsedMs

    // Bonus/tilt and spawn timings
    var lastCoinTimeMs: Long = 0L
        private set
    var lastPoisonTimeMs: Long = 0L
        private set
    var lastSpawnTimeMs: Long = -1000L
        private set
    var lastFixedBonusTimeMs: Long = 0L
        private set
    var lastGoldSpawnTimeMs: Long = 0L
        private set
    private val _tiltEnabled = MutableLiveData(false)
    val tiltEnabled: LiveData<Boolean> = _tiltEnabled
    
    fun setTiltEnabled(enabled: Boolean) { _tiltEnabled.value = enabled }
    
    fun tick(deltaMs: Long) { _timeElapsedMs.value = (timeElapsedMs.value ?: 0L) + deltaMs }
    fun resetTime() { _timeElapsedMs.value = 0L }
    
    fun markCoin(timeMs: Long) { lastCoinTimeMs = timeMs }
    fun markPoison(timeMs: Long) { lastPoisonTimeMs = timeMs }
    fun markSpawn(timeMs: Long) { lastSpawnTimeMs = timeMs }
    fun markFixedBonus(timeMs: Long) { lastFixedBonusTimeMs = timeMs }
    fun markGold(timeMs: Long) { lastGoldSpawnTimeMs = timeMs }

    fun addScore(delta: Int) {
        val current = _score.value ?: 0
        _score.value = (current + delta).coerceAtLeast(0)
    }

    fun resetScore() {
        _score.value = 0
    }

    fun fullReset() {
        resetScore()
        resetTime()
        lastCoinTimeMs = 0L
        lastPoisonTimeMs = 0L
        lastSpawnTimeMs = -1000L
        lastFixedBonusTimeMs = 0L
        lastGoldSpawnTimeMs = 0L
        _tiltEnabled.value = false
    }
}


