package com.example.zametki.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.zametki.data.local.dao.AttachmentDao
import com.example.zametki.data.local.entity.AttachmentEntity
import com.example.zametki.util.Converters
import com.example.zametki.data.local.dao.EmotionDao
import com.example.zametki.data.local.entity.EmotionEntity
import com.example.zametki.data.local.dao.EntryDao
import com.example.zametki.data.local.entity.EntryEntity
import com.example.zametki.data.local.dao.SyncQueueDao
import com.example.zametki.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EntryEntity::class,
        AttachmentEntity::class,
        EmotionEntity::class,
        SyncQueueEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun emotionDao(): EmotionDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {

        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mood_archive.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                instance?.emotionDao()?.insertAll(defaultEmotions())
                            }
                        }
                    })
                    .build()
            }
            return instance!!
        }

        private fun defaultEmotions() = listOf(
            EmotionEntity(name = "Радость", emoji = "😊", color = "#FFD700"),
            EmotionEntity(name = "Грусть", emoji = "😢", color = "#4A90D9"),
            EmotionEntity(name = "Тревога", emoji = "😰", color = "#FF6B6B"),
            EmotionEntity(name = "Злость", emoji = "😠", color = "#FF4500"),
            EmotionEntity(name = "Спокойствие", emoji = "😌", color = "#90EE90"),
            EmotionEntity(name = "Усталость", emoji = "😴", color = "#A9A9A9"),
            EmotionEntity(name = "Нейтрально", emoji = "😐", color = "#CCCCCC")
        )
    }
}