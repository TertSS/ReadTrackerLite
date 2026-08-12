package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.AppSettings
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReadTrackerViewModel
import org.json.JSONObject

private fun isValidHex(hex: String): Boolean {
    val clean = hex.trim().removePrefix("#")
    return clean.matches(Regex("^[0-9a-fA-F]{3}$|^[0-9a-fA-F]{6}$|^[0-9a-fA-F]{8}$"))
}

private fun normalizeHex(hex: String, defaultHex: String): String {
    val clean = hex.trim().removePrefix("#")
    return if (clean.matches(Regex("^[0-9a-fA-F]{3}$|^[0-9a-fA-F]{6}$|^[0-9a-fA-F]{8}$"))) {
        "#${clean.uppercase()}"
    } else {
        defaultHex
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPaletteEditorScreen(
    viewModel: ReadTrackerViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // State variables for all HEX values
    var primaryHex by remember(settings) { mutableStateOf(settings.customPrimaryHex) }
    var onPrimaryHex by remember(settings) { mutableStateOf(settings.customOnPrimaryHex) }
    var primaryContainerHex by remember(settings) { mutableStateOf(settings.customPrimaryContainerHex) }
    var onPrimaryContainerHex by remember(settings) { mutableStateOf(settings.customOnPrimaryContainerHex) }
    var secondaryHex by remember(settings) { mutableStateOf(settings.customSecondaryHex) }
    var onSecondaryHex by remember(settings) { mutableStateOf(settings.customOnSecondaryHex) }
    var secondaryContainerHex by remember(settings) { mutableStateOf(settings.customSecondaryContainerHex) }
    var onSecondaryContainerHex by remember(settings) { mutableStateOf(settings.customOnSecondaryContainerHex) }
    var tertiaryHex by remember(settings) { mutableStateOf(settings.customTertiaryHex) }
    var onTertiaryHex by remember(settings) { mutableStateOf(settings.customOnTertiaryHex) }
    var tertiaryContainerHex by remember(settings) { mutableStateOf(settings.customTertiaryContainerHex) }
    var onTertiaryContainerHex by remember(settings) { mutableStateOf(settings.customOnTertiaryContainerHex) }

    // Background & Surfaces
    var backgroundHex by remember(settings) { mutableStateOf(settings.customBackgroundHex) }
    var onBackgroundHex by remember(settings) { mutableStateOf(settings.customOnBackgroundHex) }
    var surfaceHex by remember(settings) { mutableStateOf(settings.customSurfaceHex) }
    var onSurfaceHex by remember(settings) { mutableStateOf(settings.customOnSurfaceHex) }
    var surfaceVariantHex by remember(settings) { mutableStateOf(settings.customSurfaceVariantHex) }
    var onSurfaceVariantHex by remember(settings) { mutableStateOf(settings.customOnSurfaceVariantHex) }
    var surfaceContainerLowestHex by remember(settings) { mutableStateOf(settings.customSurfaceContainerLowestHex) }
    var surfaceContainerLowHex by remember(settings) { mutableStateOf(settings.customSurfaceContainerLowHex) }
    var surfaceContainerHex by remember(settings) { mutableStateOf(settings.customSurfaceContainerHex) }
    var surfaceContainerHighHex by remember(settings) { mutableStateOf(settings.customSurfaceContainerHighHex) }
    var surfaceContainerHighestHex by remember(settings) { mutableStateOf(settings.customSurfaceContainerHighestHex) }
    var surfaceBrightHex by remember(settings) { mutableStateOf(settings.customSurfaceBrightHex) }

    // Outlines & Errors
    var outlineHex by remember(settings) { mutableStateOf(settings.customOutlineHex) }
    var outlineVariantHex by remember(settings) { mutableStateOf(settings.customOutlineVariantHex) }
    var errorHex by remember(settings) { mutableStateOf(settings.customErrorHex) }

    // Statuses
    var statusReadingHex by remember(settings) { mutableStateOf(settings.customStatusReadingHex) }
    var statusPlannedHex by remember(settings) { mutableStateOf(settings.customStatusPlannedHex) }
    var statusCompletedHex by remember(settings) { mutableStateOf(settings.customStatusCompletedHex) }
    var statusPausedHex by remember(settings) { mutableStateOf(settings.customStatusPausedHex) }
    var statusDroppedHex by remember(settings) { mutableStateOf(settings.customStatusDroppedHex) }

    var showJsonDialog by remember { mutableStateOf(false) }
    var jsonDialogText by remember { mutableStateOf("") }
    var isImportMode by remember { mutableStateOf(false) }

    fun loadFromPreset(preset: ThemePaletteDefinition) {
        primaryHex = preset.primary.toHex()
        onPrimaryHex = preset.onPrimary.toHex()
        primaryContainerHex = preset.primaryContainer.toHex()
        onPrimaryContainerHex = preset.onPrimaryContainer.toHex()
        secondaryHex = preset.secondary.toHex()
        onSecondaryHex = preset.onSecondary.toHex()
        secondaryContainerHex = preset.secondaryContainer.toHex()
        onSecondaryContainerHex = preset.onSecondaryContainer.toHex()
        tertiaryHex = preset.tertiary.toHex()
        onTertiaryHex = preset.onTertiary.toHex()
        tertiaryContainerHex = preset.tertiaryContainer.toHex()
        onTertiaryContainerHex = preset.onTertiaryContainer.toHex()
        backgroundHex = preset.background.toHex()
        onBackgroundHex = preset.onBackground.toHex()
        surfaceHex = preset.surface.toHex()
        onSurfaceHex = preset.onSurface.toHex()
        surfaceVariantHex = preset.surfaceVariant.toHex()
        onSurfaceVariantHex = preset.onSurfaceVariant.toHex()
        surfaceContainerLowestHex = preset.surfaceContainerLowest.toHex()
        surfaceContainerLowHex = preset.surfaceContainerLow.toHex()
        surfaceContainerHex = preset.surfaceContainer.toHex()
        surfaceContainerHighHex = preset.surfaceContainerHigh.toHex()
        surfaceContainerHighestHex = preset.surfaceContainerHighest.toHex()
        surfaceBrightHex = preset.surfaceBright.toHex()
        outlineHex = preset.outline.toHex()
        outlineVariantHex = preset.outlineVariant.toHex()
        errorHex = preset.error.toHex()
        statusReadingHex = preset.statusReading.toHex()
        statusPlannedHex = preset.statusPlanned.toHex()
        statusCompletedHex = preset.statusCompleted.toHex()
        statusPausedHex = preset.statusPaused.toHex()
        statusDroppedHex = preset.statusDropped.toHex()

        Toast.makeText(context, "Загружен шаблон: ${preset.name}", Toast.LENGTH_SHORT).show()
    }

    fun applyAndSave() {
        val updated = settings.copy(
            activePalette = "CUSTOM",
            customPrimaryHex = normalizeHex(primaryHex, "#9ECAFF"),
            customOnPrimaryHex = normalizeHex(onPrimaryHex, "#003258"),
            customPrimaryContainerHex = normalizeHex(primaryContainerHex, "#2196F3"),
            customOnPrimaryContainerHex = normalizeHex(onPrimaryContainerHex, "#D1E4FF"),
            customSecondaryHex = normalizeHex(secondaryHex, "#78DC77"),
            customOnSecondaryHex = normalizeHex(onSecondaryHex, "#00390A"),
            customSecondaryContainerHex = normalizeHex(secondaryContainerHex, "#00761F"),
            customOnSecondaryContainerHex = normalizeHex(onSecondaryContainerHex, "#95FB92"),
            customTertiaryHex = normalizeHex(tertiaryHex, "#FFB77B"),
            customOnTertiaryHex = normalizeHex(onTertiaryHex, "#4D2700"),
            customTertiaryContainerHex = normalizeHex(tertiaryContainerHex, "#DB7900"),
            customOnTertiaryContainerHex = normalizeHex(onTertiaryContainerHex, "#FFDCC2"),
            customBackgroundHex = normalizeHex(backgroundHex, "#131313"),
            customOnBackgroundHex = normalizeHex(onBackgroundHex, "#E5E2E1"),
            customSurfaceHex = normalizeHex(surfaceHex, "#131313"),
            customOnSurfaceHex = normalizeHex(onSurfaceHex, "#E5E2E1"),
            customSurfaceVariantHex = normalizeHex(surfaceVariantHex, "#404752"),
            customOnSurfaceVariantHex = normalizeHex(onSurfaceVariantHex, "#BFC7D4"),
            customSurfaceContainerLowestHex = normalizeHex(surfaceContainerLowestHex, "#0E0E0E"),
            customSurfaceContainerLowHex = normalizeHex(surfaceContainerLowHex, "#1C1B1B"),
            customSurfaceContainerHex = normalizeHex(surfaceContainerHex, "#201F1F"),
            customSurfaceContainerHighHex = normalizeHex(surfaceContainerHighHex, "#2A2A2A"),
            customSurfaceContainerHighestHex = normalizeHex(surfaceContainerHighestHex, "#353534"),
            customSurfaceBrightHex = normalizeHex(surfaceBrightHex, "#393939"),
            customOutlineHex = normalizeHex(outlineHex, "#89919D"),
            customOutlineVariantHex = normalizeHex(outlineVariantHex, "#404752"),
            customErrorHex = normalizeHex(errorHex, "#FFB4AB"),
            customStatusReadingHex = normalizeHex(statusReadingHex, "#78DC77"),
            customStatusPlannedHex = normalizeHex(statusPlannedHex, "#FFB77B"),
            customStatusCompletedHex = normalizeHex(statusCompletedHex, "#9ECAFF"),
            customStatusPausedHex = normalizeHex(statusPausedHex, "#BFC7D4"),
            customStatusDroppedHex = normalizeHex(statusDroppedHex, "#FFB4AB")
        )
        viewModel.updateAppSettings(updated)
        Toast.makeText(context, "Пользовательские цвета успешно сохранены!", Toast.LENGTH_SHORT).show()
        onDismiss()
    }

    // Dynamic Live Preview Color values
    val previewBg = parseColorHex(backgroundHex, Color(0xFF131313))
    val previewSurface = parseColorHex(surfaceHex, Color(0xFF131313))
    val previewPrimary = parseColorHex(primaryHex, Color(0xFF9ECAFF))
    val previewOnPrimary = parseColorHex(onPrimaryHex, Color(0xFF003258))
    val previewSecondary = parseColorHex(secondaryHex, Color(0xFF78DC77))
    val previewTertiary = parseColorHex(tertiaryHex, Color(0xFFFFB77B))
    val previewOnSurface = parseColorHex(onSurfaceHex, Color(0xFFE5E2E1))
    val previewOnSurfaceVariant = parseColorHex(onSurfaceVariantHex, Color(0xFFBFC7D4))
    val previewOutline = parseColorHex(outlineHex, Color(0xFF89919D))
    val previewStatusReading = parseColorHex(statusReadingHex, Color(0xFF78DC77))
    val previewStatusPlanned = parseColorHex(statusPlannedHex, Color(0xFFFFB77B))
    val previewStatusCompleted = parseColorHex(statusCompletedHex, Color(0xFF9ECAFF))
    val previewStatusPaused = parseColorHex(statusPausedHex, Color(0xFFBFC7D4))
    val previewStatusDropped = parseColorHex(statusDroppedHex, Color(0xFFFFB4AB))

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Редактор палитры по HEX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Полная кастомизация каждого цвета приложения",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            val json = JSONObject().apply {
                                put("background", backgroundHex)
                                put("surface", surfaceHex)
                                put("surfaceContainer", surfaceContainerHex)
                                put("surfaceContainerLow", surfaceContainerLowHex)
                                put("surfaceContainerHigh", surfaceContainerHighHex)
                                put("surfaceContainerHighest", surfaceContainerHighestHex)
                                put("primary", primaryHex)
                                put("onPrimary", onPrimaryHex)
                                put("secondary", secondaryHex)
                                put("tertiary", tertiaryHex)
                                put("onSurface", onSurfaceHex)
                                put("onSurfaceVariant", onSurfaceVariantHex)
                                put("outline", outlineHex)
                                put("statusReading", statusReadingHex)
                                put("statusPlanned", statusPlannedHex)
                                put("statusCompleted", statusCompletedHex)
                                put("statusPaused", statusPausedHex)
                                put("statusDropped", statusDroppedHex)
                            }.toString(2)
                            jsonDialogText = json
                            isImportMode = false
                            showJsonDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "JSON Экспорт/Импорт")
                    }
                    Button(
                        onClick = { applyAndSave() },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Применить", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LIVE PREVIEW CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = previewBg),
                    border = BorderStroke(1.5.dp, previewOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Интерактивный предпросмотр темы",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = previewOnSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = previewPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "LIVE HEX",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = previewPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Preview Sub-Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = previewSurface,
                            border = BorderStroke(1.dp, previewOutline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Пример карточки произведения",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = previewOnSurface
                                )
                                Text(
                                    text = "Это демонстрация того, как будут отображаться фоны, карточки, текст и кнопки во всём приложении.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = previewOnSurfaceVariant
                                )

                                // Action Controls Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = previewPrimary
                                    ) {
                                        Text(
                                            text = "Основная кнопка",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = previewOnPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = previewSecondary.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, previewSecondary)
                                    ) {
                                        Text(
                                            text = "Вторичный чип",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = previewSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = previewTertiary
                                    ) {
                                        Text(
                                            text = "★ 9.5",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Status Badges Preview
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LiveStatusBadgeSample("Читаю", previewStatusReading)
                                    LiveStatusBadgeSample("В планах", previewStatusPlanned)
                                    LiveStatusBadgeSample("Завершено", previewStatusCompleted)
                                    LiveStatusBadgeSample("Пауза", previewStatusPaused)
                                    LiveStatusBadgeSample("Брошено", previewStatusDropped)
                                }
                            }
                        }
                    }
                }
            }

            // 2. QUICK LOAD PRESET TEMPLATES
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Быстрая загрузка из готового шаблона",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Выберите базовую тему, чтобы заполнить все HEX-поля и донастроить нужные детали:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetPalettes.forEach { preset ->
                                OutlinedButton(
                                    onClick = { loadFromPreset(preset) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(preset.primary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(preset.name, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // 3. GROUP 1: BACKGROUND & SURFACES
            item {
                ColorCategoryHeader(
                    icon = Icons.Default.Wallpaper,
                    title = "Фон и Поверхности",
                    subtitle = "Настройка цвета общего фона приложения, карточек и внутренних контейнеров"
                )
            }

            item {
                HexColorEditorCard(
                    title = "Основной фон экрана (background)",
                    description = "Главный цвет подложки всего приложения, статус-бара и навигации",
                    hexValue = backgroundHex,
                    onHexChange = { backgroundHex = it },
                    quickColors = listOf("#0B0F19", "#09120D", "#0F0A1C", "#140E0E", "#000000", "#121212", "#18181B", "#0F172A", "#1E1E2E")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Поверхность карточек (surface)",
                    description = "Фон основных карточек, диалогов, меню и списков",
                    hexValue = surfaceHex,
                    onHexChange = { surfaceHex = it },
                    quickColors = listOf("#111827", "#0F1D15", "#161029", "#1D1414", "#18181B", "#1E293B", "#262626", "#1F1D2B", "#27272A")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Внутренние контейнеры (surfaceContainer)",
                    description = "Контейнеры фильтров, нижних панелей и карточек книг",
                    hexValue = surfaceContainerHex,
                    onHexChange = { surfaceContainerHex = it },
                    quickColors = listOf("#161F2E", "#13261C", "#1D1436", "#241818", "#27272A", "#334155", "#2D3748", "#24283B", "#374151")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Верхний слой / Шапки (surfaceContainerHigh)",
                    description = "Контейнеры модальных окон, всплывающих меню и фильтров",
                    hexValue = surfaceContainerHighHex,
                    onHexChange = { surfaceContainerHighHex = it },
                    quickColors = listOf("#1E293B", "#1A3326", "#281C4A", "#332121", "#3F3F46", "#475569", "#3B4252", "#2E3440", "#4A5568")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Поля ввода и чипы (surfaceContainerHighest)",
                    description = "Фон полей поиска, текстовых полей и неактивных элементов",
                    hexValue = surfaceContainerHighestHex,
                    onHexChange = { surfaceContainerHighestHex = it },
                    quickColors = listOf("#334155", "#244533", "#352561", "#432C2C", "#52525B", "#64748B", "#4C566A", "#3B4252", "#475569")
                )
            }

            // 4. GROUP 2: ACCENTS & BUTTONS
            item {
                ColorCategoryHeader(
                    icon = Icons.Default.Palette,
                    title = "Основные Акценты и Кнопки",
                    subtitle = "Цвета активных элементов, FAB-кнопок, бейджей и диаграмм"
                )
            }

            item {
                HexColorEditorCard(
                    title = "Основной акцент (primary)",
                    description = "Кнопки сохранения, активные табы, индикаторы прогресса, FAB",
                    hexValue = primaryHex,
                    onHexChange = { primaryHex = it },
                    quickColors = listOf("#38BDF8", "#4ADE80", "#C084FC", "#FB923C", "#F43F5E", "#3B82F6", "#10B981", "#A855F7", "#E11D48", "#F59E0B")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Текст на основном цвете (onPrimary)",
                    description = "Цвет надписей и иконок внутри кнопок основного цвета",
                    hexValue = onPrimaryHex,
                    onHexChange = { onPrimaryHex = it },
                    quickColors = listOf("#003258", "#052E16", "#3B0764", "#431407", "#FFFFFF", "#000000", "#0F172A", "#1E293B")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Вторичный акцент (secondary)",
                    description = "Переключатели режимов, фильтры, чипы статусов",
                    hexValue = secondaryHex,
                    onHexChange = { secondaryHex = it },
                    quickColors = listOf("#34D399", "#2DD4BF", "#F472B6", "#F87171", "#22C55E", "#06B6D4", "#EC4899", "#8B5CF6", "#FBBF24")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Третичный акцент (tertiary)",
                    description = "Цели чтения, звёзды рейтинга, графики статистики",
                    hexValue = tertiaryHex,
                    onHexChange = { tertiaryHex = it },
                    quickColors = listOf("#F472B6", "#FCD34D", "#FBBF24", "#FACC15", "#E879F9", "#FB7185", "#38BDF8", "#34D399", "#FFD700")
                )
            }

            // 5. GROUP 3: TEXT & OUTLINES
            item {
                ColorCategoryHeader(
                    icon = Icons.Default.TextFields,
                    title = "Текст, Иконки и Границы",
                    subtitle = "Цвета заголовков, основного текста, подсказок и контуров"
                )
            }

            item {
                HexColorEditorCard(
                    title = "Основной текст (onSurface / onBackground)",
                    description = "Цвет названий книг, заголовков экранов и важного текста",
                    hexValue = onSurfaceHex,
                    onHexChange = { 
                        onSurfaceHex = it
                        onBackgroundHex = it
                    },
                    quickColors = listOf("#F1F5F9", "#F0FDF4", "#FAF5FF", "#FFF7ED", "#FFFFFF", "#F8FAFC", "#E2E8F0", "#CBD5E1")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Второстепенный текст (onSurfaceVariant)",
                    description = "Подсказки, авторы, жанры, даты и неактивные иконки",
                    hexValue = onSurfaceVariantHex,
                    onHexChange = { onSurfaceVariantHex = it },
                    quickColors = listOf("#94A3B8", "#86EFAC", "#C4B5FD", "#FED7AA", "#A1A1AA", "#A0AEC0", "#9CA3AF", "#718096", "#64748B")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Границы и разделители (outline)",
                    description = "Рамки карточек, разделительные линии и контуры",
                    hexValue = outlineHex,
                    onHexChange = { outlineHex = it },
                    quickColors = listOf("#475569", "#2D5A42", "#5B458E", "#734C4C", "#3F3F46", "#4A5568", "#52525B", "#334155", "#6B7280")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Ошибки и удаление (error)",
                    description = "Кнопки удаления, предупреждения и индикаторы ошибок",
                    hexValue = errorHex,
                    onHexChange = { errorHex = it },
                    quickColors = listOf("#F87171", "#FB7185", "#EF4444", "#E11D48", "#DC2626", "#F43F5E", "#FF4D4F")
                )
            }

            // 6. GROUP 4: STATUS COLORS
            item {
                ColorCategoryHeader(
                    icon = Icons.Default.Bookmark,
                    title = "Цвета Статусов Произведений",
                    subtitle = "Цвета бейджей и фильтров чтения и просмотра во всех списках"
                )
            }

            item {
                HexColorEditorCard(
                    title = "Статус «Читаю / Смотрю»",
                    description = "Цвет бейджа активного чтения книг и просмотра экранизаций",
                    hexValue = statusReadingHex,
                    onHexChange = { statusReadingHex = it },
                    quickColors = listOf("#34D399", "#4ADE80", "#22C55E", "#10B981", "#059669", "#00E676", "#78DC77")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Статус «В планах»",
                    description = "Цвет бейджа запланированных книг и экранизаций",
                    hexValue = statusPlannedHex,
                    onHexChange = { statusPlannedHex = it },
                    quickColors = listOf("#FBBF24", "#FCD34D", "#FB923C", "#F59E0B", "#D97706", "#FF9800", "#FFB77B")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Статус «Завершено / Просмотрено»",
                    description = "Цвет бейджа завершённых и полностью прочитанных тайтлов",
                    hexValue = statusCompletedHex,
                    onHexChange = { statusCompletedHex = it },
                    quickColors = listOf("#38BDF8", "#2DD4BF", "#C084FC", "#60A5FA", "#3B82F6", "#00B0FF", "#9ECAFF")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Статус «На паузе»",
                    description = "Цвет бейджа приостановленных произведений",
                    hexValue = statusPausedHex,
                    onHexChange = { statusPausedHex = it },
                    quickColors = listOf("#94A3B8", "#9CA3AF", "#C4B5FD", "#A8A29E", "#BFC7D4", "#A0AEC0", "#78909C")
                )
            }

            item {
                HexColorEditorCard(
                    title = "Статус «Брошено»",
                    description = "Цвет бейджа отменённых и заброшенных тайтлов",
                    hexValue = statusDroppedHex,
                    onHexChange = { statusDroppedHex = it },
                    quickColors = listOf("#F87171", "#FB7185", "#EF4444", "#F43F5E", "#E11D48", "#FF5252", "#FFB4AB")
                )
            }

            // Save Footer Action Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { applyAndSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Применить и сохранить HEX-палитру",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // JSON Export / Import Dialog
    if (showJsonDialog) {
        Dialog(onDismissRequest = { showJsonDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isImportMode) "Импорт JSON палитры" else "Экспорт JSON палитры",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showJsonDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть")
                        }
                    }

                    if (!isImportMode) {
                        Text(
                            text = "Вы можете скопировать эту конфигурацию палитры или сохранить её для резервной копии:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = jsonDialogText,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Palette JSON", jsonDialogText))
                                    Toast.makeText(context, "Скопировано в буфер обмена!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Копировать")
                            }
                            Button(
                                onClick = {
                                    isImportMode = true
                                    jsonDialogText = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Импорт")
                            }
                        }
                    } else {
                        Text(
                            text = "Вставьте JSON объект с HEX-цветами для импорта:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = jsonDialogText,
                            onValueChange = { jsonDialogText = it },
                            placeholder = { Text("{\n  \"background\": \"#0B0F19\",\n  \"primary\": \"#38BDF8\"\n}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val item = clipboard.primaryClip?.getItemAt(0)
                                    if (item != null) {
                                        jsonDialogText = item.text?.toString() ?: ""
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Вставить")
                            }
                            Button(
                                onClick = {
                                    try {
                                        val obj = JSONObject(jsonDialogText)
                                        if (obj.has("background")) backgroundHex = obj.getString("background")
                                        if (obj.has("surface")) surfaceHex = obj.getString("surface")
                                        if (obj.has("surfaceContainer")) surfaceContainerHex = obj.getString("surfaceContainer")
                                        if (obj.has("surfaceContainerLow")) surfaceContainerLowHex = obj.getString("surfaceContainerLow")
                                        if (obj.has("surfaceContainerHigh")) surfaceContainerHighHex = obj.getString("surfaceContainerHigh")
                                        if (obj.has("surfaceContainerHighest")) surfaceContainerHighestHex = obj.getString("surfaceContainerHighest")
                                        if (obj.has("primary")) primaryHex = obj.getString("primary")
                                        if (obj.has("onPrimary")) onPrimaryHex = obj.getString("onPrimary")
                                        if (obj.has("secondary")) secondaryHex = obj.getString("secondary")
                                        if (obj.has("tertiary")) tertiaryHex = obj.getString("tertiary")
                                        if (obj.has("onSurface")) onSurfaceHex = obj.getString("onSurface")
                                        if (obj.has("onSurfaceVariant")) onSurfaceVariantHex = obj.getString("onSurfaceVariant")
                                        if (obj.has("outline")) outlineHex = obj.getString("outline")
                                        if (obj.has("statusReading")) statusReadingHex = obj.getString("statusReading")
                                        if (obj.has("statusPlanned")) statusPlannedHex = obj.getString("statusPlanned")
                                        if (obj.has("statusCompleted")) statusCompletedHex = obj.getString("statusCompleted")
                                        if (obj.has("statusPaused")) statusPausedHex = obj.getString("statusPaused")
                                        if (obj.has("statusDropped")) statusDroppedHex = obj.getString("statusDropped")
                                        showJsonDialog = false
                                        Toast.makeText(context, "Палитра успешно импортирована!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка разбора JSON: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Применить")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorCategoryHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HexColorEditorCard(
    title: String,
    description: String,
    hexValue: String,
    onHexChange: (String) -> Unit,
    quickColors: List<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val parsedColor = parseColorHex(hexValue, Color.Gray)
    val isValid = isValidHex(hexValue)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Color Swatch Indicator
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(parsedColor)
                        .border(
                            width = 2.dp,
                            color = if (isValid) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(10.dp)
                        )
                )
            }

            // Input Row with Paste & Quick Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = hexValue,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isLetterOrDigit() || it == '#' }
                        if (cleaned.length <= 9) {
                            onHexChange(cleaned)
                        }
                    },
                    isError = !isValid,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    placeholder = { Text("#RRGGBB") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        if (!isValid) {
                            Icon(Icons.Default.Warning, contentDescription = "Неверный HEX", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("HEX Color", hexValue))
                        Toast.makeText(context, "HEX скопирован: $hexValue", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Копировать HEX", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val item = clipboard.primaryClip?.getItemAt(0)
                        if (item != null) {
                            val text = item.text?.toString()?.trim() ?: ""
                            if (text.isNotEmpty()) {
                                val formatted = if (text.startsWith("#")) text else "#$text"
                                onHexChange(formatted)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Вставить HEX", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Quick Color Preset Swatches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Палитра:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                quickColors.forEach { quickHex ->
                    val color = parseColorHex(quickHex, Color.Gray)
                    val isSelected = hexValue.equals(quickHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable {
                                onHexChange(quickHex)
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStatusBadgeSample(
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
