package com.example.ui.redesign

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RedesignedNavItem(
    val id: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

/**
 * 🌟 REDESIGNED FLOATING DOCK BOTTOM NAVIGATION BAR
 * Ultra-modern frosted floating dock with glowing active pill, smooth spring motion, and safe navigation bar insets.
 */
@Composable
fun RedesignedBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    activeReadingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        RedesignedNavItem("library", "Библиотека", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, activeReadingCount),
        RedesignedNavItem("reviews", "Рецензии", Icons.Filled.RateReview, Icons.Outlined.RateReview),
        RedesignedNavItem("stats", "Статистика", Icons.Filled.Analytics, Icons.Outlined.Analytics),
        RedesignedNavItem("tier_list", "Тир-лист", Icons.Filled.FormatListNumbered, Icons.Outlined.FormatListNumbered),
        RedesignedNavItem("settings", "Настройки", Icons.Filled.Tune, Icons.Outlined.Tune)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0x6660A5FA),
                    ambientColor = Color(0x33000000)
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xEE111827),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0x4460A5FA),
                        Color(0x2294A3B8),
                        Color(0x448B5CF6)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentTab == item.id
                    val interactionSource = remember { MutableInteractionSource() }

                    val itemBgColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF1E293B) else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "itemBgColor"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "iconTint"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(itemBgColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onTabSelected(item.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (item.badgeCount > 0 && item.id == "library") {
                                        Badge(
                                            containerColor = Color(0xFF10B981),
                                            contentColor = Color(0xFF022C22),
                                            modifier = Modifier.offset(x = 4.dp, y = (-2).dp)
                                        ) {
                                            Text(
                                                text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = iconTint,
                                maxLines = 1
                            )
                        }

                        // Glow indicator under active item
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 3.dp)
                                    .width(16.dp)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF60A5FA), Color(0xFF8B5CF6))
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🌟 REDESIGNED TABLET NAVIGATION RAIL
 */
@Composable
fun RedesignedNavRail(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    activeReadingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        RedesignedNavItem("library", "Библиотека", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, activeReadingCount),
        RedesignedNavItem("reviews", "Рецензии", Icons.Filled.RateReview, Icons.Outlined.RateReview),
        RedesignedNavItem("stats", "Статистика", Icons.Filled.Analytics, Icons.Outlined.Analytics),
        RedesignedNavItem("tier_list", "Тир-лист", Icons.Filled.FormatListNumbered, Icons.Outlined.FormatListNumbered),
        RedesignedNavItem("settings", "Настройки", Icons.Filled.Tune, Icons.Outlined.Tune)
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(88.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0x33334155)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App Logo Brand Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = "ReadTracker",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Nav items
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEach { item ->
                    val isSelected = currentTab == item.id
                    val itemBgColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF1E293B) else Color.Transparent,
                        label = "railItemBg"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                        label = "railIconTint"
                    )

                    Surface(
                        modifier = Modifier
                            .size(64.dp, 56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTabSelected(item.id) },
                        color = itemBgColor,
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0x6660A5FA)) else null
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = iconTint,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}
