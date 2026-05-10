package com.example.zametki.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zametki.domain.model.SyncOperation

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val entityType: String,

    val entityId: Long,

    val operation: SyncOperation,

    val createdAt: Long = System.currentTimeMillis()
)