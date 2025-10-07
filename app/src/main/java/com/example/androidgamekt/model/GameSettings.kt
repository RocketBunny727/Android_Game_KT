package com.example.androidgamekt.model

data class GameSettings(
    var difficulty: Int = 0,
    var overrideMaxBugs: Int? = null,
    var overrideBonusIntervalSec: Int? = null,
    var overrideRoundDurationSec: Int? = null,
    var version: Int = 0
) {
    companion object {
        var instance = GameSettings()
    }
}