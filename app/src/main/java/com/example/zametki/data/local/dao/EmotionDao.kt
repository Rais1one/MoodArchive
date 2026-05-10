package com.example.zametki.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zametki.data.local.entity.EmotionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmotionDao {

    @Query("SELECT * FROM emotions")
    fun getAll(): Flow<List<EmotionEntity>>

    @Query("SELECT * FROM emotions")
    suspend fun getAllOnce(): List<EmotionEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAll(emotions: List<EmotionEntity>)
}