package com.example.zametki.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zametki.domain.model.AttachmentType
import com.example.zametki.util.AudioRecorder
import com.example.zametki.domain.model.EmotionItem
import com.example.zametki.util.copyFileToApp
import com.example.zametki.data.local.entity.EntryEntity
import com.example.zametki.presentation.viewmodel.ViewModel
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: ViewModel,
    userId: String,
    onBack: () -> Unit,
    onOpenCamera: () -> Unit
) {
    val context = LocalContext.current

    val title by viewModel.draftTitle.collectAsState()
    val text by viewModel.draftText.collectAsState()
    var showError by remember { mutableStateOf("") }

    val emotions by viewModel.emotions.collectAsState()
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()
    val attachedFiles by viewModel.attachedFiles.collectAsState()

    var isRecording by remember { mutableStateOf(false) }
    var audioAmplitude by remember { mutableStateOf(0) }
    val audioRecorder = remember { AudioRecorder(context) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = copyFileToApp(context, it, AttachmentType.PHOTO)
            viewModel.addAttachedFile(
                AttachedFile(uri = Uri.fromFile(File(localPath)), type = AttachmentType.PHOTO)
            )
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = copyFileToApp(context, it, AttachmentType.FILE)
            viewModel.addAttachedFile(
                AttachedFile(uri = Uri.fromFile(File(localPath)), type = AttachmentType.FILE)
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadEmotions()
        viewModel.clearEmotion()
        viewModel.clearDraft()
    }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            audioAmplitude = audioRecorder.getAmplitude()
            delay(100)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Новая запись",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearDraft()
                        viewModel.clearEmotion()
                        viewModel.clearAttachedFiles()
                        onBack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "назад",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    TextActionButton(
                        text = "Готово",
                        onClick = {
                            when {
                                title.isEmpty() -> showError = "Введи заголовок"
                                selectedEmotion == null -> showError = "Выбери настроение"
                                else -> {
                                    viewModel.addEntry(
                                        entry = EntryEntity(
                                            userId = userId,
                                            title = title,
                                            text = text,
                                            emotionName = selectedEmotion?.name ?: "",
                                            emotionEmoji = selectedEmotion?.emoji ?: "",
                                            emotionColor = selectedEmotion?.color ?: ""
                                        ),
                                        files = attachedFiles
                                    )
                                    viewModel.clearEmotion()
                                    viewModel.clearAttachedFiles()
                                    viewModel.clearDraft()
                                    onBack()
                                }
                            }
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TextField(
                    value = title,
                    onValueChange = {
                        if (it.length <= 50) {
                            viewModel.setDraftTitle(it)
                            showError = ""
                        }
                    },
                    placeholder = {
                        Text(
                            "Заголовок",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                )

                TextField(
                    value = text,
                    onValueChange = {
                        viewModel.setDraftText(it)
                    },
                    placeholder = {
                        Text(
                            "Что у тебя сегодня?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    maxLines = 10,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            if (showError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = showError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(
                title = if (selectedEmotion == null)
                    "Как ты себя чувствуешь? *"
                else
                    "Как ты себя чувствуешь?"
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emotions) { emotion ->
                    EmotionItem(
                        emotion = emotion,
                        isSelected = selectedEmotion?.id == emotion.id,
                        onClick = {
                            if (selectedEmotion?.id == emotion.id) {
                                viewModel.clearEmotion()
                            } else {
                                viewModel.selectEmotion(emotion)
                                showError = ""
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(title = "Прикрепить")

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AttachButton(label = "Камера", emoji = "📷", onClick = onOpenCamera)
                AttachButton(label = "Галерея", emoji = "🖼️", onClick = { imagePicker.launch("image/*") })
                AttachButton(label = "Файл", emoji = "📎", onClick = { filePicker.launch("*/*") })
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(title = "Голосовая заметка")

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isRecording) {
                            audioRecorder.stop()
                            isRecording = false
                            audioRecorder.currentFile?.let { file ->
                                viewModel.addAttachedFile(
                                    AttachedFile(
                                        uri = Uri.fromFile(file),
                                        type = AttachmentType.AUDIO
                                    )
                                )
                            }
                        } else {
                            audioRecorder.start()
                            isRecording = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = if (isRecording) "⏹ Стоп" else "🎙 Записать")
                }

                if (isRecording) {
                    AudioWaveform(amplitude = audioAmplitude)
                }
            }

            if (attachedFiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle(title = "Прикреплено: ${attachedFiles.size}")

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    attachedFiles.forEachIndexed { index, file ->
                        AttachedFileItem(
                            file = file,
                            onRemove = { viewModel.removeAttachedFile(index) }
                        )
                        if (index < attachedFiles.size - 1) {
                            AppleDivider()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}

@Composable
fun TextActionButton(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun AttachButton(
    label: String,
    emoji: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AudioWaveform(amplitude: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(8) { index ->
            val height = if (index % 2 == 0) {
                (amplitude * 0.8f).coerceIn(4f, 40f)
            } else {
                (amplitude * 1.2f).coerceIn(4f, 40f)
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.error)
            )
        }
    }
}

@Composable
fun AttachedFileItem(
    file: AttachedFile,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (file.type) {
                    AttachmentType.PHOTO -> "📷"
                    AttachmentType.VIDEO -> "🎥"
                    AttachmentType.AUDIO -> "🎙"
                    AttachmentType.FILE -> "📎"
                },
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = file.uri.lastPathSegment ?: "файл",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "✕",
            modifier = Modifier.clickable { onRemove() },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
    }
}

data class AttachedFile(
    val uri: Uri,
    val type: AttachmentType
)