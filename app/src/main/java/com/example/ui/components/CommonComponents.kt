package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.*
import com.example.ui.theme.*

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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Text(
            text = format,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp
        )
    }
}

@Composable
fun StarRatingBar(
    rating: Float, // 0..10
    scale: RatingScale = RatingScale.STARS_10,
    allowDecimal: Boolean = false,
    editable: Boolean = false,
    onRatingChanged: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val maxCount = if (scale == RatingScale.STARS_5) 5 else 10
    val effectiveRating = if (scale == RatingScale.STARS_5) rating / 2f else rating
    val maxVal = if (scale == RatingScale.STARS_5) 5f else 10f

    Column(
        modifier = modifier.testTag("star_rating_bar"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val starBtnSize = if (editable) (if (maxCount == 10) 28.dp else 36.dp) else 18.dp
            val starIconSize = if (editable) (if (maxCount == 10) 22.dp else 28.dp) else 15.dp

            for (i in 1..maxCount) {
                val isFilled = i <= effectiveRating + 0.25f
                val isHalf = !isFilled && i <= effectiveRating + 0.75f
                val icon = when {
                    isFilled -> Icons.Default.Star
                    isHalf -> Icons.Default.StarHalf
                    else -> Icons.Default.StarBorder
                }
                val tint = if (isFilled || isHalf) StarGold else MaterialTheme.colorScheme.surfaceContainerHighest

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
                    modifier = Modifier.size(starBtnSize)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Звезда $i",
                        tint = tint,
                        modifier = Modifier.size(starIconSize)
                    )
                }
            }

            if (editable && rating > 0f) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { onRatingChanged?.invoke(0f) },
                    modifier = Modifier.size(28.dp)
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

        // Decimal rating slider for precise tuning (e.g. 3.3, 4.5, 7.6)
        if (editable && allowDecimal && onRatingChanged != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (rating > 0f) String.format(java.util.Locale.US, "%.1f", effectiveRating) else "0.0",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(28.dp)
                )
                Slider(
                    value = effectiveRating.coerceIn(0f, maxVal),
                    onValueChange = { valRounded ->
                        val stepRounded = (Math.round(valRounded * 10f) / 10f).coerceIn(0f, maxVal)
                        val actualRating = if (scale == RatingScale.STARS_5) stepRounded * 2f else stepRounded
                        onRatingChanged(actualRating)
                    },
                    valueRange = 0f..maxVal,
                    steps = (maxVal * 10).toInt() - 1,
                    modifier = Modifier.weight(1f)
                )
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
            AsyncImage(
                model = coverUrl,
                contentDescription = "Обложка $title",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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

@Composable
fun CardBookmarkIndicator(
    bookmark: String,
    modifier: Modifier = Modifier
) {
    if (bookmark.isNotBlank()) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Закладка",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = bookmark,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuickBookmarkDialog(
    initialValue: String,
    title: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text("Закладка: $title", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Укажите, на чем вы остановились (глава, том, серия, арка, страница):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Например: Том 2, Глава 14") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(text.trim())
                    onDismiss()
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp)
    )
}
