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
}