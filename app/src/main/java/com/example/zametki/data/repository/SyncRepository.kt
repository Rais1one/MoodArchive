package com.example.zametki.data.repository

import com.example.zametki.domain.model.SyncOperation
import com.example.zametki.data.local.dao.EntryDao
import com.example.zametki.data.local.dao.SyncQueueDao
import com.example.zametki.data.local.entity.EntryEntity
import com.example.zametki.data.local.entity.SyncQueueEntity

class SyncRepository(
    private val syncQueueDao: SyncQueueDao,
    private val entryDao: EntryDao
) {

    suspend fun addToQueue(entityType: String, entityId: Long, operation: SyncOperation) {
        syncQueueDao.insert(
            SyncQueueEntity(
                entityType = entityType,
                entityId = entityId,
                operation = operation
            )
        )
    }

    suspend fun getAll(): List<SyncQueueEntity> {
        return syncQueueDao.getAll()
    }

    suspend fun delete(id: Long) {
        syncQueueDao.delete(id)
    }

    suspend fun getUnsyncedEntries(): List<EntryEntity> {
        return entryDao.getUnsynced()
    }
}