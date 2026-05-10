package com.example.zametki.data.remote

import com.example.zametki.data.local.entity.AttachmentEntity
import com.example.zametki.data.local.entity.EntryEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun entriesCollection(userId: String) =
        db.collection("users").document(userId).collection("entries")

    private fun attachmentsCollection(userId: String) =
        db.collection("users").document(userId).collection("attachments")

    suspend fun saveEntry(userId: String, entry: EntryEntity) {
        try {
            val data = mapOf(
                "id" to entry.id,
                "userId" to entry.userId,
                "title" to entry.title,
                "text" to entry.text,
                "emotionName" to entry.emotionName,
                "emotionEmoji" to entry.emotionEmoji,
                "emotionColor" to entry.emotionColor,
                "isFavorite" to entry.isFavorite,
                "isDeleted" to entry.isDeleted,
                "createdAt" to entry.createdAt,
                "updatedAt" to entry.updatedAt
            )
            entriesCollection(userId)
                .document(entry.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteEntry(userId: String, entryId: Long) {
        try {
            entriesCollection(userId)
                .document(entryId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getEntries(userId: String): List<EntryEntity> {
        return try {
            val snapshot = entriesCollection(userId)
                .whereEqualTo("isDeleted", false)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    EntryEntity(
                        id = (doc.getLong("id") ?: 0L),
                        userId = doc.getString("userId") ?: "",
                        title = doc.getString("title") ?: "",
                        text = doc.getString("text") ?: "",
                        emotionName = doc.getString("emotionName") ?: "",
                        emotionEmoji = doc.getString("emotionEmoji") ?: "",
                        emotionColor = doc.getString("emotionColor") ?: "",
                        isFavorite = doc.getBoolean("isFavorite") ?: false,
                        isDeleted = doc.getBoolean("isDeleted") ?: false,
                        isSynced = true,
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        updatedAt = doc.getLong("updatedAt") ?: 0L
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveAttachment(userId: String, attachment: AttachmentEntity) {
        try {
            val data = mapOf(
                "id" to attachment.id,
                "entryId" to attachment.entryId,
                "type" to attachment.type.name,
                "localPath" to attachment.localPath,
                "storageUrl" to attachment.storageUrl,
                "fileName" to attachment.fileName,
                "uploadStatus" to attachment.uploadStatus.name,
                "createdAt" to attachment.createdAt
            )
            attachmentsCollection(userId)
                .document(attachment.id.toString())
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}