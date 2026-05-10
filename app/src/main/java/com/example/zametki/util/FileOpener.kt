package com.example.zametki.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.zametki.domain.model.AttachmentType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun copyFileToApp(context: Context, uri: Uri, type: AttachmentType): String {
    val extension = when (type) {
        AttachmentType.PHOTO -> ".jpg"
        AttachmentType.VIDEO -> ".mp4"
        AttachmentType.AUDIO -> ".mp3"
        AttachmentType.FILE -> ".file"
    }

    val fileName = "file_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}$extension"
    val destFile = File(context.filesDir, fileName)

    context.contentResolver.openInputStream(uri)?.use { input ->
        destFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return destFile.absolutePath
}

fun openFile(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("FileOpener", "Файл не найден: $filePath")
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val mimeType = when {
            filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") -> "image/jpeg"
            filePath.endsWith(".png") -> "image/png"
            filePath.endsWith(".mp4") -> "video/mp4"
            filePath.endsWith(".mp3") -> "audio/mpeg"
            filePath.endsWith(".pdf") -> "application/pdf"
            else -> "*/*"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("FileOpener", "Ошибка открытия: $filePath", e)
    }
}