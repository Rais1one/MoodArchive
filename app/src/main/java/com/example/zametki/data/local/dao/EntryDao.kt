package com.example.zametki.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.zametki.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM entries WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getAll(userId: String): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: Long): EntryEntity?

    @Query("SELECT * FROM entries WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<EntryEntity?>

    @Query("SELECT * FROM entries WHERE userId = :userId AND isDeleted = 0 AND createdAt BETWEEN :from AND :to ORDER BY createdAt DESC")
    suspend fun getByDay(userId: String, from: Long, to: Long): List<EntryEntity>

    @Query("SELECT * FROM entries WHERE userId = :userId AND isDeleted = 0 AND (text LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%')")
    fun search(userId: String, query: String): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE userId = :userId AND isDeleted = 0 AND emotionName = :emotionName")
    fun filterByEmotion(userId: String, emotionName: String): Flow<List<EntryEntity>>

    @Query("SELECT emotionName, emotionEmoji, emotionColor, COUNT(*) as count FROM entries WHERE userId = :userId AND isDeleted = 0 AND createdAt BETWEEN :from AND :to GROUP BY emotionName")
    suspend fun getEmotionStats(userId: String, from: Long, to: Long): List<EmotionStat>

    @Query("SELECT * FROM entries WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsynced(): List<EntryEntity>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("UPDATE entries SET isDeleted = 1, isSynced = 0, updatedAt = :now WHERE id = :id")
    suspend fun delete(id: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE entries SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("SELECT * FROM entries ORDER BY id DESC LIMIT 1")
    suspend fun getLastInserted(): EntryEntity?
}

data class EmotionStat(
    val emotionName: String?,
    val emotionEmoji: String?,
    val emotionColor: String?,
    val count: Int
)