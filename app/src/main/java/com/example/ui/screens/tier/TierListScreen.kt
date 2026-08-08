package com.example.ui.screens.tier

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedItemForAction by remember { mutableStateOf<Pair<TierItem, String?>?>(null) } // item, fromRowId

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
            // Tier Rows
            items(tierRows, key = { it.id }) { row ->
                TierRowCard(
                    row = row,
                    onItemClick = { item -> selectedItemForAction = item to row.id },
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
                                        onClick = { selectedItemForAction = item to null }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Action dialog when clicking an item (Move to Tier or Remove)
    if (selectedItemForAction != null) {
        val (item, fromRowId) = selectedItemForAction!!
        AlertDialog(
            onDismissRequest = { selectedItemForAction = null },
            title = {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Выберите уровень в тир-листе:",
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
                                    selectedItemForAction = null
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
                                        .size(24.dp)
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
                                    text = "Переместить в ${targetRow.name}",
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
                                viewModel.moveTierItem(item.id, fromRowId, null) // remove to pool
                                selectedItemForAction = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
                TextButton(onClick = { selectedItemForAction = null }) {
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
        var selectedColor by remember { mutableStateOf(0xFFFF453AL) } // red default

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
                                textColor = 0xFFFFFFFFL
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

@Composable
fun TierRowCard(
    row: TierListRow,
    onItemClick: (TierItem) -> Unit,
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
            // Tier Label Header
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .fillMaxHeight()
                    .background(Color(row.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(row.textColor),
                    textAlign = TextAlign.Center
                )
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
                        text = "Перетащите или нажмите на тайтл чтобы добавить",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    row.items.forEach { item ->
                        TierItemThumbnail(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TierItemThumbnail(
    item: TierItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(60.dp)
            .clickable { onClick() }
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

@Composable
fun UnassignedItemThumbnail(
    item: TierItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
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
