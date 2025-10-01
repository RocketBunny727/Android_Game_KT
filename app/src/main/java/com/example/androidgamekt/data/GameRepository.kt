package com.example.androidgamekt.data

import android.content.Context
import androidx.room.Room

class GameRepository(context: Context) {
    private val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java, "game-database"
    ).build()
    private val playerDao = db.playerDao()
    private val scoreDao = db.scoreDao()

    suspend fun insertPlayer(player: PlayerEntity) {
        playerDao.insert(player)
    }

    suspend fun insertScore(score: ScoreEntity) {
        scoreDao.insert(score)
    }

    suspend fun getAllPlayers(): List<PlayerEntity> {
        return playerDao.getAllPlayers()
    }

    suspend fun getAllScores(): List<ScoreEntity> {
        return scoreDao.getAllScores()
    }
}