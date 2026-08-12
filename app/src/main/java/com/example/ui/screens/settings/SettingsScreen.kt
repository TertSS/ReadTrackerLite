package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReadTrackerViewModel
import com.example.utils.BackupHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var exportPayloadData by remember { mutableStateOf<BackupHelper.ExportPayload?>(null) }
    var exportJsonText by remember { mutableStateOf("") }
    var isExportingLibrary by remember { mutableStateOf(false) }
    var isExportingSettings by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var importReplaceMode by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var showSettingsExportDialog by remember { mutableStateOf(false) }
    var showSettingsImportDialog by remember { mutableStateOf(false) }
    var settingsExportJsonText by remember { mutableStateOf("") }
    var settingsImportJsonText by remember { mutableStateOf("") }
    var showCustomPaletteEditor by remember { mutableStateOf(false) }

    if (showCustomPaletteEditor) {
        CustomPaletteEditorScreen(
            viewModel = viewModel,
            onDismiss = { showCustomPaletteEditor = false }
        )
        return
    }

    // SAF File Pickers for Export and Import (Library)
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val jsonString = viewModel.exportLibraryJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(jsonString.toByteArray(Charsets.UTF_8))
                        }
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
                    val jsonString = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader(Charsets.UTF_8).readText()
                        }
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
            coroutineScope.launch {
                try {
                    val jsonString = viewModel.exportSettingsJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(jsonString.toByteArray(Charsets.UTF_8))
                        }
                    }
                    Toast.makeText(context, "Настройки успешно экспортированы в файл!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка сохранения файла настроек: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importSettingsFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val jsonString = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader(Charsets.UTF_8).readText()
                        }
                    }
                    if (!jsonString.isNullOrBlank()) {
                        val success = viewModel.importSettingsJson(jsonString)
                        if (success) {
                            Toast.makeText(context, "Настройки успешно импортированы!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Некорректный формат файла настроек", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Выбран пустой файл", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка чтения файла настроек: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
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
            // 🎨 COLOR PALETTES & THEMES SECTION
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Цветовые палитры и темы",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "5 готовых темных тем или полная настройка HEX-кодов всех цветов",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 5 Ready Dark Presets
                    PresetPalettes.forEach { palette ->
                        val isSelected = settings.activePalette == palette.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateAppSettings(settings.copy(activePalette = palette.id))
                                    Toast.makeText(context, "Применена тема: ${palette.name}", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = if (isSelected) {
                                BorderStroke(2.dp, palette.primary)
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(palette.primary)
                                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                        )
                                        Text(
                                            text = palette.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = palette.primary.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, palette.primary)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = palette.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Активна",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = palette.primary
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = palette.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Palette Color Swatches Preview Strip
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ColorDotSample(palette.background, "Фон")
                                    ColorDotSample(palette.surface, "Карточки")
                                    ColorDotSample(palette.primary, "Акцент")
                                    ColorDotSample(palette.secondary, "Вторичн.")
                                    ColorDotSample(palette.tertiary, "Третичн.")
                                    ColorDotSample(palette.statusReading, "Читаю")
                                    ColorDotSample(palette.statusPlanned, "Планы")
                                    ColorDotSample(palette.statusCompleted, "Финал")
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    // Custom HEX Palette Editor Card Entry
                    val isCustomSelected = settings.activePalette == "CUSTOM"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomPaletteEditor = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCustomSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = if (isCustomSelected) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ColorLens,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Собственная палитра (HEX редактор)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (isCustomSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(
                                            text = "Активна",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Отдельное окно для изменения цвета абсолютно любого элемента приложения по HEX-коду (общий фон, карточки, акценты, текст, рамки, кнопки и все 5 статусов).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Preview current custom colors
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ColorDotSample(parseColorHex(settings.customBackgroundHex, Color(0xFF131313)), "Фон")
                                ColorDotSample(parseColorHex(settings.customSurfaceHex, Color(0xFF131313)), "Карточка")
                                ColorDotSample(parseColorHex(settings.customPrimaryHex, Color(0xFF9ECAFF)), "Акцент")
                                ColorDotSample(parseColorHex(settings.customSecondaryHex, Color(0xFF78DC77)), "Вторичн.")
                                ColorDotSample(parseColorHex(settings.customTertiaryHex, Color(0xFFFFB77B)), "Третичн.")
                                ColorDotSample(parseColorHex(settings.customStatusReadingHex, Color(0xFF78DC77)), "Читаю")
                                ColorDotSample(parseColorHex(settings.customStatusPlannedHex, Color(0xFFFFB77B)), "Планы")
                                ColorDotSample(parseColorHex(settings.customStatusCompletedHex, Color(0xFF9ECAFF)), "Финал")
                            }

                            Button(
                                onClick = { showCustomPaletteEditor = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Открыть HEX-редактор палитры", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

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
                                onClick = { 
                                    viewModel.updateAppSettings(settings.copy(libraryMode = LibraryMode.BOOKS))
                                    viewModel.setLibraryMode(LibraryMode.BOOKS)
                                },
                                label = { Text("Книги") }
                            )
                            FilterChip(
                                selected = settings.libraryMode == LibraryMode.ADAPTATIONS,
                                onClick = { 
                                    viewModel.updateAppSettings(settings.copy(libraryMode = LibraryMode.ADAPTATIONS, adaptationsEnabled = true))
                                    viewModel.setLibraryMode(LibraryMode.ADAPTATIONS)
                                },
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
                        title = "Запоминать выбранный статус в библиотеке",
                        subtitle = "При повторном открытии приложения оставаться на выбранном статусе (например, «Читаю»)",
                        checked = settings.rememberLastStatusFilter,
                        onCheckedChange = { isEnabled ->
                            viewModel.updateAppSettings(
                                settings.copy(
                                    rememberLastStatusFilter = isEnabled,
                                    lastSelectedStatus = if (isEnabled) viewModel.selectedStatusFilter.value?.name else null
                                )
                            )
                        }
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

                    SettingToggleRow(
                        title = "Адаптация под планшеты",
                        subtitle = "Оптимизированная верстка для планшетов и больших экранов (боковая панель навигации и адаптивная сетка)",
                        checked = settings.tabletLayoutEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(tabletLayoutEnabled = it)) }
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
                        title = "Кнопка смены вида карточек",
                        subtitle = "Отображать кнопку переключения вида «сетка / список» в правом углу шапки библиотеки",
                        checked = settings.showViewModeSwitcher,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(showViewModeSwitcher = it)) }
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

                    // Library Status Bar Style
                    if (settings.showStatusFiltersInLibrary) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Text("Стиль блока статусов библиотеки", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Выберите оформление панели фильтрации по статусам", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = settings.libraryStatusBarStyle == "PILLS",
                                    onClick = { viewModel.updateAppSettings(settings.copy(libraryStatusBarStyle = "PILLS")) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Widgets,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text("Бенто-чипы") }
                                )
                                FilterChip(
                                    selected = settings.libraryStatusBarStyle == "SEGMENTED",
                                    onClick = { viewModel.updateAppSettings(settings.copy(libraryStatusBarStyle = "SEGMENTED")) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ViewAgenda,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text("Сегментная") }
                                )
                                FilterChip(
                                    selected = settings.libraryStatusBarStyle == "CARDS_COUNT",
                                    onClick = { viewModel.updateAppSettings(settings.copy(libraryStatusBarStyle = "CARDS_COUNT")) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Dashboard,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text("Карточки") }
                                )
                                FilterChip(
                                    selected = settings.libraryStatusBarStyle == "MINIMAL_LINE",
                                    onClick = { viewModel.updateAppSettings(settings.copy(libraryStatusBarStyle = "MINIMAL_LINE")) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.HorizontalRule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text("Минимал-лайн") }
                                )
                            }
                        }

                        SettingToggleRow(
                            title = "Количество тайтлов в статусах",
                            subtitle = "Отображать число тайтлов в кнопках панели статусов",
                            checked = settings.showStatusBarItemCounts,
                            onCheckedChange = { viewModel.updateAppSettings(settings.copy(showStatusBarItemCounts = it)) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }

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
                            FilterChip(
                                selected = settings.coverlessCardStyle == "OUTLINE",
                                onClick = { viewModel.updateAppSettings(settings.copy(coverlessCardStyle = "OUTLINE")) },
                                label = { Text("Контурный") }
                            )
                            FilterChip(
                                selected = settings.coverlessCardStyle == "TYPOGRAPHY",
                                onClick = { viewModel.updateAppSettings(settings.copy(coverlessCardStyle = "TYPOGRAPHY")) },
                                label = { Text("Типографика") }
                            )
                            FilterChip(
                                selected = settings.coverlessCardStyle == "TONAL",
                                onClick = { viewModel.updateAppSettings(settings.copy(coverlessCardStyle = "TONAL")) },
                                label = { Text("Тональный") }
                            )
                        }
                    }

                    // Compact tag position setting
                    if (settings.coverlessCardStyle == "COMPACT") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Text("Расположение тега формата (LN/Веб)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Где отображать тег в компактном режиме карточки", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = settings.compactTagPosition == "UNDER_STATUS",
                                    onClick = { viewModel.updateAppSettings(settings.copy(compactTagPosition = "UNDER_STATUS")) },
                                    label = { Text("Под статусом (перед томами)") }
                                )
                                FilterChip(
                                    selected = settings.compactTagPosition == "LEFT_OF_STATUS",
                                    onClick = { viewModel.updateAppSettings(settings.copy(compactTagPosition = "LEFT_OF_STATUS")) },
                                    label = { Text("Возле статуса слева") }
                                )
                            }
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

                    if (settings.statsShowYearlyGoals) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 4.dp, bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Параметры блока целей",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                SettingToggleRow(
                                    title = "Иконка кубка",
                                    subtitle = "Показывать золотой значок кубка рядом с заголовком «ЦЕЛИ»",
                                    checked = settings.statsShowGoalsTrophy,
                                    onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowGoalsTrophy = it)) }
                                )

                                SettingToggleRow(
                                    title = "Цель: Прочитать слов",
                                    subtitle = "Прогресс по суммарному количеству прочитанных слов",
                                    checked = settings.statsGoalShowWords,
                                    onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsGoalShowWords = it)) }
                                )

                                SettingToggleRow(
                                    title = "Цель: Прочитать томов",
                                    subtitle = "Прогресс по числу прочитанных томов",
                                    checked = settings.statsGoalShowVolumes,
                                    onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsGoalShowVolumes = it)) }
                                )

                                SettingToggleRow(
                                    title = "Цель: Завершить серий",
                                    subtitle = "Прогресс по завершённым книжным сериям",
                                    checked = settings.statsGoalShowSeries,
                                    onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsGoalShowSeries = it)) }
                                )

                                if (settings.statsShowSinglesCompleted) {
                                    SettingToggleRow(
                                        title = "Цель: Завершить синглов",
                                        subtitle = "Прогресс по завершённым одиночным книгам / ваншотам",
                                        checked = settings.statsGoalShowSingles,
                                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsGoalShowSingles = it)) }
                                    )
                                }

                                SettingToggleRow(
                                    title = "Цель: Завершить веб",
                                    subtitle = "Прогресс по завершённым веб-новеллам",
                                    checked = settings.statsGoalShowWeb,
                                    onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsGoalShowWeb = it)) }
                                )
                            }
                        }
                    }

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
                        title = "Завершено синглов",
                        subtitle = "Метрика завершённых одиночных книг и ваншотов (синглов)",
                        checked = settings.statsShowSinglesCompleted,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowSinglesCompleted = it)) }
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
                        subtitle = "График распределения прочитанных и просмотренных жанров",
                        checked = settings.statsShowGenreDistribution,
                        onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsShowGenreDistribution = it)) }
                    )

                    if (settings.statsShowGenreDistribution) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 2.dp, bottom = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Стиль диаграммы жанров",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Выберите тип отображения: круговая или лепестковая (радар)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = settings.genreChartType == "DONUT",
                                    onClick = { viewModel.updateAppSettings(settings.copy(genreChartType = "DONUT")) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DonutLarge,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text("Кольцевая (круговая)") }
                                )
                                FilterChip(
                                    selected = settings.genreChartType == "RADAR",
                                    onClick = { viewModel.updateAppSettings(settings.copy(genreChartType = "RADAR")) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Hub,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text("Лепестковая (радар)") }
                                )
                            }

                            SettingToggleRow(
                                title = "Количество тайтлов в жанрах",
                                subtitle = "Отображать число тайтлов в скобках на диаграмме и в списке",
                                checked = settings.statsRadarShowItemCounts,
                                onCheckedChange = { viewModel.updateAppSettings(settings.copy(statsRadarShowItemCounts = it)) }
                            )
                        }
                    }

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
                        text = "Сохраняйте и восстанавливайте библиотеку через файлы .json или прямое копирование",
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

                    // Quick Clipboard & Share actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (isExportingLibrary) return@launch
                                    isExportingLibrary = true
                                    try {
                                        val (payload, jsonString) = viewModel.getLibraryExportData()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("ReadTracker Backup", jsonString)
                                        clipboard.setPrimaryClip(clip)
                                        val sizeKb = (jsonString.toByteArray(Charsets.UTF_8).size) / 1024
                                        Toast.makeText(
                                            context,
                                            "JSON скопирован в буфер! ($sizeKb КБ • ${payload.books.size} книг, ${payload.adaptations.size} экран.)",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка копирования: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isExportingLibrary = false
                                    }
                                }
                            },
                            enabled = !isExportingLibrary,
                            modifier = Modifier.weight(1f).testTag("export_json_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            if (isExportingLibrary) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            } else {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Копировать JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (isExportingLibrary) return@launch
                                    isExportingLibrary = true
                                    try {
                                        val (_, jsonString) = viewModel.getLibraryExportData()
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, jsonString)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Отправить JSON библиотеки")
                                        context.startActivity(shareIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isExportingLibrary = false
                                    }
                                }
                            },
                            enabled = !isExportingLibrary,
                            modifier = Modifier.weight(1f).testTag("share_json_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Поделиться", fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (isExportingLibrary) return@launch
                                    isExportingLibrary = true
                                    try {
                                        val (payload, jsonString) = viewModel.getLibraryExportData()
                                        exportPayloadData = payload
                                        exportJsonText = jsonString
                                        showExportDialog = true
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isExportingLibrary = false
                                    }
                                }
                            },
                            enabled = !isExportingLibrary,
                            modifier = Modifier.weight(1f).testTag("preview_json_btn")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Просмотр JSON", fontSize = 12.sp)
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
                            Text("Экспорт в файл", fontSize = 12.sp)
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
                            Text("Импорт из файла", fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val jsonString = viewModel.exportSettingsJson()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("ReadTracker Settings", jsonString)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "JSON настроек скопирован в буфер!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Ошибка копирования: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("export_settings_text_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Копировать JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

    // Export JSON Dialog (High Performance, No Freezing)
    if (showExportDialog) {
        val payload = exportPayloadData
        val sizeKb = (exportJsonText.toByteArray(Charsets.UTF_8).size) / 1024
        val previewSnippet = remember(exportJsonText) {
            exportJsonText.lineSequence().take(35).joinToString("\n")
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Backup,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Экспорт библиотеки (JSON)")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Summary stats badges
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Содержимое резервной копии:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📚 Книги и новеллы:", style = MaterialTheme.typography.bodySmall)
                                Text("${payload?.books?.size ?: 0}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🎬 Экранизации:", style = MaterialTheme.typography.bodySmall)
                                Text("${payload?.adaptations?.size ?: 0}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("✍️ Отзывы:", style = MaterialTheme.typography.bodySmall)
                                Text("${payload?.reviews?.size ?: 0}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🏆 Ряды тир-листа:", style = MaterialTheme.typography.bodySmall)
                                Text("${payload?.tierRows?.size ?: 0}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📦 Общий объём данных:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("$sizeKb КБ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Text(
                        text = "Фрагмент структуры JSON:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Lightweight Preview Box (NOT an OutlinedTextField, does not freeze UI thread)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 180.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "$previewSnippet\n...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "При копировании или отправке экспортируется полный JSON-файл целиком.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ReadTracker Backup", exportJsonText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "JSON библиотеки успешно скопирован в буфер!", Toast.LENGTH_SHORT).show()
                            showExportDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Ошибка копирования: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Копировать")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            try {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, exportJsonText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Отправить JSON библиотеки")
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Закрыть")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        var isImporting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isImporting) showImportDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text("Импорт библиотеки")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Вставьте сохранённый ранее JSON резервной копии:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Quick Paste from Clipboard button
                    FilledTonalButton(
                        onClick = {
                            try {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val pasted = clip.getItemAt(0).text?.toString() ?: ""
                                    if (pasted.isNotBlank()) {
                                        importJsonText = pasted
                                        Toast.makeText(context, "Вставлено из буфера (${pasted.length} симв.)", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Буфер обмена пуст", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Буфер обмена пуст", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка чтения буфера: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Вставить из буфера обмена", fontSize = 12.sp)
                    }

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{\"books\": [...], \"adaptations\": [...]}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        trailingIcon = {
                            if (importJsonText.isNotEmpty()) {
                                IconButton(onClick = { importJsonText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                }
                            }
                        }
                    )

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Заменить существующие данные", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(
                                    if (importReplaceMode) "Все текущие книги и отзывы будут перезаписаны" else "Новые книги будут объединены с текущими",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = importReplaceMode,
                                onCheckedChange = { importReplaceMode = it }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isBlank()) {
                            Toast.makeText(context, "Сначала вставьте JSON", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            isImporting = true
                            try {
                                val result = viewModel.importLibraryJson(importJsonText, importReplaceMode)
                                if (result.success) {
                                    Toast.makeText(
                                        context,
                                        "Импортировано: ${result.books.size} книг, ${result.adaptations.size} экран., ${result.reviews.size} отзывов",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showImportDialog = false
                                } else {
                                    Toast.makeText(context, "Ошибка: ${result.errorMessage}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка импорта: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isImporting = false
                            }
                        }
                    },
                    enabled = !isImporting && importJsonText.isNotBlank()
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Импортировать")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportDialog = false },
                    enabled = !isImporting
                ) {
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
        val previewSnippet = remember(settingsExportJsonText) {
            settingsExportJsonText.lineSequence().take(25).joinToString("\n")
        }

        AlertDialog(
            onDismissRequest = { showSettingsExportDialog = false },
            title = { Text("Экспорт настроек (JSON)") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Превью настроек приложения:")
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 180.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "$previewSnippet\n...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ReadTracker Settings", settingsExportJsonText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Настройки скопированы в буфер обмена", Toast.LENGTH_SHORT).show()
                            showSettingsExportDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Вставьте JSON с сохранёнными настройками:")

                    FilledTonalButton(
                        onClick = {
                            try {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val pasted = clip.getItemAt(0).text?.toString() ?: ""
                                    if (pasted.isNotBlank()) {
                                        settingsImportJsonText = pasted
                                        Toast.makeText(context, "Вставлено из буфера", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Буфер пуст", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка чтения буфера", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Вставить из буфера обмена", fontSize = 12.sp)
                    }

                    OutlinedTextField(
                        value = settingsImportJsonText,
                        onValueChange = { settingsImportJsonText = it },
                        placeholder = { Text("{\"themeMode\": \"...\", ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        trailingIcon = {
                            if (settingsImportJsonText.isNotEmpty()) {
                                IconButton(onClick = { settingsImportJsonText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                }
                            }
                        }
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

@Composable
private fun ColorDotSample(
    color: Color,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
