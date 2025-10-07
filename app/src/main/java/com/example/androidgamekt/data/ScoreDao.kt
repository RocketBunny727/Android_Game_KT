package com.example.androidgamekt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: ScoreEntity)

    @Query("SELECT * FROM ScoreEntity")
    suspend fun getAllScores(): List<ScoreEntity>

    @Query("SELECT s.id as id, p.fullName as playerName, s.score as score, s.difficulty as difficulty, s.timestamp as timestamp FROM ScoreEntity s INNER JOIN PlayerEntity p ON s.playerId = p.id ORDER BY s.score DESC, s.timestamp DESC")
    suspend fun getScoresWithPlayerNames(): List<ScoreWithPlayer>
}