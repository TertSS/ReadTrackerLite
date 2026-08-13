package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.*
import com.example.ui.theme.LocalStatusColors
import com.example.ui.theme.StarGold
import com.example.utils.Formatters

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicBookCard(
    book: BookTitle,
    config: CardLayoutConfig,
    ratingScale: RatingScale = RatingScale.STARS_10,
    ratingEnabled: Boolean = true,
    bookmarksEnabled: Boolean = true,
    shortenNumbers: Boolean = false,
    alignFormatWithTitle: Boolean = false,
    isGrid: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (isGrid) {
        DynamicBookGridCard(
            book = book,
            config = config,
            ratingScale = ratingScale,
            ratingEnabled = ratingEnabled,
            bookmarksEnabled = bookmarksEnabled,
            shortenNumbers = shortenNumbers,
            alignFormatWithTitle = alignFormatWithTitle,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = modifier
        )
    } else {
        DynamicBookListCard(
            book = book,
            config = config,
            ratingScale = ratingScale,
            ratingEnabled = ratingEnabled,
            bookmarksEnabled = bookmarksEnabled,
            shortenNumbers = shortenNumbers,
            alignFormatWithTitle = alignFormatWithTitle,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicBookGridCard(
    book: BookTitle,
    config: CardLayoutConfig,
    ratingScale: RatingScale,
    ratingEnabled: Boolean,
    bookmarksEnabled: Boolean,
    shortenNumbers: Boolean,
    alignFormatWithTitle: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val hasCover = !book.coverUrl.isNullOrBlank() && config.gridCoverStyle != "NONE"
    val isFullBg = hasCover && config.gridCoverStyle == "FULL_BACKGROUND"
    val shape = RoundedCornerShape(config.cardCornerRadiusDp.dp)

    val containerColor = when (config.surfaceStyle) {
        "SURFACE_CONTAINER" -> MaterialTheme.colorScheme.surfaceContainer
        "SURFACE_HIGH" -> MaterialTheme.colorScheme.surfaceContainerHigh
        "GLASS_GRADIENT" -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (isFullBg) 0.85f else 1f)
        "OUTLINE" -> Color.Transparent
        "TONAL" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val borderStroke = if (config.borderWidthDp > 0f) {
        val borderColor = when (config.surfaceStyle) {
            "OUTLINE" -> MaterialTheme.colorScheme.outline
            "GLASS_GRADIENT" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        }
        BorderStroke(config.borderWidthDp.dp, borderColor)
    } else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .testTag("book_card_${book.id}"),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke
    ) {
        if (isFullBg) {
            // Full Background Cover with Scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(config.coverAspectRatioValue)
            ) {
                // Background Cover
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = book.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient Scrim Overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 0.96f)
                                )
                            )
                        )
                )

                // Overlay elements inside full-cover layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header row (Top overlays)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            if (config.formatVisible && config.formatPosition in listOf("COVER_TOP_START", "BODY_TOP_START")) {
                                RenderFormatBadge(book.format.shortLabel, config.formatStyle, alignFormatWithTitle)
                            }
                            if (config.ratingVisible && ratingEnabled && book.rating > 0f && config.ratingPosition == "COVER_TOP_START") {
                                RenderRatingBadge(book.rating, ratingScale)
                            }
                        }

                        Box {
                            if (config.statusVisible && config.statusPosition in listOf("COVER_TOP_END", "BODY_TOP_END")) {
                                RenderStatusBadge(book.status, config.statusStyle, isAdaptation = false)
                            }
                        }
                    }

                    // Bottom info block
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Title
                        Text(
                            text = book.title,
                            style = getTitleTypography(config.titleTextSize),
                            fontWeight = FontWeight.Bold,
                            maxLines = config.titleMaxLines,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )

                        // Author
                        if (config.authorVisible && book.author.isNotBlank() && config.authorPosition == "BELOW_TITLE") {
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f),
                                maxLines = config.authorMaxLines,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Info Row (Words, Bookmarks, Rating)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (config.wordsVisible && config.wordsPosition == "INFO_ROW") {
                                Text(
                                    text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (config.bookmarkVisible && bookmarksEnabled && book.bookmark.isNotBlank() && config.bookmarkPosition == "INFO_ROW") {
                                BookmarkChip(bookmark = book.bookmark, modifier = Modifier.weight(1f, fill = false))
                            }
                        }

                        // Footer row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                if (config.ratingVisible && ratingEnabled && book.rating > 0f && config.ratingPosition == "FOOTER_START") {
                                    RenderRatingBadge(book.rating, ratingScale)
                                } else if (config.wordsVisible && config.wordsPosition == "FOOTER_START") {
                                    Text(
                                        text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            if (config.progressVisible && config.progressPosition == "FOOTER_END") {
                                RenderProgressText(book, config.progressStyle)
                            }
                        }

                        // Progress Bar if bottom
                        if (config.progressBarVisible && config.progressBarPosition == "BOTTOM_OF_CARD") {
                            LinearProgressIndicator(
                                progress = { book.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(config.progressBarHeightDp.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        } else {
            // Standard / Banner / Top Cover layout
            Column(modifier = Modifier.fillMaxWidth()) {
                if (hasCover) {
                    val coverAspect = if (config.gridCoverStyle == "COMPACT_BANNER") 1.78f else config.coverAspectRatioValue
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(coverAspect)
                            .clip(RoundedCornerShape(topStart = config.cardCornerRadiusDp.dp, topEnd = config.cardCornerRadiusDp.dp))
                    ) {
                        CoverImage(
                            coverUrl = book.coverUrl,
                            title = book.title,
                            modifier = Modifier.fillMaxSize(),
                            corner = config.coverCornerRadiusDp.dp
                        )

                        // Top Start Slot on Cover
                        Box(modifier = Modifier.padding(8.dp).align(Alignment.TopStart)) {
                            if (config.formatVisible && config.formatPosition == "COVER_TOP_START") {
                                RenderFormatBadge(book.format.shortLabel, config.formatStyle, alignFormatWithTitle)
                            } else if (config.ratingVisible && ratingEnabled && book.rating > 0f && config.ratingPosition == "COVER_TOP_START") {
                                RenderRatingBadge(book.rating, ratingScale)
                            }
                        }

                        // Top End Slot on Cover
                        Box(modifier = Modifier.padding(8.dp).align(Alignment.TopEnd)) {
                            if (config.statusVisible && config.statusPosition == "COVER_TOP_END") {
                                RenderStatusBadge(book.status, config.statusStyle, isAdaptation = false)
                            }
                        }

                        // Bottom Cover Progress Bar
                        if (config.progressBarVisible && config.progressBarPosition == "BOTTOM_OF_COVER") {
                            LinearProgressIndicator(
                                progress = { book.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(config.progressBarHeightDp.dp)
                                    .align(Alignment.BottomCenter),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }

                // Info Body Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Body Header (if elements are placed in body header)
                    if (config.statusPosition in listOf("BODY_TOP_END", "BODY_TOP_START") || config.formatPosition in listOf("BODY_TOP_START", "BODY_TOP_END") || (!hasCover && config.statusPosition.startsWith("COVER"))) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (config.formatVisible && (config.formatPosition in listOf("BODY_TOP_START", "COVER_TOP_START") || !hasCover)) {
                                RenderFormatBadge(book.format.shortLabel, config.formatStyle, alignFormatWithTitle)
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                            if (config.statusVisible && (config.statusPosition in listOf("BODY_TOP_END", "COVER_TOP_END") || !hasCover)) {
                                RenderStatusBadge(book.status, config.statusStyle, isAdaptation = false)
                            }
                        }
                    }

                    // Title
                    Text(
                        text = book.title,
                        style = getTitleTypography(config.titleTextSize),
                        fontWeight = FontWeight.Bold,
                        maxLines = config.titleMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Author
                    if (config.authorVisible && book.author.isNotBlank() && config.authorPosition == "BELOW_TITLE") {
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = config.authorMaxLines,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Info Row
                    val hasInfoRow = (config.wordsVisible && config.wordsPosition == "INFO_ROW") ||
                            (config.bookmarkVisible && bookmarksEnabled && book.bookmark.isNotBlank() && config.bookmarkPosition == "INFO_ROW")
                    if (hasInfoRow) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (config.wordsVisible && config.wordsPosition == "INFO_ROW") {
                                Text(
                                    text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (config.bookmarkVisible && bookmarksEnabled && book.bookmark.isNotBlank() && config.bookmarkPosition == "INFO_ROW") {
                                BookmarkChip(bookmark = book.bookmark, modifier = Modifier.weight(1f, fill = false))
                            }
                        }
                    }

                    // Inside Body Progress Bar
                    if (config.progressBarVisible && config.progressBarPosition == "INSIDE_BODY") {
                        LinearProgressIndicator(
                            progress = { book.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(config.progressBarHeightDp.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }

                    // Footer Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            if (config.ratingVisible && ratingEnabled && book.rating > 0f && config.ratingPosition == "FOOTER_START") {
                                RenderRatingBadge(book.rating, ratingScale)
                            } else if (config.wordsVisible && config.wordsPosition == "FOOTER_START") {
                                Text(
                                    text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                        }

                        if (config.progressVisible && config.progressPosition == "FOOTER_END") {
                            RenderProgressText(book, config.progressStyle)
                        }
                    }

                    // Bottom of Card Progress Bar
                    if (config.progressBarVisible && config.progressBarPosition == "BOTTOM_OF_CARD") {
                        LinearProgressIndicator(
                            progress = { book.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(config.progressBarHeightDp.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicBookListCard(
    book: BookTitle,
    config: CardLayoutConfig,
    ratingScale: RatingScale,
    ratingEnabled: Boolean,
    bookmarksEnabled: Boolean,
    shortenNumbers: Boolean,
    alignFormatWithTitle: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val hasCover = !book.coverUrl.isNullOrBlank() && config.listCoverPosition != "NONE"
    val shape = RoundedCornerShape(config.cardCornerRadiusDp.dp)

    val containerColor = when (config.surfaceStyle) {
        "SURFACE_CONTAINER" -> MaterialTheme.colorScheme.surfaceContainer
        "SURFACE_HIGH" -> MaterialTheme.colorScheme.surfaceContainerHigh
        "OUTLINE" -> Color.Transparent
        "TONAL" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val borderStroke = if (config.borderWidthDp > 0f) {
        BorderStroke(config.borderWidthDp.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    } else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .testTag("book_card_${book.id}"),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(config.listCardPaddingDp.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Cover
            if (hasCover && config.listCoverPosition == "LEFT") {
                CoverImage(
                    coverUrl = book.coverUrl,
                    title = book.title,
                    width = config.listCoverWidthDp.dp,
                    height = (config.listCoverWidthDp / config.listCoverAspectRatioValue).dp,
                    corner = config.coverCornerRadiusDp.coerceAtLeast(6).dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Main Info Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header row in list item
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book.title,
                        style = getTitleTypography(config.titleTextSize),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (config.statusVisible) {
                        RenderStatusBadge(book.status, config.statusStyle, isAdaptation = false)
                    }
                }

                // Author
                if (config.authorVisible && book.author.isNotBlank()) {
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Middle Info Row (Words, Bookmark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (config.wordsVisible) {
                        Text(
                            text = "${Formatters.formatNumber(book.effectiveWords, shorten = shortenNumbers)} сл.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (config.bookmarkVisible && bookmarksEnabled && book.bookmark.isNotBlank()) {
                        BookmarkChip(bookmark = book.bookmark, modifier = Modifier.weight(1f, fill = false))
                    }
                }

                // Bottom row in list item (Format, Rating, Progress)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (config.formatVisible) {
                            RenderFormatBadge(book.format.shortLabel, config.formatStyle, alignFormatWithTitle)
                        }
                        if (config.ratingVisible && ratingEnabled && book.rating > 0f) {
                            RenderRatingBadge(book.rating, ratingScale)
                        }
                    }

                    if (config.progressVisible) {
                        RenderProgressText(book, config.progressStyle)
                    }
                }

                // Progress Bar in list card
                if (config.progressBarVisible) {
                    LinearProgressIndicator(
                        progress = { book.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(config.progressBarHeightDp.coerceAtMost(3).dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }

            // Right Cover
            if (hasCover && config.listCoverPosition == "RIGHT") {
                Spacer(modifier = Modifier.width(12.dp))
                CoverImage(
                    coverUrl = book.coverUrl,
                    title = book.title,
                    width = config.listCoverWidthDp.dp,
                    height = (config.listCoverWidthDp / config.listCoverAspectRatioValue).dp,
                    corner = config.coverCornerRadiusDp.coerceAtLeast(6).dp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicAdaptationCard(
    adaptation: Adaptation,
    config: CardLayoutConfig,
    ratingScale: RatingScale = RatingScale.STARS_10,
    ratingEnabled: Boolean = true,
    bookmarksEnabled: Boolean = true,
    isGrid: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hasCover = !adaptation.coverUrl.isNullOrBlank() && config.gridCoverStyle != "NONE"
    val isFullBg = hasCover && config.gridCoverStyle == "FULL_BACKGROUND"
    val shape = RoundedCornerShape(config.cardCornerRadiusDp.dp)

    val containerColor = when (config.surfaceStyle) {
        "SURFACE_CONTAINER" -> MaterialTheme.colorScheme.surfaceContainer
        "SURFACE_HIGH" -> MaterialTheme.colorScheme.surfaceContainerHigh
        "GLASS_GRADIENT" -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (isFullBg) 0.85f else 1f)
        "OUTLINE" -> Color.Transparent
        "TONAL" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val borderStroke = if (config.borderWidthDp > 0f) {
        BorderStroke(config.borderWidthDp.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    } else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .testTag("adaptation_card_${adaptation.id}"),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke
    ) {
        if (isGrid) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (hasCover) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(config.coverAspectRatioValue)
                            .clip(RoundedCornerShape(topStart = config.cardCornerRadiusDp.dp, topEnd = config.cardCornerRadiusDp.dp))
                    ) {
                        CoverImage(
                            coverUrl = adaptation.coverUrl,
                            title = adaptation.title,
                            modifier = Modifier.fillMaxSize(),
                            corner = config.coverCornerRadiusDp.dp
                        )

                        Box(modifier = Modifier.padding(8.dp).align(Alignment.TopStart)) {
                            if (config.formatVisible) {
                                RenderFormatBadge(adaptation.type.label, config.formatStyle)
                            }
                        }

                        Box(modifier = Modifier.padding(8.dp).align(Alignment.TopEnd)) {
                            if (config.statusVisible) {
                                RenderStatusBadge(adaptation.status, config.statusStyle, isAdaptation = true)
                            }
                        }

                        if (config.progressBarVisible) {
                            LinearProgressIndicator(
                                progress = { adaptation.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(config.progressBarHeightDp.dp)
                                    .align(Alignment.BottomCenter),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }

                // Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!hasCover) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (config.formatVisible) RenderFormatBadge(adaptation.type.label, config.formatStyle)
                            if (config.statusVisible) RenderStatusBadge(adaptation.status, config.statusStyle, isAdaptation = true)
                        }
                    }

                    Text(
                        text = adaptation.title,
                        style = getTitleTypography(config.titleTextSize),
                        fontWeight = FontWeight.Bold,
                        maxLines = config.titleMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (config.bookmarkVisible && bookmarksEnabled && adaptation.bookmark.isNotBlank()) {
                        BookmarkChip(bookmark = adaptation.bookmark)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (config.ratingVisible && ratingEnabled && adaptation.rating > 0f) {
                            RenderRatingBadge(adaptation.rating, ratingScale)
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (config.progressVisible) {
                            Text(
                                text = adaptation.progressDisplay,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else {
            // List adaptation card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(config.listCardPaddingDp.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasCover) {
                    CoverImage(
                        coverUrl = adaptation.coverUrl,
                        title = adaptation.title,
                        width = config.listCoverWidthDp.dp,
                        height = (config.listCoverWidthDp / config.listCoverAspectRatioValue).dp,
                        corner = config.coverCornerRadiusDp.coerceAtLeast(6).dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = adaptation.title,
                            style = getTitleTypography(config.titleTextSize),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (config.statusVisible) {
                            RenderStatusBadge(adaptation.status, config.statusStyle, isAdaptation = true)
                        }
                    }

                    if (config.bookmarkVisible && bookmarksEnabled && adaptation.bookmark.isNotBlank()) {
                        BookmarkChip(bookmark = adaptation.bookmark)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (config.formatVisible) {
                            RenderFormatBadge(adaptation.type.label, config.formatStyle)
                        }
                        if (config.progressVisible) {
                            Text(
                                text = adaptation.progressDisplay,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Helper Composable Renderers for Slots & Styles ---

@Composable
fun RenderStatusBadge(
    status: TitleStatus,
    style: String,
    isAdaptation: Boolean,
    modifier: Modifier = Modifier
) {
    StatusBadge(
        status = status,
        isAdaptation = isAdaptation,
        style = style,
        modifier = modifier
    )
}

@Composable
fun RenderFormatBadge(
    format: String,
    style: String,
    alignFlush: Boolean = false,
    modifier: Modifier = Modifier
) {
    when (style) {
        "SOLID" -> {
            Surface(
                modifier = modifier.clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = format,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp
                )
            }
        }
        "OUTLINE" -> {
            Surface(
                modifier = modifier.clip(RoundedCornerShape(6.dp)),
                color = Color.Transparent,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = format,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp
                )
            }
        }
        "PLAIN_TEXT" -> {
            Text(
                text = format,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = modifier.padding(vertical = 2.dp),
                fontSize = 11.sp
            )
        }
        else -> { // "BADGE"
            FormatBadge(format = format, modifier = modifier, alignFlush = alignFlush)
        }
    }
}

@Composable
fun RenderRatingBadge(
    rating: Float,
    ratingScale: RatingScale,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = StarGold,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = Formatters.formatRating(rating, ratingScale),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = StarGold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun RenderProgressText(
    book: BookTitle,
    style: String,
    modifier: Modifier = Modifier
) {
    val text = when (style) {
        "PERCENTAGE_ONLY" -> "${(book.progressFraction * 100).toInt()}%"
        "SHORT" -> when (book.format) {
            TitleFormat.SINGLE -> "${book.volumes}/${book.totalVolumes.coerceAtLeast(1)} т."
            TitleFormat.WEB_NOVEL -> "${book.webChapters} гл."
            TitleFormat.HYBRID -> "${book.volumes}т / ${book.webChapters}гл"
            TitleFormat.VISUAL_NOVEL -> "${book.endings} конц."
            else -> "${book.volumes} т."
        }
        else -> book.progressDisplay
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

private fun getTitleTypography(size: String): androidx.compose.ui.text.TextStyle {
    return when (size) {
        "SMALL" -> androidx.compose.ui.text.TextStyle(fontSize = 12.sp, lineHeight = 15.sp)
        "LARGE" -> androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 19.sp)
        else -> androidx.compose.ui.text.TextStyle(fontSize = 13.5.sp, lineHeight = 17.sp)
    }
}
