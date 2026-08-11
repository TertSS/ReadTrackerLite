package com.example.ui.screens.details

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleDetailScreen(
    bookId: String,
    viewModel: ReadTrackerViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val books by viewModel.allBooks.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    val book = books.find { it.id == bookId }
    val titleReviews = allReviews.filter { it.targetId == bookId }

    var isSynopsisExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Книга не найдена", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(book.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit, modifier = Modifier.testTag("detail_edit_btn")) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero / Cover Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                CoverImage(
                    coverUrl = book.coverUrl,
                    title = book.title,
                    modifier = Modifier.fillMaxSize(),
                    height = 300.dp,
                    width = 400.dp,
                    corner = 0.dp
                )

                // Gradient scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )

                // Title info on bottom of hero
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = book.status)
                        FormatBadge(format = book.format.label)
                        if (book.isOngoing) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Онгоинг",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (book.author.isNotEmpty()) {
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Drop Reason Banner if dropped
                if (book.status == TitleStatus.DROPPED && book.droppedReason.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Причина дропа",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = book.droppedReason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Bookmark Banner
                if (settings.bookmarksEnabled && book.bookmark.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Закладка",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = book.bookmark,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Start after adaptation reading offset
                if (settings.startAfterAdaptationEnabled && (book.startVolume != null || book.startChapter != null)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val offsetText = if (book.startVolume != null) "Начато с ${book.startVolume} тома после экранизации"
                            else "Начато с ${book.startChapter} главы после экранизации"
                            Text(
                                text = offsetText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Main Progress Bento Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ПРОГРЕСС ЧТЕНИЯ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = book.progressDisplay,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "${(book.progressFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { book.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }

                // Mini Stat Cards (Chapters & Words)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (book.format == TitleFormat.WEB_NOVEL || book.format == TitleFormat.HYBRID) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Главы",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (book.totalChapters > 0) "${book.chapters}/${book.totalChapters}" else "${book.chapters}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                imageVector = Icons.Default.SortByAlpha,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Слов прочитано",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatNumber(book.effectiveWords, shorten = settings.shortenNumbers),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Interactive Rating Section
                if (settings.ratingEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val scoreStr = if (book.rating > 0f) Formatters.formatRating(book.rating, settings.ratingScale) else "Без оценки"
                            Text(
                                text = "ОЦЕНКА ($scoreStr)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StarRatingBar(
                                rating = book.rating,
                                scale = settings.ratingScale,
                                editable = true,
                                allowFractional = settings.fractionalRatingEnabled,
                                onRatingChanged = { newRating ->
                                    viewModel.saveBook(book.copy(rating = newRating))
                                }
                            )
                        }
                    }
                }

                // Genres Cloud
                if (settings.genresEnabled && book.genres.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Жанры",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            book.genres.forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Description / Synopsis
                if (book.description.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Описание",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = book.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (book.description.length > 140) {
                            Text(
                                text = if (isSynopsisExpanded) "Свернуть ▲" else "Читать далее ▼",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // Detailed Volume Breakdown (if enabled)
                if (book.hasDetailedVolumes && book.detailedVolumes.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Детализация по томам (${book.detailedVolumes.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                book.detailedVolumes.forEach { vol ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Том ${vol.volumeNumber}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = PluralRu.pluralWord(vol.wordCount, shorten = settings.shortenNumbers),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (vol != book.detailedVolumes.last()) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Reviews Section for this Book
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val overallReviews = remember(titleReviews) { titleReviews.filter { it.reviewType == ReviewType.OVERALL } }
                    val partReviews = remember(titleReviews) {
                        titleReviews.filter { it.reviewType != ReviewType.OVERALL }
                            .sortedWith(compareBy({ it.targetNumber }, { -(it.createdAt) }))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Заметки и отзывы (${titleReviews.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = { showAddReviewDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Добавить", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (titleReviews.isEmpty()) {
                        Text(
                            text = "Пока нет отзывов. Добавьте свои впечатления по томам или главам!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    } else {
                        // 1. TOP: Overall Impression
                        if (overallReviews.isNotEmpty()) {
                            Text(
                                text = "⭐ Общее впечатление",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            overallReviews.forEach { review ->
                                ReviewItemCard(
                                    review = review,
                                    onDelete = { reviewToDelete = review }
                                )
                            }
                        }

                        // 2. BELOW: Volume and Chapter Reviews
                        if (partReviews.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📖 Отзывы по томам и главам (${partReviews.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            partReviews.forEach { review ->
                                ReviewItemCard(
                                    review = review,
                                    onDelete = { reviewToDelete = review }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Delete Book Confirmation Dialog
    ConfirmationDialog(
        show = showDeleteConfirm,
        title = "Удалить тайтл?",
        message = "Вы уверены, что хотите удалить «${book.title}» и все связанные отзывы? Это действие необратимо.",
        confirmText = "Удалить",
        onConfirm = {
            viewModel.deleteBook(book)
            onBack()
        },
        onDismiss = { showDeleteConfirm = false }
    )

    // Delete Review Confirmation
    ConfirmationDialog(
        show = reviewToDelete != null,
        title = "Удалить отзыв?",
        message = "Вы уверены, что хотите удалить этот отзыв?",
        confirmText = "Удалить",
        onConfirm = {
            reviewToDelete?.let { viewModel.deleteReview(it) }
            reviewToDelete = null
        },
        onDismiss = { reviewToDelete = null }
    )

    // Add Review Dialog
    if (showAddReviewDialog) {
        AddReviewDialog(
            targetId = book.id,
            targetTitle = book.title,
            targetType = "book",
            bookFormat = book.format,
            onDismiss = { showAddReviewDialog = false },
            onSave = { newReview ->
                viewModel.addReview(newReview)
                showAddReviewDialog = false
            }
        )
    }
}

@Composable
fun ReviewItemCard(
    review: Review,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSpoilerRevealed by remember { mutableStateOf(false) }
    val isSpoiler = review.text.contains("спойлер", ignoreCase = true) || review.text.startsWith("!")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Я",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = review.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dateFormatted = remember(review.createdAt) {
                        SimpleDateFormat("dd MMM yyyy", Locale("ru")).format(Date(review.createdAt))
                    }
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isSpoiler && !isSpoilerRevealed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        .clickable { isSpoilerRevealed = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ Спойлер. Нажмите, чтобы показать",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            } else {
                Text(
                    text = review.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewDialog(
    targetId: String,
    targetTitle: String,
    targetType: String,
    bookFormat: TitleFormat? = null,
    adaptationType: AdaptationType? = null,
    onDismiss: () -> Unit,
    onSave: (Review) -> Unit
) {
    var reviewType by remember {
        mutableStateOf(
            when {
                bookFormat == TitleFormat.WEB_NOVEL -> ReviewType.CHAPTERS
                bookFormat == TitleFormat.VISUAL_NOVEL -> ReviewType.OVERALL
                adaptationType == AdaptationType.MOVIE -> ReviewType.MOVIE
                adaptationType == AdaptationType.SERIES -> ReviewType.SEASON
                else -> ReviewType.VOLUME
            }
        )
    }

    var targetNumber by remember { mutableStateOf("1") }
    var chapterStart by remember { mutableStateOf("1") }
    var chapterEnd by remember { mutableStateOf("1") }
    var reviewText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Добавить отзыв",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Произведение: $targetTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // Selectable review types based on rules
                val allowedTypes = when {
                    bookFormat == TitleFormat.HYBRID -> listOf(ReviewType.VOLUME, ReviewType.CHAPTERS, ReviewType.OVERALL)
                    bookFormat == TitleFormat.WEB_NOVEL -> listOf(ReviewType.CHAPTERS, ReviewType.OVERALL)
                    bookFormat == TitleFormat.SERIES || bookFormat == TitleFormat.SINGLE -> listOf(ReviewType.VOLUME, ReviewType.OVERALL)
                    bookFormat == TitleFormat.VISUAL_NOVEL -> listOf(ReviewType.OVERALL)
                    adaptationType == AdaptationType.SERIES -> listOf(ReviewType.SEASON, ReviewType.OVERALL)
                    adaptationType == AdaptationType.MOVIE -> listOf(ReviewType.MOVIE, ReviewType.OVERALL)
                    else -> listOf(ReviewType.OVERALL)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allowedTypes.forEach { type ->
                        FilterChip(
                            selected = reviewType == type,
                            onClick = { reviewType = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                when (reviewType) {
                    ReviewType.VOLUME -> {
                        OutlinedTextField(
                            value = targetNumber,
                            onValueChange = { targetNumber = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Номер тома") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ReviewType.SEASON -> {
                        OutlinedTextField(
                            value = targetNumber,
                            onValueChange = { targetNumber = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Номер сезона") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ReviewType.MOVIE -> {
                        OutlinedTextField(
                            value = targetNumber,
                            onValueChange = { targetNumber = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Номер фильма") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ReviewType.CHAPTERS -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = chapterStart,
                                onValueChange = { chapterStart = it.filter { ch -> ch.isDigit() } },
                                label = { Text("С главы") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = chapterEnd,
                                onValueChange = { chapterEnd = it.filter { ch -> ch.isDigit() } },
                                label = { Text("По главу") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    ReviewType.OVERALL -> {
                        // No specific number needed
                    }
                }

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = {
                        reviewText = it
                        errorMessage = null
                    },
                    label = { Text("Текст отзыва / заметки") },
                    placeholder = { Text("Напишите свои впечатления...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reviewText.trim().isEmpty()) {
                        errorMessage = "Текст отзыва не может быть пустым"
                        return@Button
                    }
                    val num = targetNumber.toIntOrNull() ?: 1
                    if (num <= 0 && reviewType != ReviewType.OVERALL) {
                        errorMessage = "Номер должен быть больше 0"
                        return@Button
                    }

                    var cStart: Int? = null
                    var cEnd: Int? = null
                    if (reviewType == ReviewType.CHAPTERS) {
                        cStart = chapterStart.toIntOrNull() ?: 1
                        cEnd = chapterEnd.toIntOrNull() ?: cStart
                        if (cEnd < cStart) {
                            errorMessage = "Конечная глава не может быть меньше начальной"
                            return@Button
                        }
                    }

                    val review = Review(
                        targetId = targetId,
                        targetTitle = targetTitle,
                        targetType = targetType,
                        reviewType = reviewType,
                        targetNumber = num,
                        chapterStart = cStart,
                        chapterEnd = cEnd,
                        text = reviewText.trim()
                    )
                    onSave(review)
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp)
    )
}
