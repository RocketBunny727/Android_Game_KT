package com.example.androidgamekt.data

data class ScoreWithPlayer(
    val id: Int,
    val playerName: String,
    val score: Int,
    val difficulty: Int,
    val timestamp: Long
)
