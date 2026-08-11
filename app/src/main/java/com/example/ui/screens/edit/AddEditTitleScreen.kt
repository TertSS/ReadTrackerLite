package com.example.ui.screens.edit

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun AddEditTitleScreen(
    existingBook: BookTitle?,
    viewModel: ReadTrackerViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val knownGenres by viewModel.allKnownGenres.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf(existingBook?.title ?: "") }
    var author by remember { mutableStateOf(existingBook?.author ?: "") }
    var description by remember { mutableStateOf(existingBook?.description ?: "") }
    var status by remember { mutableStateOf(existingBook?.status ?: TitleStatus.READING) }
    var format by remember { mutableStateOf(existingBook?.format ?: TitleFormat.SERIES) }
    var genres by remember { mutableStateOf(existingBook?.genres ?: emptyList()) }
    var rating by remember { mutableStateOf(existingBook?.rating ?: 0f) }
    var bookmark by remember { mutableStateOf(existingBook?.bookmark ?: "") }
    var droppedReason by remember { mutableStateOf(existingBook?.droppedReason ?: "") }
    var showInReviews by remember { mutableStateOf(existingBook?.showInReviews ?: true) }
    var coverUrl by remember { mutableStateOf(existingBook?.coverUrl ?: "") }

    // Progress fields
    var words by remember { mutableStateOf(existingBook?.words?.toString() ?: "0") }
    var totalWords by remember { mutableStateOf(existingBook?.totalWords?.toString() ?: "0") }
    var volumes by remember { mutableStateOf(existingBook?.volumes?.toString() ?: "1") }
    var totalVolumes by remember { mutableStateOf(existingBook?.totalVolumes?.toString() ?: "0") }
    var isOngoing by remember { mutableStateOf(existingBook?.isOngoing ?: false) }
    var chapters by remember { mutableStateOf(existingBook?.chapters?.toString() ?: "0") }
    var totalChapters by remember { mutableStateOf(existingBook?.totalChapters?.toString() ?: "0") }
    var endings by remember { mutableStateOf(existingBook?.endings?.toString() ?: "0") }
    var totalEndings by remember { mutableStateOf(existingBook?.totalEndings?.toString() ?: "0") }
    var startVolume by remember { mutableStateOf(existingBook?.startVolume?.toString() ?: "") }
    var startChapter by remember { mutableStateOf(existingBook?.startChapter?.toString() ?: "") }

    // Detailed volume breakdown
    var hasDetailedVolumes by remember { mutableStateOf(existingBook?.hasDetailedVolumes ?: false) }
    var detailedVolumes by remember { mutableStateOf(existingBook?.detailedVolumes ?: emptyList()) }

    // Format switch warning confirmation
    var pendingFormatSwitch by remember { mutableStateOf<TitleFormat?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var newGenreInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingBook != null) "Редактировать тайтл" else "Добавить тайтл",
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
                                errorMessage = "Пожалуйста, введите название произведения"
                                return@Button
                            }

                            val wordsLong = words.toLongOrNull() ?: 0L
                            val totalWordsLong = totalWords.toLongOrNull() ?: 0L
                            val volumesInt = volumes.toIntOrNull() ?: 0
                            val totalVolumesInt = totalVolumes.toIntOrNull() ?: 0
                            val chaptersInt = chapters.toIntOrNull() ?: 0
                            val totalChaptersInt = totalChapters.toIntOrNull() ?: 0
                            val endingsInt = endings.toIntOrNull() ?: 0
                            val totalEndingsInt = totalEndings.toIntOrNull() ?: 0

                            // Validation rules from ТЗ
                            if (format == TitleFormat.VISUAL_NOVEL && totalEndingsInt > 0 && endingsInt > totalEndingsInt) {
                                errorMessage = "Пройдено концовок ($endingsInt) не может быть больше общего количества ($totalEndingsInt)"
                                return@Button
                            }

                            if ((format == TitleFormat.WEB_NOVEL || format == TitleFormat.HYBRID) && totalChaptersInt > 0 && chaptersInt > totalChaptersInt) {
                                errorMessage = "Прочитано глав ($chaptersInt) не может быть больше общего количества ($totalChaptersInt)"
                                return@Button
                            }

                            if (totalWordsLong > 0 && wordsLong > totalWordsLong) {
                                errorMessage = "Прочитано слов ($wordsLong) не может быть больше общего объема ($totalWordsLong)"
                                return@Button
                            }

                            if (totalVolumesInt > 0 && !isOngoing && volumesInt > totalVolumesInt) {
                                errorMessage = "Прочитано томов ($volumesInt) не может быть больше общего количества ($totalVolumesInt)"
                                return@Button
                            }

                            val bookToSave = (existingBook ?: BookTitle(title = title.trim())).copy(
                                title = title.trim(),
                                author = author.trim(),
                                description = description.trim(),
                                status = status,
                                format = format,
                                genres = genres,
                                rating = rating,
                                bookmark = bookmark.trim(),
                                droppedReason = if (status == TitleStatus.DROPPED) droppedReason.trim() else "",
                                showInReviews = showInReviews,
                                words = wordsLong,
                                totalWords = totalWordsLong,
                                volumes = volumesInt,
                                totalVolumes = totalVolumesInt,
                                isOngoing = isOngoing,
                                chapters = chaptersInt,
                                totalChapters = totalChaptersInt,
                                endings = endingsInt,
                                totalEndings = totalEndingsInt,
                                startVolume = startVolume.toIntOrNull(),
                                startChapter = startChapter.toIntOrNull(),
                                hasDetailedVolumes = hasDetailedVolumes,
                                detailedVolumes = detailedVolumes,
                                coverUrl = coverUrl.trim().ifEmpty { null }
                            )

                            viewModel.saveBook(bookToSave)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("save_book_btn")
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
                        title = title.ifBlank { "Книга" },
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
                            placeholder = { Text("URL изображения...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Basic Information Section
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
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = { Text("Название *") },
                        placeholder = { Text("Например: The Beginning After The End") },
                        modifier = Modifier.fillMaxWidth().testTag("input_title"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Автор / Студия") },
                        placeholder = { Text("Кто автор?") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Status and Format selection with reduced compact spacing
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Статус", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TitleStatus.entries.forEach { st ->
                                    val isSelected = status == st
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { status = st },
                                        label = { Text(st.labelBook) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Формат издания", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val availableFormats = mutableListOf(TitleFormat.SERIES, TitleFormat.NOVEL, TitleFormat.WEB_NOVEL, TitleFormat.SINGLE)
                            if (settings.hybridEnabled) availableFormats.add(TitleFormat.HYBRID)
                            if (settings.vnEnabled) availableFormats.add(TitleFormat.VISUAL_NOVEL)

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableFormats.forEach { fmt ->
                                    val isSelected = format == fmt
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (fmt != format) {
                                                val hasProgress = words != "0" || chapters != "0" || volumes != "1" || endings != "0"
                                                if (hasProgress) {
                                                    pendingFormatSwitch = fmt
                                                } else {
                                                    format = fmt
                                                }
                                            }
                                        },
                                        label = { Text(fmt.label) }
                                    )
                                }
                            }
                        }
                    }

                    // Dropped Reason (only if status is dropped)
                    AnimatedVisibility(visible = status == TitleStatus.DROPPED) {
                        OutlinedTextField(
                            value = droppedReason,
                            onValueChange = { droppedReason = it },
                            label = { Text("Причина дропа") },
                            placeholder = { Text("Почему решили остановиться?") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // Progress Section
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
                        text = "Прогресс",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    when (format) {
                        TitleFormat.VISUAL_NOVEL -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = endings,
                                    onValueChange = { endings = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Пройдено концовок") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = totalEndings,
                                    onValueChange = { totalEndings = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Всего концовок") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        TitleFormat.WEB_NOVEL -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = chapters,
                                    onValueChange = { chapters = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Прочитано глав") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = totalChapters,
                                    onValueChange = { totalChapters = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Всего глав (0 - ?)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Онгоинг (выпускается)", style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = isOngoing,
                                    onCheckedChange = { isOngoing = it }
                                )
                            }
                        }
                        TitleFormat.HYBRID -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = volumes,
                                    onValueChange = { volumes = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Прочитано томов") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = totalVolumes,
                                    onValueChange = { totalVolumes = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Всего томов") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = chapters,
                                    onValueChange = { chapters = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Веб-главы") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = totalChapters,
                                    onValueChange = { totalChapters = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Всего веб-глав") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        TitleFormat.SERIES, TitleFormat.NOVEL, TitleFormat.SINGLE -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = volumes,
                                    onValueChange = { volumes = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Прочитано томов") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = totalVolumes,
                                    onValueChange = { totalVolumes = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Всего томов (0 - ?)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Онгоинг (выпускается)", style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = isOngoing,
                                    onCheckedChange = { isOngoing = it }
                                )
                            }
                        }
                    }

                    // Words Accounting
                    if (format != TitleFormat.VISUAL_NOVEL) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = words,
                                onValueChange = { words = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Прочитано слов") },
                                modifier = Modifier.weight(1f)
                            )
                            if (settings.totalWordsEnabled) {
                                OutlinedTextField(
                                    value = totalWords,
                                    onValueChange = { totalWords = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Всего слов (0 - ?)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Start after adaptation
                    if (settings.startAfterAdaptationEnabled) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startVolume,
                                onValueChange = { startVolume = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Начальный том (после адапт.)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = startChapter,
                                onValueChange = { startChapter = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Начальная глава") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Detailed Volume Accounting Toggle & Editor
                    if (format == TitleFormat.SERIES || format == TitleFormat.SINGLE || format == TitleFormat.HYBRID) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Детальный учёт по томам", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Точные слова для каждого тома", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = hasDetailedVolumes,
                                onCheckedChange = { hasDetailedVolumes = it }
                            )
                        }

                        if (hasDetailedVolumes) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                detailedVolumes.forEachIndexed { index, vol ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Том ${vol.volumeNumber}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = vol.wordCount.toString(),
                                            onValueChange = { newW ->
                                                val count = newW.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                                detailedVolumes = detailedVolumes.toMutableList().apply {
                                                    set(index, vol.copy(wordCount = count))
                                                }
                                            },
                                            label = { Text("Слов") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        IconButton(onClick = {
                                            detailedVolumes = detailedVolumes.toMutableList().apply { removeAt(index) }
                                        }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить том", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        val nextVolNum = (detailedVolumes.maxOfOrNull { it.volumeNumber } ?: 0) + 1
                                        detailedVolumes = detailedVolumes + VolumeEntry(volumeNumber = nextVolNum, wordCount = 70_000L)
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Добавить том")
                                }
                            }
                        }
                    }
                }
            }

            // Rating, Bookmark, Genres & Options Section
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
                        text = "Оценка и теги",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    // Rating
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
                                onRatingChanged = { rating = it }
                            )
                        }
                    }

                    // Genres Tagging Cloud
                    if (settings.genresEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Жанры", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            // Existing selected genres
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                genres.forEach { g ->
                                    InputChip(
                                        selected = true,
                                        onClick = {
                                            genres = genres.filter { it != g }
                                        },
                                        label = { Text(g) },
                                        trailingIcon = {
                                            Icon(Icons.Default.Close, contentDescription = "Удалить", modifier = Modifier.size(14.dp))
                                        }
                                    )
                                }
                            }

                            // Add new genre input
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

                            // Quick add suggestions from known genres
                            val suggestions = knownGenres.filter { kg -> genres.none { it.equals(kg, ignoreCase = true) } }
                            if (suggestions.isNotEmpty()) {
                                Text("Популярные:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    suggestions.take(8).forEach { sg ->
                                        SuggestionChip(
                                            onClick = { genres = genres + sg },
                                            label = { Text(sg, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bookmark
                    if (settings.bookmarksEnabled) {
                        OutlinedTextField(
                            value = bookmark,
                            onValueChange = { bookmark = it },
                            label = { Text("Закладка (текущее место)") },
                            placeholder = { Text("Например: 1.4 глава, или Арка 3") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Description / Notes
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Описание / Личные заметки") },
                        placeholder = { Text("Ваши мысли о тайтле...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    // Show in reviews feed toggle
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
                            Text("Отображать произведение во вкладке отзывов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    // Format Change Warning Dialog
    if (pendingFormatSwitch != null) {
        AlertDialog(
            onDismissRequest = { pendingFormatSwitch = null },
            title = { Text("Сменить формат?") },
            text = {
                Text("Вы уже ввели данные прогресса. При смене формата некоторые поля могут не соответствовать новому типу. Продолжить?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        format = pendingFormatSwitch!!
                        pendingFormatSwitch = null
                    }
                ) {
                    Text("Да, сменить")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingFormatSwitch = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}
