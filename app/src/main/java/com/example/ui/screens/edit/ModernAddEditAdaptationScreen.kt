package com.example.ui.screens.edit

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReadTrackerViewModel
import com.example.utils.Formatters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ModernAddEditAdaptationScreen(
    existingAdaptation: Adaptation?,
    viewModel: ReadTrackerViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val knownGenres by viewModel.allKnownGenres.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    var title by remember { mutableStateOf(existingAdaptation?.title ?: "") }
    var status by remember { mutableStateOf(existingAdaptation?.status ?: TitleStatus.READING) }
    var type by remember { mutableStateOf(existingAdaptation?.type ?: AdaptationType.SERIES) }
    var coverUrl by remember { mutableStateOf(existingAdaptation?.coverUrl ?: "") }
    var rating by remember { mutableStateOf(existingAdaptation?.rating ?: 0f) }
    var bookmark by remember { mutableStateOf(existingAdaptation?.bookmark ?: "") }
    var droppedReason by remember { mutableStateOf(existingAdaptation?.droppedReason ?: "") }
    var showInReviews by remember { mutableStateOf(existingAdaptation?.showInReviews ?: true) }
    var genres by remember { mutableStateOf(existingAdaptation?.genres ?: emptyList()) }
    var newGenreInput by remember { mutableStateOf("") }
    val customAddedGenres = remember { mutableStateListOf<String>() }

    // Series Seasons state
    var expandedSeasons by remember { mutableStateOf(setOf<Int>()) }
    var seasons by remember {
        mutableStateOf(
            existingAdaptation?.seasons ?: listOf(
                SeasonEntry(
                    seasonNumber = 1,
                    totalEpisodes = 12,
                    watchedEpisodes = 0,
                    defaultEpisodeDurationMinutes = 24
                )
            )
        )
    }

    // Movies state
    var movies by remember {
        mutableStateOf(
            existingAdaptation?.movies ?: listOf(
                MovieEntry(
                    movieNumber = 1,
                    title = "Фильм 1",
                    isWatched = false,
                    durationMinutes = 110
                )
            )
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val handleSave = {
        if (title.trim().isEmpty()) {
            errorMessage = "Пожалуйста, введите название экранизации"
        } else {
            // Validate series
            var hasSeasonError = false
            if (type == AdaptationType.SERIES) {
                for (s in seasons) {
                    if (s.totalEpisodes > 0 && s.watchedEpisodes > s.totalEpisodes) {
                        errorMessage = "В сезоне ${s.seasonNumber} просмотрено (${s.watchedEpisodes}) больше чем всего (${s.totalEpisodes})"
                        hasSeasonError = true
                        break
                    }
                }
            }

            if (!hasSeasonError) {
                val adaptationToSave = (existingAdaptation ?: Adaptation(title = title.trim())).copy(
                    title = title.trim(),
                    status = status,
                    type = type,
                    seasons = if (type == AdaptationType.SERIES) seasons else emptyList(),
                    movies = if (type == AdaptationType.MOVIE) movies else emptyList(),
                    genres = genres,
                    rating = rating,
                    bookmark = bookmark.trim(),
                    droppedReason = if (status == TitleStatus.DROPPED) droppedReason.trim() else "",
                    showInReviews = showInReviews,
                    coverUrl = coverUrl.trim().ifEmpty { null }
                )

                viewModel.saveAdaptation(adaptationToSave)
                Toast.makeText(context, "Экранизация сохранена", Toast.LENGTH_SHORT).show()
                onDismiss()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (existingAdaptation != null) "Редактирование экранизации" else "Новая экранизация",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть")
                        }
                    },
                    actions = {
                        Button(
                            onClick = { handleSave() },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Сохранить", fontWeight = FontWeight.Bold)
                        }
                    }
                )

                // Segmented Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        text = { Text("Инфо", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.LiveTv, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        text = { Text(if (type == AdaptationType.SERIES) "Сезоны и серии" else "Фильмы", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.StarRate, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        text = { Text("Оценки и жанры", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: Basic Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Постер и название",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                CoverImage(
                                    coverUrl = coverUrl,
                                    title = title.ifBlank { "Экранизация" },
                                    width = 90.dp,
                                    height = 130.dp,
                                    corner = 12.dp
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = { title = it; errorMessage = null },
                                        label = { Text("Название *") },
                                        placeholder = { Text("Аниме / Сериал / Фильм") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = bookmark,
                                        onValueChange = { bookmark = it },
                                        label = { Text("Закладка / Где смотрю") },
                                        placeholder = { Text("2 сезон, 5 серия") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = coverUrl,
                                onValueChange = { coverUrl = it },
                                label = { Text("URL ссылки на постер") },
                                placeholder = { Text("https://example.com/poster.jpg") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = {
                                    if (coverUrl.isNotBlank()) {
                                        IconButton(onClick = { coverUrl = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Adaptation Type (Series vs Movie)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Тип экранизации",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val isSeries = type == AdaptationType.SERIES
                                FilterChip(
                                    selected = isSeries,
                                    onClick = { type = AdaptationType.SERIES },
                                    label = { Text("Сериал / Аниме", fontWeight = if (isSeries) FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = { Icon(Icons.Default.Tv, contentDescription = null) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AdaptationSeriesColor.copy(alpha = 0.22f),
                                        selectedLabelColor = AdaptationSeriesColor,
                                        selectedLeadingIconColor = AdaptationSeriesColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSeries,
                                        borderColor = if (isSeries) AdaptationSeriesColor else MaterialTheme.colorScheme.outlineVariant,
                                        selectedBorderColor = AdaptationSeriesColor,
                                        borderWidth = if (isSeries) 1.5.dp else 1.dp
                                    )
                                )

                                val isMovie = type == AdaptationType.MOVIE
                                FilterChip(
                                    selected = isMovie,
                                    onClick = { type = AdaptationType.MOVIE },
                                    label = { Text("Фильм / Франшиза", fontWeight = if (isMovie) FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = { Icon(Icons.Default.Theaters, contentDescription = null) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AdaptationMovieColor.copy(alpha = 0.22f),
                                        selectedLabelColor = AdaptationMovieColor,
                                        selectedLeadingIconColor = AdaptationMovieColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isMovie,
                                        borderColor = if (isMovie) AdaptationMovieColor else MaterialTheme.colorScheme.outlineVariant,
                                        selectedBorderColor = AdaptationMovieColor,
                                        borderWidth = if (isMovie) 1.5.dp else 1.dp
                                    )
                                )
                            }
                        }
                    }

                    // Status Picker
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Статус просмотра",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val appStatusColors = LocalStatusColors.current
                                TitleStatus.entries.forEach { st ->
                                    val isSelected = status == st
                                    val statusColor = when (st) {
                                        TitleStatus.READING -> appStatusColors.reading
                                        TitleStatus.COMPLETED -> appStatusColors.completed
                                        TitleStatus.PLANNED -> appStatusColors.planned
                                        TitleStatus.DROPPED -> appStatusColors.dropped
                                        TitleStatus.PAUSED -> appStatusColors.paused
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { status = st },
                                        label = { Text(st.labelAdaptation, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(statusColor)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = statusColor.copy(alpha = 0.22f),
                                            selectedLabelColor = statusColor,
                                            selectedLeadingIconColor = statusColor
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) statusColor else MaterialTheme.colorScheme.outlineVariant,
                                            selectedBorderColor = statusColor,
                                            borderWidth = if (isSelected) 1.5.dp else 1.dp
                                        )
                                    )
                                }
                            }

                            if (status == TitleStatus.DROPPED) {
                                OutlinedTextField(
                                    value = droppedReason,
                                    onValueChange = { droppedReason = it },
                                    label = { Text("Причина почему брошено") },
                                    placeholder = { Text("Плохая рисовка, скучно...") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Seasons / Movies
                    if (type == AdaptationType.SERIES) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Менеджер сезонов и серий",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                seasons.forEachIndexed { index, s ->
                                    val isExpanded = expandedSeasons.contains(s.seasonNumber)
                                    val seasonTotalMins = s.calculateTotalSeasonDurationMinutes()

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Сезон ${s.seasonNumber}",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Длительность: ${Formatters.formatDuration(seasonTotalMins)} (${seasonTotalMins} мин)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (seasons.size > 1) {
                                                    IconButton(
                                                        onClick = {
                                                            seasons = seasons.toMutableList().also { it.removeAt(index) }
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить сезон", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = "${s.watchedEpisodes}",
                                                    onValueChange = { str ->
                                                        val num = str.filter { it.isDigit() }.toIntOrNull() ?: 0
                                                        seasons = seasons.toMutableList().also { list ->
                                                            list[index] = s.copy(watchedEpisodes = num)
                                                        }
                                                    },
                                                    label = { Text("Просмотрено") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true
                                                )

                                                OutlinedTextField(
                                                    value = "${s.totalEpisodes}",
                                                    onValueChange = { str ->
                                                        val num = str.filter { it.isDigit() }.toIntOrNull() ?: 0
                                                        seasons = seasons.toMutableList().also { list ->
                                                            list[index] = s.copy(totalEpisodes = num)
                                                        }
                                                    },
                                                    label = { Text("Всего серий") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true
                                                )
                                            }

                                            OutlinedTextField(
                                                value = "${s.defaultEpisodeDurationMinutes}",
                                                onValueChange = { str ->
                                                    val num = str.filter { it.isDigit() }.toIntOrNull() ?: 24
                                                    seasons = seasons.toMutableList().also { list ->
                                                        list[index] = s.copy(defaultEpisodeDurationMinutes = num)
                                                    }
                                                },
                                                label = { Text("Общая длительность серии (мин)") },
                                                supportingText = {
                                                    Text("Применяется по умолчанию для всех серий этого сезона")
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )

                                            // Expandable toggle for episode durations customization
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        expandedSeasons = if (isExpanded) {
                                                            expandedSeasons - s.seasonNumber
                                                        } else {
                                                            expandedSeasons + s.seasonNumber
                                                        }
                                                    },
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.secondary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = if (isExpanded) "Свернуть список серий" else "Настроить серии по отдельности (${s.totalEpisodes} эп.)",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.secondary
                                                        )
                                                    }

                                                    if (s.episodeDurations.isNotEmpty()) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = MaterialTheme.colorScheme.secondaryContainer
                                                        ) {
                                                            Text(
                                                                text = "${s.episodeDurations.size} спец.",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Expanded Episode List
                                            if (isExpanded) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "По умолчанию везде стоит ${s.defaultEpisodeDurationMinutes} мин. Вы можете изменить длительность конкретных серий (например, для 1-й серии задать 48 мин):",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )

                                                    if (s.episodeDurations.isNotEmpty()) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.End
                                                        ) {
                                                            TextButton(
                                                                onClick = {
                                                                    seasons = seasons.toMutableList().also { list ->
                                                                        list[index] = s.copy(episodeDurations = emptyMap())
                                                                    }
                                                                },
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Сбросить все к ${s.defaultEpisodeDurationMinutes} мин", fontSize = 11.sp)
                                                            }
                                                        }
                                                    }

                                                    val episodeCount = maxOf(s.totalEpisodes, if (s.watchedEpisodes > 0) s.watchedEpisodes else 1)
                                                    for (epNum in 1..episodeCount) {
                                                        val isCustom = s.episodeDurations.containsKey(epNum.toString())
                                                        val epDuration = s.getEpisodeDuration(epNum)
                                                        val isWatched = epNum <= s.watchedEpisodes

                                                        Surface(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isCustom) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                                                                   else MaterialTheme.colorScheme.surfaceContainerLow,
                                                            border = if (isCustom) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)) else null
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.weight(1f, fill = false)
                                                                ) {
                                                                    Text(
                                                                        text = "Серия $epNum",
                                                                        style = MaterialTheme.typography.bodyMedium,
                                                                        fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Medium,
                                                                        color = if (isWatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    if (isWatched) {
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = "✓ просмотрено",
                                                                            style = MaterialTheme.typography.labelSmall,
                                                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                                            fontSize = 10.sp
                                                                        )
                                                                    }
                                                                    if (isCustom) {
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = "(спец-длит.)",
                                                                            style = MaterialTheme.typography.labelSmall,
                                                                            color = MaterialTheme.colorScheme.secondary,
                                                                            fontSize = 10.sp
                                                                        )
                                                                    }
                                                                }

                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    OutlinedTextField(
                                                                        value = epDuration.toString(),
                                                                        onValueChange = { newVal ->
                                                                            val mins = newVal.filter { it.isDigit() }.toIntOrNull() ?: s.defaultEpisodeDurationMinutes
                                                                            val updatedMap = s.episodeDurations.toMutableMap().apply {
                                                                                put(epNum.toString(), mins)
                                                                            }
                                                                            seasons = seasons.toMutableList().also { list ->
                                                                                list[index] = s.copy(episodeDurations = updatedMap)
                                                                            }
                                                                        },
                                                                        suffix = { Text("мин", fontSize = 11.sp) },
                                                                        singleLine = true,
                                                                        modifier = Modifier.width(105.dp)
                                                                    )

                                                                    if (isCustom) {
                                                                        IconButton(
                                                                            onClick = {
                                                                                val updatedMap = s.episodeDurations.toMutableMap().apply {
                                                                                    remove(epNum.toString())
                                                                                }
                                                                                seasons = seasons.toMutableList().also { list ->
                                                                                    list[index] = s.copy(episodeDurations = updatedMap)
                                                                                }
                                                                            },
                                                                            modifier = Modifier.size(32.dp)
                                                                        ) {
                                                                            Icon(
                                                                                Icons.Default.Close,
                                                                                contentDescription = "Сбросить",
                                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                                modifier = Modifier.size(16.dp)
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
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        val nextNum = (seasons.maxOfOrNull { it.seasonNumber } ?: 0) + 1
                                        seasons = seasons + SeasonEntry(seasonNumber = nextNum, totalEpisodes = 12, watchedEpisodes = 0, defaultEpisodeDurationMinutes = 24)
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Добавить сезон")
                                }
                            }
                        }
                    } else {
                        // Movies List
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Список фильмов / спешлов",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                movies.forEachIndexed { index, m ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = m.isWatched,
                                                        onCheckedChange = { chk ->
                                                            movies = movies.toMutableList().also { list ->
                                                                list[index] = m.copy(isWatched = chk)
                                                            }
                                                        }
                                                    )
                                                    Text(
                                                        text = if (m.isWatched) "Просмотрен" else "Не просмотрен",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (m.isWatched) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (m.isWatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                if (movies.size > 1) {
                                                    IconButton(
                                                        onClick = {
                                                            movies = movies.toMutableList().also { it.removeAt(index) }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = m.title,
                                                onValueChange = { str ->
                                                    movies = movies.toMutableList().also { list ->
                                                        list[index] = m.copy(title = str)
                                                    }
                                                },
                                                label = { Text("Название фильма / спешла") },
                                                placeholder = { Text("Фильм ${m.movieNumber}") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )

                                            OutlinedTextField(
                                                value = "${m.durationMinutes}",
                                                onValueChange = { str ->
                                                    val mins = str.filter { it.isDigit() }.toIntOrNull() ?: 0
                                                    movies = movies.toMutableList().also { list ->
                                                        list[index] = m.copy(durationMinutes = mins)
                                                    }
                                                },
                                                label = { Text("Продолжительность фильма (мин)") },
                                                placeholder = { Text("110") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        val nextNum = (movies.maxOfOrNull { it.movieNumber } ?: 0) + 1
                                        movies = movies + MovieEntry(movieNumber = nextNum, title = "Фильм $nextNum", isWatched = false, durationMinutes = 110)
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Добавить фильм")
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Rating & Genres
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Оценка: ${if (rating > 0) String.format(java.util.Locale.US, "%.1f / 10", rating) else "Без оценки"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold
                                )
                                if (rating > 0) {
                                    TextButton(onClick = { rating = 0f }) {
                                        Text("Сбросить")
                                    }
                                }
                            }

                            // Rating Slider
                            Slider(
                                value = rating,
                                onValueChange = { rating = (Math.round(it * 10) / 10f) },
                                valueRange = 0f..10f,
                                steps = 99,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick rating buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(2f, 4f, 6f, 8f, 10f).forEach { r ->
                                    FilterChip(
                                        selected = rating >= r,
                                        onClick = { rating = r },
                                        label = { Text("${r.toInt()}★") }
                                    )
                                }
                            }
                        }
                    }

                    // Genres Matrix
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Жанры и теги",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Add new tag input
                            val addGenreAction = {
                                val trimmed = newGenreInput.trim()
                                if (trimmed.isNotEmpty()) {
                                    if (!genres.any { it.equals(trimmed, ignoreCase = true) }) {
                                        genres = genres + trimmed
                                    }
                                    if (!customAddedGenres.any { it.equals(trimmed, ignoreCase = true) }) {
                                        customAddedGenres.add(trimmed)
                                    }
                                    newGenreInput = ""
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newGenreInput,
                                    onValueChange = { newGenreInput = it },
                                    placeholder = { Text("Новый жанр...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { addGenreAction() })
                                )
                                Button(
                                    onClick = { addGenreAction() }
                                ) {
                                    Text("Добавить")
                                }
                            }

                            // Known / Custom genres chips (added by user)
                            val availableGenres = (genres + customAddedGenres + knownGenres).distinct()
                            if (availableGenres.isEmpty()) {
                                Text(
                                    text = "Пока нет добавленных жанров. Введите название и нажмите «Добавить».",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableGenres.forEach { g ->
                                        val isSelected = genres.contains(g)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                genres = if (isSelected) genres - g else genres + g
                                            },
                                            label = { Text(g) },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Reviews Feed Toggle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Отображать в разделе «Отзывы»", fontWeight = FontWeight.Bold)
                                Text("Показывать в общей ленте рецензий и рекомендаций", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = showInReviews,
                                onCheckedChange = { showInReviews = it }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
