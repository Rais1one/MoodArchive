package com.example.zametki.data.local.entity

import androidx.room.PrimaryKey

class MediaAttachment(
    @PrimaryKey(autoGenerate = true)
    val mediaId: Int = 0,
    val entryId: Int
) {
}