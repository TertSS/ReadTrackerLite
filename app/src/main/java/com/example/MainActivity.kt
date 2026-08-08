package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.example.data.local.AppDatabase
import com.example.data.models.BookTitle
import com.example.data.models.Adaptation
import com.example.data.models.LibraryMode
import com.example.data.repository.ReadTrackerRepository
import com.example.ui.components.ReadTrackerBottomNav
import com.example.ui.screens.details.AdaptationDetailScreen
import com.example.ui.screens.details.TitleDetailScreen
import com.example.ui.screens.edit.AddEditAdaptationScreen
import com.example.ui.screens.edit.AddEditTitleScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.reviews.ReviewsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.stats.StatsScreen
import com.example.ui.screens.tier.TierListScreen
import com.example.ui.theme.ReadTrackerTheme
import com.example.ui.viewmodel.ReadTrackerViewModel
import com.example.ui.viewmodel.ReadTrackerViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ReadTrackerViewModel
    private var crashMessage: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Catch all exceptions in the main thread
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            val stackTrace = android.util.Log.getStackTraceString(exception)
            runOnUiThread {
                crashMessage = stackTrace
            }
            // We don't call defaultHandler to prevent the app from killing itself immediately
            // But if it's outside main thread, we might need to handle it.
        }

        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = ReadTrackerRepository(database = database)

            val factory = ReadTrackerViewModelFactory(repository)
            viewModel = ViewModelProvider(this, factory)[ReadTrackerViewModel::class.java]
        } catch (e: Exception) {
            crashMessage = android.util.Log.getStackTraceString(e)
        }

        setContent {
            ReadTrackerTheme {
                if (crashMessage != null) {
                    // Show crash message
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.errorContainer) {
                        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            item {
                                Text("APP CRASHED", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(crashMessage ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                } else {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: ReadTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedBookId by viewModel.selectedBookId.collectAsStateWithLifecycle()
    val selectedAdaptationId by viewModel.selectedAdaptationId.collectAsStateWithLifecycle()
    val showAddBook by viewModel.showAddBookDialog.collectAsStateWithLifecycle()
    val showAddAdaptation by viewModel.showAddAdaptationDialog.collectAsStateWithLifecycle()
    val editingBook by viewModel.editingBook.collectAsStateWithLifecycle()
    val editingAdaptation by viewModel.editingAdaptation.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allAdaptations by viewModel.allAdaptations.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }

    // Preserve the last active item during exit animations
    var activeBookId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(selectedBookId) {
        if (selectedBookId != null) activeBookId = selectedBookId
    }

    var activeAdaptationId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(selectedAdaptationId) {
        if (selectedAdaptationId != null) activeAdaptationId = selectedAdaptationId
    }

    var activeEditingBook by remember { mutableStateOf<BookTitle?>(null) }
    LaunchedEffect(editingBook) {
        if (editingBook != null) activeEditingBook = editingBook
    }

    var activeEditingAdaptation by remember { mutableStateOf<Adaptation?>(null) }
    LaunchedEffect(editingAdaptation) {
        if (editingAdaptation != null) activeEditingAdaptation = editingAdaptation
    }

    // Handle System Back Button
    BackHandler {
        when {
            editingBook != null -> {
                viewModel.editingBook.value = null
            }
            editingAdaptation != null -> {
                viewModel.editingAdaptation.value = null
            }
            showAddBook -> {
                viewModel.showAddBookDialog.value = false
            }
            showAddAdaptation -> {
                viewModel.showAddAdaptationDialog.value = false
            }
            selectedBookId != null -> {
                viewModel.closeDetails()
            }
            selectedAdaptationId != null -> {
                viewModel.closeDetails()
            }
            currentTab != "library" -> {
                viewModel.setTab("library")
            }
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime < 2000) {
                    (context as? ComponentActivity)?.finish()
                } else {
                    lastBackPressTime = now
                    Toast.makeText(context, "Нажмите ещё раз для выхода", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main Screen View
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                val isOverlayOpen = selectedBookId != null ||
                    selectedAdaptationId != null ||
                    showAddBook ||
                    showAddAdaptation ||
                    editingBook != null ||
                    editingAdaptation != null

                if (!isOverlayOpen) {
                    ReadTrackerBottomNav(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                when (currentTab) {
                    "library" -> LibraryScreen(viewModel = viewModel)
                    "reviews" -> ReviewsScreen(viewModel = viewModel)
                    "stats" -> StatsScreen(viewModel = viewModel)
                    "tier_list" -> TierListScreen(viewModel = viewModel)
                    "settings" -> SettingsScreen(viewModel = viewModel)
                }
            }
        }

        // Overlays & Detail Screens with animation
        AnimatedVisibility(
            visible = selectedBookId != null,
            enter = if (settings.disableAnimations) EnterTransition.None else (slideInHorizontally { it } + fadeIn()),
            exit = if (settings.disableAnimations) ExitTransition.None else (slideOutHorizontally { it } + fadeOut())
        ) {
            val bookIdToDisplay = selectedBookId ?: activeBookId
            bookIdToDisplay?.let { id ->
                val book = allBooks.find { it.id == id }
                TitleDetailScreen(
                    bookId = id,
                    viewModel = viewModel,
                    onBack = { viewModel.closeDetails() },
                    onEdit = {
                        viewModel.editingBook.value = book
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = selectedAdaptationId != null,
            enter = if (settings.disableAnimations) EnterTransition.None else (slideInHorizontally { it } + fadeIn()),
            exit = if (settings.disableAnimations) ExitTransition.None else (slideOutHorizontally { it } + fadeOut())
        ) {
            val adIdToDisplay = selectedAdaptationId ?: activeAdaptationId
            adIdToDisplay?.let { id ->
                val adaptation = allAdaptations.find { it.id == id }
                AdaptationDetailScreen(
                    adaptationId = id,
                    viewModel = viewModel,
                    onBack = { viewModel.closeDetails() },
                    onEdit = {
                        viewModel.editingAdaptation.value = adaptation
                    }
                )
            }
        }

        // Add / Edit Book Screen Modal
        AnimatedVisibility(
            visible = showAddBook || editingBook != null,
            enter = if (settings.disableAnimations) EnterTransition.None else (slideInVertically { it } + fadeIn()),
            exit = if (settings.disableAnimations) ExitTransition.None else (slideOutVertically { it } + fadeOut())
        ) {
            val bookToEdit = editingBook ?: (if (!showAddBook) activeEditingBook else null)
            AddEditTitleScreen(
                existingBook = bookToEdit,
                viewModel = viewModel,
                onDismiss = {
                    viewModel.showAddBookDialog.value = false
                    viewModel.editingBook.value = null
                }
            )
        }

        // Add / Edit Adaptation Screen Modal
        AnimatedVisibility(
            visible = showAddAdaptation || editingAdaptation != null,
            enter = if (settings.disableAnimations) EnterTransition.None else (slideInVertically { it } + fadeIn()),
            exit = if (settings.disableAnimations) ExitTransition.None else (slideOutVertically { it } + fadeOut())
        ) {
            val adaptationToEdit = editingAdaptation ?: (if (!showAddAdaptation) activeEditingAdaptation else null)
            AddEditAdaptationScreen(
                existingAdaptation = adaptationToEdit,
                viewModel = viewModel,
                onDismiss = {
                    viewModel.showAddAdaptationDialog.value = false
                    viewModel.editingAdaptation.value = null
                }
            )
        }
    }
}
