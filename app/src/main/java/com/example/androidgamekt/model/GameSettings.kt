package com.example.androidgamekt.model

data class GameSettings(var difficulty: Int = 0) {
    companion object {
        var instance = GameSettings()
    }
}