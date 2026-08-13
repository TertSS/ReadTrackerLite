package com.example.ui.screens.stats

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatsScreen(
    viewModel: ReadTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allAdaptations by viewModel.allAdaptations.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    val activeStatsTab = settings.statsActiveTab
    val availableTabs = remember(settings.statsShowOverviewTab, settings.statsShowBooksTab, settings.statsShowAdaptationsTab) {
        val list = mutableListOf<Pair<String, String>>()
        if (settings.statsShowOverviewTab) list.add("ALL" to "Общая сводка")
        if (settings.statsShowBooksTab) list.add("BOOKS" to "Книги и новеллы")
        if (settings.statsShowAdaptationsTab) list.add("ADAPTATIONS" to "Экранизации")
        if (list.isEmpty()) {
            list.add("ALL" to "Общая сводка")
        }
        list
    }

    val activeTab = remember(activeStatsTab, settings.statsDefaultTab, availableTabs) {
        if (availableTabs.any { it.first == activeStatsTab }) {
            activeStatsTab
        } else if (availableTabs.any { it.first == settings.statsDefaultTab }) {
            settings.statsDefaultTab
        } else {
            availableTabs.first().first
        }
    }

    var showEditGoalsDialog by remember { mutableStateOf(false) }

    // Computed Book Statistics
    val totalSeriesCompleted = allBooks.count { it.status == TitleStatus.COMPLETED && it.format != TitleFormat.SINGLE }
    val totalSinglesCompleted = allBooks.count { it.status == TitleStatus.COMPLETED && it.format == TitleFormat.SINGLE }
    val totalBooksInProgress = allBooks.count { it.status == TitleStatus.READING }
    val totalVolumesRead = allBooks.sumOf { it.volumes }
    val totalChaptersRead = allBooks.sumOf { it.chapters }
    val totalWordsRead = allBooks.sumOf { it.effectiveWords }
    val totalEndingsRead = allBooks.sumOf { it.endings }
    val totalCompletedWebNovels = allBooks.count { it.status == TitleStatus.COMPLETED && (it.format == TitleFormat.WEB_NOVEL || it.format == TitleFormat.HYBRID) }

    // Computed Adaptation Statistics
    val totalAdaptationsWatched = allAdaptations.count { it.status == TitleStatus.COMPLETED }
    val totalEpisodesWatched = allAdaptations.sumOf { it.watchedEpisodes }
    val totalWatchTimeMinutes = allAdaptations.sumOf { it.watchTimeMinutes }
    val totalSeasonsCompleted = allAdaptations.sumOf { it.completedSeasons }

    // Genre Distribution Computation
    val genreCounts = remember(allBooks, allAdaptations) {
        val map = mutableMapOf<String, Int>()
        allBooks.forEach { b -> b.genres.forEach { g -> map[g] = (map[g] ?: 0) + 1 } }
        allAdaptations.forEach { a -> a.genres.forEach { g -> map[g] = (map[g] ?: 0) + 1 } }
        map.toList().sortedByDescending { it.second }
    }

    val topBooks = remember(allBooks) {
        allBooks.sortedByDescending { it.effectiveWords }.take(5)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (settings.headerEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Статистика",
                            style = if (settings.uniformHeadersEnabled) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Stats sub-tabs
                if (availableTabs.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableTabs.forEach { (key, label) ->
                            val isSelected = activeTab == key
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { viewModel.updateAppSettings(settings.copy(statsActiveTab = key)) }
                                    .testTag("stats_tab_$key"),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = CircleShape,
                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Yearly Goals Card (if enabled)
            if (settings.goalsEnabled && settings.statsShowYearlyGoals) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("yearly_goals_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (settings.statsShowGoalsTrophy) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = StarGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = "ЦЕЛИ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(
                                onClick = { showEditGoalsDialog = true },
                                contentPadding = PaddingValues(start = 8.dp, end = 0.dp, top = 2.dp, bottom = 2.dp)
                            ) {
                                Text(
                                    text = "Изменить",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        // 1. Words Goal Progress
                        if (settings.statsGoalShowWords) {
                            val wordsProgress = if (settings.wordsTarget > 0) (totalWordsRead.toFloat() / settings.wordsTarget).coerceIn(0f, 1f) else 0f
                            GoalProgressBar(
                                title = "Прочитать слов",
                                current = Formatters.formatNumber(totalWordsRead, shorten = settings.shortenNumbers),
                                target = Formatters.formatNumber(settings.wordsTarget, shorten = settings.shortenNumbers),
                                progress = wordsProgress,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // 2. Volumes Goal Progress
                        if (settings.statsGoalShowVolumes) {
                            val volumesProgress = if (settings.volumesTarget > 0) (totalVolumesRead.toFloat() / settings.volumesTarget).coerceIn(0f, 1f) else 0f
                            GoalProgressBar(
                                title = "Прочитать томов",
                                current = "$totalVolumesRead",
                                target = "${settings.volumesTarget}",
                                progress = volumesProgress,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // 3. Series Goal Progress
                        if (settings.statsGoalShowSeries) {
                            val seriesProgress = if (settings.seriesTarget > 0) (totalSeriesCompleted.toFloat() / settings.seriesTarget).coerceIn(0f, 1f) else 0f
                            GoalProgressBar(
                                title = "Завершить серий",
                                current = "$totalSeriesCompleted",
                                target = "${settings.seriesTarget}",
                                progress = seriesProgress,
                                color = StarGold
                            )
                        }

                        // 3.1. Singles Goal Progress (if enabled)
                        if (settings.statsGoalShowSingles && settings.statsShowSinglesCompleted && settings.singlesTarget > 0) {
                            val singlesProgress = (totalSinglesCompleted.toFloat() / settings.singlesTarget).coerceIn(0f, 1f)
                            GoalProgressBar(
                                title = "Завершить синглов",
                                current = "$totalSinglesCompleted",
                                target = "${settings.singlesTarget}",
                                progress = singlesProgress,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // 4. Web Novels Goal Progress
                        if (settings.statsGoalShowWeb) {
                            val webProgress = if (settings.webTarget > 0) (totalCompletedWebNovels.toFloat() / settings.webTarget).coerceIn(0f, 1f) else 0f
                            GoalProgressBar(
                                title = "Завершить веб",
                                current = "$totalCompletedWebNovels",
                                target = "${settings.webTarget}",
                                progress = webProgress,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }

            // Wide Full-Width Words Card (available in any mode under blocks)
            if (settings.statsShowWords) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stats_words_wide_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ПРОЧИТАНО СЛОВ",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (settings.wordsTarget > 0) {
                                val percent = ((totalWordsRead.toDouble() / settings.wordsTarget.toDouble()) * 100).toInt()
                                Text(
                                    text = "$percent% от цели",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${Formatters.formatNumber(totalWordsRead, shorten = settings.shortenNumbers)} слов",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!settings.hideWordsEquivalent) {
                            val approxVolumes = String.format("%.1f", totalWordsRead.toDouble() / 60_000.0)
                            Text(
                                text = "Эквивалент ~$approxVolumes стандартных печатных томов (по ~60 тыс. слов)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (settings.wordsTarget > 0) {
                            val wordsProgress = (totalWordsRead.toFloat() / settings.wordsTarget).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { wordsProgress },
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

            // Bento Metric Grid: Books (Series completed on LEFT, Volumes read on RIGHT)
            if (activeTab == "ALL" || activeTab == "BOOKS") {
                val bookCards = buildList<@Composable (Modifier) -> Unit> {
                    if (settings.statsShowTitlesCompleted) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.CheckCircle,
                                title = "Завершено серий",
                                value = "$totalSeriesCompleted",
                                color = StarGold
                            )
                        }
                    }
                    if (settings.statsShowSinglesCompleted) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.Book,
                                title = "Синглов завершено",
                                value = "$totalSinglesCompleted",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (settings.statsShowVolumes) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.CollectionsBookmark,
                                title = "Томов прочитано",
                                value = "$totalVolumesRead",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    if (settings.statsShowWebChapters) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.Language,
                                title = "Завершено веб",
                                value = "$totalCompletedWebNovels",
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                    if (settings.vnEnabled && settings.statsShowVnEndings && totalEndingsRead > 0) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.SportsEsports,
                                title = "Пройдено концовок VN",
                                value = "$totalEndingsRead концовок",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (bookCards.isNotEmpty()) {
                    Text(
                        text = "Чтение книг и новелл",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    bookCards.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (pair.size == 2) {
                                pair[0](Modifier.weight(1f))
                                pair[1](Modifier.weight(1f))
                            } else {
                                pair[0](Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }

            // Bento Metric Grid: Adaptations
            if (settings.adaptationsEnabled && (activeTab == "ALL" || activeTab == "ADAPTATIONS")) {
                val adaptationCards = buildList<@Composable (Modifier) -> Unit> {
                    if (settings.statsShowWatchTime) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.Schedule,
                                title = "Время просмотра",
                                value = Formatters.formatDuration(totalWatchTimeMinutes),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    if (settings.statsShowEpisodes) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.Tv,
                                title = "Серий просмотрено",
                                value = "$totalEpisodesWatched",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    if (settings.statsShowSeasons) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.Movie,
                                title = "Сезонов завершено",
                                value = "$totalSeasonsCompleted",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (settings.statsShowAdaptationsCompleted) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.DoneAll,
                                title = "Завершено проектов",
                                value = "$totalAdaptationsWatched",
                                color = StarGold
                            )
                        }
                    }
                }

                if (adaptationCards.isNotEmpty()) {
                    Text(
                        text = "Просмотр экранизаций",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    adaptationCards.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (pair.size == 2) {
                                pair[0](Modifier.weight(1f))
                                pair[1](Modifier.weight(1f))
                            } else {
                                pair[0](Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }

            // Genre Distribution Section (Donut / Radar Choice)
            if (settings.statsShowGenreDistribution && genreCounts.isNotEmpty()) {
                val chartType = settings.genreChartType
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stats_genre_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header with Title and Total Count Badge (chart mode chosen in Settings)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (chartType == "RADAR") Icons.Default.Hub else Icons.Default.PieChart,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Распределение по жанрам",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = "${genreCounts.size} ${PluralRu.form(genreCounts.size.toLong(), "жанр", "жанра", "жанров")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    maxLines = 1
                                )
                            }
                        }

                        // Chart Body with AnimatedContent
                        AnimatedContent(
                            targetState = chartType,
                            transitionSpec = {
                                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                            },
                            label = "GenreChartAnimation"
                        ) { type ->
                            if (type == "RADAR") {
                                GenreRadarChart(
                                    genreCounts = genreCounts,
                                    showCounts = settings.statsRadarShowItemCounts
                                )
                            } else {
                                GenreDonutChart(
                                    genreCounts = genreCounts,
                                    showCounts = settings.statsRadarShowItemCounts
                                )
                            }
                        }
                    }
                }
            }

            // Top 5 Books by Volume / Words
            if (settings.statsShowTopBooks && topBooks.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Топ-5 по объёму чтения",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        topBooks.forEachIndexed { index, book ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openBookDetails(book.id) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (index == 0) StarGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (index == 0) StarGold else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${Formatters.formatNumber(book.effectiveWords, shorten = settings.shortenNumbers)} слов (${book.progressDisplay})",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Goals Dialog
    if (showEditGoalsDialog) {
        var goalWordsInput by remember { mutableStateOf(settings.wordsTarget.toString()) }
        var goalVolumesInput by remember { mutableStateOf(settings.volumesTarget.toString()) }
        var goalSeriesInput by remember { mutableStateOf(settings.seriesTarget.toString()) }
        var goalSinglesInput by remember { mutableStateOf(settings.singlesTarget.toString()) }
        var goalWebInput by remember { mutableStateOf(settings.webTarget.toString()) }

        AlertDialog(
            onDismissRequest = { showEditGoalsDialog = false },
            title = {
                Text(
                    text = "Настройка целей",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (settings.statsGoalShowWords) {
                        OutlinedTextField(
                            value = goalWordsInput,
                            onValueChange = { goalWordsInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Цель по словам (например, 10000000)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (settings.statsGoalShowVolumes) {
                        OutlinedTextField(
                            value = goalVolumesInput,
                            onValueChange = { goalVolumesInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Цель по томам (например, 50)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (settings.statsGoalShowSeries) {
                        OutlinedTextField(
                            value = goalSeriesInput,
                            onValueChange = { goalSeriesInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Цель по сериям (например, 15)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (settings.statsGoalShowSingles && settings.statsShowSinglesCompleted) {
                        OutlinedTextField(
                            value = goalSinglesInput,
                            onValueChange = { goalSinglesInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Цель по синглам (например, 10)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (settings.statsGoalShowWeb) {
                        OutlinedTextField(
                            value = goalWebInput,
                            onValueChange = { goalWebInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Цель по веб-новеллам (например, 10)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = goalWordsInput.toLongOrNull() ?: 10_000_000L
                        val v = goalVolumesInput.toIntOrNull() ?: 50
                        val s = goalSeriesInput.toIntOrNull() ?: 15
                        val single = goalSinglesInput.toIntOrNull() ?: 10
                        val web = goalWebInput.toIntOrNull() ?: 10
                        viewModel.updateAppSettings(
                            settings.copy(
                                wordsTarget = w,
                                volumesTarget = v,
                                seriesTarget = s,
                                singlesTarget = single,
                                webTarget = web
                            )
                        )
                        showEditGoalsDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalsDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun GoalProgressBar(
    title: String,
    current: String,
    target: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = "$current / $target (${(progress * 100).toInt()}%)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

@Composable
fun BentoStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GenreDonutChart(
    genreCounts: List<Pair<String, Int>>,
    showCounts: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (genreCounts.isEmpty()) return

    val totalTags = remember(genreCounts) { genreCounts.sumOf { it.second }.coerceAtLeast(1) }
    
    val palette = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald
        Color(0xFF8B5CF6), // Purple
        Color(0xFFF59E0B), // Amber
        Color(0xFFEC4899), // Pink
        Color(0xFF06B6D4), // Cyan
        Color(0xFFF97316), // Orange
        Color(0xFF6366F1)  // Indigo
    )

    val topLimit = 5
    val topGenres = remember(genreCounts) { genreCounts.take(topLimit) }
    val otherGenres = remember(genreCounts) { genreCounts.drop(topLimit) }
    val othersCount = remember(otherGenres) { otherGenres.sumOf { it.second } }
    val hasOthers = othersCount > 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Row: Left Donut, Right Genre Legend List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left: Donut with Total Count in Center
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                val strokeWidth = 14.dp

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasSize = size.minDimension
                    val arcSize = Size(canvasSize - strokeWidth.toPx(), canvasSize - strokeWidth.toPx())
                    val topLeft = Offset(strokeWidth.toPx() / 2f, strokeWidth.toPx() / 2f)

                    // 1. Subtle background track ring
                    drawArc(
                        color = Color.White.copy(alpha = 0.07f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )

                    // 2. Arcs for top genres with micro gap separation
                    var currentAngle = -90f
                    val slices = buildList {
                        topGenres.forEachIndexed { i, pair ->
                            add(Triple(pair.first, pair.second, palette[i % palette.size]))
                        }
                        if (hasOthers) {
                            add(Triple("Другие", othersCount, Color(0xFF64748B)))
                        }
                    }

                    val gapAngle = if (slices.size > 1) 3f else 0f

                    slices.forEach { (_, count, color) ->
                        val rawSweep = (count.toFloat() / totalTags) * 360f
                        val sweep = (rawSweep - gapAngle).coerceAtLeast(1.5f)

                        drawArc(
                            color = color,
                            startAngle = currentAngle + (gapAngle / 2f),
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                        currentAngle += rawSweep
                    }
                }

                // Center Typography
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$totalTags",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "всего",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Genre List
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                topGenres.forEachIndexed { index, (genre, count) ->
                    val color = palette[index % palette.size]
                    val percent = ((count.toFloat() / totalTags) * 100).toInt().coerceAtLeast(1)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (showCounts) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = color.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                if (hasOthers) {
                    val othersPercent = ((othersCount.toFloat() / totalTags) * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Другие (${otherGenres.size})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (showCounts) {
                                Text(
                                    text = "$othersCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF64748B).copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "$othersPercent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Proportional Segmented Progress Strip
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                topGenres.forEachIndexed { i, (_, count) ->
                    val weight = count.toFloat() / totalTags
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(weight.coerceAtLeast(0.01f))
                            .background(palette[i % palette.size])
                    )
                }
                if (hasOthers) {
                    val otherWeight = othersCount.toFloat() / totalTags
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(otherWeight.coerceAtLeast(0.01f))
                            .background(Color(0xFF64748B))
                    )
                }
            }
        }
    }
}

@Composable
fun GenreRadarChart(
    genreCounts: List<Pair<String, Int>>,
    showCounts: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (genreCounts.isEmpty()) return

    val totalTags = remember(genreCounts) { genreCounts.sumOf { it.second }.coerceAtLeast(1) }
    val displayGenres = remember(genreCounts) { genreCounts.take(6) }
    val maxCount = remember(displayGenres) { displayGenres.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1 }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (displayGenres.size < 3) {
            // Elegant linear progress rows for 1 or 2 genres
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayGenres.forEachIndexed { _, (genre, count) ->
                    val percent = ((count.toFloat() / totalTags) * 100).toInt()
                    val progress = (count.toFloat() / maxCount).coerceIn(0f, 1f)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, outlineVariantColor.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Text(
                                    text = if (showCounts) "$count отм. ($percent%)" else "$percent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(CircleShape),
                                color = primaryColor,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        } else {
            // High-fidelity, Modern Radial Spider Radar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val n = displayGenres.size
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = (min(size.width, size.height) / 2f) - 36.dp.toPx()
                    if (maxRadius <= 0f) return@Canvas

                    val angleStep = (2 * PI / n).toFloat()
                    val startAngle = (-PI / 2).toFloat() // 12 o'clock

                    // 1. Concentric spider-web levels (4 levels: 25%, 50%, 75%, 100%)
                    val levels = 4
                    for (level in 1..levels) {
                        val levelRadius = maxRadius * (level.toFloat() / levels)
                        val gridPath = Path()
                        for (i in 0 until n) {
                            val angle = startAngle + i * angleStep
                            val x = center.x + levelRadius * cos(angle)
                            val y = center.y + levelRadius * sin(angle)
                            if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                        }
                        gridPath.close()

                        drawPath(
                            path = gridPath,
                            color = outlineVariantColor.copy(alpha = if (level == levels) 0.4f else 0.18f),
                            style = Stroke(
                                width = if (level == levels) 1.2.dp.toPx() else 0.8.dp.toPx()
                            )
                        )
                    }

                    // 2. Radial spokes from center to outer vertices
                    for (i in 0 until n) {
                        val angle = startAngle + i * angleStep
                        val endX = center.x + maxRadius * cos(angle)
                        val endY = center.y + maxRadius * sin(angle)
                        drawLine(
                            color = outlineVariantColor.copy(alpha = 0.22f),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 0.8.dp.toPx()
                        )
                    }

                    // Center subtle anchor point
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.5f),
                        radius = 2.5.dp.toPx(),
                        center = center
                    )

                    // 3. Data Radar Polygon (Filled Shape + Stroke)
                    val dataPath = Path()
                    val points = mutableListOf<Offset>()

                    for (i in 0 until n) {
                        val (_, count) = displayGenres[i]
                        val fraction = (count.toFloat() / maxCount).coerceIn(0.15f, 1f)
                        val r = maxRadius * fraction
                        val angle = startAngle + i * angleStep
                        val pt = Offset(center.x + r * cos(angle), center.y + r * sin(angle))
                        points.add(pt)
                        if (i == 0) dataPath.moveTo(pt.x, pt.y) else dataPath.lineTo(pt.x, pt.y)
                    }
                    dataPath.close()

                    // Gradient filled polygon
                    drawPath(
                        path = dataPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                primaryColor.copy(alpha = 0.12f)
                            ),
                            center = center,
                            radius = maxRadius
                        ),
                        style = Fill
                    )

                    // Outer stroke
                    drawPath(
                        path = dataPath,
                        color = primaryColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Vertex marker dots
                    points.forEach { pt ->
                        drawCircle(
                            color = surfaceColor,
                            radius = 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 2.8.dp.toPx(),
                            center = pt
                        )
                    }

                    // 4. Vertex Labels (Positioned cleanly outside the chart)
                    for (i in 0 until n) {
                        val (genreName, count) = displayGenres[i]
                        val percent = ((count.toFloat() / totalTags) * 100).toInt()
                        val angle = startAngle + i * angleStep
                        val labelDistance = maxRadius + 15.dp.toPx()
                        val anchorX = center.x + labelDistance * cos(angle)
                        val anchorY = center.y + labelDistance * sin(angle)

                        val truncatedName = if (genreName.length > 10) genreName.take(9) + "…" else genreName
                        val labelText = if (showCounts) "$truncatedName ($count)" else "$truncatedName ($percent%)"

                        val textLayout = textMeasurer.measure(
                            text = labelText,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurfaceColor
                            )
                        )

                        val tw = textLayout.size.width
                        val th = textLayout.size.height

                        val cosA = cos(angle)
                        val sinA = sin(angle)

                        val posX = when {
                            cosA > 0.2f -> anchorX
                            cosA < -0.2f -> anchorX - tw
                            else -> anchorX - tw / 2f
                        }

                        val posY = when {
                            sinA > 0.2f -> anchorY
                            sinA < -0.2f -> anchorY - th
                            else -> anchorY - th / 2f
                        }

                        val clampedX = posX.coerceIn(4.dp.toPx(), size.width - tw - 4.dp.toPx())
                        val clampedY = posY.coerceIn(2.dp.toPx(), size.height - th - 2.dp.toPx())

                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(clampedX, clampedY)
                        )
                    }
                }
            }
        }

        // Clean Ranked Cards below the Radar chart (list with progress)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            displayGenres.forEachIndexed { index, (genre, count) ->
                val percent = ((count.toFloat() / totalTags) * 100).toInt()
                val fraction = count.toFloat() / totalTags

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, outlineVariantColor.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = primaryColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = onSurfaceColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (showCounts) {
                                    Text(
                                        text = "$count ${PluralRu.form(count.toLong(), "книга", "книги", "книг")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onSurfaceVariantColor
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = primaryColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "$percent%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .clip(CircleShape),
                            color = primaryColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (genreCounts.size > displayGenres.size) {
                val otherCount = genreCounts.drop(displayGenres.size).sumOf { it.second }
                val otherPercent = ((otherCount.toFloat() / totalTags) * 100).toInt()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ещё ${genreCounts.size - displayGenres.size} других жанров",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariantColor
                        )
                        Text(
                            text = if (showCounts) "$otherCount отм. ($otherPercent%)" else "$otherPercent%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceVariantColor
                        )
                    }
                }
            }
        }
    }
}
