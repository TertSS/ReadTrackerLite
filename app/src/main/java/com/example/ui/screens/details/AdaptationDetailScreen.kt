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
fun AdaptationDetailScreen(
    adaptationId: String,
    viewModel: ReadTrackerViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val adaptations by viewModel.allAdaptations.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    val adaptation = adaptations.find { it.id == adaptationId }
    val titleReviews = allReviews.filter { it.targetId == adaptationId }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }
    var expandedDetailSeasons by remember { mutableStateOf(setOf<Int>()) }

    if (adaptation == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Экранизация не найдена", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(adaptation.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit, modifier = Modifier.testTag("detail_edit_btn")) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = MaterialTheme.colorScheme.secondary)
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
            // Hero / Cover Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                CoverImage(
                    coverUrl = adaptation.coverUrl,
                    title = adaptation.title,
                    modifier = Modifier.fillMaxSize(),
                    height = 280.dp,
                    width = 400.dp,
                    corner = 0.dp
                )

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

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = adaptation.status, isAdaptation = true)
                        FormatBadge(format = adaptation.type.shortLabel)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = adaptation.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Drop Reason Banner if dropped
                if (adaptation.status == TitleStatus.DROPPED && adaptation.droppedReason.isNotEmpty()) {
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
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Причина дропа",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = adaptation.droppedReason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Bookmark Banner
                if (settings.bookmarksEnabled && adaptation.bookmark.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Закладка", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = adaptation.bookmark, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
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
                                    text = "ПРОГРЕСС ПРОСМОТРА",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = adaptation.progressDisplay,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "${(adaptation.progressFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { adaptation.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }

                // Watch Time and Stats Mini Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Время просмотра", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = Formatters.formatDuration(adaptation.watchTimeMinutes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (adaptation.type == AdaptationType.SERIES) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Сезоны", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${adaptation.completedSeasons}/${adaptation.seasons.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Interactive Rating
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
                            val scoreStr = if (adaptation.rating > 0f) Formatters.formatRating(adaptation.rating, settings.ratingScale, settings.allowDecimalRating) else "Без оценки"
                            Text(
                                text = "ОЦЕНКА ($scoreStr)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StarRatingBar(
                                rating = adaptation.rating,
                                scale = settings.ratingScale,
                                allowDecimal = settings.allowDecimalRating,
                                editable = true,
                                onRatingChanged = { newRating ->
                                    viewModel.saveAdaptation(adaptation.copy(rating = newRating))
                                }
                            )
                        }
                    }
                }

                // Seasons Details Breakdown
                if (adaptation.type == AdaptationType.SERIES && adaptation.seasons.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Сезоны (${adaptation.seasons.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        adaptation.seasons.forEach { season ->
                            val isExpanded = expandedDetailSeasons.contains(season.seasonNumber)
                            val seasonTotalMins = season.calculateTotalSeasonDurationMinutes()
                            val seasonWatchMins = season.calculateWatchTimeMinutes()

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Сезон ${season.seasonNumber}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${season.watchedEpisodes} из ${season.totalEpisodes} серий (${Formatters.formatDuration(seasonWatchMins)} из ${Formatters.formatDuration(seasonTotalMins)})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (season.isCompleted) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "Завершён",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    expandedDetailSeasons = if (isExpanded) {
                                                        expandedDetailSeasons - season.seasonNumber
                                                    } else {
                                                        expandedDetailSeasons + season.seasonNumber
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть серии",
                                                    tint = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }

                                    // Episode breakdown if expanded
                                    if (isExpanded) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        Text(
                                            text = "Серии и длительность:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )

                                        val episodeCount = maxOf(season.totalEpisodes, if (season.watchedEpisodes > 0) season.watchedEpisodes else 1)
                                        for (epNum in 1..episodeCount) {
                                            val epDur = season.getEpisodeDuration(epNum)
                                            val isCustom = season.episodeDurations.containsKey(epNum.toString())
                                            val isWatched = epNum <= season.watchedEpisodes

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Серия $epNum",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (isWatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = if (isWatched) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    if (isWatched) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "✓",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = if (isCustom) "$epDur мин (спец.)" else "$epDur мин",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isCustom) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Movies List Breakdown
                if (adaptation.type == AdaptationType.MOVIE && adaptation.movies.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Фильмы (${adaptation.movies.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        adaptation.movies.forEach { movie ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (movie.title.isNotEmpty()) movie.title else "Фильм ${movie.movieNumber}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${movie.durationMinutes} мин",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Checkbox(
                                        checked = movie.isWatched,
                                        onCheckedChange = { checked ->
                                            val updatedMovies = adaptation.movies.map {
                                                if (it.movieNumber == movie.movieNumber) it.copy(isWatched = checked) else it
                                            }
                                            viewModel.saveAdaptation(adaptation.copy(movies = updatedMovies))
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Reviews Section
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
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Добавить", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (titleReviews.isEmpty()) {
                        Text(
                            text = "Пока нет отзывов по этой экранизации.",
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
                                color = MaterialTheme.colorScheme.secondary
                            )
                            overallReviews.forEach { review ->
                                ReviewItemCard(
                                    review = review,
                                    onDelete = { reviewToDelete = review }
                                )
                            }
                        }

                        // 2. BELOW: Season / Movie Reviews
                        if (partReviews.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🎬 Отзывы по сезонам и сериям (${partReviews.size})",
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

    // Delete Confirmation Dialog
    ConfirmationDialog(
        show = showDeleteConfirm,
        title = "Удалить экранизацию?",
        message = "Вы уверены, что хотите удалить «${adaptation.title}»?",
        confirmText = "Удалить",
        onConfirm = {
            viewModel.deleteAdaptation(adaptation)
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
            targetId = adaptation.id,
            targetTitle = adaptation.title,
            targetType = "adaptation",
            adaptationType = adaptation.type,
            onDismiss = { showAddReviewDialog = false },
            onSave = { newReview ->
                viewModel.addReview(newReview)
                showAddReviewDialog = false
            }
        )
    }
}
