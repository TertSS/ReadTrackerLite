package com.example.ui.screens.edit

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ModernAddEditTitleScreen(
    existingBook: BookTitle?,
    viewModel: ReadTrackerViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val knownGenres by viewModel.allKnownGenres.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

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

    var pendingFormatSwitch by remember { mutableStateOf<TitleFormat?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var newGenreInput by remember { mutableStateOf("") }

    val handleSave = {
        if (title.trim().isEmpty()) {
            errorMessage = "Пожалуйста, введите название произведения"
        } else {
            val wordsLong = words.toLongOrNull() ?: 0L
            val totalWordsLong = totalWords.toLongOrNull() ?: 0L
            val volsInt = volumes.toIntOrNull() ?: 0
            val totalVolsInt = totalVolumes.toIntOrNull() ?: 0
            val chapsInt = chapters.toIntOrNull() ?: 0
            val totalChapsInt = totalChapters.toIntOrNull() ?: 0
            val endsInt = endings.toIntOrNull() ?: 0
            val totalEndsInt = totalEndings.toIntOrNull() ?: 0

            if (totalVolsInt > 0 && volsInt > totalVolsInt) {
                errorMessage = "Прочитано томов ($volsInt) не может быть больше общего числа ($totalVolsInt)"
            } else if (totalChapsInt > 0 && chapsInt > totalChapsInt) {
                errorMessage = "Прочитано глав ($chapsInt) не может быть больше общего числа ($totalChapsInt)"
            } else if (totalWordsLong > 0 && wordsLong > totalWordsLong) {
                errorMessage = "Прочитано слов ($wordsLong) не может быть больше общего числа ($totalWordsLong)"
            } else {
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
                    coverUrl = coverUrl.trim().ifEmpty { null },
                    words = wordsLong,
                    totalWords = totalWordsLong,
                    volumes = volsInt,
                    totalVolumes = totalVolsInt,
                    isOngoing = isOngoing,
                    chapters = chapsInt,
                    totalChapters = totalChapsInt,
                    endings = endsInt,
                    totalEndings = totalEndsInt,
                    startVolume = startVolume.toIntOrNull(),
                    startChapter = startChapter.toIntOrNull(),
                    hasDetailedVolumes = hasDetailedVolumes,
                    detailedVolumes = if (hasDetailedVolumes) detailedVolumes else emptyList()
                )

                viewModel.saveBook(bookToSave)
                Toast.makeText(context, "Тайтл сохранён", Toast.LENGTH_SHORT).show()
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
                            text = if (existingBook != null) "Редактирование тайтла" else "Новый тайтл",
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
                            modifier = Modifier.testTag("modern_save_btn"),
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
                        icon = { Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        text = { Text("Инфо", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        text = { Text("Прогресс", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
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
                                text = "Обложка и название",
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
                                    title = title.ifBlank { "Тайтл" },
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
                                        placeholder = { Text("Например: Властелин колец") },
                                        modifier = Modifier.fillMaxWidth().testTag("modern_title_input"),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = author,
                                        onValueChange = { author = it },
                                        label = { Text("Автор / Студия") },
                                        placeholder = { Text("Дж. Р. Р. Толкин") },
                                        modifier = Modifier.fillMaxWidth().testTag("modern_author_input"),
                                        singleLine = true
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = coverUrl,
                                onValueChange = { coverUrl = it },
                                label = { Text("URL ссылки на обложку") },
                                placeholder = { Text("https://example.com/cover.jpg") },
                                modifier = Modifier.fillMaxWidth().testTag("modern_cover_url_input"),
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

                    // Format Picker
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Формат издания",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            val formats = listOf(
                                TitleFormat.SERIES to "Серия томов (LN)",
                                TitleFormat.NOVEL to "Роман",
                                TitleFormat.SINGLE to "Сингл (одиночный том)",
                                TitleFormat.WEB_NOVEL to "Веб-новелла (WN)",
                                TitleFormat.HYBRID to "Гибрид (LN+WN)",
                                TitleFormat.VISUAL_NOVEL to "Визуальная новелла (VN)"
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                formats.forEach { (fmt, label) ->
                                    val isSelected = format == fmt
                                    val fmtColor = when (fmt) {
                                        TitleFormat.SERIES -> FormatSeriesColor
                                        TitleFormat.NOVEL -> FormatNovelColor
                                        TitleFormat.SINGLE -> FormatSingleColor
                                        TitleFormat.WEB_NOVEL -> FormatWebNovelColor
                                        TitleFormat.HYBRID -> FormatHybridColor
                                        TitleFormat.VISUAL_NOVEL -> FormatVisualNovelColor
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (words != "0" || chapters != "0" || volumes != "1") {
                                                pendingFormatSwitch = fmt
                                            } else {
                                                format = fmt
                                            }
                                        },
                                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(fmtColor)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = fmtColor.copy(alpha = 0.22f),
                                            selectedLabelColor = fmtColor,
                                            selectedLeadingIconColor = fmtColor
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) fmtColor else MaterialTheme.colorScheme.outlineVariant,
                                            selectedBorderColor = fmtColor,
                                            borderWidth = if (isSelected) 1.5.dp else 1.dp
                                        )
                                    )
                                }
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
                                text = "Статус чтения",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TitleStatus.entries.forEach { st ->
                                    val isSelected = status == st
                                    val statusColor = when (st) {
                                        TitleStatus.READING -> StatusReadingColor
                                        TitleStatus.COMPLETED -> StatusCompletedColor
                                        TitleStatus.PLANNED -> StatusPlannedColor
                                        TitleStatus.DROPPED -> StatusDroppedColor
                                        TitleStatus.PAUSED -> StatusPausedColor
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { status = st },
                                        label = { Text(st.labelBook, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
                                    placeholder = { Text("Скучный сюжет, слабый перевод...") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Description & Bookmark
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Описание и заметки",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = bookmark,
                                onValueChange = { bookmark = it },
                                label = { Text("Закладка / Где остановился") },
                                placeholder = { Text("Глава 45, абзац 3") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) }
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Аннотация / Описание") },
                                placeholder = { Text("Краткое содержание тайтла...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                    }
                }

                1 -> {
                    // TAB 1: Progress Tracker
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Счётчик прогресса",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                if (format != TitleFormat.VISUAL_NOVEL) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Онгоинг", style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Switch(
                                            checked = isOngoing,
                                            onCheckedChange = { isOngoing = it },
                                            modifier = Modifier.testTag("modern_ongoing_toggle")
                                        )
                                    }
                                }
                            }

                            when (format) {
                                TitleFormat.VISUAL_NOVEL -> {
                                    Text("Концовки визуальной новеллы", fontWeight = FontWeight.SemiBold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = endings,
                                            onValueChange = { endings = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Пройдено концовок") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = totalEndings,
                                            onValueChange = { totalEndings = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Всего концовок (0 - ?)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }
                                }

                                TitleFormat.WEB_NOVEL -> {
                                    Text("Главы веб-новеллы", fontWeight = FontWeight.SemiBold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = chapters,
                                            onValueChange = { chapters = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Прочитано глав") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = totalChapters,
                                            onValueChange = { totalChapters = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Всего глав (0 - ?)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }
                                }

                                TitleFormat.HYBRID -> {
                                    Text("Тома", fontWeight = FontWeight.SemiBold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = volumes,
                                            onValueChange = { volumes = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Прочитано томов") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = totalVolumes,
                                            onValueChange = { totalVolumes = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Всего томов") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    Text("Веб-главы", fontWeight = FontWeight.SemiBold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = chapters,
                                            onValueChange = { chapters = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Прочитано глав") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = totalChapters,
                                            onValueChange = { totalChapters = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Всего веб-глав") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }
                                }

                                TitleFormat.SERIES, TitleFormat.NOVEL, TitleFormat.SINGLE -> {
                                    Text("Тома", fontWeight = FontWeight.SemiBold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = volumes,
                                            onValueChange = { volumes = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Прочитано томов") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = totalVolumes,
                                            onValueChange = { totalVolumes = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("Всего томов (0 - ?)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }
                                }
                            }

                            // Words counter
                            if (format != TitleFormat.VISUAL_NOVEL && !settings.hideWordsEquivalent) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Text("Количество слов", fontWeight = FontWeight.SemiBold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = words,
                                        onValueChange = { words = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Прочитано слов") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = totalWords,
                                        onValueChange = { totalWords = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Всего слов") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    // Detailed Volume Breakdown Toggle
                    if (format == TitleFormat.SERIES || format == TitleFormat.NOVEL || format == TitleFormat.SINGLE || format == TitleFormat.HYBRID) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Потомный менеджер", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Детализировать каждый том по словам", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = hasDetailedVolumes,
                                        onCheckedChange = {
                                            hasDetailedVolumes = it
                                            if (it && detailedVolumes.isEmpty()) {
                                                detailedVolumes = listOf(VolumeEntry(volumeNumber = 1, wordCount = 50000L))
                                            }
                                        }
                                    )
                                }

                                if (hasDetailedVolumes) {
                                    detailedVolumes.forEachIndexed { index, volume ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Том ${volume.volumeNumber}", fontWeight = FontWeight.Bold)
                                                    Text("${Formatters.formatNumber(volume.wordCount)} слов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    IconButton(
                                                        onClick = {
                                                            detailedVolumes = detailedVolumes.toMutableList().also { list ->
                                                                list.removeAt(index)
                                                            }
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            val nextNum = (detailedVolumes.maxOfOrNull { it.volumeNumber } ?: 0) + 1
                                            detailedVolumes = detailedVolumes + VolumeEntry(volumeNumber = nextNum, wordCount = 50000L)
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Добавить следующий том")
                                    }
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

                            // Interactive Star Rating Slider
                            Slider(
                                value = rating,
                                onValueChange = { rating = (Math.round(it * 10) / 10f) },
                                valueRange = 0f..10f,
                                steps = 99,
                                modifier = Modifier.fillMaxWidth().testTag("modern_rating_slider")
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
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        val trimmed = newGenreInput.trim()
                                        if (trimmed.isNotEmpty() && !genres.contains(trimmed)) {
                                            genres = genres + trimmed
                                            newGenreInput = ""
                                        }
                                    }
                                ) {
                                    Text("Добавить")
                                }
                            }

                            // Popular / Known genres chips
                            val availableGenres = (knownGenres + listOf("Фэнтези", "Исекай", "Боевик", "Романтика", "Детектив", "Комедия", "Драма", "Приключения")).distinct()
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
                                onCheckedChange = { showInReviews = it },
                                modifier = Modifier.testTag("modern_show_in_reviews_toggle")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
