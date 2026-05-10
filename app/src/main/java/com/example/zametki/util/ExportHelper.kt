package com.example.zametki.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.zametki.data.local.entity.EntryEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun exportToMarkdown(context: Context, entries: List<EntryEntity>) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("ru"))
    val sb = StringBuilder()

    sb.appendLine("# MoodArchive — Экспорт записей")
    sb.appendLine("Дата экспорта: ${sdf.format(Date())}")
    sb.appendLine("Всего записей: ${entries.size}")
    sb.appendLine()
    sb.appendLine("---")
    sb.appendLine()

    entries.sortedByDescending { it.createdAt }.forEach { entry ->
        sb.appendLine("## ${sdf.format(Date(entry.createdAt))}")

        if (entry.emotionEmoji.isNotEmpty()) {
            sb.appendLine("**Эмоция:** ${entry.emotionEmoji} ${entry.emotionName}")
        }

        if (entry.title.isNotEmpty()) {
            sb.appendLine("### ${entry.title}")
        }

        if (entry.text.isNotEmpty()) {
            sb.appendLine(entry.text)
        }

        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
    }

    val fileName = "moodarchive_export_${System.currentTimeMillis()}.md"
    val file = File(context.filesDir, fileName)
    file.writeText(sb.toString())

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/markdown"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(Intent.createChooser(intent, "Экспорт записей"))
}