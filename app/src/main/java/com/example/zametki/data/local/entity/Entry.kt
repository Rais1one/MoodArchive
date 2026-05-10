package com.example.zametki.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String,
    val title: String = "",
    val text: String = "",

    val emotionName: String = "",
    val emotionEmoji: String = "",
    val emotionColor: String = "",

    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val isSynced: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)