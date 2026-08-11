package com.example.ui.screens.tier

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TierListScreen(
    viewModel: ReadTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tierRows by viewModel.allTierRows.collectAsStateWithLifecycle()
    val currentMode by viewModel.libraryMode.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allAdaptations by viewModel.allAdaptations.collectAsStateWithLifecycle()

    var showPresetMenu by remember { mutableStateOf(false) }
    var showAddRowDialog by remember { mutableStateOf(false) }
    var showAddCustomItemDialog by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<TierListRow?>(null) }
    var selectedItemForMove by remember { mutableStateOf<Pair<TierItem, String?>?>(null) } // item, fromRowId
    var selectedItemForCoverSheet by remember { mutableStateOf<Pair<TierItem, String?>?>(null) } // item, fromRowId

    val unassignedItems = remember(tierRows, allBooks, allAdaptations, currentMode) {
        viewModel.getUnassignedTierItems(tierRows, allBooks, allAdaptations, currentMode)
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
                        text = "Тир-лист",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                exportTierListToGallery(
                                    context = context,
                                    tierRows = tierRows,
                                    modeLabel = if (currentMode == LibraryMode.BOOKS) "Книги и новеллы" else "Экранизации"
                                )
                            },
                            modifier = Modifier.testTag("tier_download_btn")
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Скачать в галерею",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        IconButton(
                            onClick = { showPresetMenu = true },
                            modifier = Modifier.testTag("tier_presets_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Пресеты", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(
                            onClick = { showAddRowDialog = true },
                            modifier = Modifier.testTag("tier_add_row_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить строку", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Switch between Books & Adaptations
                ModeTogglePill(
                    currentMode = currentMode,
                    onModeChanged = { viewModel.libraryMode.value = it }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hint banner
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Нажатие на карточку — обложка. Зажатие (удержание) — переместить тайтл. Нажатие на уровень — HEX цвет и имя.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tier Rows
            items(tierRows, key = { it.id }) { row ->
                TierRowCard(
                    row = row,
                    onHeaderClick = { editingRow = row },
                    onItemClick = { item -> selectedItemForCoverSheet = item to row.id },
                    onItemLongClick = { item -> selectedItemForMove = item to row.id },
                    onDeleteRow = { viewModel.deleteTierRow(row) }
                )
            }

            // Unassigned Pool Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
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
                            Text(
                                text = "Неразмещённые (${unassignedItems.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            TextButton(onClick = { showAddCustomItemDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Кастомный", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (unassignedItems.isEmpty()) {
                            Text(
                                text = "Все серии распределены по категориям!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // Flow of unassigned items
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                unassignedItems.forEach { item ->
                                    UnassignedItemThumbnail(
                                        item = item,
                                        onClick = { selectedItemForCoverSheet = item to null },
                                        onLongClick = { selectedItemForMove = item to null }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Tier Row Dialog (HEX background, HEX text color, name)
    if (editingRow != null) {
        val row = editingRow!!
        var rowNameInput by remember(row.id) { mutableStateOf(row.name) }
        var bgHexInput by remember(row.id) { mutableStateOf(formatHexColor(row.color)) }
        var textHexInput by remember(row.id) { mutableStateOf(formatHexColor(row.textColor)) }

        val currentBgColor = parseHexColor(bgHexInput, row.color)
        val currentTextColor = parseHexColor(textHexInput, row.textColor)

        val quickBgPalette = listOf(
            0xFFFF5252L to "Красный",
            0xFFFF793FL to "Оранжевый",
            0xFFFFB142L to "Янтарь",
            0xFFFFDA79L to "Желтый",
            0xFF33D9B2L to "Мята",
            0xFF2ED573L to "Зеленый",
            0xFF34ACC0L to "Бирюза",
            0xFF1E90FFL to "Синий",
            0xFF706FD3L to "Фиолет",
            0xFFFF5289L to "Розовый",
            0xFF474787L to "Индиго",
            0xFF2C2C54L to "Темный"
        )

        val quickTextPalette = listOf(
            0xFFFFFFFFL to "Белый",
            0xFF131313L to "Черный",
            0xFFFFD700L to "Золото",
            0xFFFF5252L to "Красный",
            0xFF33D9B2L to "Мята"
        )

        AlertDialog(
            onDismissRequest = { editingRow = null },
            title = {
                Text(
                    text = "Настройка уровня «${row.name}»",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(currentBgColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rowNameInput.ifBlank { "Тир" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(currentTextColor)
                        )
                    }

                    // 1. Name input
                    OutlinedTextField(
                        value = rowNameInput,
                        onValueChange = { rowNameInput = it },
                        label = { Text("Название уровня") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 2. Background HEX input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(currentBgColor))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            OutlinedTextField(
                                value = bgHexInput,
                                onValueChange = { bgHexInput = it },
                                label = { Text("HEX фона (например #FF5252)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Quick background chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickBgPalette.forEach { (colorVal, _) ->
                                val isSel = currentBgColor == colorVal
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorVal))
                                        .border(
                                            width = if (isSel) 2.5.dp else 0.dp,
                                            color = if (isSel) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { bgHexInput = formatHexColor(colorVal) }
                                )
                            }
                        }
                    }

                    // 3. Text HEX input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(currentTextColor))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            OutlinedTextField(
                                value = textHexInput,
                                onValueChange = { textHexInput = it },
                                label = { Text("HEX текста названия (например #FFFFFF)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Quick text color chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickTextPalette.forEach { (colorVal, name) ->
                                FilterChip(
                                    selected = currentTextColor == colorVal,
                                    onClick = { textHexInput = formatHexColor(colorVal) },
                                    label = { Text(name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = rowNameInput.trim().ifEmpty { row.name }
                        val updatedRow = row.copy(
                            name = finalName,
                            color = currentBgColor,
                            textColor = currentTextColor
                        )
                        viewModel.updateTierRow(updatedRow)
                        editingRow = null
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            viewModel.deleteTierRow(row)
                            editingRow = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Удалить")
                    }

                    TextButton(onClick = { editingRow = null }) {
                        Text("Отмена")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Cover Management Bottom Sheet (Open on normal click)
    if (selectedItemForCoverSheet != null) {
        val (item, fromRowId) = selectedItemForCoverSheet!!
        var showUrlDialog by remember { mutableStateOf(false) }

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                viewModel.updateTierItemCover(item.id, uri.toString())
                Toast.makeText(context, "Обложка обновлена!", Toast.LENGTH_SHORT).show()
                selectedItemForCoverSheet = null
            }
        }

        ModalBottomSheet(
            onDismissRequest = { selectedItemForCoverSheet = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with current cover preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CoverImage(
                        coverUrl = item.coverUrl,
                        title = item.title,
                        width = 56.dp,
                        height = 80.dp,
                        corner = 8.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (fromRowId != null) "Уровень: ${tierRows.find { it.id == fromRowId }?.name ?: "—"}" else "В неразмещённых",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Actions: Change / Choose Cover
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Управление обложкой",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 1. Pick from gallery
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Выбрать из галереи", fontWeight = FontWeight.SemiBold)
                                Text("Загрузить фото или арт с устройства", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // 2. Enter URL
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showUrlDialog = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Указать ссылку (URL)", fontWeight = FontWeight.SemiBold)
                                Text("Вставить прямую веб-ссылку на изображение", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // 3. Remove Cover
                    if (!item.coverUrl.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateTierItemCover(item.id, null)
                                    Toast.makeText(context, "Обложка убрана", Toast.LENGTH_SHORT).show()
                                    selectedItemForCoverSheet = null
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text("Убрать обложку", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 4. Shortcut to Move Tier
                    OutlinedButton(
                        onClick = {
                            val savedItem = item to fromRowId
                            selectedItemForCoverSheet = null
                            selectedItemForMove = savedItem
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Переместить в другой уровень")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Dialog for URL input
        if (showUrlDialog) {
            var urlInput by remember { mutableStateOf(item.coverUrl ?: "") }
            AlertDialog(
                onDismissRequest = { showUrlDialog = false },
                title = { Text("Ссылка на обложку") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            placeholder = { Text("https://example.com/cover.jpg") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = urlInput.trim().ifEmpty { null }
                            viewModel.updateTierItemCover(item.id, trimmed)
                            showUrlDialog = false
                            selectedItemForCoverSheet = null
                            Toast.makeText(context, "Обложка обновлена!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Применить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUrlDialog = false }) {
                        Text("Отмена")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    // Move to Tier Action Dialog (Opened on Long Press / Зажатие)
    if (selectedItemForMove != null) {
        val (item, fromRowId) = selectedItemForMove!!
        AlertDialog(
            onDismissRequest = { selectedItemForMove = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CoverImage(
                        coverUrl = item.coverUrl,
                        title = item.title,
                        width = 36.dp,
                        height = 50.dp,
                        corner = 4.dp
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Выберите уровень перемещения:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    tierRows.forEach { targetRow ->
                        val isCurrent = fromRowId == targetRow.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.moveTierItem(item.id, fromRowId, targetRow.id)
                                    selectedItemForMove = null
                                }
                                .padding(8.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(targetRow.color)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = targetRow.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(targetRow.textColor)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Переместить в «${targetRow.name}»",
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (fromRowId != null) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        TextButton(
                            onClick = {
                                viewModel.moveTierItem(item.id, fromRowId, null) // remove to unassigned pool
                                selectedItemForMove = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Убрать в неразмещённые")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedItemForMove = null }) {
                    Text("Закрыть")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Presets Bottom Sheet / Menu
    if (showPresetMenu) {
        ModalBottomSheet(
            onDismissRequest = { showPresetMenu = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Пресеты тир-листов",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val presets = listOf(
                    TierPreset.CLASSIC to "Классический (Peak, Mid, Weak, Trash)",
                    TierPreset.LETTERS to "Буквенный (S, A, B, C, D, F)",
                    TierPreset.NUMBERS to "Числовой (10 .. 1)"
                )

                presets.forEach { (preset, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.applyTierPreset(preset)
                                showPresetMenu = false
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add Custom Tier Row Dialog
    if (showAddRowDialog) {
        var rowName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(0xFFFF453AL) }
        var selectedTextColor by remember { mutableStateOf(0xFFFFFFFFL) }

        val palette = listOf(
            0xFFFF453AL to "Красный",
            0xFFFF9F0AL to "Оранжевый",
            0xFFFFD60AL to "Желтый",
            0xFF30D158L to "Зеленый",
            0xFF0A84FFL to "Синий",
            0xFFBF5AF2L to "Фиолетовый"
        )

        AlertDialog(
            onDismissRequest = { showAddRowDialog = false },
            title = { Text("Добавить уровень в тир-лист") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = rowName,
                        onValueChange = { rowName = it },
                        label = { Text("Название (например: GOAT / S+)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Цвет уровня:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        palette.forEach { (colorHex, _) ->
                            val isSelected = selectedColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = colorHex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rowName.trim().isNotEmpty()) {
                            viewModel.addTierRow(
                                name = rowName.trim(),
                                color = selectedColor,
                                textColor = selectedTextColor
                            )
                            showAddRowDialog = false
                        }
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRowDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add Custom Independent Tier Item Dialog
    if (showAddCustomItemDialog) {
        var customTitle by remember { mutableStateOf("") }
        var customCoverUrl by remember { mutableStateOf("") }
        var targetRowId by remember { mutableStateOf(tierRows.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showAddCustomItemDialog = false },
            title = { Text("Кастомный элемент") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("Название тайтла") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customCoverUrl,
                        onValueChange = { customCoverUrl = it },
                        label = { Text("URL обложки (необязательно)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customTitle.trim().isNotEmpty() && targetRowId != null) {
                            viewModel.addIndependentTierItem(
                                title = customTitle.trim(),
                                coverUrl = customCoverUrl.trim().ifEmpty { null },
                                targetRowId = targetRowId
                            )
                            showAddCustomItemDialog = false
                        }
                    }
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomItemDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TierRowCard(
    row: TierListRow,
    onHeaderClick: () -> Unit,
    onItemClick: (TierItem) -> Unit,
    onItemLongClick: (TierItem) -> Unit,
    onDeleteRow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tier_row_${row.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp)
        ) {
            // Tier Label Header (Clickable to change name, HEX background, HEX text color)
            Box(
                modifier = Modifier
                    .width(74.dp)
                    .fillMaxHeight()
                    .background(Color(row.color))
                    .clickable { onHeaderClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = row.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(row.textColor),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать уровень",
                        tint = Color(row.textColor).copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Items list in this tier
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (row.items.isEmpty()) {
                    Text(
                        text = "Зажмите тайтл внизу, чтобы переместить сюда",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    row.items.forEach { item ->
                        TierItemThumbnail(
                            item = item,
                            onClick = { onItemClick(item) },
                            onLongClick = { onItemLongClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TierItemThumbnail(
    item: TierItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("tier_item_${item.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CoverImage(
            coverUrl = item.coverUrl,
            title = item.title,
            width = 56.dp,
            height = 76.dp,
            corner = 6.dp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnassignedItemThumbnail(
    item: TierItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CoverImage(
            coverUrl = item.coverUrl,
            title = item.title,
            width = 56.dp,
            height = 76.dp,
            corner = 6.dp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatHexColor(color: Long): String {
    val rgb = color and 0xFFFFFFL
    return String.format("#%06X", rgb)
}

fun parseHexColor(hex: String, defaultColor: Long): Long {
    return try {
        val clean = hex.trim().removePrefix("#")
        when (clean.length) {
            6 -> 0xFF000000L or clean.toLong(16)
            8 -> clean.toLong(16)
            else -> defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

fun exportTierListToGallery(
    context: Context,
    tierRows: List<TierListRow>,
    modeLabel: String
) {
    if (tierRows.isEmpty()) {
        Toast.makeText(context, "Тир-лист пуст!", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val width = 1080
        val rowHeight = 160
        val headerHeight = 140
        val footerHeight = 60
        val totalHeight = headerHeight + (tierRows.size * rowHeight) + footerHeight

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#131217")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), bgPaint)

        // Header Background & Title
        val titlePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 48f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#9E9DA8")
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("ТИР-ЛИСТ • $modeLabel", 40f, 70f, titlePaint)
        canvas.drawText("Создано в приложении ReadTracker", 40f, 110f, subtitlePaint)

        var currentY = headerHeight.toFloat()

        tierRows.forEach { row ->
            // Row background
            val rowBgPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#1D1C24")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, currentY, width.toFloat(), currentY + rowHeight - 8f, rowBgPaint)

            // Tier Name Header Box
            val headerBoxPaint = Paint().apply {
                color = row.color.toInt()
                style = Paint.Style.FILL
            }
            val headerWidth = 160f
            canvas.drawRect(0f, currentY, headerWidth, currentY + rowHeight - 8f, headerBoxPaint)

            // Tier Name Text
            val textPaint = Paint().apply {
                color = row.textColor.toInt()
                textSize = 36f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val textY = currentY + (rowHeight - 8f) / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(row.name, headerWidth / 2f, textY, textPaint)

            // Draw items in row
            var itemX = headerWidth + 20f
            val itemWidth = 100f
            val itemHeight = 130f
            val itemMargin = 16f

            val itemBgPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#2B2A36")
                style = Paint.Style.FILL
            }
            val itemBorderPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#444352")
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val itemTextPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 18f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            row.items.forEach { item ->
                if (itemX + itemWidth < width - 20f) {
                    val rect = RectF(itemX, currentY + 10f, itemX + itemWidth, currentY + 10f + itemHeight)
                    canvas.drawRoundRect(rect, 8f, 8f, itemBgPaint)
                    canvas.drawRoundRect(rect, 8f, 8f, itemBorderPaint)

                    val truncated = if (item.title.length > 12) item.title.take(10) + "…" else item.title
                    canvas.drawText(truncated, itemX + itemWidth / 2f, currentY + 10f + itemHeight - 16f, itemTextPaint)

                    itemX += itemWidth + itemMargin
                }
            }

            currentY += rowHeight
        }

        // Footer Watermark
        val footerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#6B6A78")
            textSize = 20f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("ReadTracker • Экспорт в Галерею", (width - 40).toFloat(), (totalHeight - 24).toFloat(), footerPaint)

        // Save to MediaStore
        val filename = "TierList_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ReadTracker")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            Toast.makeText(context, "Тир-лист сохранён в галерею!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
