package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.viewmodel.ReadTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardEditorScreen(
    viewModel: ReadTrackerViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Local editor state
    var currentConfig by remember(settings.cardLayoutConfigJson) {
        mutableStateOf(settings.getCardLayoutConfig())
    }

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Presets, 1 = Geometry, 2 = Slots/Elements

    // Custom preset dialog states
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<CustomCardPreset?>(null) }

    val customPresets = remember(settings.customCardPresetsJson) {
        settings.getCustomPresets()
    }

    // Preview controls
    var previewModeIsAdaptation by remember { mutableStateOf(false) }
    var previewModeIsGrid by remember { mutableStateOf(true) }
    var previewHasCover by remember { mutableStateOf(true) }

    // Sample data for preview
    val sampleBook = remember(previewHasCover) {
        BookTitle(
            id = "preview_sample",
            title = "Магическая битва: Хроники Проклятий",
            author = "Гэгэ Акутами",
            format = TitleFormat.SERIES,
            status = TitleStatus.READING,
            totalVolumes = 26,
            volumes = 14,
            webChapters = 215,
            totalWords = 650000,
            words = 345000,
            rating = 8.8f,
            coverUrl = if (previewHasCover) "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80" else null,
            bookmark = "Том 14, Глава 121 (Битва в Сибуе)"
        )
    }

    val sampleAdaptation = remember(previewHasCover) {
        Adaptation(
            id = "preview_adap_sample",
            title = "Атака титанов: Финал",
            type = AdaptationType.SERIES,
            status = TitleStatus.READING,
            seasons = listOf(
                SeasonEntry(
                    seasonNumber = 1,
                    totalEpisodes = 28,
                    watchedEpisodes = 18,
                    defaultEpisodeDurationMinutes = 24
                )
            ),
            rating = 9.4f,
            coverUrl = if (previewHasCover) "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80" else null,
            bookmark = "Серия 18 (Громовое копьё)"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Конструктор карточки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Внешний вид и расположение элементов", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            currentConfig = CardLayoutConfig.DEFAULT
                            val updated = settings.copy(
                                activeCardPresetId = "STANDARD",
                                cardLayoutConfigJson = ""
                            )
                            viewModel.updateAppSettings(updated)
                            Toast.makeText(context, "Включен стандартный стиль приложения", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Сбросить к стандартному")
                    }

                    Button(
                        onClick = {
                            val isStandard = settings.activeCardPresetId == "STANDARD"
                            val updated = settings.copy(
                                cardLayoutConfigJson = currentConfig.toJson(),
                                activeCardPresetId = if (isStandard) "CUSTOM_MANUAL" else settings.activeCardPresetId
                            )
                            viewModel.updateAppSettings(updated)
                            Toast.makeText(context, "Настройки карточки сохранены!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Готово", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- STICKY / TOP LIVE PREVIEW CANVAS ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Preview Controls Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = !previewModeIsAdaptation,
                                onClick = { previewModeIsAdaptation = false },
                                label = { Text("Книга", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            )
                            FilterChip(
                                selected = previewModeIsAdaptation,
                                onClick = { previewModeIsAdaptation = true },
                                label = { Text("Экранизация", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { previewModeIsGrid = !previewModeIsGrid },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (previewModeIsGrid) Icons.Default.GridView else Icons.Default.ViewAgenda,
                                    contentDescription = "Сетка / Список",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { previewHasCover = !previewHasCover },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (previewHasCover) Icons.Default.Image else Icons.Default.HideImage,
                                    contentDescription = "С обложкой / Без",
                                    tint = if (previewHasCover) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Live Rendered Sample Card
                    Box(
                        modifier = Modifier
                            .then(
                                if (previewModeIsGrid) Modifier.widthIn(max = 210.dp) else Modifier.fillMaxWidth()
                            )
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (previewModeIsAdaptation) {
                            DynamicAdaptationCard(
                                adaptation = sampleAdaptation,
                                config = currentConfig,
                                ratingScale = settings.ratingScale,
                                ratingEnabled = settings.ratingEnabled,
                                bookmarksEnabled = settings.bookmarksEnabled,
                                isGrid = previewModeIsGrid
                            )
                        } else {
                            DynamicBookCard(
                                book = sampleBook,
                                config = currentConfig,
                                ratingScale = settings.ratingScale,
                                ratingEnabled = settings.ratingEnabled,
                                bookmarksEnabled = settings.bookmarksEnabled,
                                shortenNumbers = settings.shortenNumbers,
                                alignFormatWithTitle = settings.alignFormatWithTitle,
                                isGrid = previewModeIsGrid
                            )
                        }
                    }
                }
            }

            // Tab Navigation
            PrimaryTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Пресеты", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Форма и обложка", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Элементы и слоты", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (activeTab) {
                    0 -> {
                        // PRESETS TAB
                        item {
                            Text(
                                text = "Режим оформления карточек",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Выберите стандартный стиль библиотеки, созданный вами пресет или готовый системный шаблон",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // STANDARD APP STYLE
                        item {
                            PresetCard(
                                title = "Стандартный стиль приложения",
                                description = "Классический вид библиотеки с поддержкой стилей без обложки (Компактный, Градиент, Контурный и др.).",
                                icon = Icons.Default.Dashboard,
                                isSelected = settings.activeCardPresetId == "STANDARD",
                                onClick = {
                                    currentConfig = CardLayoutConfig.DEFAULT
                                    viewModel.updateAppSettings(
                                        settings.copy(
                                            activeCardPresetId = "STANDARD",
                                            cardLayoutConfigJson = ""
                                        )
                                    )
                                    Toast.makeText(context, "Включен стандартный стиль библиотеки", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // CUSTOM USER PRESETS SECTION
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Мои созданные стили",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (customPresets.isNotEmpty()) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                            Text(
                                                text = "${customPresets.size}",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }

                                FilledTonalButton(
                                    onClick = {
                                        presetNameInput = "Мой стиль ${customPresets.size + 1}"
                                        showSavePresetDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Сохранить текущий", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        if (customPresets.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "Вы пока не создали своих стилей",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Настройте параметры карточки во вкладках «Геометрия» и «Элементы», затем нажмите кнопку «Сохранить текущий»",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(customPresets, key = { it.id }) { preset ->
                                CustomPresetCard(
                                    preset = preset,
                                    isSelected = settings.activeCardPresetId == preset.id,
                                    onClick = {
                                        currentConfig = preset.config
                                        viewModel.updateAppSettings(
                                            settings.copy(
                                                activeCardPresetId = preset.id,
                                                cardLayoutConfigJson = preset.config.toJson()
                                            )
                                        )
                                        Toast.makeText(context, "Выбран стиль: «${preset.name}»", Toast.LENGTH_SHORT).show()
                                    },
                                    onDeleteClick = {
                                        presetToDelete = preset
                                    }
                                )
                            }
                        }

                        // SYSTEM PRESETS SECTION
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Готовые шаблоны оформления",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Быстрые шаблоны с готовыми визуальными акцентами",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        item {
                            PresetCard(
                                title = "Классический постер",
                                description = "Обложка 2:3 сверху, аккуратный прогресс-бар, формат слева, статус справа вверху.",
                                icon = Icons.Default.ViewModule,
                                isSelected = settings.activeCardPresetId == "SYSTEM_CLASSIC",
                                onClick = {
                                    currentConfig = CardLayoutConfig.PRESET_CLASSIC_POSTER
                                    viewModel.updateAppSettings(
                                        settings.copy(
                                            activeCardPresetId = "SYSTEM_CLASSIC",
                                            cardLayoutConfigJson = currentConfig.toJson()
                                        )
                                    )
                                    Toast.makeText(context, "Применен шаблон: Классический постер", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        item {
                            PresetCard(
                                title = "Стеклянный арт (Full Cover)",
                                description = "Обложка заполняет всю карточку фоном со стеклянным затемнением и парящими бейджами.",
                                icon = Icons.Default.BlurOn,
                                isSelected = settings.activeCardPresetId == "SYSTEM_GLASS",
                                onClick = {
                                    currentConfig = CardLayoutConfig.PRESET_GLASS_POSTER
                                    viewModel.updateAppSettings(
                                        settings.copy(
                                            activeCardPresetId = "SYSTEM_GLASS",
                                            cardLayoutConfigJson = currentConfig.toJson()
                                        )
                                    )
                                    Toast.makeText(context, "Применен шаблон: Стеклянный арт", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        item {
                            PresetCard(
                                title = "Чистый минимализм",
                                description = "Тонкие рамки, статус точкой без фона, лаконичный прогресс и акцент на названии.",
                                icon = Icons.Default.FilterNone,
                                isSelected = settings.activeCardPresetId == "SYSTEM_MINIMAL",
                                onClick = {
                                    currentConfig = CardLayoutConfig.PRESET_MINIMAL
                                    viewModel.updateAppSettings(
                                        settings.copy(
                                            activeCardPresetId = "SYSTEM_MINIMAL",
                                            cardLayoutConfigJson = currentConfig.toJson()
                                        )
                                    )
                                    Toast.makeText(context, "Применен шаблон: Чистый минимализм", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        item {
                            PresetCard(
                                title = "Инфо-максимум",
                                description = "Все счетчики: точные слова, рейтинг со звездой, закладка, формат и детальный прогресс.",
                                icon = Icons.Default.Analytics,
                                isSelected = settings.activeCardPresetId == "SYSTEM_INFO_RICH",
                                onClick = {
                                    currentConfig = CardLayoutConfig.PRESET_INFO_RICH
                                    viewModel.updateAppSettings(
                                        settings.copy(
                                            activeCardPresetId = "SYSTEM_INFO_RICH",
                                            cardLayoutConfigJson = currentConfig.toJson()
                                        )
                                    )
                                    Toast.makeText(context, "Применен шаблон: Инфо-максимум", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        item {
                            PresetCard(
                                title = "Компактная сетка",
                                description = "Узкий 16:9 баннер обложки, компактная высота — максимальное количество тайтлов на экране.",
                                icon = Icons.Default.ViewCompact,
                                isSelected = settings.activeCardPresetId == "SYSTEM_COMPACT",
                                onClick = {
                                    currentConfig = CardLayoutConfig.PRESET_COMPACT
                                    viewModel.updateAppSettings(
                                        settings.copy(
                                            activeCardPresetId = "SYSTEM_COMPACT",
                                            cardLayoutConfigJson = currentConfig.toJson()
                                        )
                                    )
                                    Toast.makeText(context, "Применен шаблон: Компактная сетка", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        item {
                            PresetCard(
                                title = "Книжная полка",
                                description = "Пропорция обложки 3:4, акцент на томах и сбалансированное отображение статуса.",
                                icon = Icons.Default.Book,
                                isSelected = settings.activeCardPresetId == "SYSTEM_BOOK_SHELF",
                                onClick = {
                                    currentConfig = CardLayoutConfig.PRESET_BOOK_SHELF
                                    viewModel.updateAppSettings(
                                        settings.copy(
                                            activeCardPresetId = "SYSTEM_BOOK_SHELF",
                                            cardLayoutConfigJson = currentConfig.toJson()
                                        )
                                    )
                                    Toast.makeText(context, "Применен шаблон: Книжная полка", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    1 -> {
                        // GEOMETRY & STYLES TAB
                        item {
                            EditorSectionCard(title = "Геометрия и стиль обложки (Сетка)") {
                                // Cover Style
                                Text("Режим отображения обложки", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = currentConfig.gridCoverStyle == "TOP",
                                        onClick = {
                                            currentConfig = currentConfig.copy(gridCoverStyle = "TOP")
                                            viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                        },
                                        label = { Text("Сверху карточки") }
                                    )
                                    FilterChip(
                                        selected = currentConfig.gridCoverStyle == "FULL_BACKGROUND",
                                        onClick = {
                                            currentConfig = currentConfig.copy(gridCoverStyle = "FULL_BACKGROUND")
                                            viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                        },
                                        label = { Text("Фоном на всю карточку") }
                                    )
                                    FilterChip(
                                        selected = currentConfig.gridCoverStyle == "COMPACT_BANNER",
                                        onClick = {
                                            currentConfig = currentConfig.copy(gridCoverStyle = "COMPACT_BANNER")
                                            viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                        },
                                        label = { Text("Компактный баннер") }
                                    )
                                    FilterChip(
                                        selected = currentConfig.gridCoverStyle == "NONE",
                                        onClick = {
                                            currentConfig = currentConfig.copy(gridCoverStyle = "NONE")
                                            viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                        },
                                        label = { Text("Скрыть обложку") }
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                // Aspect Ratio
                                Text("Пропорции обложки", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("2:3" to "2:3 (Книги)", "3:4" to "3:4 (Новеллы)", "1:1" to "1:1 (Квадрат)", "16:9" to "16:9 (Кино)", "WIDE" to "4:3 (Широкая)").forEach { (key, label) ->
                                        FilterChip(
                                            selected = currentConfig.gridCoverAspect == key,
                                            onClick = {
                                                currentConfig = currentConfig.copy(gridCoverAspect = key)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label) }
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                // Card Corner Radius
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Скругление углов карточки", style = MaterialTheme.typography.bodyMedium)
                                    Text("${currentConfig.cardCornerRadiusDp} dp", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = currentConfig.cardCornerRadiusDp.toFloat(),
                                    onValueChange = {
                                        currentConfig = currentConfig.copy(cardCornerRadiusDp = it.toInt())
                                        viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                    },
                                    valueRange = 0f..28f,
                                    steps = 13
                                )

                                // Border width
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Толщина рамки карточки", style = MaterialTheme.typography.bodyMedium)
                                    Text("${currentConfig.borderWidthDp} dp", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = currentConfig.borderWidthDp,
                                    onValueChange = {
                                        currentConfig = currentConfig.copy(borderWidthDp = it)
                                        viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                    },
                                    valueRange = 0f..2.5f,
                                    steps = 4
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                // Surface style
                                Text("Фон и поверхность карточки", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "SURFACE_LOW" to "Низкий контраст",
                                        "SURFACE_CONTAINER" to "Контейнер",
                                        "SURFACE_HIGH" to "Высокий контейнер",
                                        "GLASS_GRADIENT" to "Стекло / Неон",
                                        "OUTLINE" to "Только контур",
                                        "TONAL" to "Тональный"
                                    ).forEach { (key, label) ->
                                        FilterChip(
                                            selected = currentConfig.surfaceStyle == key,
                                            onClick = {
                                                currentConfig = currentConfig.copy(surfaceStyle = key)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label) }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            EditorSectionCard(title = "Параметры режима списка") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ширина обложки в списке", style = MaterialTheme.typography.bodyMedium)
                                    Text("${currentConfig.listCoverWidthDp} dp", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = currentConfig.listCoverWidthDp.toFloat(),
                                    onValueChange = {
                                        currentConfig = currentConfig.copy(listCoverWidthDp = it.toInt())
                                        viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                    },
                                    valueRange = 45f..95f,
                                    steps = 9
                                )

                                Text("Расположение обложки в списке", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = currentConfig.listCoverPosition == "LEFT",
                                        onClick = {
                                            currentConfig = currentConfig.copy(listCoverPosition = "LEFT")
                                            viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                        },
                                        label = { Text("Слева") }
                                    )
                                    FilterChip(
                                        selected = currentConfig.listCoverPosition == "RIGHT",
                                        onClick = {
                                            currentConfig = currentConfig.copy(listCoverPosition = "RIGHT")
                                            viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                        },
                                        label = { Text("Справа") }
                                    )
                                    FilterChip(
                                        selected = currentConfig.listCoverPosition == "NONE",
                                        onClick = {
                                            currentConfig = currentConfig.copy(listCoverPosition = "NONE")
                                            viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                        },
                                        label = { Text("Без обложки") }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // ELEMENTS & SLOTS TAB
                        item {
                            Text(
                                text = "Настройка расположения и стилей элементов",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Включайте или отключайте элементы, выбирайте их точные позиции и внешний вид",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 1. STATUS
                        item {
                            ElementSlotCard(
                                title = "Статус произведения",
                                subtitle = "Метка «Читаю», «В планах», «Завершено» и др.",
                                icon = Icons.Default.Bookmarks,
                                isVisible = currentConfig.statusVisible,
                                onVisibilityChange = {
                                    currentConfig = currentConfig.copy(statusVisible = it)
                                    viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                }
                            ) {
                                Text("Позиция на карточке", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "COVER_TOP_END" to "Сверху справа",
                                        "COVER_TOP_START" to "Сверху слева",
                                        "BODY_TOP_END" to "В шапке текста",
                                        "FOOTER_END" to "В подвале"
                                    ).forEach { (pos, label) ->
                                        FilterChip(
                                            selected = currentConfig.statusPosition == pos,
                                            onClick = {
                                                currentConfig = currentConfig.copy(statusPosition = pos)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Text("Стиль отображения", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "PILL" to "Капсула с точкой",
                                        "DOT_TEXT" to "Точка с текстом",
                                        "MINIMAL_DOT" to "Только точка",
                                        "ACCENT_BAR" to "Сплошной акцент",
                                        "OUTLINE_GLOW" to "Неоновый контур",
                                        "GLASS_FROSTED" to "Матовое стекло",
                                        "ICON_CHIP" to "Иконка с символом"
                                    ).forEach { (style, label) ->
                                        FilterChip(
                                            selected = currentConfig.statusStyle == style,
                                            onClick = {
                                                currentConfig = currentConfig.copy(statusStyle = style)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        // 2. FORMAT
                        item {
                            ElementSlotCard(
                                title = "Формат произведения",
                                subtitle = "Метка формата (Серия, LN, Веб, Фильм, Аниме)",
                                icon = Icons.Default.Category,
                                isVisible = currentConfig.formatVisible,
                                onVisibilityChange = {
                                    currentConfig = currentConfig.copy(formatVisible = it)
                                    viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                }
                            ) {
                                Text("Позиция на карточке", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "COVER_TOP_START" to "Сверху слева",
                                        "BODY_TOP_START" to "В шапке текста",
                                        "INFO_ROW" to "В инфо-строке",
                                        "FOOTER_START" to "В подвале"
                                    ).forEach { (pos, label) ->
                                        FilterChip(
                                            selected = currentConfig.formatPosition == pos,
                                            onClick = {
                                                currentConfig = currentConfig.copy(formatPosition = pos)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Text("Стиль отображения", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "BADGE" to "Бейдж с рамкой",
                                        "SOLID" to "Сплошной контейнер",
                                        "OUTLINE" to "Контурный",
                                        "PLAIN_TEXT" to "Обычный текст"
                                    ).forEach { (style, label) ->
                                        FilterChip(
                                            selected = currentConfig.formatStyle == style,
                                            onClick = {
                                                currentConfig = currentConfig.copy(formatStyle = style)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        // 3. PROGRESS BAR
                        item {
                            ElementSlotCard(
                                title = "Полоса прогресса (Progress Bar)",
                                subtitle = "Визуальный индикатор прочитанных томов или глав",
                                icon = Icons.Default.HorizontalRule,
                                isVisible = currentConfig.progressBarVisible,
                                onVisibilityChange = {
                                    currentConfig = currentConfig.copy(progressBarVisible = it)
                                    viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                }
                            ) {
                                Text("Расположение полосы", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "BOTTOM_OF_COVER" to "Внизу обложки",
                                        "BOTTOM_OF_CARD" to "В самом низу карточки",
                                        "INSIDE_BODY" to "Внутри текста"
                                    ).forEach { (pos, label) ->
                                        FilterChip(
                                            selected = currentConfig.progressBarPosition == pos,
                                            onClick = {
                                                currentConfig = currentConfig.copy(progressBarPosition = pos)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Text("Толщина полосы: ${currentConfig.progressBarHeightDp} dp", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(2, 3, 4, 6).forEach { height ->
                                        FilterChip(
                                            selected = currentConfig.progressBarHeightDp == height,
                                            onClick = {
                                                currentConfig = currentConfig.copy(progressBarHeightDp = height)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text("${height} dp", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        // 4. PROGRESS TEXT
                        item {
                            ElementSlotCard(
                                title = "Текст прогресса",
                                subtitle = "Надпись «14/26 т.» или проценты",
                                icon = Icons.Default.Numbers,
                                isVisible = currentConfig.progressVisible,
                                onVisibilityChange = {
                                    currentConfig = currentConfig.copy(progressVisible = it)
                                    viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                }
                            ) {
                                Text("Формат отображения", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "DETAILED" to "Детальный (14/26 т.)",
                                        "PERCENTAGE_ONLY" to "Только проценты (54%)",
                                        "SHORT" to "Короткий"
                                    ).forEach { (style, label) ->
                                        FilterChip(
                                            selected = currentConfig.progressStyle == style,
                                            onClick = {
                                                currentConfig = currentConfig.copy(progressStyle = style)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        // 5. WORDS COUNT
                        item {
                            ElementSlotCard(
                                title = "Счетчик прочитанных слов",
                                subtitle = "Отображение «345K сл.»",
                                icon = Icons.Default.ShortText,
                                isVisible = currentConfig.wordsVisible,
                                onVisibilityChange = {
                                    currentConfig = currentConfig.copy(wordsVisible = it)
                                    viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                }
                            ) {
                                Text("Позиция", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "INFO_ROW" to "В инфо-строке",
                                        "FOOTER_START" to "В подвале слева"
                                    ).forEach { (pos, label) ->
                                        FilterChip(
                                            selected = currentConfig.wordsPosition == pos,
                                            onClick = {
                                                currentConfig = currentConfig.copy(wordsPosition = pos)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        // 6. RATING
                        item {
                            ElementSlotCard(
                                title = "Оценка со звездой",
                                subtitle = "Отображение «★ 8.8»",
                                icon = Icons.Default.Star,
                                isVisible = currentConfig.ratingVisible,
                                onVisibilityChange = {
                                    currentConfig = currentConfig.copy(ratingVisible = it)
                                    viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                }
                            ) {
                                Text("Позиция", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "FOOTER_START" to "В подвале слева",
                                        "COVER_TOP_START" to "Сверху на обложке",
                                        "INFO_ROW" to "В инфо-строке"
                                    ).forEach { (pos, label) ->
                                        FilterChip(
                                            selected = currentConfig.ratingPosition == pos,
                                            onClick = {
                                                currentConfig = currentConfig.copy(ratingPosition = pos)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        // 7. BOOKMARK
                        item {
                            ElementSlotCard(
                                title = "Закладка",
                                subtitle = "Отображение чипа текущего места чтения",
                                icon = Icons.Default.Bookmark,
                                isVisible = currentConfig.bookmarkVisible,
                                onVisibilityChange = {
                                    currentConfig = currentConfig.copy(bookmarkVisible = it)
                                    viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                }
                            )
                        }

                        // 8. TITLE & AUTHOR
                        item {
                            EditorSectionCard(title = "Типографика заголовка и автора") {
                                Text("Размер шрифта названия", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        "SMALL" to "Компактный",
                                        "MEDIUM" to "Стандартный",
                                        "LARGE" to "Крупный"
                                    ).forEach { (size, label) ->
                                        FilterChip(
                                            selected = currentConfig.titleTextSize == size,
                                            onClick = {
                                                currentConfig = currentConfig.copy(titleTextSize = size)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text(label) }
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                Text("Максимум строк названия", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(1, 2, 3).forEach { lines ->
                                        FilterChip(
                                            selected = currentConfig.titleMaxLines == lines,
                                            onClick = {
                                                currentConfig = currentConfig.copy(titleMaxLines = lines)
                                                viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
                                            },
                                            label = { Text("$lines ${if (lines == 1) "строка" else "строки"}") }
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Отображать имя автора", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Под названием книги", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = currentConfig.authorVisible,
                                        onCheckedChange = {
                                            currentConfig = currentConfig.copy(authorVisible = it)
                                            viewModel.updateAppSettings(settings.copy(cardLayoutConfigJson = currentConfig.toJson()))
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

    // Dialog: Save current layout as custom preset
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            icon = {
                Icon(
                    Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Сохранить свой стиль", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Введите название для вашего нового пресета карточки:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Название стиля") },
                        placeholder = { Text("Например: Мой постер") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = presetNameInput.trim().ifBlank { "Мой стиль ${customPresets.size + 1}" }
                        val newPreset = CustomCardPreset(name = name, config = currentConfig)
                        val updatedList = settings.getCustomPresets() + newPreset
                        val updatedSettings = settings.copy(
                            customCardPresetsJson = CustomCardPreset.listToJson(updatedList),
                            activeCardPresetId = newPreset.id,
                            cardLayoutConfigJson = currentConfig.toJson()
                        )
                        viewModel.updateAppSettings(updatedSettings)
                        showSavePresetDialog = false
                        Toast.makeText(context, "Стиль «$name» успешно сохранён!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Dialog: Delete confirmation for custom preset
    if (presetToDelete != null) {
        val target = presetToDelete!!
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            icon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Удалить стиль?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить пресет «${target.name}»? Это действие нельзя отменить.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentList = settings.getCustomPresets()
                        val updatedList = currentList.filter { it.id != target.id }
                        val wasActive = settings.activeCardPresetId == target.id
                        val updatedSettings = settings.copy(
                            customCardPresetsJson = CustomCardPreset.listToJson(updatedList),
                            activeCardPresetId = if (wasActive) "STANDARD" else settings.activeCardPresetId,
                            cardLayoutConfigJson = if (wasActive) "" else settings.cardLayoutConfigJson
                        )
                        viewModel.updateAppSettings(updatedSettings)
                        if (wasActive) {
                            currentConfig = CardLayoutConfig.DEFAULT
                        }
                        presetToDelete = null
                        Toast.makeText(context, "Стиль «${target.name}» удалён", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        "Удалить",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// --- Reusable Subcomponents for Card Editor ---

@Composable
private fun CustomPresetCard(
    preset: CustomCardPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val coverAspectStr = when (preset.config.gridCoverAspect) {
                    "POSTER_2_3" -> "2:3"
                    "BOOK_3_4" -> "3:4"
                    "BANNER_16_9" -> "16:9"
                    "SQUARE_1_1" -> "1:1"
                    else -> "Стандарт"
                }
                Text(
                    text = "Обложка: $coverAspectStr • Скругление: ${preset.config.cardCornerRadiusDp}dp • ${if (preset.config.statusVisible) "Статус" else "Без статуса"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Выбрано",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(4.dp).size(14.dp)
                    )
                }
            }

            // Cross delete icon button with alert dialog trigger
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить пресет",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PresetCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Выбрано",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(4.dp).size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun ElementSlotCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isVisible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isVisible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isVisible,
                    onCheckedChange = onVisibilityChange
                )
            }

            if (isVisible && content != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                content()
            }
        }
    }
}
