package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.data.models.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ReadTrackerBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar"),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    Triple("library", "Библиотека", Icons.Default.AutoStories),
                    Triple("reviews", "Отзывы", Icons.Default.RateReview),
                    Triple("stats", "Статистика", Icons.Default.Leaderboard),
                    Triple("tier_list", "Тир-лист", Icons.Default.FormatListNumbered),
                    Triple("settings", "Настройки", Icons.Default.Settings)
                )

                tabs.forEach { (id, label, icon) ->
                    val selected = currentTab == id
                    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onTabSelected(id) }
                            .padding(vertical = 4.dp)
                            .testTag("nav_tab_$id"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = color,
                            maxLines = 1,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeTogglePill(
    currentMode: LibraryMode,
    onModeChanged: (LibraryMode) -> Unit,
    showAdaptations: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .testTag("mode_toggle_pill"),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isBooks = currentMode == LibraryMode.BOOKS
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isBooks) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onModeChanged(LibraryMode.BOOKS) }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("mode_books_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Книги",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isBooks) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showAdaptations) {
                val isAdap = currentMode == LibraryMode.ADAPTATIONS
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isAdap) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onModeChanged(LibraryMode.ADAPTATIONS) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("mode_adaptations_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Экранизации",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdap) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: TitleStatus,
    isAdaptation: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (status) {
        TitleStatus.READING -> Triple(
            StatusReadingColor.copy(alpha = 0.15f),
            StatusReadingColor,
            if (isAdaptation) "Смотрю" else "Читаю"
        )
        TitleStatus.PLANNED -> Triple(
            StatusPlannedColor.copy(alpha = 0.15f),
            StatusPlannedColor,
            "В планах"
        )
        TitleStatus.COMPLETED -> Triple(
            StatusCompletedColor.copy(alpha = 0.15f),
            StatusCompletedColor,
            if (isAdaptation) "Просмотрено" else "Завершено"
        )
        TitleStatus.PAUSED -> Triple(
            StatusPausedColor.copy(alpha = 0.15f),
            StatusPausedColor,
            "На паузе"
        )
        TitleStatus.DROPPED -> Triple(
            StatusDroppedColor.copy(alpha = 0.15f),
            StatusDroppedColor,
            "Брошено"
        )
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun FormatBadge(
    format: String,
    modifier: Modifier = Modifier,
    alignFlush: Boolean = false
) {
    Surface(
        modifier = modifier
            .then(if (alignFlush) Modifier.offset(x = (-4).dp) else Modifier)
            .clip(RoundedCornerShape(6.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Text(
            text = format,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                start = if (alignFlush) 4.dp else 6.dp,
                end = if (alignFlush) 5.dp else 6.dp,
                top = 2.dp,
                bottom = 2.dp
            ),
            fontSize = 10.sp
        )
    }
}

@Composable
fun StarRatingBar(
    rating: Float, // 0..10
    scale: RatingScale = RatingScale.STARS_10,
    editable: Boolean = false,
    allowFractional: Boolean = false,
    onRatingChanged: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val maxCount = if (scale == RatingScale.STARS_5) 5 else 10
    val effectiveRating = if (scale == RatingScale.STARS_5) rating / 2f else rating
    val isStar10 = scale == RatingScale.STARS_10

    Column(
        modifier = modifier.testTag("star_rating_bar"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 1..maxCount) {
                val icon = when {
                    effectiveRating >= i - 0.2f -> Icons.Default.Star
                    effectiveRating >= i - 0.7f -> Icons.AutoMirrored.Filled.StarHalf
                    else -> Icons.Default.StarBorder
                }
                val isFilled = effectiveRating >= i - 0.7f
                val tint = if (isFilled) StarGold else MaterialTheme.colorScheme.surfaceContainerHighest

                val btnSize = if (editable) (if (isStar10) 26.dp else 34.dp) else 18.dp
                val iconSize = if (editable) (if (isStar10) 20.dp else 26.dp) else 14.dp

                IconButton(
                    onClick = {
                        if (editable && onRatingChanged != null) {
                            val newRating = if (scale == RatingScale.STARS_5) i * 2f else i.toFloat()
                            if (effectiveRating == i.toFloat()) {
                                onRatingChanged(0f) // Reset
                            } else {
                                onRatingChanged(newRating)
                            }
                        }
                    },
                    enabled = editable,
                    modifier = Modifier.size(btnSize)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Звезда $i",
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            if (editable && rating > 0f) {
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = { onRatingChanged?.invoke(0f) },
                    modifier = Modifier.size(if (isStar10) 24.dp else 28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Сбросить оценку",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Fractional precision slider / stepper when enabled
        if (editable && allowFractional) {
            val maxLimit = if (scale == RatingScale.STARS_5) 5f else 10f
            val currentScore = if (scale == RatingScale.STARS_5) rating / 2f else rating
            val scoreText = if (currentScore % 1f == 0f) String.format(Locale.US, "%.0f", currentScore) else String.format(Locale.US, "%.1f", currentScore)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "★ $scoreText/$maxLimit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Slider(
                    value = currentScore.coerceIn(0f, maxLimit),
                    onValueChange = { newVal ->
                        val rounded = Math.round(newVal * 10f) / 10f
                        val actualRating = if (scale == RatingScale.STARS_5) rounded * 2f else rounded
                        onRatingChanged?.invoke(actualRating)
                    },
                    valueRange = 0f..maxLimit,
                    steps = ((maxLimit * 10).toInt() - 1),
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = StarGold,
                        activeTrackColor = StarGold,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )

                // Quick -0.1 and +0.1 buttons
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    FilledTonalIconButton(
                        onClick = {
                            val next = (currentScore - 0.1f).coerceAtLeast(0f)
                            val rounded = Math.round(next * 10f) / 10f
                            val actualRating = if (scale == RatingScale.STARS_5) rounded * 2f else rounded
                            onRatingChanged?.invoke(actualRating)
                        },
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    FilledTonalIconButton(
                        onClick = {
                            val next = (currentScore + 0.1f).coerceAtMost(maxLimit)
                            val rounded = Math.round(next * 10f) / 10f
                            val actualRating = if (scale == RatingScale.STARS_5) rounded * 2f else rounded
                            onRatingChanged?.invoke(actualRating)
                        },
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("+", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CoverImage(
    coverUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    width: Dp = 80.dp,
    corner: Dp = 10.dp
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(corner)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!coverUrl.isNullOrBlank()) {
            val context = LocalContext.current
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Обложка $title",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                error = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize().padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = title.take(2).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title.take(2).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String = "Удалить",
    cancelText: String = "Отмена",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = if (isDestructive) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(cancelText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
