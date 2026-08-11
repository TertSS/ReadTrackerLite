package com.example.ui.screens.stats

import androidx.compose.animation.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReadTrackerViewModel
import com.example.utils.Formatters
import com.example.utils.PluralRu

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
    val totalBooksRead = allBooks.count { it.status == TitleStatus.COMPLETED }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Статистика",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = StarGold, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ЦЕЛИ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(onClick = { showEditGoalsDialog = true }) {
                                Text("Изменить", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 1. Words Goal Progress
                        val wordsProgress = if (settings.wordsTarget > 0) (totalWordsRead.toFloat() / settings.wordsTarget).coerceIn(0f, 1f) else 0f
                        GoalProgressBar(
                            title = "Прочитать слов",
                            current = Formatters.formatNumber(totalWordsRead, shorten = settings.shortenNumbers),
                            target = Formatters.formatNumber(settings.wordsTarget, shorten = settings.shortenNumbers),
                            progress = wordsProgress,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 2. Volumes Goal Progress
                        val volumesProgress = if (settings.volumesTarget > 0) (totalVolumesRead.toFloat() / settings.volumesTarget).coerceIn(0f, 1f) else 0f
                        GoalProgressBar(
                            title = "Прочитать томов",
                            current = "$totalVolumesRead",
                            target = "${settings.volumesTarget}",
                            progress = volumesProgress,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        // 3. Series Goal Progress
                        val seriesProgress = if (settings.seriesTarget > 0) (totalBooksRead.toFloat() / settings.seriesTarget).coerceIn(0f, 1f) else 0f
                        GoalProgressBar(
                            title = "Завершить серий",
                            current = "$totalBooksRead",
                            target = "${settings.seriesTarget}",
                            progress = seriesProgress,
                            color = StarGold
                        )

                        // 4. Web Novels Goal Progress
                        val webProgress = if (settings.webTarget > 0) (totalCompletedWebNovels.toFloat() / settings.webTarget).coerceIn(0f, 1f) else 0f
                        GoalProgressBar(
                            title = "Завершить веб",
                            current = "$totalCompletedWebNovels",
                            target = "${settings.webTarget}",
                            progress = webProgress,
                            color = MaterialTheme.colorScheme.tertiary
                        )
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

                        val approxVolumes = String.format("%.1f", totalWordsRead.toDouble() / 60_000.0)
                        Text(
                            text = "Эквивалент ~$approxVolumes стандартных печатных томов (по ~60 тыс. слов)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

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

            // Bento Metric Grid: Books
            if (activeTab == "ALL" || activeTab == "BOOKS") {
                val bookCards = buildList<@Composable (Modifier) -> Unit> {
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
                    if (settings.statsShowTitlesCompleted) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.Default.CheckCircle,
                                title = "Завершено серий",
                                value = "$totalBooksRead",
                                color = StarGold
                            )
                        }
                    }
                    if (settings.statsShowWebChapters) {
                        add { mod ->
                            BentoStatCard(
                                modifier = mod,
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                title = "Завершено веб",
                                value = "$totalCompletedWebNovels",
                                color = MaterialTheme.colorScheme.tertiary
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

            // Genre Distribution Section (Custom Donut Chart)
            if (settings.statsShowGenreDistribution && genreCounts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Распределение по жанрам",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val totalTags = genreCounts.sumOf { it.second }.coerceAtLeast(1)
                            val palette = listOf(
                                PrimaryBlue,
                                SecondaryGreen,
                                TertiaryAmber,
                                StarGold,
                                Color(0xFFE082FF),
                                Color(0xFFFF7E79),
                                Color(0xFF70DBFF)
                            )

                            // Donut Chart Canvas
                            Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(120.dp)) {
                                    var startAngle = -90f
                                    genreCounts.take(6).forEachIndexed { index, (_, count) ->
                                        val sweep = (count.toFloat() / totalTags) * 360f
                                        val color = palette[index % palette.size]
                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt),
                                            size = Size(size.width, size.height)
                                        )
                                        startAngle += sweep
                                    }
                                }

                                Text(
                                    text = "${genreCounts.size} жанр.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Legend
                            Column(
                                modifier = Modifier.padding(start = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                genreCounts.take(5).forEachIndexed { index, (genre, count) ->
                                    val color = palette[index % palette.size]
                                    val percent = ((count.toFloat() / totalTags) * 100).toInt()
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$genre ($percent%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
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
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${Formatters.formatNumber(book.effectiveWords, shorten = settings.shortenNumbers)} слов (${book.progressDisplay})",
                                        style = MaterialTheme.typography.labelSmall,
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
                    OutlinedTextField(
                        value = goalWordsInput,
                        onValueChange = { goalWordsInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Цель по словам (например, 10000000)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalVolumesInput,
                        onValueChange = { goalVolumesInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Цель по томам (например, 50)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalSeriesInput,
                        onValueChange = { goalSeriesInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Цель по сериям (например, 15)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalWebInput,
                        onValueChange = { goalWebInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Цель по веб-новеллам (например, 10)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = goalWordsInput.toLongOrNull() ?: 10_000_000L
                        val v = goalVolumesInput.toIntOrNull() ?: 50
                        val s = goalSeriesInput.toIntOrNull() ?: 15
                        val web = goalWebInput.toIntOrNull() ?: 10
                        viewModel.updateAppSettings(
                            settings.copy(
                                wordsTarget = w,
                                volumesTarget = v,
                                seriesTarget = s,
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
