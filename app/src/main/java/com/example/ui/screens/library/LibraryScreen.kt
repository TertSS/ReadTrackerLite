package com.example.ui.screens.library

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReadTrackerViewModel
import com.example.utils.Formatters
import com.example.utils.PluralRu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: ReadTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.libraryMode.collectAsStateWithLifecycle()
    val isGrid by viewModel.isGridView.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedFormat by viewModel.selectedFormatFilter.collectAsStateWithLifecycle()
    val books by viewModel.filteredBooks.collectAsStateWithLifecycle()
    val adaptations by viewModel.filteredAdaptations.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allAdaptations by viewModel.allAdaptations.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<BookTitle?>(null) }
    var adaptationToDelete by remember { mutableStateOf<Adaptation?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (settings.headerEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // App Bar Header with ReadTracker title and Mode Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "ReadTracker",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Mode Selector Pill (Книги / Экранизации) placed in top bar to the right of title
                            if (settings.adaptationsEnabled && settings.showLibraryModeSwitcher) {
                                ModeTogglePill(
                                    currentMode = currentMode,
                                    onModeChanged = { viewModel.setLibraryMode(it) },
                                    showAdaptations = true
                                )
                            }
                        }

                        if (settings.showViewModeSwitcher) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.isGridView.value = !isGrid },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                        .testTag("view_mode_toggle_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isGrid) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                        contentDescription = "Переключить вид",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // If header is disabled, we might still want the mode switcher and view mode switcher.
                // Let's place them in a smaller top bar without the app title.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (settings.adaptationsEnabled && settings.showLibraryModeSwitcher) {
                            ModeTogglePill(
                                currentMode = currentMode,
                                onModeChanged = { viewModel.setLibraryMode(it) },
                                showAdaptations = true
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        if (settings.showViewModeSwitcher) {
                            IconButton(
                                onClick = { viewModel.isGridView.value = !isGrid },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .testTag("view_mode_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isGrid) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                    contentDescription = "Переключить вид",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (currentMode == LibraryMode.BOOKS) {
                        viewModel.editingBook.value = null
                        viewModel.showAddBookDialog.value = true
                    } else {
                        viewModel.editingAdaptation.value = null
                        viewModel.showAddAdaptationDialog.value = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 0.dp)
                    .testTag("add_item_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and Filter Bar
            if (settings.searchFilterEnabled) {
                val showFilterButton = settings.librarySearchMode != "SEARCH_ONLY"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("search_input"),
                        placeholder = {
                            Text(
                                text = if (currentMode == LibraryMode.BOOKS) "Поиск книг, авторов, жанров..." else "Поиск экранизаций...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Поиск",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Очистить",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = CircleShape,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true
                    )

                    if (showFilterButton) {
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedFormat != "ALL") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceContainerLow
                                )
                                .testTag("filter_tune_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Фильтры",
                                tint = if (selectedFormat != "ALL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Horizontal Status Filter Tabs
            if (settings.showStatusFiltersInLibrary) {
                LibraryStatusFilterBar(
                    selectedStatus = selectedStatus,
                    currentMode = currentMode,
                    style = settings.libraryStatusBarStyle,
                    showCounts = settings.showStatusBarItemCounts,
                    allBooks = allBooks,
                    allAdaptations = allAdaptations,
                    onSelectStatus = { viewModel.setStatusFilter(it) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content: Books or Adaptations
            if (currentMode == LibraryMode.BOOKS) {
                if (books.isEmpty()) {
                    EmptyLibraryPlaceholder(
                        text = if (searchQuery.isNotEmpty() || selectedStatus != null) "Ничего не найдено" else "Библиотека книг пуста",
                        subtext = "Нажмите + чтобы добавить произведение"
                    )
                } else {
                    if (isGrid) {
                        val gridCells = if (settings.tabletLayoutEnabled) GridCells.Adaptive(minSize = 160.dp) else GridCells.Fixed(2)
                        LazyVerticalGrid(
                            columns = gridCells,
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(books, key = { it.id }) { book ->
                                BookGridCard(
                                    book = book,
                                    ratingScale = settings.ratingScale,
                                    ratingEnabled = settings.ratingEnabled,
                                    showCovers = settings.showCoversInLibrary,
                                    coverlessStyle = settings.coverlessCardStyle,
                                    compactTagPosition = settings.compactTagPosition,
                                    bookmarksEnabled = settings.bookmarksEnabled,
                                    shortenNumbers = settings.shortenNumbers,
                                    alignFormatWithTitle = settings.alignFormatWithTitle,
                                    cardLayoutConfig = settings.getActiveCardLayoutConfig(),
                                    onClick = { viewModel.openBookDetails(book.id) },
                                    onLongClick = { bookToDelete = book }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(books, key = { it.id }) { book ->
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 800.dp)) {
                                        BookListCard(
                                            book = book,
                                            ratingScale = settings.ratingScale,
                                            ratingEnabled = settings.ratingEnabled,
                                            showCovers = settings.showCoversInLibrary,
                                            coverlessStyle = settings.coverlessCardStyle,
                                            compactTagPosition = settings.compactTagPosition,
                                            bookmarksEnabled = settings.bookmarksEnabled,
                                            shortenNumbers = settings.shortenNumbers,
                                            alignFormatWithTitle = settings.alignFormatWithTitle,
                                            cardLayoutConfig = settings.getActiveCardLayoutConfig(),
                                            onClick = { viewModel.openBookDetails(book.id) },
                                            onLongClick = { bookToDelete = book }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Adaptations
                if (adaptations.isEmpty()) {
                    EmptyLibraryPlaceholder(
                        text = if (searchQuery.isNotEmpty() || selectedStatus != null) "Ничего не найдено" else "Список экранизаций пуст",
                        subtext = "Нажмите + чтобы добавить сериал или фильм"
                    )
                } else {
                    if (isGrid) {
                        val gridCells = if (settings.tabletLayoutEnabled) GridCells.Adaptive(minSize = 160.dp) else GridCells.Fixed(2)
                        LazyVerticalGrid(
                            columns = gridCells,
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(adaptations, key = { it.id }) { adaptation ->
                                AdaptationGridCard(
                                    adaptation = adaptation,
                                    ratingScale = settings.ratingScale,
                                    ratingEnabled = settings.ratingEnabled,
                                    showCovers = settings.showCoversInLibrary,
                                    coverlessStyle = settings.coverlessCardStyle,
                                    compactTagPosition = settings.compactTagPosition,
                                    bookmarksEnabled = settings.bookmarksEnabled,
                                    alignFormatWithTitle = settings.alignFormatWithTitle,
                                    cardLayoutConfig = settings.getActiveCardLayoutConfig(),
                                    onClick = { viewModel.openAdaptationDetails(adaptation.id) },
                                    onLongClick = { adaptationToDelete = adaptation }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(adaptations, key = { it.id }) { adaptation ->
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 800.dp)) {
                                        AdaptationListCard(
                                            adaptation = adaptation,
                                            ratingScale = settings.ratingScale,
                                            ratingEnabled = settings.ratingEnabled,
                                            showCovers = settings.showCoversInLibrary,
                                            coverlessStyle = settings.coverlessCardStyle,
                                            compactTagPosition = settings.compactTagPosition,
                                            bookmarksEnabled = settings.bookmarksEnabled,
                                            alignFormatWithTitle = settings.alignFormatWithTitle,
                                            cardLayoutConfig = settings.getActiveCardLayoutConfig(),
                                            onClick = { viewModel.openAdaptationDetails(adaptation.id) },
                                            onLongClick = { adaptationToDelete = adaptation }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Book Alert Dialog
    if (bookToDelete != null) {
        val book = bookToDelete!!
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            icon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Удалить произведение?") },
            text = {
                Text("Вы действительно хотите удалить «${book.title}»? Все связанные отзывы и записи будут удалены.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBook(book)
                        bookToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete Adaptation Alert Dialog
    if (adaptationToDelete != null) {
        val adaptation = adaptationToDelete!!
        AlertDialog(
            onDismissRequest = { adaptationToDelete = null },
            icon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Удалить экранизацию?") },
            text = {
                Text("Вы действительно хотите удалить «${adaptation.title}»? Все связанные отзывы будут удалены.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAdaptation(adaptation)
                        adaptationToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { adaptationToDelete = null }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Format Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Фильтр по формату",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                val filterOptions = if (currentMode == LibraryMode.BOOKS) {
                    listOf(
                        "ALL" to "Все форматы",
                        "SERIES" to "Серии томов / Ранобэ (LN)",
                        "WEB_NOVEL" to "Веб-новеллы (WN)",
                        "SINGLE" to "Синглы (одиночные книги)",
                        "HYBRID" to "Гибриды (LN + WN)",
                        "VISUAL_NOVEL" to "Визуальные новеллы (VN)"
                    )
                } else {
                    listOf(
                        "ALL" to "Все экранизации",
                        "SERIES" to "Сериалы",
                        "MOVIE" to "Фильмы",
                        "COMPLETED_SEASONS" to "Есть просмотренные сезоны",
                        "HAS_WATCH_TIME" to "Есть учтенное время",
                        "MULTI_SEASONS" to "Несколько сезонов"
                    )
                }

                filterOptions.forEach { (key, label) ->
                    val isSelected = selectedFormat == key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.selectedFormatFilter.value = key
                                showFilterSheet = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun LibraryStatusFilterBar(
    selectedStatus: TitleStatus?,
    currentMode: LibraryMode,
    style: String, // "PILLS" (Modern Chips), "SEGMENTED", "CARDS_COUNT", "MINIMAL_LINE"
    showCounts: Boolean = true,
    allBooks: List<BookTitle>,
    allAdaptations: List<Adaptation>,
    onSelectStatus: (TitleStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    val appStatusColors = LocalStatusColors.current
    val statusColors = listOf(
        null to ("Все" to MaterialTheme.colorScheme.primary),
        TitleStatus.READING to ((if (currentMode == LibraryMode.BOOKS) "Читаю" else "Смотрю") to appStatusColors.reading),
        TitleStatus.PLANNED to ("В планах" to appStatusColors.planned),
        TitleStatus.COMPLETED to ((if (currentMode == LibraryMode.BOOKS) "Завершено" else "Просмотрено") to appStatusColors.completed),
        TitleStatus.PAUSED to ("Пауза" to appStatusColors.paused),
        TitleStatus.DROPPED to ("Брошено" to appStatusColors.dropped)
    )

    when (style) {
        "SEGMENTED" -> {
            val statusIcons = listOf(
                null to ("Все" to Icons.Default.Layers),
                TitleStatus.READING to ((if (currentMode == LibraryMode.BOOKS) "Читаю" else "Смотрю") to Icons.Default.AutoStories),
                TitleStatus.PLANNED to ("В планах" to Icons.Default.BookmarkBorder),
                TitleStatus.COMPLETED to ((if (currentMode == LibraryMode.BOOKS) "Завершено" else "Просмотрено") to Icons.Default.CheckCircleOutline),
                TitleStatus.PAUSED to ("Пауза" to Icons.Default.PauseCircleOutline),
                TitleStatus.DROPPED to ("Брошено" to Icons.Default.HighlightOff)
            )
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    statusIcons.forEach { (status, pair) ->
                        val (label, icon) = pair
                        val isSelected = selectedStatus == status
                        val count = if (status == null) {
                            if (currentMode == LibraryMode.BOOKS) allBooks.size else allAdaptations.size
                        } else {
                            if (currentMode == LibraryMode.BOOKS) allBooks.count { it.status == status } else allAdaptations.count { it.status == status }
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectStatus(status) }
                                .testTag("status_tab_${status?.id ?: "all"}"),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            shadowElevation = if (isSelected) 1.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (showCounts) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        "CARDS_COUNT" -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusColors.forEach { (status, pair) ->
                    val (label, color) = pair
                    val isSelected = selectedStatus == status
                    val count = if (status == null) {
                        if (currentMode == LibraryMode.BOOKS) allBooks.size else allAdaptations.size
                    } else {
                        if (currentMode == LibraryMode.BOOKS) allBooks.count { it.status == status } else allAdaptations.count { it.status == status }
                    }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectStatus(status) }
                            .testTag("status_tab_${status?.id ?: "all"}"),
                        color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 0.8.dp,
                            if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (showCounts) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
        "MINIMAL_LINE" -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusColors.forEach { (status, pair) ->
                    val (label, color) = pair
                    val isSelected = selectedStatus == status
                    val count = if (status == null) {
                        if (currentMode == LibraryMode.BOOKS) allBooks.size else allAdaptations.size
                    } else {
                        if (currentMode == LibraryMode.BOOKS) allBooks.count { it.status == status } else allAdaptations.count { it.status == status }
                    }
                    Column(
                        modifier = Modifier
                            .clickable { onSelectStatus(status) }
                            .padding(vertical = 4.dp)
                            .testTag("status_tab_${status?.id ?: "all"}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) color else color.copy(alpha = 0.4f))
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (showCounts) {
                                Text(
                                    text = "($count)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .height(2.5.dp)
                                .width(if (isSelected) 36.dp else 0.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) color else Color.Transparent)
                        )
                    }
                }
            }
        }
        else -> {
            // "PILLS" -> Redesigned into Modern Elevated Bento Chips with Glow Dots & Counts
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusColors.forEach { (status, pair) ->
                    val (label, color) = pair
                    val isSelected = selectedStatus == status
                    val count = if (status == null) {
                        if (currentMode == LibraryMode.BOOKS) allBooks.size else allAdaptations.size
                    } else {
                        if (currentMode == LibraryMode.BOOKS) allBooks.count { it.status == status } else allAdaptations.count { it.status == status }
                    }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectStatus(status) }
                            .testTag("status_tab_${status?.id ?: "all"}"),
                        color = if (isSelected) color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) color.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Glowing Dot Indicator
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(12.dp)
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.25f))
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) color else color.copy(alpha = 0.6f))
                                    )
                            }

                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (showCounts) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookGridCard(
    book: BookTitle,
    ratingScale: RatingScale,
    ratingEnabled: Boolean,
    showCovers: Boolean = true,
    coverlessStyle: String = "CLASSIC",
    compactTagPosition: String = "UNDER_STATUS",
    bookmarksEnabled: Boolean = true,
    shortenNumbers: Boolean = false,
    alignFormatWithTitle: Boolean = false,
    cardLayoutConfig: CardLayoutConfig? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    if (cardLayoutConfig != null) {
        DynamicBookGridCard(
            book = book,
            config = cardLayoutConfig,
            ratingScale = ratingScale,
            ratingEnabled = ratingEnabled,
            bookmarksEnabled = bookmarksEnabled,
            shortenNumbers = shortenNumbers,
            alignFormatWithTitle = alignFormatWithTitle,
            onClick = onClick,
            onLongClick = onLongClick
        )
        return
    }

    val hasCover = showCovers && !book.coverUrl.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("book_card_${book.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!hasCover && coverlessStyle == "GRADIENT") {
                MaterialTheme.colorScheme.surfaceContainer
            } else if (!hasCover && coverlessStyle == "OUTLINE") {
                Color.Transparent
            } else if (!hasCover && coverlessStyle == "TONAL") {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = BorderStroke(
            1.dp,
            if (!hasCover && coverlessStyle == "GRADIENT") {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else if (!hasCover && coverlessStyle == "OUTLINE") {
                MaterialTheme.colorScheme.outline
            } else if (!hasCover && coverlessStyle == "TONAL") {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            }
        )
    ) {
        if (hasCover) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.72f)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        )
                ) {
                    CoverImage(
                        coverUrl = book.coverUrl,
                        title = book.title,
                        modifier = Modifier.fillMaxSize(),
                        height = 200.dp,
                        width = 160.dp,
                        corner = 0.dp
                    )

                    Box(modifier = Modifier.padding(8.dp).align(Alignment.TopStart)) {
                        FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                    }

                    Box(modifier = Modifier.padding(8.dp).align(Alignment.TopEnd)) {
                        StatusBadge(status = book.status)
                    }

                    LinearProgressIndicator(
                        progress = { book.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }

                // Info Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (book.author.isNotEmpty()) {
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (bookmarksEnabled && book.bookmark.isNotBlank()) {
                            BookmarkChip(bookmark = book.bookmark, modifier = Modifier.weight(1f, fill = false))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (ratingEnabled && book.rating > 0f) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = StarGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = Formatters.formatRating(book.rating, ratingScale),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Text(
                            text = book.progressDisplay,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            // Coverless variations based on coverlessStyle
            when (coverlessStyle) {
                "MINIMAL" -> {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        // Left color accent strip
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                                StatusBadge(status = book.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (book.author.isNotEmpty()) {
                                Text(
                                    text = book.author,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (bookmarksEnabled && book.bookmark.isNotBlank()) {
                                    BookmarkChip(bookmark = book.bookmark, modifier = Modifier.weight(1f, fill = false))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (ratingEnabled && book.rating > 0f) {
                                    Text(
                                        text = "★ ${Formatters.formatRating(book.rating, ratingScale)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StarGold
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Text(
                                    text = book.progressDisplay,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { book.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }
                "GRADIENT" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = book.title.take(2).uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            StatusBadge(status = book.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (book.author.isNotEmpty()) {
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (bookmarksEnabled && book.bookmark.isNotBlank()) {
                                    BookmarkChip(bookmark = book.bookmark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (ratingEnabled && book.rating > 0f) {
                                Text(
                                    text = "★ ${Formatters.formatRating(book.rating, ratingScale)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Text(
                                text = book.progressDisplay,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { book.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
                "COMPACT" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (compactTagPosition == "LEFT_OF_STATUS") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                                Spacer(modifier = Modifier.width(4.dp))
                                StatusBadge(status = book.status)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(status = book.status)
                            }
                        }

                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (bookmarksEnabled && book.bookmark.isNotBlank()) {
                                    BookmarkChip(bookmark = book.bookmark)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (compactTagPosition == "UNDER_STATUS") {
                                    FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                                }
                                if (ratingEnabled && book.rating > 0f) {
                                    Text(
                                        text = "★ ${Formatters.formatRating(book.rating, ratingScale)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StarGold
                                    )
                                }
                                Text(
                                    text = book.progressDisplay,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { book.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
                "OUTLINE" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                            StatusBadge(status = book.status)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = book.progressDisplay,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (ratingEnabled && book.rating > 0f) {
                                Text(
                                    text = "★ ${Formatters.formatRating(book.rating, ratingScale)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold
                                )
                            }
                        }
                    }
                }
                "TYPOGRAPHY" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = book.title.take(3).uppercase(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusBadge(status = book.status)
                    }
                }
                "TONAL" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = book.title.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            StatusBadge(status = book.status)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (book.author.isNotEmpty()) {
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                            Text(
                                text = book.progressDisplay,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                else -> {
                    // CLASSIC (Default monogram style)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                            StatusBadge(status = book.status)
                        }

                        LinearProgressIndicator(
                            progress = { book.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (book.author.isNotEmpty()) {
                                Text(
                                    text = book.author,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (bookmarksEnabled && book.bookmark.isNotBlank()) {
                                    BookmarkChip(bookmark = book.bookmark, modifier = Modifier.weight(1f, fill = false))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (ratingEnabled && book.rating > 0f) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = Formatters.formatRating(book.rating, ratingScale),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StarGold
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Text(
                                    text = book.progressDisplay,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookListCard(
    book: BookTitle,
    ratingScale: RatingScale,
    ratingEnabled: Boolean,
    showCovers: Boolean = true,
    coverlessStyle: String = "CLASSIC",
    compactTagPosition: String = "UNDER_STATUS",
    bookmarksEnabled: Boolean = true,
    shortenNumbers: Boolean = false,
    alignFormatWithTitle: Boolean = false,
    cardLayoutConfig: CardLayoutConfig? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    if (cardLayoutConfig != null) {
        DynamicBookListCard(
            book = book,
            config = cardLayoutConfig,
            ratingScale = ratingScale,
            ratingEnabled = ratingEnabled,
            bookmarksEnabled = bookmarksEnabled,
            shortenNumbers = shortenNumbers,
            alignFormatWithTitle = alignFormatWithTitle,
            onClick = onClick,
            onLongClick = onLongClick
        )
        return
    }

    val hasCover = showCovers && !book.coverUrl.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("book_card_${book.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!hasCover && coverlessStyle == "GRADIENT") {
                MaterialTheme.colorScheme.surfaceContainer
            } else if (!hasCover && coverlessStyle == "OUTLINE") {
                Color.Transparent
            } else if (!hasCover && coverlessStyle == "TONAL") {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = BorderStroke(
            1.dp,
            if (!hasCover && coverlessStyle == "GRADIENT") {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else if (!hasCover && coverlessStyle == "OUTLINE") {
                MaterialTheme.colorScheme.outline
            } else if (!hasCover && coverlessStyle == "TONAL") {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            }
        )
    ) {
        if (!hasCover && coverlessStyle == "MINIMAL") {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        StatusBadge(status = book.status)
                    }

                    if (book.author.isNotEmpty()) {
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (bookmarksEnabled && book.bookmark.isNotBlank()) {
                            BookmarkChip(bookmark = book.bookmark, modifier = Modifier.weight(1f, fill = false))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                            if (ratingEnabled && book.rating > 0f) {
                                Text(
                                    text = "★ ${Formatters.formatRating(book.rating, ratingScale)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold
                                )
                            }
                        }

                        Text(
                            text = book.progressDisplay,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else if (!hasCover && coverlessStyle == "COMPACT") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (compactTagPosition == "LEFT_OF_STATUS") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                            StatusBadge(status = book.status)
                        }
                    } else {
                        StatusBadge(status = book.status)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (bookmarksEnabled && book.bookmark.isNotBlank()) {
                            BookmarkChip(bookmark = book.bookmark)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (compactTagPosition == "UNDER_STATUS") {
                            FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                        }
                        if (ratingEnabled && book.rating > 0f) {
                            Text(
                                text = "★ ${Formatters.formatRating(book.rating, ratingScale)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StarGold
                            )
                        }
                        Text(
                            text = book.progressDisplay,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            // CLASSIC or GRADIENT list card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasCover) {
                    CoverImage(
                        coverUrl = book.coverUrl,
                        title = book.title,
                        width = 60.dp,
                        height = 86.dp,
                        corner = 8.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        StatusBadge(status = book.status)
                    }

                    if (book.author.isNotEmpty()) {
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (bookmarksEnabled && book.bookmark.isNotBlank()) {
                            BookmarkChip(bookmark = book.bookmark, modifier = Modifier.weight(1f, fill = false))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FormatBadge(format = book.format.shortLabel, alignFlush = alignFormatWithTitle)
                            if (ratingEnabled && book.rating > 0f) {
                                Text(
                                    text = "★ ${Formatters.formatRating(book.rating, ratingScale)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold
                                )
                            }
                        }

                        Text(
                            text = book.progressDisplay,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdaptationGridCard(
    adaptation: Adaptation,
    ratingScale: RatingScale,
    ratingEnabled: Boolean,
    showCovers: Boolean = true,
    coverlessStyle: String = "CLASSIC",
    compactTagPosition: String = "UNDER_STATUS",
    bookmarksEnabled: Boolean = true,
    alignFormatWithTitle: Boolean = false,
    cardLayoutConfig: CardLayoutConfig? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    if (cardLayoutConfig != null) {
        DynamicAdaptationCard(
            adaptation = adaptation,
            config = cardLayoutConfig,
            ratingScale = ratingScale,
            ratingEnabled = ratingEnabled,
            bookmarksEnabled = bookmarksEnabled,
            isGrid = true,
            onClick = onClick,
            onLongClick = onLongClick
        )
        return
    }

    val hasCover = showCovers && !adaptation.coverUrl.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("adaptation_card_${adaptation.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!hasCover && coverlessStyle == "GRADIENT") {
                MaterialTheme.colorScheme.surfaceContainer
            } else if (!hasCover && coverlessStyle == "OUTLINE") {
                Color.Transparent
            } else if (!hasCover && coverlessStyle == "TONAL") {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = BorderStroke(
            1.dp,
            if (!hasCover && coverlessStyle == "GRADIENT") {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
            } else if (!hasCover && coverlessStyle == "OUTLINE") {
                MaterialTheme.colorScheme.outline
            } else if (!hasCover && coverlessStyle == "TONAL") {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            }
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (hasCover) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.72f)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    CoverImage(
                        coverUrl = adaptation.coverUrl,
                        title = adaptation.title,
                        modifier = Modifier.fillMaxSize(),
                        height = 200.dp,
                        width = 160.dp,
                        corner = 0.dp
                    )

                    Box(modifier = Modifier.padding(8.dp).align(Alignment.TopStart)) {
                        FormatBadge(format = adaptation.type.shortLabel, alignFlush = alignFormatWithTitle)
                    }

                    Box(modifier = Modifier.padding(8.dp).align(Alignment.TopEnd)) {
                        StatusBadge(status = adaptation.status, isAdaptation = true)
                    }

                    LinearProgressIndicator(
                        progress = { adaptation.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormatBadge(format = adaptation.type.shortLabel, alignFlush = alignFormatWithTitle)
                    StatusBadge(status = adaptation.status, isAdaptation = true)
                }
                LinearProgressIndicator(
                    progress = { adaptation.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = adaptation.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (bookmarksEnabled && adaptation.bookmark.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    BookmarkChip(bookmark = adaptation.bookmark)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (ratingEnabled && adaptation.rating > 0f) {
                        Text(
                            text = "★ ${Formatters.formatRating(adaptation.rating, ratingScale)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StarGold
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Text(
                        text = adaptation.progressDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdaptationListCard(
    adaptation: Adaptation,
    ratingScale: RatingScale,
    ratingEnabled: Boolean,
    showCovers: Boolean = true,
    coverlessStyle: String = "CLASSIC",
    compactTagPosition: String = "UNDER_STATUS",
    bookmarksEnabled: Boolean = true,
    alignFormatWithTitle: Boolean = false,
    cardLayoutConfig: CardLayoutConfig? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    if (cardLayoutConfig != null) {
        DynamicAdaptationCard(
            adaptation = adaptation,
            config = cardLayoutConfig,
            ratingScale = ratingScale,
            ratingEnabled = ratingEnabled,
            bookmarksEnabled = bookmarksEnabled,
            isGrid = false,
            onClick = onClick,
            onLongClick = onLongClick
        )
        return
    }

    val hasCover = showCovers && !adaptation.coverUrl.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("adaptation_card_${adaptation.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!hasCover && coverlessStyle == "GRADIENT") {
                MaterialTheme.colorScheme.surfaceContainer
            } else if (!hasCover && coverlessStyle == "OUTLINE") {
                Color.Transparent
            } else if (!hasCover && coverlessStyle == "TONAL") {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = BorderStroke(
            1.dp,
            if (!hasCover && coverlessStyle == "GRADIENT") {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
            } else if (!hasCover && coverlessStyle == "OUTLINE") {
                MaterialTheme.colorScheme.outline
            } else if (!hasCover && coverlessStyle == "TONAL") {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasCover) {
                CoverImage(
                    coverUrl = adaptation.coverUrl,
                    title = adaptation.title,
                    width = 60.dp,
                    height = 86.dp,
                    corner = 8.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = adaptation.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    StatusBadge(status = adaptation.status, isAdaptation = true)
                }

                if (bookmarksEnabled && adaptation.bookmark.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    BookmarkChip(bookmark = adaptation.bookmark)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FormatBadge(format = adaptation.type.shortLabel, alignFlush = alignFormatWithTitle)
                        if (ratingEnabled && adaptation.rating > 0f) {
                            Text(
                                text = "★ ${Formatters.formatRating(adaptation.rating, ratingScale)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StarGold
                            )
                        }
                    }

                    Text(
                        text = adaptation.progressDisplay,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryPlaceholder(
    text: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
