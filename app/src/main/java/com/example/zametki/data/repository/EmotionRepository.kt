package com.example.zametki.data.repository

import com.example.zametki.data.local.dao.EmotionDao
import com.example.zametki.data.local.entity.EmotionEntity
import kotlinx.coroutines.flow.Flow

class EmotionRepository(private val emotionDao: EmotionDao) {

    fun getAll(): Flow<List<EmotionEntity>> {
        return emotionDao.getAll()
    }

    suspend fun getAllOnce(): List<EmotionEntity> {
        return emotionDao.getAllOnce()
    }
}