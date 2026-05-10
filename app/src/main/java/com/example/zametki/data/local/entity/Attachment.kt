package com.example.zametki.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.zametki.domain.model.AttachmentType
import com.example.zametki.domain.model.UploadStatus

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entryId")]
)
data class AttachmentEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val entryId: Long,

    val type: AttachmentType,

    val localPath: String = "",
    val storageUrl: String = "",
    val fileName: String = "",

    val uploadStatus: UploadStatus = UploadStatus.PENDING,

    val createdAt: Long = System.currentTimeMillis()
)