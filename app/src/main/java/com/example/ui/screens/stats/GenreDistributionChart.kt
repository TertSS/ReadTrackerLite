@file:OptIn(ExperimentalLayoutApi::class)

package com.example.ui.screens.stats

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Adaptation
import com.example.data.models.AppSettings
import com.example.data.models.BookTitle
import com.example.utils.PluralRu
import kotlin.math.*

// Vibrant modern palette for distinct genre identification
val GenrePalette = listOf(
    Color(0xFF38BDF8), // Sky Blue
    Color(0xFFA855F7), // Purple / Violet
    Color(0xFF34D399), // Emerald Mint
    Color(0xFFFBBF24), // Amber Gold
    Color(0xFFF43F5E), // Rose Coral
    Color(0xFF818CF8), // Indigo
    Color(0xFFFB923C), // Sunset Orange
    Color(0xFF2DD4BF), // Teal
    Color(0xFFA3E635), // Lime
    Color(0xFFE879F9), // Fuchsia
    Color(0xFF60A5FA), // Royal Blue
    Color(0xFFF472B6)  // Pink
)

/**
 * Main Container for Genre Distribution with interactive genre inspection.
 * (Chart type is configured exclusively in Settings).
 */
@Composable
fun GenreDistributionSection(
    genreCounts: List<Pair<String, Int>>,
    allBooks: List<BookTitle>,
    allAdaptations: List<Adaptation>,
    settings: AppSettings,
    onOpenBook: (String) -> Unit,
    onOpenAdaptation: (String) -> Unit,
    modifier: Modifier = Modifier,
    onChartTypeChange: ((String) -> Unit)? = null
) {
    if (genreCounts.isEmpty()) return

    val currentChartType = when (settings.genreChartType) {
        "RADAR" -> "RADAR"
        "DONUT" -> "DONUT"
        "BARS" -> "BARS"
        else -> "PETAL" // Default to the new Petal Rose chart
    }

    val chartSubtitle = when (currentChartType) {
        "PETAL" -> "Нажмите на лепесток для просмотра тайтлов"
        "RADAR" -> "Нажмите на вершину для просмотра тайтлов"
        "DONUT" -> "Нажмите на сектор для просмотра тайтлов"
        "BARS" -> "Нажмите на полосу для просмотра тайтлов"
        else -> "Нажмите на элемент для просмотра тайтлов"
    }

    var selectedGenre by remember { mutableStateOf<String?>(null) }

    // Clear selection if genre is no longer present
    LaunchedEffect(genreCounts) {
        if (selectedGenre != null && genreCounts.none { it.first == selectedGenre }) {
            selectedGenre = null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stats_genre_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Icon, Title, and Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (currentChartType) {
                                    "PETAL" -> Icons.Default.LocalFlorist
                                    "RADAR" -> Icons.Default.Hub
                                    "DONUT" -> Icons.Default.DonutLarge
                                    else -> Icons.Default.BarChart
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Распределение по жанрам",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = chartSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "${genreCounts.size} ${PluralRu.form(genreCounts.size.toLong(), "жанр", "жанра", "жанров")}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }

            // Chart Animation Container
            AnimatedContent(
                targetState = currentChartType,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "GenreChartStyleTransition"
            ) { type ->
                when (type) {
                    "PETAL" -> {
                        GenrePetalRoseChart(
                            genreCounts = genreCounts,
                            showCounts = settings.statsRadarShowItemCounts,
                            selectedGenre = selectedGenre,
                            onSelectGenre = { genre ->
                                selectedGenre = if (selectedGenre == genre) null else genre
                            }
                        )
                    }
                    "RADAR" -> {
                        GenreModernRadarChart(
                            genreCounts = genreCounts,
                            showCounts = settings.statsRadarShowItemCounts,
                            selectedGenre = selectedGenre,
                            onSelectGenre = { genre ->
                                selectedGenre = if (selectedGenre == genre) null else genre
                            }
                        )
                    }
                    "DONUT" -> {
                        GenreModernDonutChart(
                            genreCounts = genreCounts,
                            showCounts = settings.statsRadarShowItemCounts,
                            selectedGenre = selectedGenre,
                            onSelectGenre = { genre ->
                                selectedGenre = if (selectedGenre == genre) null else genre
                            }
                        )
                    }
                    else -> {
                        GenreRankedBarsChart(
                            genreCounts = genreCounts,
                            showCounts = settings.statsRadarShowItemCounts,
                            selectedGenre = selectedGenre,
                            onSelectGenre = { genre ->
                                selectedGenre = if (selectedGenre == genre) null else genre
                            }
                        )
                    }
                }
            }

            // Interactive Genre Inspector Card (shown when a genre is selected/tapped)
            AnimatedVisibility(
                visible = selectedGenre != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedGenre?.let { genreName ->
                    val matchingBooks = remember(allBooks, genreName) {
                        allBooks.filter { it.genres.contains(genreName) }
                    }
                    val matchingAdaptations = remember(allAdaptations, genreName) {
                        allAdaptations.filter { it.genres.contains(genreName) }
                    }
                    val totalGenreTags = remember(genreCounts) { genreCounts.sumOf { it.second }.coerceAtLeast(1) }
                    val currentCount = genreCounts.find { it.first == genreName }?.second ?: 0
                    val percent = ((currentCount.toFloat() / totalGenreTags) * 100).toInt()
                    val rankIndex = genreCounts.indexOfFirst { it.first == genreName } + 1
                    val genreColor = GenrePalette.getOrElse((rankIndex - 1).coerceAtLeast(0) % GenrePalette.size) { MaterialTheme.colorScheme.primary }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, genreColor.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Inspector Header
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
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(genreColor)
                                    )
                                    Text(
                                        text = genreName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = genreColor.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "#$rankIndex по доле",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = genreColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { selectedGenre = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Закрыть",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Metric Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${matchingBooks.size}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = PluralRu.form(matchingBooks.size.toLong(), "книга", "книги", "книг"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${matchingAdaptations.size}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = PluralRu.form(matchingAdaptations.size.toLong(), "экранизация", "экранизации", "экранизаций"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$percent%",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = genreColor
                                        )
                                        Text(
                                            text = "доля",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Quick title list (up to 4 items)
                            val sampleTitles = buildList {
                                matchingBooks.take(2).forEach { add(Pair(it.title, { onOpenBook(it.id) })) }
                                matchingAdaptations.take(2).forEach { add(Pair(it.title, { onOpenAdaptation(it.id) })) }
                            }

                            if (sampleTitles.isNotEmpty()) {
                                Text(
                                    text = "Примеры тайтлов в жанре:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    sampleTitles.forEach { (title, onClick) ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onClick() },
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerLowest
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🌸 MODERN ORGANIC PETAL (POLAR ROSE) CHART
 * Completely reimagines the genre chart with blooming organic flower petals with glowing gradients.
 */
@Composable
fun GenrePetalRoseChart(
    genreCounts: List<Pair<String, Int>>,
    showCounts: Boolean = true,
    selectedGenre: String? = null,
    onSelectGenre: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (genreCounts.isEmpty()) return

    val totalTags = remember(genreCounts) { genreCounts.sumOf { it.second }.coerceAtLeast(1) }
    val displayLimit = 7
    val topGenres = remember(genreCounts) { genreCounts.take(displayLimit) }
    val otherGenres = remember(genreCounts) { genreCounts.drop(displayLimit) }
    val othersCount = remember(otherGenres) { otherGenres.sumOf { it.second } }
    val maxCount = remember(topGenres) { topGenres.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1 }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Polar Petal Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(topGenres, totalTags) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist >= 20.dp.toPx()) {
                                var angle = atan2(dy, dx)
                                // Convert to standard 0..2PI relative to startAngle (-PI/2)
                                val startAngle = (-PI / 2).toFloat()
                                var relAngle = angle - startAngle
                                while (relAngle < 0) relAngle += (2 * PI).toFloat()
                                while (relAngle >= 2 * PI) relAngle -= (2 * PI).toFloat()

                                val n = topGenres.size + (if (othersCount > 0) 1 else 0)
                                val sectorAngle = (2 * PI / n).toFloat()
                                val index = (relAngle / sectorAngle).toInt().coerceIn(0, n - 1)

                                if (index < topGenres.size) {
                                    onSelectGenre(topGenres[index].first)
                                }
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = (min(size.width, size.height) / 2f) - 38.dp.toPx()
                if (maxRadius <= 0f) return@Canvas

                val n = topGenres.size + (if (othersCount > 0) 1 else 0)
                val sectorAngle = (2 * PI / n).toFloat()
                val startAngle = (-PI / 2).toFloat() // 12 o'clock

                // 1. Concentric ambient halo rings (25%, 50%, 75%, 100%)
                val ringLevels = 4
                for (lvl in 1..ringLevels) {
                    val r = maxRadius * (lvl.toFloat() / ringLevels)
                    drawCircle(
                        color = outlineVariantColor.copy(alpha = if (lvl == ringLevels) 0.35f else 0.12f),
                        radius = r,
                        center = center,
                        style = Stroke(
                            width = if (lvl == ringLevels) 1.2.dp.toPx() else 0.8.dp.toPx(),
                            pathEffect = if (lvl < ringLevels) PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) else null
                        )
                    )
                }

                // 2. Render each Flower Petal with glowing gradients & smooth curves
                val innerRadius = 24.dp.toPx()

                for (i in 0 until n) {
                    val isOther = i == topGenres.size
                    val genreName = if (isOther) "Другие" else topGenres[i].first
                    val count = if (isOther) othersCount else topGenres[i].second
                    val isSelected = selectedGenre == genreName

                    val fraction = (count.toFloat() / maxCount).coerceIn(0.22f, 1f)
                    val petalRadius = innerRadius + (maxRadius - innerRadius) * fraction + (if (isSelected) 8.dp.toPx() else 0f)

                    val angleCenter = startAngle + (i + 0.5f) * sectorAngle
                    val angleHalf = sectorAngle * 0.42f // 84% width for organic gap between petals
                    val angleLeft = angleCenter - angleHalf
                    val angleRight = angleCenter + angleHalf

                    val baseColor = if (isOther) Color(0xFF64748B) else GenrePalette[i % GenrePalette.size]

                    // Build Petal Path (Bézier curves for an organic curved flower petal)
                    val petalPath = Path()

                    // Start at inner arc left
                    val innerXLeft = center.x + innerRadius * cos(angleLeft)
                    val innerYLeft = center.y + innerRadius * sin(angleLeft)
                    petalPath.moveTo(innerXLeft, innerYLeft)

                    // Outer left and right points
                    val outerXLeft = center.x + petalRadius * 0.85f * cos(angleLeft)
                    val outerYLeft = center.y + petalRadius * 0.85f * sin(angleLeft)

                    val tipX = center.x + petalRadius * cos(angleCenter)
                    val tipY = center.y + petalRadius * sin(angleCenter)

                    val outerXRight = center.x + petalRadius * 0.85f * cos(angleRight)
                    val outerYRight = center.y + petalRadius * 0.85f * sin(angleRight)

                    val innerXRight = center.x + innerRadius * cos(angleRight)
                    val innerYRight = center.y + innerRadius * sin(angleRight)

                    // Curve to tip
                    petalPath.cubicTo(
                        outerXLeft, outerYLeft,
                        tipX - 10f * cos(angleCenter + PI.toFloat() / 2),
                        tipY - 10f * sin(angleCenter + PI.toFloat() / 2),
                        tipX, tipY
                    )
                    petalPath.cubicTo(
                        tipX + 10f * cos(angleCenter + PI.toFloat() / 2),
                        tipY + 10f * sin(angleCenter + PI.toFloat() / 2),
                        outerXRight, outerYRight,
                        innerXRight, innerYRight
                    )

                    // Close back along inner arc
                    petalPath.lineTo(innerXLeft, innerYLeft)
                    petalPath.close()

                    // Gradient fill: radial from center outwards
                    val fillAlpha = if (selectedGenre == null || isSelected) 0.55f else 0.22f
                    drawPath(
                        path = petalPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                baseColor.copy(alpha = fillAlpha * 0.4f),
                                baseColor.copy(alpha = fillAlpha),
                                baseColor.copy(alpha = fillAlpha * 0.9f)
                            ),
                            center = center,
                            radius = petalRadius
                        ),
                        style = Fill
                    )

                    // Glowing outer border
                    val strokeAlpha = if (selectedGenre == null || isSelected) 0.95f else 0.35f
                    val strokeW = if (isSelected) 2.4.dp.toPx() else 1.4.dp.toPx()
                    drawPath(
                        path = petalPath,
                        color = baseColor.copy(alpha = strokeAlpha),
                        style = Stroke(
                            width = strokeW,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Petal Tip Glow Dot
                    val dotRadius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx()
                    drawCircle(
                        color = surfaceColor,
                        radius = dotRadius + 1.5.dp.toPx(),
                        center = Offset(tipX, tipY)
                    )
                    drawCircle(
                        color = baseColor,
                        radius = dotRadius,
                        center = Offset(tipX, tipY)
                    )

                    // 3. Peripheral Genre Tag Labels (Smart placement outside petal tip)
                    val percent = ((count.toFloat() / totalTags) * 100).toInt().coerceAtLeast(1)
                    val labelDist = maxRadius + 18.dp.toPx()
                    val anchorX = center.x + labelDist * cos(angleCenter)
                    val anchorY = center.y + labelDist * sin(angleCenter)

                    val shortName = if (genreName.length > 9) genreName.take(8) + "…" else genreName
                    val labelText = if (showCounts) "$shortName ($count)" else "$shortName $percent%"

                    val textLayout = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) baseColor else onSurfaceColor
                        )
                    )

                    val tw = textLayout.size.width
                    val th = textLayout.size.height

                    val cosA = cos(angleCenter)
                    val sinA = sin(angleCenter)

                    val posX = when {
                        cosA > 0.25f -> anchorX
                        cosA < -0.25f -> anchorX - tw
                        else -> anchorX - tw / 2f
                    }

                    val posY = when {
                        sinA > 0.25f -> anchorY
                        sinA < -0.25f -> anchorY - th
                        else -> anchorY - th / 2f
                    }

                    val clampedX = posX.coerceIn(2.dp.toPx(), size.width - tw - 2.dp.toPx())
                    val clampedY = posY.coerceIn(2.dp.toPx(), size.height - th - 2.dp.toPx())

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(clampedX, clampedY)
                    )
                }

                // 4. Central Glowing Flower Nucleus
                drawCircle(
                    color = surfaceColor,
                    radius = innerRadius - 2.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.15f),
                    radius = innerRadius - 2.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.6f),
                    radius = innerRadius - 2.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Center Content inside Flower Core
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${topGenres.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }

        // Horizontal Genre Badges Legend with Tap Selection
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topGenres.forEachIndexed { i, (genre, count) ->
                val color = GenrePalette[i % GenrePalette.size]
                val percent = ((count.toFloat() / totalTags) * 100).toInt().coerceAtLeast(1)
                val isSelected = selectedGenre == genre

                GenreLegendBadge(
                    name = genre,
                    count = count,
                    percent = percent,
                    color = color,
                    isSelected = isSelected,
                    showCount = showCounts,
                    onClick = { onSelectGenre(genre) }
                )
            }

            if (othersCount > 0) {
                val othersPercent = ((othersCount.toFloat() / totalTags) * 100).toInt()
                GenreLegendBadge(
                    name = "Другие (${otherGenres.size})",
                    count = othersCount,
                    percent = othersPercent,
                    color = Color(0xFF64748B),
                    isSelected = false,
                    showCount = showCounts,
                    onClick = {}
                )
            }
        }
    }
}

/**
 * 🕸️ MODERN POLYGON RADAR CHART
 * Clean high-tech polygon spider chart with glowing vertices and vibrant gradients.
 */
@Composable
fun GenreModernRadarChart(
    genreCounts: List<Pair<String, Int>>,
    showCounts: Boolean = true,
    selectedGenre: String? = null,
    onSelectGenre: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (genreCounts.isEmpty()) return

    val totalTags = remember(genreCounts) { genreCounts.sumOf { it.second }.coerceAtLeast(1) }
    val displayGenres = remember(genreCounts) { genreCounts.take(6) }
    val maxCount = remember(displayGenres) { displayGenres.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1 }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val n = displayGenres.size
                if (n < 3) return@Canvas

                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = (min(size.width, size.height) / 2f) - 36.dp.toPx()
                if (maxRadius <= 0f) return@Canvas

                val angleStep = (2 * PI / n).toFloat()
                val startAngle = (-PI / 2).toFloat()

                // Spider grid web
                val levels = 4
                for (level in 1..levels) {
                    val levelRadius = maxRadius * (level.toFloat() / levels)
                    val gridPath = Path()
                    for (i in 0 until n) {
                        val angle = startAngle + i * angleStep
                        val x = center.x + levelRadius * cos(angle)
                        val y = center.y + levelRadius * sin(angle)
                        if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                    }
                    gridPath.close()

                    drawPath(
                        path = gridPath,
                        color = outlineVariantColor.copy(alpha = if (level == levels) 0.35f else 0.15f),
                        style = Stroke(width = if (level == levels) 1.2.dp.toPx() else 0.8.dp.toPx())
                    )
                }

                // Spokes
                for (i in 0 until n) {
                    val angle = startAngle + i * angleStep
                    val endX = center.x + maxRadius * cos(angle)
                    val endY = center.y + maxRadius * sin(angle)
                    drawLine(
                        color = outlineVariantColor.copy(alpha = 0.2f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 0.8.dp.toPx()
                    )
                }

                // Polygon data shape
                val dataPath = Path()
                val points = mutableListOf<Offset>()

                for (i in 0 until n) {
                    val (_, count) = displayGenres[i]
                    val fraction = (count.toFloat() / maxCount).coerceIn(0.2f, 1f)
                    val r = maxRadius * fraction
                    val angle = startAngle + i * angleStep
                    val pt = Offset(center.x + r * cos(angle), center.y + r * sin(angle))
                    points.add(pt)
                    if (i == 0) dataPath.moveTo(pt.x, pt.y) else dataPath.lineTo(pt.x, pt.y)
                }
                dataPath.close()

                drawPath(
                    path = dataPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.45f),
                            primaryColor.copy(alpha = 0.15f)
                        ),
                        center = center,
                        radius = maxRadius
                    ),
                    style = Fill
                )

                drawPath(
                    path = dataPath,
                    color = primaryColor,
                    style = Stroke(
                        width = 2.2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Vertices
                points.forEachIndexed { i, pt ->
                    val color = GenrePalette[i % GenrePalette.size]
                    drawCircle(
                        color = surfaceColor,
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = color,
                        radius = 3.2.dp.toPx(),
                        center = pt
                    )
                }

                // Labels
                for (i in 0 until n) {
                    val (genreName, count) = displayGenres[i]
                    val percent = ((count.toFloat() / totalTags) * 100).toInt()
                    val angle = startAngle + i * angleStep
                    val labelDist = maxRadius + 14.dp.toPx()
                    val anchorX = center.x + labelDist * cos(angle)
                    val anchorY = center.y + labelDist * sin(angle)

                    val shortName = if (genreName.length > 9) genreName.take(8) + "…" else genreName
                    val labelText = if (showCounts) "$shortName ($count)" else "$shortName $percent%"

                    val textLayout = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurfaceColor
                        )
                    )

                    val tw = textLayout.size.width
                    val th = textLayout.size.height
                    val cosA = cos(angle)
                    val sinA = sin(angle)

                    val posX = when {
                        cosA > 0.2f -> anchorX
                        cosA < -0.2f -> anchorX - tw
                        else -> anchorX - tw / 2f
                    }

                    val posY = when {
                        sinA > 0.2f -> anchorY
                        sinA < -0.2f -> anchorY - th
                        else -> anchorY - th / 2f
                    }

                    val clampedX = posX.coerceIn(2.dp.toPx(), size.width - tw - 2.dp.toPx())
                    val clampedY = posY.coerceIn(2.dp.toPx(), size.height - th - 2.dp.toPx())

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(clampedX, clampedY)
                    )
                }
            }
        }

        // Legend Badges
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            displayGenres.forEachIndexed { i, (genre, count) ->
                val color = GenrePalette[i % GenrePalette.size]
                val percent = ((count.toFloat() / totalTags) * 100).toInt().coerceAtLeast(1)
                GenreLegendBadge(
                    name = genre,
                    count = count,
                    percent = percent,
                    color = color,
                    isSelected = selectedGenre == genre,
                    showCount = showCounts,
                    onClick = { onSelectGenre(genre) }
                )
            }
        }
    }
}

/**
 * 🍩 MODERN DONUT SPECTRUM CHART
 */
@Composable
fun GenreModernDonutChart(
    genreCounts: List<Pair<String, Int>>,
    showCounts: Boolean = true,
    selectedGenre: String? = null,
    onSelectGenre: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (genreCounts.isEmpty()) return

    val totalTags = remember(genreCounts) { genreCounts.sumOf { it.second }.coerceAtLeast(1) }
    val topLimit = 6
    val topGenres = remember(genreCounts) { genreCounts.take(topLimit) }
    val otherGenres = remember(genreCounts) { genreCounts.drop(topLimit) }
    val othersCount = remember(otherGenres) { otherGenres.sumOf { it.second } }
    val hasOthers = othersCount > 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Donut
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                val strokeWidth = 14.dp

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasSize = size.minDimension
                    val arcSize = Size(canvasSize - strokeWidth.toPx(), canvasSize - strokeWidth.toPx())
                    val topLeft = Offset(strokeWidth.toPx() / 2f, strokeWidth.toPx() / 2f)

                    // Track
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )

                    var currentAngle = -90f
                    val slices = buildList {
                        topGenres.forEachIndexed { i, pair ->
                            add(Triple(pair.first, pair.second, GenrePalette[i % GenrePalette.size]))
                        }
                        if (hasOthers) {
                            add(Triple("Другие", othersCount, Color(0xFF64748B)))
                        }
                    }

                    val gapAngle = if (slices.size > 1) 3f else 0f

                    slices.forEach { (genreName, count, color) ->
                        val rawSweep = (count.toFloat() / totalTags) * 360f
                        val sweep = (rawSweep - gapAngle).coerceAtLeast(2f)
                        val isSelected = selectedGenre == genreName

                        drawArc(
                            color = if (isSelected) color else color.copy(alpha = 0.85f),
                            startAngle = currentAngle + (gapAngle / 2f),
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = if (isSelected) (strokeWidth + 3.dp).toPx() else strokeWidth.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                        currentAngle += rawSweep
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$totalTags",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "тегов",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right Legend
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                topGenres.forEachIndexed { i, (genre, count) ->
                    val color = GenrePalette[i % GenrePalette.size]
                    val percent = ((count.toFloat() / totalTags) * 100).toInt().coerceAtLeast(1)
                    val isSelected = selectedGenre == genre

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectGenre(genre) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = if (showCounts) "$count ($percent%)" else "$percent%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 📊 RANKED BARS CASCADE CHART
 */
@Composable
fun GenreRankedBarsChart(
    genreCounts: List<Pair<String, Int>>,
    showCounts: Boolean = true,
    selectedGenre: String? = null,
    onSelectGenre: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTags = remember(genreCounts) { genreCounts.sumOf { it.second }.coerceAtLeast(1) }
    val displayLimit = 6
    val topGenres = remember(genreCounts) { genreCounts.take(displayLimit) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        topGenres.forEachIndexed { index, (genre, count) ->
            val color = GenrePalette[index % GenrePalette.size]
            val percent = ((count.toFloat() / totalTags) * 100).toInt().coerceAtLeast(1)
            val fraction = count.toFloat() / totalTags
            val isSelected = selectedGenre == genre

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectGenre(genre) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = color.copy(alpha = 0.2f),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (showCounts) {
                                Text(
                                    text = "$count ${PluralRu.form(count.toLong(), "тайтл", "тайтла", "тайтлов")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = color.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GenreLegendBadge(
    name: String,
    count: Int,
    percent: Int,
    color: Color,
    isSelected: Boolean,
    showCount: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else color.copy(alpha = 0.12f),
        border = BorderStroke(
            1.dp,
            if (isSelected) color else color.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (showCount) "$count ($percent%)" else "$percent%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
