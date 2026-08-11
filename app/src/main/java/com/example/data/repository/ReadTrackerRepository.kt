package com.example.data.repository

import com.example.data.local.*
import com.example.data.models.*
import com.example.utils.BackupHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ReadTrackerRepository(
    private val database: AppDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val bookDao = database.bookDao()
    private val adaptationDao = database.adaptationDao()
    private val reviewDao = database.reviewDao()
    private val tierListDao = database.tierListDao()
    private val settingsDao = database.settingsDao()

    val allBooks: Flow<List<BookTitle>> = bookDao.getAllBooks()
    val allAdaptations: Flow<List<Adaptation>> = adaptationDao.getAllAdaptations()
    val allReviews: Flow<List<Review>> = reviewDao.getAllReviews()
    val allTierRows: Flow<List<TierListRow>> = tierListDao.getAllRows()
    val allCustomTierItems: Flow<List<CustomTierItem>> = tierListDao.getAllCustomTierItems()
    
    val settingsFlow: Flow<AppSettings> = settingsDao.getSettingsFlow()
        .map { it ?: AppSettings() }

    init {
        scope.launch {
            checkAndSeedInitialData()
        }
    }

    // Cleanup orphan reviews where target title no longer exists in database
    suspend fun cleanupOrphanReviews() = withContext(Dispatchers.IO) {
        val books = bookDao.getAllBooks().firstOrNull() ?: emptyList()
        val adaptations = adaptationDao.getAllAdaptations().firstOrNull() ?: emptyList()
        val validIds = (books.map { it.id } + adaptations.map { it.id }).toSet()
        val currentReviews = reviewDao.getAllReviews().firstOrNull() ?: emptyList()
        currentReviews.forEach { review ->
            if (review.targetId !in validIds) {
                reviewDao.deleteReview(review)
            }
        }
    }

    // Books CRUD
    suspend fun insertBook(book: BookTitle) = withContext(Dispatchers.IO) {
        bookDao.insertBook(book)
    }

    suspend fun updateBook(book: BookTitle) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: BookTitle) = withContext(Dispatchers.IO) {
        bookDao.deleteBook(book)
        reviewDao.deleteReviewsByTargetId(book.id)
        // Also remove from any tier rows
        removeSourceFromTierList(book.id)
        cleanupOrphanReviews()
    }

    fun getBookFlow(id: String): Flow<BookTitle?> = bookDao.getBookByIdFlow(id)

    suspend fun getBook(id: String): BookTitle? = withContext(Dispatchers.IO) {
        bookDao.getBookById(id)
    }

    // Adaptations CRUD
    suspend fun insertAdaptation(adaptation: Adaptation) = withContext(Dispatchers.IO) {
        adaptationDao.insertAdaptation(adaptation)
    }

    suspend fun updateAdaptation(adaptation: Adaptation) = withContext(Dispatchers.IO) {
        adaptationDao.updateAdaptation(adaptation)
    }

    suspend fun deleteAdaptation(adaptation: Adaptation) = withContext(Dispatchers.IO) {
        adaptationDao.deleteAdaptation(adaptation)
        reviewDao.deleteReviewsByTargetId(adaptation.id)
        removeSourceFromTierList(adaptation.id)
        cleanupOrphanReviews()
    }

    fun getAdaptationFlow(id: String): Flow<Adaptation?> = adaptationDao.getAdaptationByIdFlow(id)

    suspend fun getAdaptation(id: String): Adaptation? = withContext(Dispatchers.IO) {
        adaptationDao.getAdaptationById(id)
    }

    // Reviews CRUD
    suspend fun insertReview(review: Review) = withContext(Dispatchers.IO) {
        reviewDao.insertReview(review)
    }

    suspend fun deleteReview(review: Review) = withContext(Dispatchers.IO) {
        reviewDao.deleteReview(review)
    }

    fun getReviewsForTarget(targetId: String): Flow<List<Review>> = reviewDao.getReviewsForTarget(targetId)

    // Tier List Operations
    suspend fun insertTierRow(row: TierListRow) = withContext(Dispatchers.IO) {
        tierListDao.insertRow(row)
    }

    suspend fun updateTierRow(row: TierListRow) = withContext(Dispatchers.IO) {
        tierListDao.updateRow(row)
    }

    suspend fun deleteTierRow(row: TierListRow) = withContext(Dispatchers.IO) {
        tierListDao.deleteRow(row)
    }

    suspend fun setTierRows(rows: List<TierListRow>) = withContext(Dispatchers.IO) {
        tierListDao.deleteAllRows()
        tierListDao.insertRows(rows)
    }

    suspend fun insertCustomTierItem(item: CustomTierItem) = withContext(Dispatchers.IO) {
        tierListDao.insertCustomTierItem(item)
    }

    suspend fun updateCustomTierItem(item: CustomTierItem) = withContext(Dispatchers.IO) {
        tierListDao.updateCustomTierItem(item)
    }

    suspend fun deleteCustomTierItem(item: CustomTierItem) = withContext(Dispatchers.IO) {
        tierListDao.deleteCustomTierItem(item)
    }

    suspend fun deleteCustomTierItemById(id: String) = withContext(Dispatchers.IO) {
        tierListDao.deleteCustomTierItemById(id)
    }

    suspend fun applyTierPreset(preset: TierPreset) = withContext(Dispatchers.IO) {
        val newRows = when (preset) {
            TierPreset.CLASSIC -> listOf(
                TierListRow(id = UUID.randomUUID().toString(), name = "Peak", color = 0xFFFF5252L, textColor = 0xFFFFFFFFL, orderIndex = 0),
                TierListRow(id = UUID.randomUUID().toString(), name = "Mid", color = 0xFFFFB142L, textColor = 0xFF131313L, orderIndex = 1),
                TierListRow(id = UUID.randomUUID().toString(), name = "Weak", color = 0xFFFFDA79L, textColor = 0xFF131313L, orderIndex = 2),
                TierListRow(id = UUID.randomUUID().toString(), name = "Trash", color = 0xFF33D9B2L, textColor = 0xFF131313L, orderIndex = 3)
            )
            TierPreset.LETTERS -> listOf(
                TierListRow(id = UUID.randomUUID().toString(), name = "S", color = 0xFFFF5252L, textColor = 0xFFFFFFFFL, orderIndex = 0),
                TierListRow(id = UUID.randomUUID().toString(), name = "A", color = 0xFFFFB142L, textColor = 0xFF131313L, orderIndex = 1),
                TierListRow(id = UUID.randomUUID().toString(), name = "B", color = 0xFFFFDA79L, textColor = 0xFF131313L, orderIndex = 2),
                TierListRow(id = UUID.randomUUID().toString(), name = "C", color = 0xFF33D9B2L, textColor = 0xFF131313L, orderIndex = 3),
                TierListRow(id = UUID.randomUUID().toString(), name = "D", color = 0xFF34ACE0L, textColor = 0xFFFFFFFFL, orderIndex = 4),
                TierListRow(id = UUID.randomUUID().toString(), name = "F", color = 0xFF706FD3L, textColor = 0xFFFFFFFFL, orderIndex = 5)
            )
            TierPreset.NUMBERS -> (10 downTo 1).mapIndexed { index, num ->
                TierListRow(
                    id = UUID.randomUUID().toString(),
                    name = num.toString(),
                    color = when (num) {
                        in 9..10 -> 0xFFFF5252L
                        in 7..8 -> 0xFFFFB142L
                        in 5..6 -> 0xFFFFDA79L
                        in 3..4 -> 0xFF33D9B2L
                        else -> 0xFF34ACE0L
                    },
                    textColor = if (num in 5..8) 0xFF131313L else 0xFFFFFFFFL,
                    orderIndex = index
                )
            }
        }
        tierListDao.deleteAllRows()
        tierListDao.insertRows(newRows)
    }

    private suspend fun removeSourceFromTierList(sourceId: String) {
        val rows = tierListDao.getAllRows().firstOrNull() ?: return
        var updated = false
        val newRows = rows.map { row ->
            val filtered = row.items.filter { it.sourceId != sourceId }
            if (filtered.size != row.items.size) {
                updated = true
                row.copy(items = filtered)
            } else {
                row
            }
        }
        if (updated) {
            tierListDao.deleteAllRows()
            tierListDao.insertRows(newRows)
        }
    }

    // Settings
    suspend fun updateSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    // Backup & Restore
    suspend fun exportLibrary(): String = withContext(Dispatchers.IO) {
        val books = bookDao.getAllBooks().firstOrNull() ?: emptyList()
        val adaptations = adaptationDao.getAllAdaptations().firstOrNull() ?: emptyList()
        val reviews = reviewDao.getAllReviews().firstOrNull() ?: emptyList()
        val tierRows = tierListDao.getAllRows().firstOrNull() ?: emptyList()
        BackupHelper.exportLibraryToJson(books, adaptations, reviews, tierRows)
    }

    suspend fun importLibrary(jsonString: String, replace: Boolean): BackupHelper.ImportResult = withContext(Dispatchers.IO) {
        val result = BackupHelper.parseLibraryJson(jsonString)
        if (result.success) {
            if (replace) {
                bookDao.deleteAllBooks()
                adaptationDao.deleteAllAdaptations()
                reviewDao.deleteAllReviews()
                tierListDao.deleteAllRows()

                bookDao.insertBooks(result.books)
                adaptationDao.insertAdaptations(result.adaptations)
                reviewDao.insertReviews(result.reviews)
                tierListDao.insertRows(result.tierRows)
            } else {
                // Append / Merge
                bookDao.insertBooks(result.books)
                adaptationDao.insertAdaptations(result.adaptations)
                reviewDao.insertReviews(result.reviews)
                tierListDao.insertRows(result.tierRows)
            }
            cleanupOrphanReviews()
        }
        result
    }

    // Initial Seed Data (no pre-populated titles, ready for fresh user data)
    private suspend fun checkAndSeedInitialData() {
        try {
            cleanupOrphanReviews()
            val existingSettings = settingsDao.getSettings()
            if (existingSettings == null) {
                settingsDao.insertOrUpdateSettings(AppSettings())
            }

            if (tierListDao.getRowCount() == 0) {
                val initialRows = listOf(
                    TierListRow(
                        id = "tr_s",
                        name = "S",
                        color = 0xFFFF5252L,
                        textColor = 0xFFFFFFFFL,
                        orderIndex = 0,
                        items = emptyList()
                    ),
                    TierListRow(
                        id = "tr_a",
                        name = "A",
                        color = 0xFFFFB142L,
                        textColor = 0xFF131313L,
                        orderIndex = 1,
                        items = emptyList()
                    ),
                    TierListRow(
                        id = "tr_b",
                        name = "B",
                        color = 0xFFFFDA79L,
                        textColor = 0xFF131313L,
                        orderIndex = 2,
                        items = emptyList()
                    ),
                    TierListRow(
                        id = "tr_c",
                        name = "C",
                        color = 0xFF33D9B2L,
                        textColor = 0xFF131313L,
                        orderIndex = 3,
                        items = emptyList()
                    ),
                    TierListRow(
                        id = "tr_d",
                        name = "D",
                        color = 0xFF34ACE0L,
                        textColor = 0xFFFFFFFFL,
                        orderIndex = 4,
                        items = emptyList()
                    )
                )
                tierListDao.insertRows(initialRows)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        bookDao.deleteAllBooks()
        adaptationDao.deleteAllAdaptations()
        reviewDao.deleteAllReviews()
        val rows = tierListDao.getAllRowsList()
        val clearedRows = rows.map { it.copy(items = emptyList()) }
        tierListDao.insertRows(clearedRows)
    }
}
