package com.example.zametki.domain.model

enum class AttachmentType {
    PHOTO, VIDEO, AUDIO, FILE
}

enum class UploadStatus {
    PENDING, UPLOADING, DONE, ERROR
}

enum class SyncOperation {
    CREATE, UPDATE, DELETE
}