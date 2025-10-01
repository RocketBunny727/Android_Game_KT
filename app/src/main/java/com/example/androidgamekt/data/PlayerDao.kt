package com.example.androidgamekt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlayerDao {
    @Insert
    suspend fun insert(player: PlayerEntity)

    @Query("SELECT * FROM PlayerEntity")
    suspend fun getAllPlayers(): List<PlayerEntity>
}