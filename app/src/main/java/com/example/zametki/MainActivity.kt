package com.example.zametki

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.example.zametki.data.local.AppDatabase
import com.example.zametki.data.local.entity.EntryEntity
import com.example.zametki.data.repository.AttachmentRepository
import com.example.zametki.data.repository.EmotionRepository
import com.example.zametki.data.repository.EntryRepository
import com.example.zametki.domain.model.AttachmentType
import com.example.zametki.preferences.ThemePreferences
import com.example.zametki.presentation.screen.AddEntryScreen
import com.example.zametki.presentation.screen.AttachedFile
import com.example.zametki.presentation.screen.AuthScreen
import com.example.zametki.presentation.screen.CalendarScreen
import com.example.zametki.presentation.screen.CameraScreen
import com.example.zametki.presentation.screen.EditEntryScreen
import com.example.zametki.presentation.screen.MainScreen
import com.example.zametki.presentation.screen.PinScreen
import com.example.zametki.presentation.screen.SettingsScreen
import com.example.zametki.presentation.screen.SplashScreen
import com.example.zametki.presentation.screen.StatisticsScreen
import com.example.zametki.presentation.screen.ViewEntryScreen
import com.example.zametki.presentation.viewmodel.MainViewModelFactory
import com.example.zametki.presentation.viewmodel.ViewModel
import com.example.zametki.sync.SyncManager
import com.example.zametki.ui.theme.AppDarkColors
import com.example.zametki.ui.theme.AppLightColors
import com.example.zametki.ui.theme.AppShapes
import com.example.zametki.preferences.PinManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ViewModel
    private lateinit var themePreferences: ThemePreferences
    private lateinit var syncManager: SyncManager
    private lateinit var pinManager: PinManager

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        val db = AppDatabase.getInstance(this)
        syncManager = SyncManager(this)
        pinManager = PinManager(this)

        val factory = MainViewModelFactory(
            entryRepository = EntryRepository(db.entryDao()),
            attachmentRepository = AttachmentRepository(db.attachmentDao()),
            emotionRepository = EmotionRepository(db.emotionDao()),
            syncManager = syncManager
        )
        viewModel = ViewModelProvider(this, factory)[ViewModel::class.java]
        themePreferences = ThemePreferences(this)

        setContent {
            val scope = rememberCoroutineScope()
            val isDarkTheme by themePreferences.isDarkTheme.collectAsState(initial = false)
            val savedPin by pinManager.pin.collectAsState(initial = "")

            MaterialTheme(
                colorScheme = if (isDarkTheme) AppDarkColors else AppLightColors,
                shapes = AppShapes
            ) {
                var currentScreen by remember { mutableStateOf("splash") }
                var selectedEntry by remember { mutableStateOf<EntryEntity?>(null) }
                var userId by remember { mutableStateOf("") }

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && userId.isEmpty()) {
                    userId = currentUser.uid
                    viewModel.setUserId(currentUser.uid)
                }

                when (currentScreen) {

                    "splash" -> SplashScreen(
                        onFinished = {
                            currentScreen = when {
                                userId.isEmpty() -> "auth"
                                savedPin.isNotEmpty() -> "pin"
                                else -> "main"
                            }
                        }
                    )

                    "auth" -> AuthScreen(
                        onAuthSuccess = { uid ->
                            userId = uid
                            viewModel.setUserId(uid)
                            syncManager.startPeriodicSync(uid)
                            currentScreen = "main"
                        }
                    )

                    "pin" -> PinScreen(
                        correctPin = savedPin,
                        onSuccess = { currentScreen = "main" },
                        onForgot = {
                            FirebaseAuth.getInstance().signOut()
                            userId = ""
                            viewModel.setUserId("")
                            currentScreen = "auth"
                        }
                    )

                    "main" -> MainScreen(
                        viewModel = viewModel,
                        userId = userId,
                        onEntryClick = { entry ->
                            selectedEntry = entry
                            currentScreen = "view"
                        },
                        onAddClick = { currentScreen = "add" },
                        onCalendarClick = { currentScreen = "calendar" },
                        onStatisticsClick = { currentScreen = "statistics" },
                        onSettingsClick = { currentScreen = "settings" }
                    )

                    "add" -> AddEntryScreen(
                        viewModel = viewModel,
                        userId = userId,
                        onBack = {
                            viewModel.clearAttachedFiles()
                            currentScreen = "main"
                        },
                        onOpenCamera = { currentScreen = "camera" }
                    )

                    "camera" -> CameraScreen(
                        onPhotoTaken = { uri ->
                            viewModel.addAttachedFile(
                                AttachedFile(uri = uri, type = AttachmentType.PHOTO)
                            )
                            currentScreen = "add"
                        },
                        onBack = { currentScreen = "add" }
                    )

                    "view" -> selectedEntry?.let { entry ->
                        ViewEntryScreen(
                            entry = entry,
                            viewModel = viewModel,
                            onBack = { currentScreen = "main" },
                            onEdit = {
                                selectedEntry = it
                                currentScreen = "edit"
                            }
                        )
                    }

                    "edit" -> selectedEntry?.let { entry ->
                        EditEntryScreen(
                            entry = entry,
                            viewModel = viewModel,
                            onBack = { currentScreen = "view" }
                        )
                    }

                    "calendar" -> CalendarScreen(
                        viewModel = viewModel,
                        userId = userId,
                        onBack = { currentScreen = "main" },
                        onEntryClick = { entry ->
                            selectedEntry = entry
                            currentScreen = "view"
                        }
                    )

                    "statistics" -> StatisticsScreen(
                        viewModel = viewModel,
                        userId = userId,
                        onBack = { currentScreen = "main" }
                    )

                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDark ->
                            scope.launch {
                                themePreferences.setDarkTheme(isDark)
                            }
                        },
                        syncManager = syncManager,
                        pinManager = pinManager,
                        onBack = { currentScreen = "main" },
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            userId = ""
                            viewModel.setUserId("")
                            currentScreen = "auth"
                        }
                    )
                }
            }
        }
    }
}