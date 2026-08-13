package com.example.ui.redesign

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.models.BookTitle
import com.example.data.models.TierListRow
import com.example.ui.viewmodel.ReadTrackerViewModel

data class TierCategory(
    val id: String,
    val name: String,
    val colorStart: Color,
    val colorEnd: Color
)

val DefaultTierCategories = listOf(
    TierCategory("S", "S", Color(0xFFFF5252), Color(0xFFFF7A00)),
    TierCategory("A", "A", Color(0xFFFF9100), Color(0xFFFFD600)),
    TierCategory("B", "B", Color(0xFF00E676), Color(0xFF1DE9B6)),
    TierCategory("C", "C", Color(0xFF00B0FF), Color(0xFF2979FF)),
    TierCategory("D", "D", Color(0xFF7C4DFF), Color(0xFF651FFF))
)

/**
 * 🌟 REDESIGNED 2.0 TIER LIST SCREEN
 */
@Composable
fun RedesignedTierListScreen(
    viewModel: ReadTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val tierRows by viewModel.tierListRows.collectAsStateWithLifecycle()

    var selectedBookForPlacement by remember { mutableStateOf<BookTitle?>(null) }

    // Map tier items
    val tierItemsMap = remember(tierRows, allBooks) {
        val map = mutableMapOf<String, MutableList<BookTitle>>()
        DefaultTierCategories.forEach { map[it.id] = mutableListOf() }
        
        tierRows.forEach { row ->
            val tierId = row.tierName
            row.itemIds.forEach { bookId ->
                val book = allBooks.find { it.id == bookId }
                if (book != null) {
                    map.getOrPut(tierId) { mutableListOf() }.add(book)
                }
            }
        }
        map
    }

    val placedBookIds = remember(tierRows) {
        tierRows.flatMap { it.itemIds }.toSet()
    }

    val unrankedBooks = remember(allBooks, placedBookIds) {
        allBooks.filter { it.id !in placedBookIds }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Тир-лист тайтлов",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Распределите прочитанные тайтлы по рангам от S до D",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tiers Rows
        items(DefaultTierCategories, key = { it.id }) { category ->
            val booksInTier = tierItemsMap[category.id] ?: emptyList()

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tier Rank Badge Box
                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    listOf(category.colorStart, category.colorEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Horizontal Scrolling Books List in Tier
                    if (booksInTier.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Нажмите на тайтл внизу, чтобы добавить сюда",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(booksInTier, key = { it.id }) { book ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .size(54.dp, 76.dp)
                                        .clickable { selectedBookForPlacement = book }
                                ) {
                                    if (!book.coverUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = book.coverUrl,
                                            contentDescription = book.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color(0xFF1E293B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(book.title.take(2), fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Unranked Pool
        if (unrankedBooks.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Нераспределенные тайтлы (${unrankedBooks.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unrankedBooks, key = { it.id }) { book ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .size(64.dp, 90.dp)
                                    .clickable { selectedBookForPlacement = book }
                            ) {
                                if (!book.coverUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = book.coverUrl,
                                        contentDescription = book.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(book.title.take(2), fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to assign book to a tier
    if (selectedBookForPlacement != null) {
        val book = selectedBookForPlacement!!
        AlertDialog(
            onDismissRequest = { selectedBookForPlacement = null },
            title = { Text("Назначить ранг для «${book.title}»", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DefaultTierCategories.forEach { tier ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Remove from any existing tier, then add to chosen
                                    val currentRows = tierRows.toMutableList()
                                    // Remove
                                    val cleanRows = currentRows.map { row ->
                                        row.copy(itemIds = row.itemIds.filter { it != book.id })
                                    }.toMutableList()
                                    // Add to target
                                    val targetIndex = cleanRows.indexOfFirst { it.tierName == tier.id }
                                    if (targetIndex >= 0) {
                                        cleanRows[targetIndex] = cleanRows[targetIndex].copy(
                                            itemIds = cleanRows[targetIndex].itemIds + book.id
                                        )
                                    } else {
                                        cleanRows.add(
                                            TierListRow(
                                                tierName = tier.id,
                                                tierColorHex = "#FF0000",
                                                itemIds = listOf(book.id)
                                            )
                                        )
                                    }
                                    cleanRows.forEach { viewModel.saveTierListRow(it) }
                                    selectedBookForPlacement = null
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(tier.colorStart),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(tier.name, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Text("Ранг ${tier.name}", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Option to unrank
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cleanRows = tierRows.map { row ->
                                    row.copy(itemIds = row.itemIds.filter { it != book.id })
                                }
                                cleanRows.forEach { viewModel.saveTierListRow(it) }
                                selectedBookForPlacement = null
                            }
                    ) {
                        Text(
                            text = "Убрать из тир-листа (в нераспределенные)",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBookForPlacement = null }) {
                    Text("Закрыть")
                }
            }
        )
    }
}
