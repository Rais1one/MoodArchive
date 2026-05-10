package com.example.zametki.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zametki.data.local.AppDatabase
import com.example.zametki.data.remote.FirebaseRepository

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.Companion.getInstance(applicationContext)
            val entryDao = db.entryDao()
            val firebaseRepository = FirebaseRepository()

            val userId = inputData.getString("userId") ?: return Result.failure()

            val unsyncedEntries = entryDao.getUnsynced()
            unsyncedEntries.forEach { entry ->
                if (entry.isDeleted) {
                    firebaseRepository.deleteEntry(userId, entry.id)
                } else {
                    firebaseRepository.saveEntry(userId, entry)
                }
                entryDao.markSynced(entry.id)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}