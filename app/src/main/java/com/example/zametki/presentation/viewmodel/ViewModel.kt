package com.example.zametki.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zametki.presentation.screen.AttachedFile
import com.example.zametki.sync.SyncManager
import com.example.zametki.data.local.dao.EmotionStat
import com.example.zametki.data.local.entity.AttachmentEntity
import com.example.zametki.data.local.entity.EmotionEntity
import com.example.zametki.data.local.entity.EntryEntity
import com.example.zametki.data.repository.AttachmentRepository
import com.example.zametki.data.repository.EmotionRepository
import com.example.zametki.data.repository.EntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewModel(
    private val entryRepository: EntryRepository,
    private val attachmentRepository: AttachmentRepository,
    private val emotionRepository: EmotionRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _entries = MutableStateFlow<List<EntryEntity>>(emptyList())
    val entries: StateFlow<List<EntryEntity>> = _entries

    private val _searchResults = MutableStateFlow<List<EntryEntity>>(emptyList())
    val searchResults: StateFlow<List<EntryEntity>> = _searchResults

    private val _dayEntries = MutableStateFlow<List<EntryEntity>>(emptyList())
    val dayEntries: StateFlow<List<EntryEntity>> = _dayEntries

    private val _attachments = MutableStateFlow<List<AttachmentEntity>>(emptyList())
    val attachments: StateFlow<List<AttachmentEntity>> = _attachments

    private val _emotions = MutableStateFlow<List<EmotionEntity>>(emptyList())
    val emotions: StateFlow<List<EmotionEntity>> = _emotions

    private val _selectedEmotion = MutableStateFlow<EmotionEntity?>(null)
    val selectedEmotion: StateFlow<EmotionEntity?> = _selectedEmotion

    private val _emotionStats = MutableStateFlow<List<EmotionStat>>(emptyList())
    val emotionStats: StateFlow<List<EmotionStat>> = _emotionStats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _attachedFiles = MutableStateFlow<List<AttachedFile>>(emptyList())
    val attachedFiles: StateFlow<List<AttachedFile>> = _attachedFiles

    private var currentUserId: String = ""

    fun setUserId(userId: String) {
        currentUserId = userId
    }

    fun loadEntries(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            try {
                entryRepository.getAll(userId).collectLatest { list ->
                    _entries.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addEntry(entry: EntryEntity, files: List<AttachedFile>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                entryRepository.insert(entry)

                val savedEntry = entryRepository.getLastInserted()

                files.forEach { file ->
                    attachmentRepository.insert(
                        AttachmentEntity(
                            entryId = savedEntry?.id ?: 0,
                            type = file.type,
                            localPath = file.uri.path ?: file.uri.toString(),
                            fileName = file.uri.lastPathSegment ?: "файл"
                        )
                    )
                }

                if (currentUserId.isNotEmpty()) {
                    syncManager.syncNow(currentUserId)
                }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEntry(entry: EntryEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                entryRepository.update(entry)
                if (currentUserId.isNotEmpty()) {
                    syncManager.syncNow(currentUserId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            try {
                entryRepository.delete(id)
                if (currentUserId.isNotEmpty()) {
                    syncManager.syncNow(currentUserId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addAttachedFile(file: AttachedFile) {
        _attachedFiles.value = _attachedFiles.value + file
    }

    fun removeAttachedFile(index: Int) {
        _attachedFiles.value = _attachedFiles.value.toMutableList().also { it.removeAt(index) }
    }

    fun clearAttachedFiles() {
        _attachedFiles.value = emptyList()
    }

    fun clearAttachments() {
        _attachments.value = emptyList()
    }

    fun searchEntries(userId: String, query: String) {
        viewModelScope.launch {
            try {
                entryRepository.search(userId, query).collectLatest { list ->
                    _searchResults.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun filterByEmotion(userId: String, emotionName: String) {
        viewModelScope.launch {
            try {
                entryRepository.filterByEmotion(userId, emotionName).collectLatest { list ->
                    _entries.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadEntriesByDay(userId: String, from: Long, to: Long) {
        viewModelScope.launch {
            try {
                _dayEntries.value = entryRepository.getByDay(userId, from, to)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadAttachments(entryId: Long) {
        viewModelScope.launch {
            try {
                attachmentRepository.getByEntry(entryId).collectLatest { list ->
                    _attachments.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addAttachment(attachment: AttachmentEntity) {
        viewModelScope.launch {
            try {
                attachmentRepository.insert(attachment)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteAttachments(entryId: Long) {
        viewModelScope.launch {
            try {
                attachmentRepository.deleteByEntry(entryId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteAttachment(id: Long) {
        viewModelScope.launch {
            try {
                attachmentRepository.deleteById(id)
                _attachments.value = _attachments.value.filter { it.id != id }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadEmotions() {
        viewModelScope.launch {
            try {
                emotionRepository.getAll().collectLatest { list ->
                    _emotions.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectEmotion(emotion: EmotionEntity) {
        _selectedEmotion.value = emotion
    }

    fun clearEmotion() {
        _selectedEmotion.value = null
    }

    fun loadEmotionStats(userId: String, from: Long, to: Long) {
        viewModelScope.launch {
            try {
                _emotionStats.value = entryRepository.getEmotionStats(userId, from, to)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class MainViewModelFactory(
    private val entryRepository: EntryRepository,
    private val attachmentRepository: AttachmentRepository,
    private val emotionRepository: EmotionRepository,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewModel(
            entryRepository,
            attachmentRepository,
            emotionRepository,
            syncManager
        ) as T
    }
}