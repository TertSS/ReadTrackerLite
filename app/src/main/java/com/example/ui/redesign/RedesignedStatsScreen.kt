package com.example.ui.redesign

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.TitleStatus
import com.example.ui.screens.stats.GenreDistributionSection
import com.example.ui.viewmodel.ReadTrackerViewModel
import com.example.utils.formatNumberWordsRu

/**
 * 🌟 REDESIGNED 2.0 STATS & ANALYTICS SCREEN
 */
@Composable
fun RedesignedStatsScreen(
    viewModel: ReadTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allAdaptations by viewModel.allAdaptations.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    val totalReadVolumes = remember(allBooks) {
        allBooks.sumOf { book -> book.volumes.count { it.isRead } }
    }

    val totalReadWords = remember(allBooks) {
        allBooks.sumOf { book ->
            book.volumes.filter { it.isRead }.sumOf { it.wordCount.toLong() }
        }
    }

    val totalWebChaptersRead = remember(allBooks) {
        allBooks.sumOf { it.webChaptersRead }
    }

    val totalCompletedBooks = remember(allBooks) {
        allBooks.count { it.status == TitleStatus.COMPLETED }
    }

    val totalEpisodesWatched = remember(allAdaptations) {
        allAdaptations.sumOf { it.episodesWatched }
    }

    val totalCompletedAdaptations = remember(allAdaptations) {
        allAdaptations.count { it.status == TitleStatus.COMPLETED }
    }

    // Genre counts for chart
    val genreCounts = remember(allBooks, allAdaptations) {
        val map = mutableMapOf<String, Int>()
        allBooks.forEach { b -> b.genres.forEach { g -> map[g] = (map[g] ?: 0) + 1 } }
        allAdaptations.forEach { a -> a.genres.forEach { g -> map[g] = (map[g] ?: 0) + 1 } }
        map.toList().sortedByDescending { it.second }
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
                    text = "Статистика",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Аналитика вашего читательского пути",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Hero Metric Banner Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0x223B82F6),
                                    Color(0x118B5CF6),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Всего прочитано",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$totalReadVolumes томов",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(10.dp).size(24.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Grid of small metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RedesignedMiniMetric(
                                label = "Слов",
                                value = formatNumberWordsRu(totalReadWords)
                            )
                            RedesignedMiniMetric(
                                label = "Веб-глав",
                                value = "$totalWebChaptersRead"
                            )
                            RedesignedMiniMetric(
                                label = "Завершено",
                                value = "$totalCompletedBooks"
                            )
                            if (settings.adaptationsEnabled) {
                                RedesignedMiniMetric(
                                    label = "Серий",
                                    value = "$totalEpisodesWatched"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Yearly Goals Card (if enabled)
        if (settings.goalsEnabled) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Цели на год",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Volumes Goal
                        val volumesProgress = (totalReadVolumes.toFloat() / settings.volumesTarget.coerceAtLeast(1)).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Тома: $totalReadVolumes из ${settings.volumesTarget}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(volumesProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { volumesProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }

                        // Words Goal
                        val wordsProgress = (totalReadWords.toFloat() / settings.wordsTarget.coerceAtLeast(1)).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Слова: ${formatNumberWordsRu(totalReadWords)} из ${formatNumberWordsRu(settings.wordsTarget)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(wordsProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { wordsProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }
            }
        }

        // Interactive Genre Visualizer
        if (genreCounts.isNotEmpty() && settings.statsShowGenreDistribution) {
            item {
                GenreDistributionSection(
                    genreCounts = genreCounts,
                    allBooks = allBooks,
                    allAdaptations = allAdaptations,
                    settings = settings,
                    onOpenBook = { viewModel.openBookDetails(it) },
                    onOpenAdaptation = { viewModel.openAdaptationDetails(it) }
                )
            }
        }
    }
}

@Composable
private fun RedesignedMiniMetric(
    label: String,
    value: String
) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
