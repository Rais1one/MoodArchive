package com.example.zametki.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zametki.preferences.PinManager
import com.example.zametki.preferences.ReminderPreferences
import com.example.zametki.presentation.viewmodel.ViewModel
import com.example.zametki.sync.SyncManager
import com.example.zametki.util.ReminderManager
import com.example.zametki.util.exportToMarkdown
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ViewModel,
    isDarkTheme: Boolean,
    syncManager: SyncManager,
    pinManager: PinManager,
    reminderPreferences: ReminderPreferences,
    reminderManager: ReminderManager,
    onLogout: () -> Unit,
    onThemeChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entries by viewModel.entries.collectAsState()

    val savedPin by pinManager.pin.collectAsState(initial = "")
    val isReminderEnabled by reminderPreferences.isEnabled.collectAsState(initial = false)
    val reminderHour by reminderPreferences.hour.collectAsState(initial = 20)
    val reminderMinute by reminderPreferences.minute.collectAsState(initial = 0)

    var isPinEnabled by remember { mutableStateOf(false) }
    var isSyncEnabled by remember { mutableStateOf(true) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }
    var hourInput by remember(reminderHour) { mutableStateOf(reminderHour.toString()) }
    var minuteInput by remember(reminderMinute) { mutableStateOf(reminderMinute.toString()) }

    androidx.compose.runtime.LaunchedEffect(savedPin) {
        isPinEnabled = savedPin.isNotEmpty()
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
                pinError = ""
                if (savedPin.isEmpty()) isPinEnabled = false
            },
            title = { Text("Установить PIN") },
            text = {
                Column {
                    Text(
                        "Введи 4-значный PIN код",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                pinInput = it
                                pinError = ""
                            }
                        },
                        label = { Text("PIN код") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinError.isNotEmpty(),
                        supportingText = {
                            if (pinError.isNotEmpty()) {
                                Text(pinError, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinInput.length == 4) {
                            scope.launch { pinManager.setPin(pinInput) }
                            isPinEnabled = true
                            showPinDialog = false
                            pinInput = ""
                        } else {
                            pinError = "PIN должен быть 4 цифры"
                        }
                    }
                ) {
                    Text("Сохранить", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    pinInput = ""
                    pinError = ""
                    if (savedPin.isEmpty()) isPinEnabled = false
                }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Время напоминания") },
            text = {
                Column {
                    Text(
                        "Текущее: ${reminderHour.toString().padStart(2, '0')}:${reminderMinute.toString().padStart(2, '0')}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Час (0-23):", fontSize = 13.sp)
                    OutlinedTextField(
                        value = hourInput,
                        onValueChange = {
                            if (it.length <= 2 && it.all { c -> c.isDigit() }) hourInput = it
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Минуты (0-59):", fontSize = 13.sp)
                    OutlinedTextField(
                        value = minuteInput,
                        onValueChange = {
                            if (it.length <= 2 && it.all { c -> c.isDigit() }) minuteInput = it
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = hourInput.toIntOrNull() ?: 20
                    val m = minuteInput.toIntOrNull() ?: 0
                    if (h in 0..23 && m in 0..59) {
                        scope.launch {
                            reminderPreferences.setReminder(true, h, m)
                        }
                        reminderManager.setReminder(h, m)
                        showTimePicker = false
                    }
                }) {
                    Text("Сохранить", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Экспорт записей") },
            text = { Text("Записи будут сохранены в формате Markdown") },
            confirmButton = {
                TextButton(
                    onClick = {
                        exportToMarkdown(context, entries)
                        showExportDialog = false
                    }
                ) {
                    Text("Экспорт", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Очистить все записи?") },
            text = { Text("Это действие нельзя отменить. Все записи будут удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        entries.forEach { entry -> viewModel.deleteEntry(entry.id) }
                        showClearDialog = false
                    }
                ) {
                    Text("Удалить всё", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "назад",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Spacer(modifier = Modifier.height(4.dp))

            SettingsSection(title = "Внешний вид") {
                SettingsSwitchRow(
                    emoji = "🌙",
                    title = "Тёмная тема",
                    subtitle = if (isDarkTheme) "Включена" else "Выключена",
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeChange(it) }
                )
            }

            SettingsSection(title = "Аккаунт") {
                SettingsClickRow(
                    emoji = "🚪",
                    title = "Выйти из аккаунта",
                    subtitle = "Выход из текущего аккаунта",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = onLogout
                )
            }

            SettingsSection(title = "Синхронизация") {
                AppleDivider()
                SettingsClickRow(
                    emoji = "🔄",
                    title = "Синхронизировать сейчас",
                    subtitle = "Отправить все записи в облако",
                    onClick = { syncManager.syncNow("user123") }
                )
            }

            SettingsSection(title = "Напоминания") {
                SettingsSwitchRow(
                    emoji = "🔔",
                    title = "Ежедневное напоминание",
                    subtitle = if (isReminderEnabled)
                        "Включено в ${reminderHour.toString().padStart(2, '0')}:${reminderMinute.toString().padStart(2, '0')}"
                    else
                        "Выключено",
                    checked = isReminderEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            reminderPreferences.setReminder(enabled, reminderHour, reminderMinute)
                        }
                        if (enabled) {
                            reminderManager.setReminder(reminderHour, reminderMinute)
                        } else {
                            reminderManager.cancelReminder()
                        }
                    }
                )
                if (isReminderEnabled) {
                    AppleDivider()
                    SettingsClickRow(
                        emoji = "🕐",
                        title = "Изменить время",
                        subtitle = "${reminderHour.toString().padStart(2, '0')}:${reminderMinute.toString().padStart(2, '0')}",
                        onClick = { showTimePicker = true }
                    )
                }
            }



            SettingsSection(title = "Данные") {
                SettingsClickRow(
                    emoji = "📤",
                    title = "Экспорт записей",
                    subtitle = "Сохранить в Markdown",
                    onClick = { showExportDialog = true }
                )
                AppleDivider()
                SettingsClickRow(
                    emoji = "🗑️",
                    title = "Очистить все записи",
                    subtitle = "Удалить все данные",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showClearDialog = true }
                )
            }

            SettingsSection(title = "О приложении") {
                SettingsInfoRow(emoji = "📱", title = "Версия", value = "1.0.0")
                AppleDivider()
                SettingsInfoRow(emoji = "👨‍💻", title = "Разработчик", value = "MoodArchive")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AppleDivider() {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(52.dp))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    emoji: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsClickRow(
    emoji: String,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "›",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsInfoRow(
    emoji: String,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}