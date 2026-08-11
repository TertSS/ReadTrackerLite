package com.example.ui.screens.reviews

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.screens.details.AddReviewDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.ReadTrackerViewModel
import com.example.utils.Formatters
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    viewModel: ReadTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allAdaptations by viewModel.allAdaptations.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("TITLES") } // "TITLES" (Произведения) or "FEED" (Все отзывы)
    var selectedTargetId by remember { mutableStateOf<String?>(null) } // When a title is clicked, shows its reviews
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }
    var targetForReviewAdd by remember { mutableStateOf<Triple<String, String, String>?>(null) } // id, title, type

    val existingTargetIds = remember(allBooks, allAdaptations) {
        (allBooks.map { it.id } + allAdaptations.map { it.id }).toSet()
    }

    // Only reviews whose target titles actually exist in the library
    val validReviews = remember(allReviews, existingTargetIds) {
        allReviews.filter { it.targetId in existingTargetIds }
    }

    // Library titles enabled for reviews section
    val titlesInReviews = remember(allBooks, allAdaptations) {
        val books = allBooks.filter { it.showInReviews }.map {
            ReviewTitleItem(
                id = it.id,
                title = it.title,
                author = it.author,
                formatLabel = it.format.shortLabel,
                status = it.status,
                rating = it.rating,
                coverUrl = it.coverUrl,
                type = "book",
                bookFormat = it.format
            )
        }
        val adaps = allAdaptations.filter { it.showInReviews }.map {
            ReviewTitleItem(
                id = it.id,
                title = it.title,
                author = "",
                formatLabel = it.type.shortLabel,
                status = it.status,
                rating = it.rating,
                coverUrl = it.coverUrl,
                type = "adaptation",
                adaptationType = it.type
            )
        }
        books + adaps
    }

    // Selected Title detail data
    val selectedTitle = remember(selectedTargetId, allBooks, allAdaptations) {
        if (selectedTargetId == null) null
        else {
            val b = allBooks.find { it.id == selectedTargetId }
            if (b != null) {
                ReviewTitleItem(
                    id = b.id,
                    title = b.title,
                    author = b.author,
                    formatLabel = b.format.shortLabel,
                    status = b.status,
                    rating = b.rating,
                    coverUrl = b.coverUrl,
                    type = "book",
                    bookFormat = b.format
                )
            } else {
                val a = allAdaptations.find { it.id == selectedTargetId }
                if (a != null) {
                    ReviewTitleItem(
                        id = a.id,
                        title = a.title,
                        author = "",
                        formatLabel = a.type.shortLabel,
                        status = a.status,
                        rating = a.rating,
                        coverUrl = a.coverUrl,
                        type = "adaptation",
                        adaptationType = a.type
                    )
                } else null
            }
        }
    }

    val selectedTitleReviews = remember(validReviews, selectedTargetId) {
        if (selectedTargetId == null) emptyList()
        else validReviews.filter { it.targetId == selectedTargetId }
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
                if (selectedTitle != null) {
                    // Title Reviews Header with Back button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedTargetId = null },
                            modifier = Modifier.testTag("back_from_title_reviews")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedTitle.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Отзывы к произведению (${selectedTitleReviews.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FormatBadge(format = selectedTitle.formatLabel)
                    }
                } else {
                    // Main Reviews Header with Tab toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Отзывы",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tabs: "Произведения" (Default) and "Все отзывы" (Feed)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tabs = listOf("TITLES" to "Произведения (${titlesInReviews.size})", "FEED" to "Все отзывы (${validReviews.size})")
                        tabs.forEach { (key, label) ->
                            val isSelected = activeTab == key
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { activeTab = key }
                                    .testTag("review_tab_$key"),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = CircleShape,
                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTitle != null) {
                        targetForReviewAdd = Triple(selectedTitle.id, selectedTitle.title, selectedTitle.type)
                    } else {
                        showQuickAddDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 0.dp)
                    .testTag("add_review_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = "Написать отзыв",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        if (selectedTitle != null) {
            // Dedicated Reviews View for Selected Title
            TitleReviewsView(
                titleItem = selectedTitle,
                reviews = selectedTitleReviews,
                ratingScale = settings.ratingScale,
                ratingEnabled = settings.ratingEnabled,
                paddingValues = paddingValues,
                onAddReview = {
                    targetForReviewAdd = Triple(selectedTitle.id, selectedTitle.title, selectedTitle.type)
                },
                onDeleteReview = { reviewToDelete = it }
            )
        } else if (activeTab == "TITLES") {
            // Tab 1: Titles
            if (titlesInReviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нет добавленных произведений",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Чтобы произведение отображалось здесь, включите переключатель «Отображать в отзывах» при добавлении или редактировании тайтла.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(titlesInReviews, key = { it.id }) { item ->
                        val reviewCount = validReviews.count { it.targetId == item.id }
                        TitleReviewCard(
                            item = item,
                            reviewCount = reviewCount,
                            ratingScale = settings.ratingScale,
                            ratingEnabled = settings.ratingEnabled,
                            onClick = { selectedTargetId = item.id }
                        )
                    }
                }
            }
        } else {
            // Tab 2: Global Reviews Feed
            if (validReviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Отзывов пока нет",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (titlesInReviews.isEmpty())
                                "Добавьте произведение в библиотеку, чтобы написать свой первый отзыв!"
                            else
                                "Нажмите «+» или выберите произведение, чтобы добавить свой первый отзыв!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(validReviews, key = { it.id }) { review ->
                        ReviewFeedCard(
                            review = review,
                            onDelete = { reviewToDelete = review },
                            onClickTarget = {
                                if (review.targetId in existingTargetIds) {
                                    selectedTargetId = review.targetId
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation
    ConfirmationDialog(
        show = reviewToDelete != null,
        title = "Удалить отзыв?",
        message = "Вы действительно хотите удалить этот отзыв?",
        confirmText = "Удалить",
        onConfirm = {
            reviewToDelete?.let { viewModel.deleteReview(it) }
            reviewToDelete = null
        },
        onDismiss = { reviewToDelete = null }
    )

    // Quick Add Target Picker Dialog
    if (showQuickAddDialog) {
        AlertDialog(
            onDismissRequest = { showQuickAddDialog = false },
            title = {
                Text("Выберите произведение", fontWeight = FontWeight.Bold)
            },
            text = {
                val availableTitles = (allBooks.map { Triple(it.id, it.title, "book") } +
                    allAdaptations.map { Triple(it.id, it.title, "adaptation") })

                if (availableTitles.isEmpty()) {
                    Text("В вашей библиотеке пока нет произведений. Сначала добавьте книгу или экранизацию.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableTitles) { (id, name, type) ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        targetForReviewAdd = Triple(id, name, type)
                                        showQuickAddDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f, fill = false),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FormatBadge(format = if (type == "book") "Книга" else "Экранизация")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQuickAddDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add Review Dialog
    if (targetForReviewAdd != null) {
        val (id, title, type) = targetForReviewAdd!!
        val targetBook = allBooks.find { it.id == id }
        val targetAdaptation = allAdaptations.find { it.id == id }

        AddReviewDialog(
            targetId = id,
            targetTitle = title,
            targetType = type,
            bookFormat = targetBook?.format,
            adaptationType = targetAdaptation?.type,
            onDismiss = { targetForReviewAdd = null },
            onSave = { newReview ->
                viewModel.addReview(newReview)
                targetForReviewAdd = null
            }
        )
    }
}

data class ReviewTitleItem(
    val id: String,
    val title: String,
    val author: String,
    val formatLabel: String,
    val status: TitleStatus,
    val rating: Float,
    val coverUrl: String?,
    val type: String,
    val bookFormat: TitleFormat? = null,
    val adaptationType: AdaptationType? = null
)

@Composable
fun TitleReviewCard(
    item: ReviewTitleItem,
    reviewCount: Int,
    ratingScale: RatingScale,
    ratingEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("title_review_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverImage(
                coverUrl = item.coverUrl,
                title = item.title,
                width = 54.dp,
                height = 76.dp,
                corner = 8.dp
            )
            Spacer(modifier = Modifier.width(12.dp))

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
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    StatusBadge(status = item.status, isAdaptation = item.type == "adaptation")
                }

                if (item.author.isNotEmpty()) {
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FormatBadge(format = item.formatLabel)
                        if (ratingEnabled && item.rating > 0f) {
                            Text(
                                text = "★ ${Formatters.formatRating(item.rating, ratingScale)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StarGold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (reviewCount > 0) "💬 $reviewCount" else "Нет отзывов",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TitleReviewsView(
    titleItem: ReviewTitleItem,
    reviews: List<Review>,
    ratingScale: RatingScale,
    ratingEnabled: Boolean,
    paddingValues: PaddingValues,
    onAddReview: () -> Unit,
    onDeleteReview: (Review) -> Unit,
    modifier: Modifier = Modifier
) {
    val overallReviews = remember(reviews) { reviews.filter { it.reviewType == ReviewType.OVERALL } }
    val partReviews = remember(reviews) {
        reviews.filter { it.reviewType != ReviewType.OVERALL }
            .sortedWith(compareBy({ it.targetNumber }, { -(it.createdAt) }))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoverImage(
                        coverUrl = titleItem.coverUrl,
                        title = titleItem.title,
                        width = 64.dp,
                        height = 92.dp,
                        corner = 8.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = titleItem.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (titleItem.author.isNotEmpty()) {
                            Text(
                                text = titleItem.author,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormatBadge(format = titleItem.formatLabel)
                            StatusBadge(status = titleItem.status, isAdaptation = titleItem.type == "adaptation")
                            if (ratingEnabled && titleItem.rating > 0f) {
                                Text(
                                    text = "★ ${Formatters.formatRating(titleItem.rating, ratingScale)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Отзывы (${reviews.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 1. TOP SECTION: Overall Impression (Общее впечатление)
        item {
            Text(
                text = "⭐ Общее впечатление",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (overallReviews.isNotEmpty()) {
            items(overallReviews, key = { it.id }) { review ->
                ReviewFeedCard(
                    review = review,
                    onDelete = { onDeleteReview(review) },
                    onClickTarget = {}
                )
            }
        } else {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAddReview() },
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Общее впечатление не написано",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Нажмите, чтобы добавить общий отзыв о произведении в целом",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. BOTTOM SECTION: Volume / Chapter / Episode Reviews (По томам и главам)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📖 Отзывы по томам и главам (${partReviews.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (partReviews.isNotEmpty()) {
            items(partReviews, key = { it.id }) { review ->
                ReviewFeedCard(
                    review = review,
                    onDelete = { onDeleteReview(review) },
                    onClickTarget = {}
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Пока нет отзывов по отдельным томам или главам",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewFeedCard(
    review: Review,
    onDelete: () -> Unit,
    onClickTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSpoilerRevealed by remember { mutableStateOf(false) }
    val isSpoiler = review.text.contains("спойлер", ignoreCase = true) || review.text.startsWith("!")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("review_feed_card_${review.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).clickable { onClickTarget() }) {
                    Text(
                        text = review.targetTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = review.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dateStr = remember(review.createdAt) {
                        SimpleDateFormat("dd MMM yyyy", Locale("ru")).format(Date(review.createdAt))
                    }
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isSpoiler && !isSpoilerRevealed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        .clickable { isSpoilerRevealed = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ Спойлер. Нажмите, чтобы открыть отзыв",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            } else {
                Text(
                    text = review.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
