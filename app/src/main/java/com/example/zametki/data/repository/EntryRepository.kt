package com.example.zametki.data.repository

import com.example.zametki.data.local.dao.EmotionStat
import com.example.zametki.data.local.dao.EntryDao
import com.example.zametki.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

class EntryRepository(private val entryDao: EntryDao) {

    fun getAll(userId: String): Flow<List<EntryEntity>> {
        return entryDao.getAll(userId)
    }

    suspend fun getById(id: Long): EntryEntity? {
        return entryDao.getById(id)
    }

    suspend fun getByDay(userId: String, from: Long, to: Long): List<EntryEntity> {
        return entryDao.getByDay(userId, from, to)
    }

    fun search(userId: String, query: String): Flow<List<EntryEntity>> {
        return entryDao.search(userId, query)
    }

    fun filterByEmotion(userId: String, emotionName: String): Flow<List<EntryEntity>> {
        return entryDao.filterByEmotion(userId, emotionName)
    }

    suspend fun getEmotionStats(userId: String, from: Long, to: Long): List<EmotionStat> {
        return entryDao.getEmotionStats(userId, from, to)
    }

    suspend fun insert(entry: EntryEntity) {
        entryDao.insert(entry)
    }

    suspend fun update(entry: EntryEntity) {
        entryDao.update(entry.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(id: Long) {
        entryDao.delete(id)
    }

    suspend fun markSynced(id: Long) {
        entryDao.markSynced(id)
    }

    suspend fun getUnsynced(): List<EntryEntity> {
        return entryDao.getUnsynced()
    }

    suspend fun getLastInserted(): EntryEntity? {
        return entryDao.getLastInserted()
    }
}