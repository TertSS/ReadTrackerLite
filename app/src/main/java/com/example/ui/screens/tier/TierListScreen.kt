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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReadTrackerViewModel
import kotlin.math.roundToInt

data class DropTarget(
    val rowId: String?,
    val insertIndex: Int
)

class TierDragDropState {
    var isDragging by mutableStateOf(false)
        private set
    var draggedItem by mutableStateOf<TierItem?>(null)
        private set
    var draggedFromRowId by mutableStateOf<String?>(null)
        private set
    var dragPosition by mutableStateOf(Offset.Zero)
        private set
    var currentDropTarget by mutableStateOf<DropTarget?>(null)
        private set

    val rowBounds = mutableMapOf<String, Rect>()
    val rowItemBounds = mutableMapOf<String, MutableMap<Int, Rect>>()
    var unassignedBounds: Rect? = null

    fun registerRow(rowId: String, rect: Rect) {
        rowBounds[rowId] = rect
    }

    fun registerItem(rowId: String, index: Int, rect: Rect) {
        rowItemBounds.getOrPut(rowId) { mutableMapOf() }[index] = rect
    }

    fun registerUnassigned(rect: Rect) {
        unassignedBounds = rect
    }

    fun startDrag(item: TierItem, fromRowId: String?, initialPos: Offset) {
        draggedItem = item
        draggedFromRowId = fromRowId
        dragPosition = initialPos
        isDragging = true
        updateDropTarget(initialPos)
    }

    fun dragBy(delta: Offset) {
        val newPos = dragPosition + delta
        dragPosition = newPos
        updateDropTarget(newPos)
    }

    private fun updateDropTarget(pos: Offset) {
        val uBounds = unassignedBounds
        if (uBounds != null && uBounds.contains(pos)) {
            val target = DropTarget(rowId = null, insertIndex = -1)
            if (currentDropTarget != target) currentDropTarget = target
            return
        }

        for ((rowId, rect) in rowBounds) {
            if (rect.contains(pos)) {
                val itemsMap = rowItemBounds[rowId] ?: emptyMap()
                if (itemsMap.isEmpty()) {
                    val target = DropTarget(rowId = rowId, insertIndex = 0)
                    if (currentDropTarget != target) currentDropTarget = target
                    return
                }
                val sortedEntries = itemsMap.entries.sortedBy { it.key }
                var targetIdx = sortedEntries.size
                for ((idx, itemRect) in sortedEntries) {
                    if (pos.x < itemRect.center.x) {
                        targetIdx = idx
                        break
                    }
                }
                val target = DropTarget(rowId = rowId, insertIndex = targetIdx)
                if (currentDropTarget != target) currentDropTarget = target
                return
            }
        }

        if (currentDropTarget != null) {
            currentDropTarget = null
        }
    }

    fun endDrag(): DropTarget? {
        val target = currentDropTarget
        isDragging = false
        draggedItem = null
        draggedFromRowId = null
        dragPosition = Offset.Zero
        currentDropTarget = null
        return target
    }

    fun cancelDrag() {
        isDragging = false
        draggedItem = null
        draggedFromRowId = null
        dragPosition = Offset.Zero
        currentDropTarget = null
    }
}

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
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current
    val dragState = remember { TierDragDropState() }
    var activeDraggedItem by remember { mutableStateOf<TierItem?>(null) }
    var activeDraggedFromRowId by remember { mutableStateOf<String?>(null) }

    val handleStartDrag: (TierItem, String?, Offset) -> Unit = { item, fromRowId, pos ->
        activeDraggedItem = item
        activeDraggedFromRowId = fromRowId
        dragState.startDrag(item, fromRowId, pos)
    }

    val handleDragDelta: (Offset) -> Unit = { delta ->
        dragState.dragBy(delta)
    }

    val handleEndDrag: () -> Unit = {
        val item = activeDraggedItem
        val fromRowId = activeDraggedFromRowId
        val dropTarget = dragState.endDrag()
        activeDraggedItem = null
        activeDraggedFromRowId = null

        if (item != null && dropTarget != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.moveTierItem(
                itemId = item.id,
                fromRowId = fromRowId,
                toRowId = dropTarget.rowId,
                targetIndex = dropTarget.insertIndex
            )
        }
    }

    val handleCancelDrag: () -> Unit = {
        activeDraggedItem = null
        activeDraggedFromRowId = null
        dragState.cancelDrag()
    }

    var showPresetMenu by remember { mutableStateOf(false) }
    var showAddRowDialog by remember { mutableStateOf(false) }
    var showAddCustomItemDialog by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<TierListRow?>(null) }
    var selectedItemForSheet by remember { mutableStateOf<Pair<TierItem, String?>?>(null) } // item, fromRowId
    var itemForCoverEdit by remember { mutableStateOf<TierItem?>(null) }
    
    val visibleTierRows = remember(tierRows) {
        tierRows.filter { it.id != "__UNASSIGNED_CUSTOM__" }
    }

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
                        style = if (settings.uniformHeadersEnabled) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                exportTierListToGallery(
                                    context = context,
                                    tierRows = visibleTierRows,
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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tier Rows
                items(visibleTierRows, key = { it.id }) { row ->
                    TierRowCard(
                        row = row,
                        dragState = dragState,
                        onHeaderClick = { editingRow = row },
                        onItemClick = { item ->
                            selectedItemForSheet = item to row.id
                        },
                        onItemStartDrag = { item, pos -> handleStartDrag(item, row.id, pos) },
                        onItemDragDelta = handleDragDelta,
                        onItemEndDrag = handleEndDrag,
                        onItemCancelDrag = handleCancelDrag
                    )
                }

                // Unassigned Pool Section
                item {
                    val isUnassignedHovered = dragState.isDragging && dragState.currentDropTarget?.rowId == null && dragState.currentDropTarget != null
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .onGloballyPositioned { coords ->
                                dragState.registerUnassigned(coords.boundsInRoot())
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnassignedHovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(
                            width = if (isUnassignedHovered) 2.dp else 1.dp,
                            color = if (isUnassignedHovered) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        )
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
                                    text = if (isUnassignedHovered) "Отпустите, чтобы вернуть в неразмещённые" else "Неразмещённые (${unassignedItems.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnassignedHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )

                                TextButton(
                                    onClick = { showAddCustomItemDialog = true },
                                    modifier = Modifier.testTag("add_custom_tier_item_btn")
                                ) {
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
                                            onClick = {
                                                selectedItemForSheet = item to null
                                            },
                                            onStartDrag = { pos -> handleStartDrag(item, null, pos) },
                                            onDragDelta = handleDragDelta,
                                            onEndDrag = handleEndDrag,
                                            onCancelDrag = handleCancelDrag
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Dragged Item Preview
            if (dragState.isDragging && dragState.draggedItem != null) {
                val item = dragState.draggedItem!!
                val pos = dragState.dragPosition
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(9999f)
                ) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (pos.x - 30.dp.toPx()).roundToInt(),
                                    (pos.y - 45.dp.toPx()).roundToInt()
                                )
                            }
                            .shadow(16.dp, RoundedCornerShape(10.dp))
                            .scale(1.12f)
                            .alpha(0.95f)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                    ) {
                        CoverImage(
                            coverUrl = item.coverUrl,
                            title = item.title,
                            width = 60.dp,
                            height = 82.dp,
                            corner = 10.dp
                        )
                    }
                }
            }
        }
    }

    // 1. Edit Tier Row Dialog (Click on Peak / S badge)
    if (editingRow != null) {
        val row = editingRow!!
        var rowName by remember(row) { mutableStateOf(row.name) }
        var rowColorHex by remember(row) { mutableStateOf(colorToHex(row.color)) }
        var rowTextColorHex by remember(row) { mutableStateOf(colorToHex(row.textColor)) }

        val bgPresets = listOf(
            0xFFFF453AL to "Красный",
            0xFFFF9F0AL to "Оранжевый",
            0xFFFFD60AL to "Желтый",
            0xFF30D158L to "Зеленый",
            0xFF0A84FFL to "Синий",
            0xFFBF5AF2L to "Фиолетовый",
            0xFF5E5CE6L to "Индиго",
            0xFF8E8E93L to "Серый"
        )

        val textPresets = listOf(
            0xFFFFFFFFL to "Белый",
            0xFF000000L to "Черный",
            0xFFFFD60AL to "Желтый",
            0xFFFF453AL to "Красный"
        )

        AlertDialog(
            onDismissRequest = { editingRow = null },
            title = {
                Text("Настройка уровня тир-листа", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Preview Badge
                    val previewBg = parseHexColor(rowColorHex, row.color)
                    val previewText = parseHexColor(rowTextColorHex, row.textColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(previewBg)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rowName.ifEmpty { "Tier" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(previewText),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    OutlinedTextField(
                        value = rowName,
                        onValueChange = { rowName = it },
                        label = { Text("Название (например: Peak / S+)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Background color hex & palette
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Цвет фона (HEX):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = rowColorHex,
                            onValueChange = { rowColorHex = it },
                            placeholder = { Text("#FF453A") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            bgPresets.forEach { (c, _) ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(c))
                                        .border(
                                            width = if (parseHexColor(rowColorHex, 0L) == c) 2.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                        .clickable { rowColorHex = colorToHex(c) }
                                )
                            }
                        }
                    }

                    // Text color hex & palette
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Цвет текста (HEX):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = rowTextColorHex,
                            onValueChange = { rowTextColorHex = it },
                            placeholder = { Text("#FFFFFF") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            textPresets.forEach { (c, label) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(c),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clickable { rowTextColorHex = colorToHex(c) }
                                        .padding(2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (c == 0xFFFFFFFFL) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Delete row option
                    TextButton(
                        onClick = {
                            viewModel.deleteTierRow(row)
                            editingRow = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Удалить этот уровень")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bg = parseHexColor(rowColorHex, row.color)
                        val txt = parseHexColor(rowTextColorHex, row.textColor)
                        viewModel.updateTierRowProperties(
                            rowId = row.id,
                            newName = rowName.trim().ifEmpty { row.name },
                            newColor = bg,
                            newTextColor = txt
                        )
                        editingRow = null
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRow = null }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 2. Action Bottom Sheet when clicking an item (Cover URL, Move to tier, Remove, Delete)
    if (selectedItemForSheet != null) {
        val (item, fromRowId) = selectedItemForSheet!!

        ModalBottomSheet(
            onDismissRequest = { selectedItemForSheet = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Cover & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CoverImage(
                        coverUrl = item.coverUrl,
                        title = item.title,
                        width = 48.dp,
                        height = 68.dp,
                        corner = 6.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (fromRowId != null) {
                                val current = tierRows.find { it.id == fromRowId }
                                "Текущий уровень: ${current?.name ?: "Тир"}"
                            } else "Неразмещённый",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Action: Change Cover URL
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            itemForCoverEdit = item
                            selectedItemForSheet = null
                        },
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Изменить / удалить обложку", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Reorder in current row (if in a tier row with > 1 item)
                val currentRow = fromRowId?.let { rId -> visibleTierRows.find { it.id == rId } }
                val currentIndex = currentRow?.items?.indexOfFirst { it.id == item.id } ?: -1
                if (currentRow != null && currentIndex != -1 && currentRow.items.size > 1) {
                    Text("Порядок в ряду:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                if (currentIndex > 0) {
                                    viewModel.moveTierItem(item.id, fromRowId, fromRowId, currentIndex - 1)
                                    selectedItemForSheet = null
                                }
                            },
                            enabled = currentIndex > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Влево", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                if (currentIndex < currentRow.items.size - 1) {
                                    viewModel.moveTierItem(item.id, fromRowId, fromRowId, currentIndex + 2)
                                    selectedItemForSheet = null
                                }
                            },
                            enabled = currentIndex < currentRow.items.size - 1,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Вправо", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Action: Move to specific Tier Row
                Text("Переместить в уровень:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visibleTierRows.forEach { targetRow ->
                        val isCurrent = fromRowId == targetRow.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.moveTierItem(item.id, fromRowId, targetRow.id)
                                    selectedItemForSheet = null
                                },
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, Color(targetRow.color))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(targetRow.color))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = targetRow.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Action: Remove from Tier to Unassigned pool
                if (fromRowId != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.moveTierItem(item.id, fromRowId, null)
                                selectedItemForSheet = null
                            },
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Убрать из тир-листа в неразмещённые", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Action: Delete custom item
                if (item.sourceId == null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.deleteTierItem(item.id, fromRowId)
                                selectedItemForSheet = null
                            },
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Удалить этот кастомный тайтл", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 3. Edit Item Cover Dialog
    if (itemForCoverEdit != null) {
        val targetItem = itemForCoverEdit!!
        var coverInput by remember(targetItem) { mutableStateOf(targetItem.coverUrl ?: "") }

        AlertDialog(
            onDismissRequest = { itemForCoverEdit = null },
            title = { Text("Обложка для «${targetItem.title}»") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = coverInput,
                        onValueChange = { coverInput = it },
                        label = { Text("URL ссылки на картинку") },
                        placeholder = { Text("https://example.com/cover.jpg") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (coverInput.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CoverImage(
                                coverUrl = coverInput.trim(),
                                title = targetItem.title,
                                width = 80.dp,
                                height = 110.dp,
                                corner = 8.dp
                            )
                        }
                    }

                    if (targetItem.coverUrl != null) {
                        TextButton(
                            onClick = {
                                viewModel.updateTierItemCover(targetItem.id, null)
                                itemForCoverEdit = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Удалить текущую обложку")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTierItemCover(targetItem.id, coverInput.trim().ifEmpty { null })
                        itemForCoverEdit = null
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemForCoverEdit = null }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Presets Bottom Sheet
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
        var targetRowId by remember { mutableStateOf<String?>(null) } // Default: Unassigned (null)

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
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customCoverUrl,
                        onValueChange = { customCoverUrl = it },
                        label = { Text("URL обложки (необязательно)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Куда поместить:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isUnassigned = targetRowId == null
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { targetRowId = null },
                            color = if (isUnassigned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (isUnassigned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "В неразмещённые",
                                fontWeight = if (isUnassigned) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (isUnassigned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        visibleTierRows.forEach { row ->
                            val isSelected = targetRowId == row.id
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { targetRowId = row.id },
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, Color(row.color))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(row.color))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = row.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customTitle.trim().isNotEmpty()) {
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
    dragState: TierDragDropState,
    onHeaderClick: () -> Unit,
    onItemClick: (TierItem) -> Unit,
    onItemStartDrag: (TierItem, Offset) -> Unit,
    onItemDragDelta: (Offset) -> Unit,
    onItemEndDrag: () -> Unit,
    onItemCancelDrag: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRowHovered = dragState.isDragging && dragState.currentDropTarget?.rowId == row.id
    val targetInsertIndex = if (isRowHovered) (dragState.currentDropTarget?.insertIndex ?: -1) else -1

    Card(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                dragState.registerRow(row.id, coords.boundsInRoot())
            }
            .testTag("tier_row_${row.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRowHovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = if (isRowHovered) 2.dp else 1.dp,
            color = if (isRowHovered) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp)
        ) {
            // Tier Label Header (Clickable to edit name, text color, background color via hex)
            Box(
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
                    .background(Color(row.color))
                    .clickable { onHeaderClick() }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(row.textColor),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
                    if (isRowHovered) {
                        InsertionSlotIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Перетащите или нажмите на тайтл, чтобы поместить сюда",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                } else {
                    if (isRowHovered && targetInsertIndex == 0) {
                        InsertionSlotIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    row.items.forEachIndexed { index, item ->
                        TierItemThumbnail(
                            item = item,
                            onClick = { onItemClick(item) },
                            onStartDrag = { pos -> onItemStartDrag(item, pos) },
                            onDragDelta = onItemDragDelta,
                            onEndDrag = onItemEndDrag,
                            onCancelDrag = onItemCancelDrag,
                            modifier = Modifier.onGloballyPositioned { coords ->
                                dragState.registerItem(row.id, index, coords.boundsInRoot())
                            }
                        )

                        if (isRowHovered && targetInsertIndex == index + 1) {
                            InsertionSlotIndicator(color = MaterialTheme.colorScheme.primary)
                        }
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
    onStartDrag: (Offset) -> Unit,
    onDragDelta: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onCancelDrag: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var itemPosition by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = modifier
            .width(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .onGloballyPositioned { coords ->
                itemPosition = coords.positionInRoot()
            }
            .pointerInput(item.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStartDrag(itemPosition + localOffset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount)
                    },
                    onDragEnd = onEndDrag,
                    onDragCancel = onCancelDrag
                )
            }
            .clickable { onClick() }
            .padding(2.dp)
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
    onStartDrag: (Offset) -> Unit,
    onDragDelta: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onCancelDrag: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var itemPosition by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = modifier
            .width(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .onGloballyPositioned { coords ->
                itemPosition = coords.positionInRoot()
            }
            .pointerInput(item.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStartDrag(itemPosition + localOffset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount)
                    },
                    onDragEnd = onEndDrag,
                    onDragCancel = onCancelDrag
                )
            }
            .clickable { onClick() }
            .padding(4.dp)
            .testTag("unassigned_item_${item.id}"),
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

@Composable
fun InsertionSlotIndicator(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(28.dp)
            .height(76.dp),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(2.dp, color)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun parseHexColor(hex: String, defaultColor: Long): Long {
    val clean = hex.trim().removePrefix("#")
    return try {
        when (clean.length) {
            6 -> ("FF$clean").toLong(16)
            8 -> clean.toLong(16)
            else -> defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

fun colorToHex(colorLong: Long): String {
    val hex = String.format("%08X", colorLong)
    return "#" + hex.substring(2)
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
