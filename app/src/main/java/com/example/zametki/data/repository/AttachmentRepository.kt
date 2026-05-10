package com.example.zametki.data.repository

import com.example.zametki.domain.model.UploadStatus
import com.example.zametki.data.local.dao.AttachmentDao
import com.example.zametki.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

class AttachmentRepository(private val attachmentDao: AttachmentDao) {

    fun getByEntry(entryId: Long): Flow<List<AttachmentEntity>> {
        return attachmentDao.getByEntry(entryId)
    }

    suspend fun getPending(): List<AttachmentEntity> {
        return attachmentDao.getPending()
    }

    suspend fun insert(attachment: AttachmentEntity) {
        attachmentDao.insert(attachment)
    }

    suspend fun markUploaded(id: Long, url: String) {
        attachmentDao.updateUploadStatus(id, UploadStatus.DONE, url)
    }

    suspend fun markUploading(id: Long) {
        attachmentDao.updateUploadStatus(id, UploadStatus.UPLOADING)
    }

    suspend fun markError(id: Long) {
        attachmentDao.updateUploadStatus(id, UploadStatus.ERROR)
    }

    suspend fun deleteByEntry(entryId: Long) {
        attachmentDao.deleteByEntry(entryId)
    }

    suspend fun deleteById(id: Long){
        attachmentDao.deleteById(id)
    }
}