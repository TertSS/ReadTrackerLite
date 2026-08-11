package com.example.ui.screens.edit

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditAdaptationScreen(
    existingAdaptation: Adaptation?,
    viewModel: ReadTrackerViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val knownGenres by viewModel.allKnownGenres.collectAsStateWithLifecycle()

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingAdaptation != null) "Редактировать экранизацию" else "Добавить экранизацию",
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
                        onClick = {
                            if (title.trim().isEmpty()) {
                                errorMessage = "Пожалуйста, введите название экранизации"
                                return@Button
                            }

                            // Validate series
                            if (type == AdaptationType.SERIES) {
                                for (s in seasons) {
                                    if (s.totalEpisodes > 0 && s.watchedEpisodes > s.totalEpisodes) {
                                        errorMessage = "В сезоне ${s.seasonNumber} просмотрено (${s.watchedEpisodes}) больше чем всего (${s.totalEpisodes})"
                                        return@Button
                                    }
                                }
                            }

                            val adaptationToSave = (existingAdaptation ?: Adaptation(title = title.trim())).copy(
                                title = title.trim(),
                                status = status,
                                type = type,
                                seasons = if (type == AdaptationType.SERIES) seasons else emptyList(),
                                movies = if (type == AdaptationType.MOVIE) movies else emptyList(),
                                rating = rating,
                                genres = genres,
                                bookmark = bookmark.trim(),
                                droppedReason = if (status == TitleStatus.DROPPED) droppedReason.trim() else "",
                                showInReviews = showInReviews,
                                coverUrl = coverUrl.trim().ifEmpty { null }
                            )

                            viewModel.saveAdaptation(adaptationToSave)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.testTag("save_adaptation_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сохранить", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Cover URL Input & Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoverImage(
                        coverUrl = coverUrl.ifBlank { null },
                        title = title.ifBlank { "Экранизация" },
                        width = 64.dp,
                        height = 92.dp,
                        corner = 8.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Обложка", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = coverUrl,
                            onValueChange = { coverUrl = it },
                            placeholder = { Text("URL постера...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Main Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Основная информация",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = { Text("Название экранизации *") },
                        placeholder = { Text("Например: Re:Zero 2nd Season") },
                        modifier = Modifier.fillMaxWidth().testTag("input_adaptation_title"),
                        singleLine = true
                    )

                    // Type Selector: Series vs Movie
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Тип экранизации", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AdaptationType.entries.forEach { tp ->
                                val isSelected = type == tp
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { type = tp },
                                    label = { Text(tp.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                        selectedLabelColor = MaterialTheme.colorScheme.secondary
                                    )
                                )
                            }
                        }
                    }

                    // Status Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Статус просмотра", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TitleStatus.entries.forEach { st ->
                                val isSelected = status == st
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { status = st },
                                    label = { Text(st.labelAdaptation) }
                                )
                            }
                        }
                    }

                    // Dropped reason
                    AnimatedVisibility(visible = status == TitleStatus.DROPPED) {
                        OutlinedTextField(
                            value = droppedReason,
                            onValueChange = { droppedReason = it },
                            label = { Text("Причина дропа") },
                            placeholder = { Text("Почему прекратили просмотр?") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }

            // Series Breakdown Editor
            if (type == AdaptationType.SERIES) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Сезоны (${seasons.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            TextButton(
                                onClick = {
                                    val nextNum = (seasons.maxOfOrNull { it.seasonNumber } ?: 0) + 1
                                    seasons = seasons + SeasonEntry(
                                        seasonNumber = nextNum,
                                        totalEpisodes = 12,
                                        watchedEpisodes = 0,
                                        defaultEpisodeDurationMinutes = 24
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Добавить сезон")
                            }
                        }

                        seasons.forEachIndexed { index, season ->
                            val isExpanded = expandedSeasons.contains(season.seasonNumber)
                            val seasonTotalMins = season.calculateTotalSeasonDurationMinutes()
                            val seasonWatchMins = season.calculateWatchTimeMinutes()

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Сезон ${season.seasonNumber}", fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Длительность: ${Formatters.formatDuration(seasonTotalMins)} (${seasonTotalMins} мин)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (seasons.size > 1) {
                                            IconButton(onClick = {
                                                seasons = seasons.toMutableList().apply { removeAt(index) }
                                            }) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить сезон", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = season.watchedEpisodes.toString(),
                                            onValueChange = { w ->
                                                val count = w.filter { it.isDigit() }.toIntOrNull() ?: 0
                                                seasons = seasons.toMutableList().apply {
                                                    set(index, season.copy(watchedEpisodes = count))
                                                }
                                            },
                                            label = { Text("Просмотрено") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = season.totalEpisodes.toString(),
                                            onValueChange = { t ->
                                                val count = t.filter { it.isDigit() }.toIntOrNull() ?: 0
                                                seasons = seasons.toMutableList().apply {
                                                    set(index, season.copy(totalEpisodes = count))
                                                }
                                            },
                                            label = { Text("Всего серий") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = season.defaultEpisodeDurationMinutes.toString(),
                                        onValueChange = { d ->
                                            val count = d.filter { it.isDigit() }.toIntOrNull() ?: 24
                                            seasons = seasons.toMutableList().apply {
                                                set(index, season.copy(defaultEpisodeDurationMinutes = count))
                                            }
                                        },
                                        label = { Text("Общая длительность серии (мин)") },
                                        supportingText = {
                                            Text("Применяется по умолчанию для всех серий этого сезона")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Expandable toggle for episode durations customization
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                expandedSeasons = if (isExpanded) {
                                                    expandedSeasons - season.seasonNumber
                                                } else {
                                                    expandedSeasons + season.seasonNumber
                                                }
                                            },
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
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
                                                    text = if (isExpanded) "Свернуть список серий" else "Настроить серии по отдельности (${season.totalEpisodes} эп.)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }

                                            if (season.episodeDurations.isNotEmpty()) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        text = "${season.episodeDurations.size} спец.",
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
                                                text = "По умолчанию везде стоит ${season.defaultEpisodeDurationMinutes} мин. Вы можете изменить длительность конкретных серий (например, для 1-й серии задать 60 мин):",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            if (season.episodeDurations.isNotEmpty()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    TextButton(
                                                        onClick = {
                                                            seasons = seasons.toMutableList().apply {
                                                                set(index, season.copy(episodeDurations = emptyMap()))
                                                            }
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Сбросить все к ${season.defaultEpisodeDurationMinutes} мин", fontSize = 11.sp)
                                                    }
                                                }
                                            }

                                            val episodeCount = maxOf(season.totalEpisodes, if (season.watchedEpisodes > 0) season.watchedEpisodes else 1)
                                            for (epNum in 1..episodeCount) {
                                                val isCustom = season.episodeDurations.containsKey(epNum.toString())
                                                val epDuration = season.getEpisodeDuration(epNum)
                                                val isWatched = epNum <= season.watchedEpisodes

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
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                                                    val mins = newVal.filter { it.isDigit() }.toIntOrNull() ?: season.defaultEpisodeDurationMinutes
                                                                    val updatedMap = season.episodeDurations.toMutableMap().apply {
                                                                        put(epNum.toString(), mins)
                                                                    }
                                                                    seasons = seasons.toMutableList().apply {
                                                                        set(index, season.copy(episodeDurations = updatedMap))
                                                                    }
                                                                },
                                                                suffix = { Text("мин", fontSize = 11.sp) },
                                                                singleLine = true,
                                                                modifier = Modifier.width(105.dp)
                                                            )

                                                            if (isCustom) {
                                                                IconButton(
                                                                    onClick = {
                                                                        val updatedMap = season.episodeDurations.toMutableMap().apply {
                                                                            remove(epNum.toString())
                                                                        }
                                                                        seasons = seasons.toMutableList().apply {
                                                                            set(index, season.copy(episodeDurations = updatedMap))
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
                    }
                }
            } else {
                // Movies Breakdown Editor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Фильмы (${movies.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            TextButton(
                                onClick = {
                                    val nextNum = (movies.maxOfOrNull { it.movieNumber } ?: 0) + 1
                                    movies = movies + MovieEntry(
                                        movieNumber = nextNum,
                                        title = "Фильм $nextNum",
                                        isWatched = false,
                                        durationMinutes = 110
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Добавить фильм")
                            }
                        }

                        movies.forEachIndexed { index, movie ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = movie.title,
                                            onValueChange = { t ->
                                                movies = movies.toMutableList().apply {
                                                    set(index, movie.copy(title = t))
                                                }
                                            },
                                            label = { Text("Название фильма") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (movies.size > 1) {
                                            IconButton(onClick = {
                                                movies = movies.toMutableList().apply { removeAt(index) }
                                            }) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить фильм", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = movie.durationMinutes.toString(),
                                            onValueChange = { d ->
                                                val count = d.filter { it.isDigit() }.toIntOrNull() ?: 100
                                                movies = movies.toMutableList().apply {
                                                    set(index, movie.copy(durationMinutes = count))
                                                }
                                            },
                                            label = { Text("Длительность (мин)") },
                                            modifier = Modifier.width(180.dp)
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Просмотрен", style = MaterialTheme.typography.bodyMedium)
                                            Checkbox(
                                                checked = movie.isWatched,
                                                onCheckedChange = { ch ->
                                                    movies = movies.toMutableList().apply {
                                                        set(index, movie.copy(isWatched = ch))
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Rating and Bookmark
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (settings.ratingEnabled) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Моя оценка: ${Formatters.formatRating(rating, settings.ratingScale)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            StarRatingBar(
                                rating = rating,
                                scale = settings.ratingScale,
                                editable = true,
                                allowFractional = settings.fractionalRatingEnabled,
                                onRatingChanged = { rating = it }
                            )
                        }
                    }

                    // Genres Tagging Cloud
                    if (settings.genresEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Жанры", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                genres.forEach { g ->
                                    InputChip(
                                        selected = true,
                                        onClick = { genres = genres.filter { it != g } },
                                        label = { Text(g) },
                                        trailingIcon = {
                                            Icon(Icons.Default.Close, contentDescription = "Удалить", modifier = Modifier.size(14.dp))
                                        }
                                    )
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
                                    placeholder = { Text("Добавить жанр (Enter)...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        val trimmed = newGenreInput.trim()
                                        if (trimmed.isNotEmpty() && genres.none { it.equals(trimmed, ignoreCase = true) }) {
                                            genres = genres + trimmed
                                            newGenreInput = ""
                                        }
                                    }
                                ) {
                                    Text("OK")
                                }
                            }
                        }
                    }

                    if (settings.bookmarksEnabled) {
                        OutlinedTextField(
                            value = bookmark,
                            onValueChange = { bookmark = it },
                            label = { Text("Закладка (текущая серия / таймкод)") },
                            placeholder = { Text("Например: 2 сезон 7 серия 14:20") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showInReviews = !showInReviews }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Отображать в отзывах", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Отображать экранизацию во вкладке отзывов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = showInReviews,
                            onCheckedChange = { showInReviews = it },
                            modifier = Modifier.testTag("toggle_show_in_reviews")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
