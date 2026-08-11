package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.ReadTrackerRepository
import com.example.utils.BackupHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReadTrackerViewModel(
    private val repository: ReadTrackerRepository
) : ViewModel() {

    // Main Navigation
    val currentTab = MutableStateFlow("library") // library, reviews, stats, tier_list, settings
    val libraryMode = MutableStateFlow(LibraryMode.BOOKS)
    val isGridView = MutableStateFlow(false)

    // Filters & Search
    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow<TitleStatus?>(null) // null == "Все"
    val selectedFormatFilter = MutableStateFlow<String?>("ALL") // ALL, SERIES, WEB_NOVEL, SINGLE, HYBRID, VISUAL_NOVEL

    // Detail & Form Navigation State
    val selectedBookId = MutableStateFlow<String?>(null)
    val selectedAdaptationId = MutableStateFlow<String?>(null)
    val showAddBookDialog = MutableStateFlow(false)
    val showAddAdaptationDialog = MutableStateFlow(false)
    val editingBook = MutableStateFlow<BookTitle?>(null)
    val editingAdaptation = MutableStateFlow<Adaptation?>(null)

    // Data from Repository
    val allBooks = repository.allBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allAdaptations = repository.allAdaptations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allReviews = repository.allReviews.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTierRows = repository.allTierRows.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appSettings = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    init {
        viewModelScope.launch {
            combine(repository.allBooks, repository.allAdaptations) { books, adaptations ->
                books to adaptations
            }.collectLatest {
                repository.cleanupOrphanReviews()
            }
        }
        viewModelScope.launch {
            repository.settingsFlow.firstOrNull()?.let { settings ->
                if (settings.rememberLastTab && settings.lastActiveTab.isNotBlank()) {
                    currentTab.value = settings.lastActiveTab
                }
            }
        }
    }

    // Filtered Books Flow
    val filteredBooks = combine(
        allBooks,
        searchQuery,
        selectedStatusFilter,
        selectedFormatFilter,
        appSettings
    ) { books, query, status, format, settings ->
        books.filter { book ->
            val matchesQuery = query.isBlank() ||
                book.title.contains(query, ignoreCase = true) ||
                book.author.contains(query, ignoreCase = true) ||
                book.genres.any { it.contains(query, ignoreCase = true) }

            val matchesStatus = status == null || book.status == status

            val matchesFormat = when (format) {
                "SERIES" -> book.format == TitleFormat.SERIES
                "NOVEL" -> book.format == TitleFormat.NOVEL
                "WEB_NOVEL" -> book.format == TitleFormat.WEB_NOVEL
                "SINGLE" -> book.format == TitleFormat.SINGLE
                "HYBRID" -> book.format == TitleFormat.HYBRID
                "VISUAL_NOVEL" -> book.format == TitleFormat.VISUAL_NOVEL
                else -> true
            }

            matchesQuery && matchesStatus && matchesFormat
        }.let { list ->
            if (settings.sortByStatus) {
                list.sortedBy { it.status.sortOrder }
            } else {
                list
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Adaptations Flow
    val filteredAdaptations = combine(
        allAdaptations,
        searchQuery,
        selectedStatusFilter,
        selectedFormatFilter,
        appSettings
    ) { adaptations, query, status, format, settings ->
        adaptations.filter { ad ->
            val matchesQuery = query.isBlank() ||
                ad.title.contains(query, ignoreCase = true) ||
                ad.genres.any { it.contains(query, ignoreCase = true) }

            val matchesStatus = status == null || ad.status == status

            val matchesFormat = when (format) {
                "SERIES" -> ad.type == AdaptationType.SERIES
                "MOVIE" -> ad.type == AdaptationType.MOVIE
                "COMPLETED_SEASONS" -> ad.completedSeasons > 0
                "HAS_WATCH_TIME" -> ad.watchTimeMinutes > 0
                "MULTI_SEASONS" -> ad.seasons.size > 1
                else -> true
            }

            matchesQuery && matchesStatus && matchesFormat
        }.let { list ->
            if (settings.sortByStatus) {
                list.sortedBy { it.status.sortOrder }
            } else {
                list
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected Book Detail Flow
    val selectedBook = combine(allBooks, selectedBookId) { books, id ->
        if (id == null) null else books.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Selected Adaptation Detail Flow
    val selectedAdaptation = combine(allAdaptations, selectedAdaptationId) { ads, id ->
        if (id == null) null else ads.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All distinct used genres across books & adaptations
    val allKnownGenres = combine(allBooks, allAdaptations) { books, ads ->
        val set = mutableSetOf<String>()
        books.forEach { b -> b.genres.forEach { set.add(it.trim()) } }
        ads.forEach { a -> a.genres.forEach { set.add(it.trim()) } }
        set.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions: Navigation
    fun setTab(tab: String) {
        currentTab.value = tab
        selectedBookId.value = null
        selectedAdaptationId.value = null
        editingBook.value = null
        editingAdaptation.value = null
        if (appSettings.value.rememberLastTab) {
            updateAppSettings(appSettings.value.copy(lastActiveTab = tab))
        }
    }

    fun openBookDetails(id: String) {
        selectedBookId.value = id
    }

    fun openAdaptationDetails(id: String) {
        selectedAdaptationId.value = id
    }

    fun closeDetails() {
        selectedBookId.value = null
        selectedAdaptationId.value = null
    }

    // Book Actions
    fun saveBook(book: BookTitle) {
        viewModelScope.launch {
            if (editingBook.value != null || allBooks.value.any { it.id == book.id }) {
                repository.updateBook(book.copy(updatedAt = System.currentTimeMillis()))
            } else {
                repository.insertBook(book)
            }
            showAddBookDialog.value = false
            editingBook.value = null
        }
    }

    fun deleteBook(book: BookTitle) {
        viewModelScope.launch {
            repository.deleteBook(book)
            if (selectedBookId.value == book.id) {
                selectedBookId.value = null
            }
        }
    }

    fun incrementBookProgress(book: BookTitle) {
        viewModelScope.launch {
            val updated = when (book.format) {
                TitleFormat.VISUAL_NOVEL -> {
                    val next = (book.endings + 1).coerceAtMost(if (book.totalEndings > 0) book.totalEndings else Int.MAX_VALUE)
                    book.copy(
                        endings = next,
                        status = if (book.totalEndings > 0 && next >= book.totalEndings) TitleStatus.COMPLETED else book.status
                    )
                }
                TitleFormat.WEB_NOVEL -> {
                    val next = (book.chapters + 1).coerceAtMost(if (book.totalChapters > 0) book.totalChapters else Int.MAX_VALUE)
                    book.copy(
                        chapters = next,
                        status = if (book.totalChapters > 0 && next >= book.totalChapters) TitleStatus.COMPLETED else book.status
                    )
                }
                TitleFormat.HYBRID -> {
                    val nextVol = (book.volumes + 1).coerceAtMost(if (book.totalVolumes > 0) book.totalVolumes else Int.MAX_VALUE)
                    book.copy(
                        volumes = nextVol,
                        status = if (!book.isOngoing && book.totalVolumes > 0 && nextVol >= book.totalVolumes) TitleStatus.COMPLETED else book.status
                    )
                }
                TitleFormat.SERIES, TitleFormat.NOVEL, TitleFormat.SINGLE -> {
                    val next = (book.volumes + 1).coerceAtMost(if (book.totalVolumes > 0) book.totalVolumes else Int.MAX_VALUE)
                    book.copy(
                        volumes = next,
                        status = if (!book.isOngoing && book.totalVolumes > 0 && next >= book.totalVolumes) TitleStatus.COMPLETED else book.status
                    )
                }
            }
            repository.updateBook(updated.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun decrementBookProgress(book: BookTitle) {
        viewModelScope.launch {
            val updated = when (book.format) {
                TitleFormat.VISUAL_NOVEL -> book.copy(endings = (book.endings - 1).coerceAtLeast(0))
                TitleFormat.WEB_NOVEL -> book.copy(chapters = (book.chapters - 1).coerceAtLeast(0))
                TitleFormat.HYBRID -> book.copy(volumes = (book.volumes - 1).coerceAtLeast(0))
                TitleFormat.SERIES, TitleFormat.NOVEL, TitleFormat.SINGLE -> book.copy(volumes = (book.volumes - 1).coerceAtLeast(0))
            }
            repository.updateBook(updated.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    // Adaptation Actions
    fun saveAdaptation(adaptation: Adaptation) {
        viewModelScope.launch {
            if (editingAdaptation.value != null || allAdaptations.value.any { it.id == adaptation.id }) {
                repository.updateAdaptation(adaptation.copy(updatedAt = System.currentTimeMillis()))
            } else {
                repository.insertAdaptation(adaptation)
            }
            showAddAdaptationDialog.value = false
            editingAdaptation.value = null
        }
    }

    fun deleteAdaptation(adaptation: Adaptation) {
        viewModelScope.launch {
            repository.deleteAdaptation(adaptation)
            if (selectedAdaptationId.value == adaptation.id) {
                selectedAdaptationId.value = null
            }
        }
    }

    fun incrementAdaptationProgress(adaptation: Adaptation) {
        viewModelScope.launch {
            if (adaptation.type == AdaptationType.SERIES) {
                val seasons = adaptation.seasons.toMutableList()
                // Find first incomplete season
                val targetIndex = seasons.indexOfFirst { it.watchedEpisodes < it.totalEpisodes }
                if (targetIndex != -1) {
                    val s = seasons[targetIndex]
                    seasons[targetIndex] = s.copy(watchedEpisodes = s.watchedEpisodes + 1)
                    val allDone = seasons.all { it.isCompleted }
                    val updated = adaptation.copy(
                        seasons = seasons,
                        status = if (allDone) TitleStatus.COMPLETED else adaptation.status,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateAdaptation(updated)
                }
            } else {
                val movies = adaptation.movies.toMutableList()
                val targetIndex = movies.indexOfFirst { !it.isWatched }
                if (targetIndex != -1) {
                    movies[targetIndex] = movies[targetIndex].copy(isWatched = true)
                    val allDone = movies.all { it.isWatched }
                    val updated = adaptation.copy(
                        movies = movies,
                        status = if (allDone) TitleStatus.COMPLETED else adaptation.status,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateAdaptation(updated)
                }
            }
        }
    }

    // Review Actions
    fun addReview(review: Review) {
        viewModelScope.launch {
            repository.insertReview(review)
        }
    }

    fun deleteReview(review: Review) {
        viewModelScope.launch {
            repository.deleteReview(review)
        }
    }

    // Tier List Actions
    fun updateTierRow(row: TierListRow) {
        viewModelScope.launch {
            repository.updateTierRow(row)
        }
    }

    fun updateTierRowProperties(rowId: String, newName: String, newColor: Long, newTextColor: Long) {
        viewModelScope.launch {
            val rows = allTierRows.value.toMutableList()
            val idx = rows.indexOfFirst { it.id == rowId }
            if (idx != -1) {
                rows[idx] = rows[idx].copy(name = newName, color = newColor, textColor = newTextColor)
                repository.setTierRows(rows)
            }
        }
    }

    fun updateTierItemCover(itemId: String, newCoverUrl: String?) {
        viewModelScope.launch {
            val rows = allTierRows.value.toMutableList()
            var changed = false
            for (i in rows.indices) {
                val items = rows[i].items.toMutableList()
                val idx = items.indexOfFirst { it.id == itemId }
                if (idx != -1) {
                    val item = items[idx]
                    items[idx] = item.copy(coverUrl = newCoverUrl?.ifBlank { null })
                    rows[i] = rows[i].copy(items = items)
                    changed = true
                    if (item.sourceId != null) {
                        val book = allBooks.value.find { it.id == item.sourceId }
                        if (book != null) {
                            repository.updateBook(book.copy(coverUrl = newCoverUrl?.ifBlank { null }, updatedAt = System.currentTimeMillis()))
                        }
                        val adap = allAdaptations.value.find { it.id == item.sourceId }
                        if (adap != null) {
                            repository.updateAdaptation(adap.copy(coverUrl = newCoverUrl?.ifBlank { null }, updatedAt = System.currentTimeMillis()))
                        }
                    }
                    break
                }
            }
            if (changed) {
                repository.setTierRows(rows)
            }
        }
    }

    fun deleteTierItem(itemId: String, fromRowId: String?) {
        viewModelScope.launch {
            val rows = allTierRows.value.toMutableList()
            if (fromRowId != null) {
                val idx = rows.indexOfFirst { it.id == fromRowId }
                if (idx != -1) {
                    val row = rows[idx]
                    val item = row.items.find { it.id == itemId }
                    rows[idx] = row.copy(items = row.items.filter { it.id != itemId })
                    repository.setTierRows(rows)
                    if (item?.sourceId == null) {
                        // Independent custom item removed completely
                    }
                }
            }
        }
    }

    fun addTierRow(name: String, color: Long, textColor: Long) {
        viewModelScope.launch {
            val rows = allTierRows.value
            val newRow = TierListRow(
                name = name,
                color = color,
                textColor = textColor,
                orderIndex = rows.size
            )
            repository.insertTierRow(newRow)
        }
    }

    fun deleteTierRow(row: TierListRow) {
        viewModelScope.launch {
            repository.deleteTierRow(row)
        }
    }

    fun applyTierPreset(preset: TierPreset) {
        viewModelScope.launch {
            repository.applyTierPreset(preset)
        }
    }

    fun moveTierItem(itemId: String, fromRowId: String?, toRowId: String?, targetIndex: Int = -1) {
        viewModelScope.launch {
            val rows = allTierRows.value.toMutableList()
            var movedItem: TierItem? = null

            // Find item in source
            if (fromRowId != null) {
                val fromIndex = rows.indexOfFirst { it.id == fromRowId }
                if (fromIndex != -1) {
                    val sourceRow = rows[fromIndex]
                    movedItem = sourceRow.items.find { it.id == itemId }
                    val newItems = sourceRow.items.filter { it.id != itemId }
                    rows[fromIndex] = sourceRow.copy(items = newItems)
                }
            } else {
                // From unassigned pool
                val unassigned = getUnassignedTierItems(rows)
                movedItem = unassigned.find { it.id == itemId }
            }

            if (movedItem != null && toRowId != null) {
                val toIndex = rows.indexOfFirst { it.id == toRowId }
                if (toIndex != -1) {
                    val destRow = rows[toIndex]
                    val destItems = destRow.items.toMutableList()
                    if (targetIndex in 0..destItems.size) {
                        destItems.add(targetIndex, movedItem)
                    } else {
                        destItems.add(movedItem)
                    }
                    rows[toIndex] = destRow.copy(items = destItems)
                }
            }

            repository.setTierRows(rows)
        }
    }

    fun addIndependentTierItem(title: String, coverUrl: String?, targetRowId: String?) {
        viewModelScope.launch {
            val newItem = TierItem(
                title = title,
                coverUrl = coverUrl,
                sourceId = null
            )
            if (targetRowId != null) {
                val rows = allTierRows.value.toMutableList()
                val idx = rows.indexOfFirst { it.id == targetRowId }
                if (idx != -1) {
                    val row = rows[idx]
                    rows[idx] = row.copy(items = row.items + newItem)
                    repository.setTierRows(rows)
                }
            }
        }
    }

    fun getUnassignedTierItems(
        rows: List<TierListRow>,
        books: List<BookTitle> = allBooks.value,
        adaptations: List<Adaptation> = allAdaptations.value,
        mode: LibraryMode = libraryMode.value
    ): List<TierItem> {
        val assignedSourceIds = rows.flatMap { it.items }.mapNotNull { it.sourceId }.toSet()

        return if (mode == LibraryMode.BOOKS) {
            books
                .filter { it.id !in assignedSourceIds }
                .map {
                    TierItem(
                        id = "book_${it.id}",
                        sourceId = it.id,
                        title = it.title,
                        coverUrl = it.coverUrl,
                        colorPlaceholder = it.coverColor
                    )
                }
        } else {
            adaptations
                .filter { it.id !in assignedSourceIds }
                .map {
                    TierItem(
                        id = "ad_${it.id}",
                        sourceId = it.id,
                        title = it.title,
                        coverUrl = it.coverUrl,
                        colorPlaceholder = it.coverColor
                    )
                }
        }
    }

    // Settings & Goals Actions
    fun updateAppSettings(settings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    // Import / Export
    suspend fun exportLibraryJson(): String {
        return repository.exportLibrary()
    }

    suspend fun importLibraryJson(jsonString: String, replace: Boolean): BackupHelper.ImportResult {
        return repository.importLibrary(jsonString, replace)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            selectedBookId.value = null
            selectedAdaptationId.value = null
            editingBook.value = null
            editingAdaptation.value = null
        }
    }
}

class ReadTrackerViewModelFactory(
    private val repository: ReadTrackerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReadTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReadTrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
