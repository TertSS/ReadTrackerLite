package com.example.ui.redesign

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.models.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalStatusColors
import com.example.ui.viewmodel.ReadTrackerViewModel

enum class RedesignedViewMode {
    GRID_POSTER,
    EDITORIAL_CARD,
    COMPACT_LIST
}

/**
 * 🌟 REDESIGNED 2.0 LIBRARY SCREEN
 * Completely rebuilt from scratch with bespoke visual polish, smooth filters, instant search, and delightful UX.
 */
@Composable
fun RedesignedLibraryScreen(
    viewModel: ReadTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allAdaptations by viewModel.allAdaptations.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<TitleStatus?>(null) }
    var selectedViewMode by remember { mutableStateOf(RedesignedViewMode.EDITORIAL_CARD) }
    var selectedCategoryTab by remember { mutableStateOf(if (settings.libraryMode == LibraryMode.ADAPTATIONS) "ADAPTATIONS" else "BOOKS") }
    var sortAscending by remember { mutableStateOf(false) }
    var sortCriteria by remember { mutableStateOf("UPDATED") } // "UPDATED", "TITLE", "RATING", "PROGRESS"
    var showSortMenu by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Filtered books
    val filteredBooks = remember(allBooks, searchQuery, selectedStatusFilter, sortCriteria, sortAscending) {
        var list = allBooks.filter { book ->
            val matchesSearch = searchQuery.isBlank() ||
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.originalTitle.contains(searchQuery, ignoreCase = true) ||
                book.author.contains(searchQuery, ignoreCase = true) ||
                book.genres.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesStatus = selectedStatusFilter == null || book.status == selectedStatusFilter
            matchesSearch && matchesStatus
        }

        list = when (sortCriteria) {
            "TITLE" -> if (sortAscending) list.sortedBy { it.title.lowercase() } else list.sortedByDescending { it.title.lowercase() }
            "RATING" -> if (sortAscending) list.sortedBy { it.rating ?: 0.0 } else list.sortedByDescending { it.rating ?: 0.0 }
            "PROGRESS" -> if (sortAscending) list.sortedBy { it.calculateProgress() } else list.sortedByDescending { it.calculateProgress() }
            else -> if (sortAscending) list.sortedBy { it.updatedAt } else list.sortedByDescending { it.updatedAt }
        }
        list
    }

    // Filtered adaptations
    val filteredAdaptations = remember(allAdaptations, searchQuery, selectedStatusFilter, sortCriteria, sortAscending) {
        var list = allAdaptations.filter { ad ->
            val matchesSearch = searchQuery.isBlank() ||
                ad.title.contains(searchQuery, ignoreCase = true) ||
                ad.originalTitle.contains(searchQuery, ignoreCase = true) ||
                ad.directorOrStudio.contains(searchQuery, ignoreCase = true) ||
                ad.genres.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesStatus = selectedStatusFilter == null || ad.status == selectedStatusFilter
            matchesSearch && matchesStatus
        }

        list = when (sortCriteria) {
            "TITLE" -> if (sortAscending) list.sortedBy { it.title.lowercase() } else list.sortedByDescending { it.title.lowercase() }
            "RATING" -> if (sortAscending) list.sortedBy { it.rating ?: 0.0 } else list.sortedByDescending { it.rating ?: 0.0 }
            "PROGRESS" -> if (sortAscending) list.sortedBy { it.calculateProgress() } else list.sortedByDescending { it.calculateProgress() }
            else -> if (sortAscending) list.sortedBy { it.updatedAt } else list.sortedByDescending { it.updatedAt }
        }
        list
    }

    val isShowingBooks = selectedCategoryTab == "BOOKS"
    val isShowingAdaptations = selectedCategoryTab == "ADAPTATIONS"
    val isShowingAll = selectedCategoryTab == "ALL"

    val totalItemsCount = if (isShowingBooks) allBooks.size else if (isShowingAdaptations) allAdaptations.size else (allBooks.size + allAdaptations.size)
    val readingCount = if (isShowingBooks) allBooks.count { it.status == TitleStatus.READING } else if (isShowingAdaptations) allAdaptations.count { it.status == TitleStatus.READING } else (allBooks.count { it.status == TitleStatus.READING } + allAdaptations.count { it.status == TitleStatus.READING })
    val plannedCount = if (isShowingBooks) allBooks.count { it.status == TitleStatus.PLANNED } else if (isShowingAdaptations) allAdaptations.count { it.status == TitleStatus.PLANNED } else (allBooks.count { it.status == TitleStatus.PLANNED } + allAdaptations.count { it.status == TitleStatus.PLANNED })
    val completedCount = if (isShowingBooks) allBooks.count { it.status == TitleStatus.COMPLETED } else if (isShowingAdaptations) allAdaptations.count { it.status == TitleStatus.COMPLETED } else (allBooks.count { it.status == TitleStatus.COMPLETED } + allAdaptations.count { it.status == TitleStatus.COMPLETED })

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar & Search Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top App Title & View Mode Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Библиотека",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "$totalItemsCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Ваша личная коллекция тайтлов",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // View Mode Switcher Pills
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            ViewModeIconButton(
                                icon = Icons.Default.GridView,
                                isSelected = selectedViewMode == RedesignedViewMode.GRID_POSTER,
                                onClick = { selectedViewMode = RedesignedViewMode.GRID_POSTER }
                            )
                            ViewModeIconButton(
                                icon = Icons.Default.ViewAgenda,
                                isSelected = selectedViewMode == RedesignedViewMode.EDITORIAL_CARD,
                                onClick = { selectedViewMode = RedesignedViewMode.EDITORIAL_CARD }
                            )
                            ViewModeIconButton(
                                icon = Icons.Default.ViewHeadline,
                                isSelected = selectedViewMode == RedesignedViewMode.COMPACT_LIST,
                                onClick = { selectedViewMode = RedesignedViewMode.COMPACT_LIST }
                            )
                        }
                    }
                }

                // Interactive Search & Sort Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        placeholder = {
                            Text(
                                "Поиск по названию, автору, жанрам...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Поиск",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Очистить",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )

                    // Sort Button with dropdown
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .height(52.dp)
                                .clickable { showSortMenu = true }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Сортировка",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            DropdownMenuItem(
                                text = { Text("По дате обновления") },
                                onClick = { sortCriteria = "UPDATED"; showSortMenu = false },
                                leadingIcon = { if (sortCriteria == "UPDATED") Icon(Icons.Default.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("По названию (А-Я)") },
                                onClick = { sortCriteria = "TITLE"; showSortMenu = false },
                                leadingIcon = { if (sortCriteria == "TITLE") Icon(Icons.Default.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("По оценке") },
                                onClick = { sortCriteria = "RATING"; showSortMenu = false },
                                leadingIcon = { if (sortCriteria == "RATING") Icon(Icons.Default.Check, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("По прогрессу") },
                                onClick = { sortCriteria = "PROGRESS"; showSortMenu = false },
                                leadingIcon = { if (sortCriteria == "PROGRESS") Icon(Icons.Default.Check, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (sortAscending) "По возрастанию ▲" else "По убыванию ▼") },
                                onClick = { sortAscending = !sortAscending; showSortMenu = false }
                            )
                        }
                    }
                }

                // Category Switcher (Книги / Адаптации / Все)
                if (settings.adaptationsEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(2.dp)
                    ) {
                        CategoryTabItem(
                            label = "Книги и Ранобэ",
                            count = allBooks.size,
                            isSelected = isShowingBooks,
                            onClick = { selectedCategoryTab = "BOOKS" },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryTabItem(
                            label = "Адаптации",
                            count = allAdaptations.size,
                            isSelected = isShowingAdaptations,
                            onClick = { selectedCategoryTab = "ADAPTATIONS" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Status Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RedesignedFilterChip(
                        label = "Все ($totalItemsCount)",
                        isSelected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null }
                    )
                    RedesignedFilterChip(
                        label = "Читаю ($readingCount)",
                        isSelected = selectedStatusFilter == TitleStatus.READING,
                        color = LocalStatusColors.current.reading,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == TitleStatus.READING) null else TitleStatus.READING }
                    )
                    RedesignedFilterChip(
                        label = "В планах ($plannedCount)",
                        isSelected = selectedStatusFilter == TitleStatus.PLANNED,
                        color = LocalStatusColors.current.planned,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == TitleStatus.PLANNED) null else TitleStatus.PLANNED }
                    )
                    RedesignedFilterChip(
                        label = "Завершено ($completedCount)",
                        isSelected = selectedStatusFilter == TitleStatus.COMPLETED,
                        color = LocalStatusColors.current.completed,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == TitleStatus.COMPLETED) null else TitleStatus.COMPLETED }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Lists
            if (isShowingBooks) {
                if (filteredBooks.isEmpty()) {
                    RedesignedEmptyState(
                        title = if (searchQuery.isNotEmpty() || selectedStatusFilter != null) "Ничего не найдено" else "Библиотека пока пуста",
                        subtitle = if (searchQuery.isNotEmpty() || selectedStatusFilter != null) "Попробуйте изменить параметры поиска или фильтры" else "Добавьте свою первую книгу, ранобэ или веб-новеллу",
                        buttonText = if (allBooks.isEmpty()) "Добавить тайтл" else null,
                        onButtonClick = { viewModel.showAddBookDialog.value = true }
                    )
                } else {
                    when (selectedViewMode) {
                        RedesignedViewMode.GRID_POSTER -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredBooks, key = { it.id }) { book ->
                                    RedesignedBookPosterCard(
                                        book = book,
                                        onClick = { viewModel.openBookDetails(book.id) }
                                    )
                                }
                            }
                        }
                        RedesignedViewMode.EDITORIAL_CARD -> {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredBooks, key = { it.id }) { book ->
                                    RedesignedBookEditorialCard(
                                        book = book,
                                        onClick = { viewModel.openBookDetails(book.id) },
                                        onQuickIncrement = {
                                            // Increment volume or chapter
                                            val currentReadVol = book.volumes.count { it.isRead }
                                            if (currentReadVol < book.volumes.size) {
                                                val updatedVolumes = book.volumes.mapIndexed { idx, v ->
                                                    if (idx == currentReadVol) v.copy(isRead = true) else v
                                                }
                                                viewModel.updateBook(book.copy(volumes = updatedVolumes))
                                            } else {
                                                viewModel.updateBook(book.copy(webChaptersRead = book.webChaptersRead + 1))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        RedesignedViewMode.COMPACT_LIST -> {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredBooks, key = { it.id }) { book ->
                                    RedesignedBookCompactRow(
                                        book = book,
                                        onClick = { viewModel.openBookDetails(book.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Adaptations
                if (filteredAdaptations.isEmpty()) {
                    RedesignedEmptyState(
                        title = if (searchQuery.isNotEmpty() || selectedStatusFilter != null) "Ничего не найдено" else "Список адаптаций пуст",
                        subtitle = if (searchQuery.isNotEmpty() || selectedStatusFilter != null) "Попробуйте изменить параметры поиска или фильтры" else "Добавьте экранизацию, аниме, фильм или дораму",
                        buttonText = if (allAdaptations.isEmpty()) "Добавить адаптацию" else null,
                        onButtonClick = { viewModel.showAddAdaptationDialog.value = true }
                    )
                } else {
                    when (selectedViewMode) {
                        RedesignedViewMode.GRID_POSTER -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredAdaptations, key = { it.id }) { ad ->
                                    RedesignedAdaptationPosterCard(
                                        adaptation = ad,
                                        onClick = { viewModel.openAdaptationDetails(ad.id) }
                                    )
                                }
                            }
                        }
                        RedesignedViewMode.EDITORIAL_CARD -> {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredAdaptations, key = { it.id }) { ad ->
                                    RedesignedAdaptationEditorialCard(
                                        adaptation = ad,
                                        onClick = { viewModel.openAdaptationDetails(ad.id) },
                                        onQuickIncrement = {
                                            viewModel.updateAdaptation(ad.copy(episodesWatched = ad.episodesWatched + 1))
                                        }
                                    )
                                }
                            }
                        }
                        RedesignedViewMode.COMPACT_LIST -> {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredAdaptations, key = { it.id }) { ad ->
                                    RedesignedAdaptationCompactRow(
                                        adaptation = ad,
                                        onClick = { viewModel.openAdaptationDetails(ad.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                if (isShowingBooks) {
                    viewModel.showAddBookDialog.value = true
                } else {
                    viewModel.showAddAdaptationDialog.value = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 86.dp, end = 20.dp)
                .shadow(12.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Добавить",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ViewModeIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CategoryTabItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$count",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RedesignedFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            color?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        border = BorderStroke(
            1.dp,
            if (isSelected) {
                color?.copy(alpha = 0.8f) ?: MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            }
        ),
        modifier = Modifier
            .height(34.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    color ?: MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * 🌟 EDITORIAL MAGAZINE CARD FOR BOOKS
 */
@Composable
fun RedesignedBookEditorialCard(
    book: BookTitle,
    onClick: () -> Unit,
    onQuickIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = book.calculateProgress()
    val progressAnimated by animateFloatAsState(targetValue = progress, label = "progressAnim")
    val readVols = book.volumes.count { it.isRead }
    val totalVols = book.volumes.size

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Book Cover with ambient glow
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .height(124.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (!book.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Info Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = book.status)

                        if (book.rating != null && book.rating > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = String.format("%.1f", book.rating),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (book.author.isNotBlank()) {
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar & Quick Action
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (totalVols > 0) "Том $readVols / $totalVols" else if (book.webChaptersTotal > 0) "Гл. ${book.webChaptersRead} / ${book.webChaptersTotal}" else "Прогресс",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressAnimated },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }
    }
}

/**
 * 🌟 POSTER GRID CARD FOR BOOKS
 */
@Composable
fun RedesignedBookPosterCard(
    book: BookTitle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (!book.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Top Badge Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    StatusBadge(status = book.status)
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (book.volumes.isNotEmpty()) "${book.volumes.count { it.isRead }}/${book.volumes.size} т." else "Веб",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (book.rating != null && book.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", book.rating),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🌟 COMPACT ROW FOR BOOKS
 */
@Composable
fun RedesignedBookCompactRow(
    book: BookTitle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (!book.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (book.volumes.isNotEmpty()) "${book.volumes.count { it.isRead }} из ${book.volumes.size} томов" else "Глава ${book.webChaptersRead}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StatusBadge(status = book.status)
        }
    }
}

/**
 * 🌟 EDITORIAL MAGAZINE CARD FOR ADAPTATIONS
 */
@Composable
fun RedesignedAdaptationEditorialCard(
    adaptation: Adaptation,
    onClick: () -> Unit,
    onQuickIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = adaptation.calculateProgress()
    val progressAnimated by animateFloatAsState(targetValue = progress, label = "progressAdAnim")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .height(124.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (!adaptation.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = adaptation.coverUrl,
                        contentDescription = adaptation.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF7C3AED), Color(0xFFA78BFA))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = adaptation.status, isAdaptation = true)

                        if (adaptation.rating != null && adaptation.rating > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = String.format("%.1f", adaptation.rating),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Text(
                        text = adaptation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (adaptation.directorOrStudio.isNotBlank()) {
                        Text(
                            text = adaptation.directorOrStudio,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Серия ${adaptation.episodesWatched} / ${if (adaptation.episodesTotal > 0) adaptation.episodesTotal else "∞"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressAnimated },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }
    }
}

/**
 * 🌟 POSTER GRID CARD FOR ADAPTATIONS
 */
@Composable
fun RedesignedAdaptationPosterCard(
    adaptation: Adaptation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (!adaptation.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = adaptation.coverUrl,
                        contentDescription = adaptation.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0xFF2E1065), Color(0xFF0F172A)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Movie, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    StatusBadge(status = adaptation.status, isAdaptation = true)
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = adaptation.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${adaptation.episodesWatched}/${if (adaptation.episodesTotal > 0) adaptation.episodesTotal else "∞"} сер.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (adaptation.rating != null && adaptation.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", adaptation.rating),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🌟 COMPACT ROW FOR ADAPTATIONS
 */
@Composable
fun RedesignedAdaptationCompactRow(
    adaptation: Adaptation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (!adaptation.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = adaptation.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Movie, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = adaptation.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Серия ${adaptation.episodesWatched} из ${if (adaptation.episodesTotal > 0) adaptation.episodesTotal else "∞"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StatusBadge(status = adaptation.status, isAdaptation = true)
        }
    }
}

/**
 * 🌟 REDESIGNED INVITING EMPTY STATE
 */
@Composable
fun RedesignedEmptyState(
    title: String,
    subtitle: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
