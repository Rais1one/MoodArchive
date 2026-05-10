package com.example.zametki.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zametki.data.local.entity.AttachmentEntity
import com.example.zametki.domain.model.UploadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE entryId = :entryId")
    fun getByEntry(entryId: Long): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE uploadStatus = 'PENDING'")
    suspend fun getPending(): List<AttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(attachment: AttachmentEntity)

    @Query("UPDATE attachments SET uploadStatus = :status, storageUrl = :url WHERE id = :id")
    suspend fun updateUploadStatus(id: Long, status: UploadStatus, url: String = "")

    @Query("DELETE FROM attachments WHERE entryId = :entryId")
    suspend fun deleteByEntry(entryId: Long)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteById(id: Long)
}