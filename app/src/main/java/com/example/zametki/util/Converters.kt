package com.example.zametki.util

import androidx.room.TypeConverter
import com.example.zametki.domain.model.AttachmentType
import com.example.zametki.domain.model.SyncOperation
import com.example.zametki.domain.model.UploadStatus

class Converters {

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType): String = value.name

    @TypeConverter
    fun toAttachmentType(value: String): AttachmentType =
        AttachmentType.valueOf(value)

    @TypeConverter
    fun fromUploadStatus(value: UploadStatus): String = value.name

    @TypeConverter
    fun toUploadStatus(value: String): UploadStatus =
        UploadStatus.valueOf(value)

    @TypeConverter
    fun fromSyncOperation(value: SyncOperation): String = value.name

    @TypeConverter
    fun toSyncOperation(value: String): SyncOperation =
        SyncOperation.valueOf(value)
}