package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReadTrackerViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: ReadTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var exportJsonText by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var importReplaceMode by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var showSettingsExportDialog by remember { mutableStateOf(false) }
    var showSettingsImportDialog by remember { mutableStateOf(false) }
    var settingsExportJsonText by remember { mutableStateOf("") }
    var settingsImportJsonText by remember { mutableStateOf("") }

    // SAF File Pickers for Export and Import (Library)
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val jsonString = viewModel.exportLibraryJson()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(jsonString.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, "Библиотека успешно экспортирована в файл!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка сохранения файла: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader(Charsets.UTF_8).readText()
                    }
                    if (!jsonString.isNullOrBlank()) {
                        importJsonText = jsonString
                        showImportDialog = true
                    } else {
                        Toast.makeText(context, "Выбран пустой файл", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка чтения файла: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // SAF File Pickers for Export and Import (Settings)
    val exportSettingsFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = viewModel.exportSettingsJson()
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Настройки успешно экспортированы в файл!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка сохранения файла настроек: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val importSettingsFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }
                if (!jsonString.isNullOrBlank()) {
                    val success = viewModel.importSettingsJson(jsonString)
                    if (success) {
                        Toast.makeText(context, "Настройки успешно импортированы!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Ошибка импорта: неверный формат файла настроек", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Выбран пустой файл", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка чтения файла настроек: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Preferences
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
                        text = "Основные настройки",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Default Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Режим библиотеки по умолчанию", style = MaterialTheme.typography.bodyMedium)
                            Text("Какой экран открывать первым", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = settings.libraryMode == LibraryMode.BOOKS,
                                onClick = { viewModel.updateAppSettings(settings.copy(libraryMode = LibraryMode.BOOKS)) },
                                label = { Text("Книги") }
                            )
                            FilterChip(
                                selected = settings.libraryMode == LibraryMode.ADAPTATIONS,
                                onClick = { viewModel.updateAppSettings(settings.copy(libraryMode = LibraryMode.ADAPTATIONS)) },
                                label = { Text("Экранизации") }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    // Rating Scale
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Шкала оценок", style = MaterialTheme.typography.bodyMedium)
                            Text("10-балльная или 5-звёздочная", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = settings.ratingScale == RatingScale.STARS_10,
                                onClick = { viewModel.updateAppSettings(settings.copy(ratingScale = RatingScale.STARS_10)) },
                                label = { Text("10 ★") }
                            )
                            FilterChip(
                                selected = settings.ratingScale == RatingScale.STARS_5,
                                onClick = { viewModel.updateAppSettings(settings.copy(ratingScale = RatingScale.STARS_5)) },
                                label = { Text("5 ★") }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    SettingToggleRow(
                        title = "Запоминать последнюю вкладку",
                        subtitle = "При повторном открытии приложения оставаться на той же вкладке (например, Отзывы)",
                        checked = settings.rememberLastTab,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(rememberLastTab = it)) }
                    )

                    SettingToggleRow(
                        title = "Единый стиль шапок и заголовков",
                        subtitle = "Одинаковое оформление и размер заголовков на всех вкладках приложения",
                        checked = settings.uniformHeadersEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(uniformHeadersEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Дробные оценки (например 7.3, 3.3)",
                        subtitle = "Разрешить ставить точные нецелые оценки при клике по звёздам",
                        checked = settings.fractionalRatingEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(fractionalRatingEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Обновленный редактор",
                        subtitle = "Использовать обновленный интерфейс добавления и редактирования тайтлов",
                        checked = settings.updatedEditorEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(updatedEditorEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Закругленные поля ввода",
                        subtitle = "Использовать плавную закругленную форму для текстовых полей ввода и диалогов",
                        checked = settings.roundedInputFields,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(roundedInputFields = it)) }
                    )
                }
            }

            // Modules & Feature Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Включение и отключение модулей",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    SettingToggleRow(
                        title = "Блок статусов в библиотеке",
                        subtitle = "Показывать кнопки фильтра «Все», «Читаю», «В планах» и т.д.",
                        checked = settings.showStatusFiltersInLibrary,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(showStatusFiltersInLibrary = it)) }
                    )

                    SettingToggleRow(
                        title = "Переключатель «Книги / Экранизации»",
                        subtitle = "Отображать переключатель между разделами книг и экранизаций в шапке библиотеки",
                        checked = settings.showLibraryModeSwitcher,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(showLibraryModeSwitcher = it)) }
                    )

                    SettingToggleRow(
                        title = "Модуль экранизаций",
                        subtitle = "Отслеживание аниме, дорам, фильмов и сериалов",
                        checked = settings.adaptationsEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(adaptationsEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Визуальные новеллы (VN)",
                        subtitle = "Поддержка формата VN и учёт концовок",
                        checked = settings.vnEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(vnEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Гибридный формат (LN + WN)",
                        subtitle = "Одновременный учёт томов и веб-глав",
                        checked = settings.hybridEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(hybridEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Жанры и теги",
                        subtitle = "Классификация и диаграмма распределения",
                        checked = settings.genresEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(genresEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Оценки произведений",
                        subtitle = "Рейтинг и подсветка звёзд",
                        checked = settings.ratingEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(ratingEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Цели",
                        subtitle = "Планирование количества прочитанных слов и томов",
                        checked = settings.goalsEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(goalsEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Закладки",
                        subtitle = "Быстрое сохранение точного места чтения/просмотра",
                        checked = settings.bookmarksEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(bookmarksEnabled = it)) }
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SettingToggleRow(
                            title = "Поиск в библиотеке",
                            subtitle = "Поисковая строка для поиска по названию, авторам и жанрам",
                            checked = settings.searchFilterEnabled,
                            onCheckedChange = { viewModel.updateAppSettings(settings.copy(searchFilterEnabled = it)) }
                        )

                        if (settings.searchFilterEnabled) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 2.dp, bottom = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Режим поиска",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilterChip(
                                        selected = settings.librarySearchMode == "SEARCH_ONLY",
                                        onClick = { viewModel.updateAppSettings(settings.copy(librarySearchMode = "SEARCH_ONLY")) },
                                        label = { Text("Только поиск") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                    FilterChip(
                                        selected = settings.librarySearchMode == "SEARCH_AND_FILTER",
                                        onClick = { viewModel.updateAppSettings(settings.copy(librarySearchMode = "SEARCH_AND_FILTER")) },
                                        label = { Text("Поиск + фильтр") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Tune,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    SettingToggleRow(
                        title = "Учёт общего объёма слов в тайтле",
                        subtitle = "Поле общего количества слов",
                        checked = settings.totalWordsEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(totalWordsEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Начинать после экранизации",
                        subtitle = "Указание начального тома или главы после просмотра",
                        checked = settings.startAfterAdaptationEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(startAfterAdaptationEnabled = it)) }
                    )

                    SettingToggleRow(
                        title = "Сокращение больших чисел",
                        subtitle = "Отображение как 150K или 1.2M вместо 1 200 000",
                        checked = settings.shortenNumbers,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(shortenNumbers = it)) }
                    )

                    SettingToggleRow(
                        title = "Отключить анимации",
                        subtitle = "Мгновенные переходы между экранами без анимационных эффектов",
                        checked = settings.disableAnimations,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(disableAnimations = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    // Card Style for items without cover
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text("Вид карточек без обложки", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Внешний вид карточек в библиотеке при отсутствии обложки", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = settings.coverlessCardStyle == "CLASSIC",
                                onClick = { viewModel.updateAppSettings(settings.copy(coverlessCardStyle = "CLASSIC")) },
                                label = { Text("Классический") }
                            )
                            FilterChip(
                                selected = settings.coverlessCardStyle == "MINIMAL",
                                onClick = { viewModel.updateAppSettings(settings.copy(coverlessCardStyle = "MINIMAL")) },
                                label = { Text("Минималистичный") }
                            )
                            FilterChip(
                                selected = settings.coverlessCardStyle == "GRADIENT",
                                onClick = { viewModel.updateAppSettings(settings.copy(coverlessCardStyle = "GRADIENT")) },
                                label = { Text("Градиентный") }
                            )
                            FilterChip(
                                selected = settings.coverlessCardStyle == "COMPACT",
                                onClick = { viewModel.updateAppSettings(settings.copy(coverlessCardStyle = "COMPACT")) },
                                label = { Text("Компактный") }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    SettingToggleRow(
                        title = "Выравнивание формата (LN/Веб)",
                        subtitle = "Выравнивать текст формата строго по вертикали с началом названия",
                        checked = settings.alignFormatWithTitle,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(alignFormatWithTitle = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    SettingToggleRow(
                        title = "Сортировка по статусу",
                        subtitle = "Сначала Читаю, затем В планах, Завершено и т.д.",
                        checked = settings.sortByStatus,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(sortByStatus = it)) }
                    )
                }
            }

            // Analytics Customization Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Отображение в аналитике",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Настройте вкладку по умолчанию и отображаемые блоки аналитики",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Default Analytics Screen / Tab
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Окно аналитики по умолчанию",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Какую вкладку открывать первой при переходе в аналитику",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = settings.statsDefaultTab == "ALL",
                                onClick = { viewModel.updateAppSettings(settings.copy(statsDefaultTab = "ALL", statsActiveTab = "ALL")) },
                                label = { Text("Общая сводка") }
                            )
                            FilterChip(
                                selected = settings.statsDefaultTab == "BOOKS",
                                onClick = { viewModel.updateAppSettings(settings.copy(statsDefaultTab = "BOOKS", statsActiveTab = "BOOKS")) },
                                label = { Text("Книги и новеллы") }
                            )
                            FilterChip(
                                selected = settings.statsDefaultTab == "ADAPTATIONS",
                                onClick = { viewModel.updateAppSettings(settings.copy(statsDefaultTab = "ADAPTATIONS", statsActiveTab = "ADAPTATIONS")) },
                                label = { Text("Экранизации") }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    Text(
                        text = "Вкладки аналитики",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    SettingToggleRow(
                        title = "Вкладка «Общая сводка»",
                        subtitle = "Сводные цели года, объём слов, распределение по жанрам",
                        checked = settings.statsShowOverviewTab,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowOverviewTab = it)) }
                    )

                    SettingToggleRow(
                        title = "Вкладка «Книги и новеллы»",
                        subtitle = "Метрики прочитанных томов, завершённых серий и концовок VN",
                        checked = settings.statsShowBooksTab,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowBooksTab = it)) }
                    )

                    SettingToggleRow(
                        title = "Вкладка «Экранизации»",
                        subtitle = "Метрики просмотренных аниме, фильмов, сериалов и сезонов",
                        checked = settings.statsShowAdaptationsTab,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowAdaptationsTab = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    Text(
                        text = "Отдельные блоки и метрики",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    SettingToggleRow(
                        title = "Блок прочитанных слов",
                        subtitle = "Широкий блок общего количества прочитанных слов",
                        checked = settings.statsShowWords,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowWords = it)) }
                    )

                    if (settings.statsShowWords) {
                        SettingToggleRow(
                            title = "Скрыть эквивалент в блоке слов",
                            subtitle = "Не показывать текстовое сравнение в томах (например, «≈ 4.5 тома») в карточке слов",
                            checked = settings.hideWordsEquivalent,
                            onCheckedChange = { viewModel.updateAppSettings(settings.copy(hideWordsEquivalent = it)) }
                        )
                    }

                    SettingToggleRow(
                        title = "Цели",
                        subtitle = "Прогресс-бары целей по словам, томам, сериям и веб-новеллам",
                        checked = settings.statsShowYearlyGoals,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowYearlyGoals = it)) }
                    )

                    SettingToggleRow(
                        title = "Прочитано томов",
                        subtitle = "Метрика общего числа прочитанных томов",
                        checked = settings.statsShowVolumes,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowVolumes = it)) }
                    )

                    SettingToggleRow(
                        title = "Завершено веб",
                        subtitle = "Количество полностью прочитанных веб-новелл",
                        checked = settings.statsShowWebChapters,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowWebChapters = it)) }
                    )

                    SettingToggleRow(
                        title = "Завершено серий",
                        subtitle = "Количество полностью прочитанных серий произведений",
                        checked = settings.statsShowTitlesCompleted,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowTitlesCompleted = it)) }
                    )

                    SettingToggleRow(
                        title = "Концовки новелл (VN)",
                        subtitle = "Количество пройденных концовок визуальных новелл",
                        checked = settings.statsShowVnEndings,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowVnEndings = it)) }
                    )

                    SettingToggleRow(
                        title = "Время просмотра",
                        subtitle = "Суммарное время просмотра экранизаций",
                        checked = settings.statsShowWatchTime,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowWatchTime = it)) }
                    )

                    SettingToggleRow(
                        title = "Просмотрено серий",
                        subtitle = "Счётчик просмотренных эпизодов",
                        checked = settings.statsShowEpisodes,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowEpisodes = it)) }
                    )

                    SettingToggleRow(
                        title = "Завершено сезонов",
                        subtitle = "Счётчик полностью просмотренных сезонов",
                        checked = settings.statsShowSeasons,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowSeasons = it)) }
                    )

                    SettingToggleRow(
                        title = "Завершено экранизаций",
                        subtitle = "Количество завершённых аниме, фильмов и сериалов",
                        checked = settings.statsShowAdaptationsCompleted,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowAdaptationsCompleted = it)) }
                    )

                    SettingToggleRow(
                        title = "Распределение по жанрам",
                        subtitle = "Круговая диаграмма распределения прочитанных жанров",
                        checked = settings.statsShowGenreDistribution,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowGenreDistribution = it)) }
                    )

                    SettingToggleRow(
                        title = "Топ книг по объёму",
                        subtitle = "Список самых объёмных прочитанных произведений",
                        checked = settings.statsShowTopBooks,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowTopBooks = it)) }
                    )
                }
            }

            // Backup & Data Export / Import
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
                        text = "Резервное копирование и данные JSON",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    // SECTION 1: LIBRARY BACKUP
                    Text(
                        text = "Библиотека (произведения, экранизации, отзывы)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Экспортируйте и импортируйте библиотеку через выбор файлов .json на вашем устройстве",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Export via SAF File Picker
                    Button(
                        onClick = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            exportFileLauncher.launch("readtracker_library_$timeStamp.json")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("export_json_file_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Экспорт библиотеки в файл JSON")
                    }

                    // Import via SAF File Picker
                    Button(
                        onClick = {
                            importFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth().testTag("import_json_file_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выбрать файл JSON библиотеки")
                    }

                    // Secondary text clipboard options for Library
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    exportJsonText = viewModel.exportLibraryJson()
                                    showExportDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("export_json_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Текст JSON", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                importJsonText = ""
                                showImportDialog = true
                            },
                            modifier = Modifier.weight(1f).testTag("import_json_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Вставить JSON", fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                    // SECTION 2: SETTINGS BACKUP
                    Text(
                        text = "Настройки приложения",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Сохраните или восстановите персональные параметры (шкалы, темы, стили карточек, цели и видимость модулей)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                exportSettingsFileLauncher.launch("readtracker_settings_$timeStamp.json")
                            },
                            modifier = Modifier.weight(1f).testTag("export_settings_file_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Экспорт настроек", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                importSettingsFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            modifier = Modifier.weight(1f).testTag("import_settings_file_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Импорт настроек", fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                settingsExportJsonText = viewModel.exportSettingsJson()
                                showSettingsExportDialog = true
                            },
                            modifier = Modifier.weight(1f).testTag("export_settings_text_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Текст настроек", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                settingsImportJsonText = ""
                                showSettingsImportDialog = true
                            },
                            modifier = Modifier.weight(1f).testTag("import_settings_text_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Вставить настройки", fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                    // Clear library option
                    OutlinedButton(
                        onClick = { showClearDataDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("clear_all_data_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Очистить библиотеку", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // About App
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "О приложении",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ReadTracker v1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Учёт чтения книг, ранобэ, веб-новелл, визуальных новелл и просмотра экранизаций со статистикой, тир-листом и отзывами.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Export JSON Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Экспорт библиотеки (JSON)") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Скопируйте JSON для резервного сохранения:")
                    OutlinedTextField(
                        value = exportJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("ReadTracker Backup", exportJsonText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Копировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Закрыть")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Импорт библиотеки") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Проверьте или вставьте JSON резервной копии:")
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{\"books\": [...], \"adaptations\": [...]}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Заменить существующие данные", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = importReplaceMode,
                            onCheckedChange = { importReplaceMode = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.importLibraryJson(importJsonText, importReplaceMode)
                            if (result.success) {
                                Toast.makeText(
                                    context,
                                    "Импортировано: ${result.books.size} книг, ${result.adaptations.size} экранизаций, ${result.reviews.size} отзывов",
                                    Toast.LENGTH_LONG
                                ).show()
                                showImportDialog = false
                            } else {
                                Toast.makeText(context, "Ошибка: ${result.errorMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Импортировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Clear Library Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Очистить библиотеку?") },
            text = {
                Text("Все книги, экранизации и отзывы будут безвозвратно удалены. Тир-лист будет очищен. Вы уверены?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                        Toast.makeText(context, "Библиотека очищена", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Очистить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Export Settings JSON Dialog
    if (showSettingsExportDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsExportDialog = false },
            title = { Text("Экспорт настроек (JSON)") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Скопируйте JSON настроек:")
                    OutlinedTextField(
                        value = settingsExportJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("ReadTracker Settings", settingsExportJsonText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Настройки скопированы в буфер обмена", Toast.LENGTH_SHORT).show()
                        showSettingsExportDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Копировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsExportDialog = false }) {
                    Text("Закрыть")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Import Settings JSON Dialog
    if (showSettingsImportDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsImportDialog = false },
            title = { Text("Импорт настроек (JSON)") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Вставьте JSON с сохранёнными настройками:")
                    OutlinedTextField(
                        value = settingsImportJsonText,
                        onValueChange = { settingsImportJsonText = it },
                        placeholder = { Text("{\"themeMode\": \"...\", \"uniformHeadersEnabled\": true, ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (settingsImportJsonText.isNotBlank()) {
                            val success = viewModel.importSettingsJson(settingsImportJsonText)
                            if (success) {
                                Toast.makeText(context, "Настройки успешно применены!", Toast.LENGTH_SHORT).show()
                                showSettingsImportDialog = false
                            } else {
                                Toast.makeText(context, "Некорректный JSON настроек", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Введите JSON строку", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Применить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsImportDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
