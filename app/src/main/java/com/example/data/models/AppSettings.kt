package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val libraryMode: LibraryMode = LibraryMode.BOOKS,
    val adaptationsEnabled: Boolean = true,
    val vnEnabled: Boolean = true,
    val hybridEnabled: Boolean = true,
    val genresEnabled: Boolean = true,
    val ratingEnabled: Boolean = true,
    val ratingScale: RatingScale = RatingScale.STARS_10,
    val goalsEnabled: Boolean = true,
    val bookmarksEnabled: Boolean = true,
    val searchFilterEnabled: Boolean = true,
    val librarySearchMode: String = "SEARCH_AND_FILTER", // "SEARCH_ONLY", "SEARCH_AND_FILTER"
    val showLibraryModeSwitcher: Boolean = false,
    val showViewModeSwitcher: Boolean = true,
    val totalWordsEnabled: Boolean = true,
    val startAfterAdaptationEnabled: Boolean = true,
    val shortenNumbers: Boolean = false,
    val sortByStatus: Boolean = true,
    val showCoversInLibrary: Boolean = true,
    val showStatusFiltersInLibrary: Boolean = false,
    val rememberLastStatusFilter: Boolean = false,
    val lastSelectedStatus: String? = null,
    val libraryStatusBarStyle: String = "PILLS", // PILLS, SEGMENTED, CARDS_COUNT
    val showStatusBarItemCounts: Boolean = true,
    val coverlessCardStyle: String = "CLASSIC", // CLASSIC, MINIMAL, GRADIENT, COMPACT, OUTLINE, TYPOGRAPHY, TONAL
    val compactTagPosition: String = "UNDER_STATUS", // UNDER_STATUS, LEFT_OF_STATUS
    val statsActiveTab: String = "ALL", // ALL, BOOKS, ADAPTATIONS
    val statsDefaultTab: String = "ALL", // ALL, BOOKS, ADAPTATIONS
    
    // Analytics tabs customization
    val statsShowOverviewTab: Boolean = true,
    val statsShowBooksTab: Boolean = true,
    val statsShowAdaptationsTab: Boolean = true,
    
    // Analytics customization display flags
    val statsShowYearlyGoals: Boolean = true,
    val statsShowWords: Boolean = true,
    val statsShowVolumes: Boolean = true,
    val statsShowWebChapters: Boolean = true,
    val statsShowTitlesCompleted: Boolean = true,
    val statsShowVnEndings: Boolean = true,
    val statsShowWatchTime: Boolean = true,
    val statsShowEpisodes: Boolean = true,
    val statsShowSeasons: Boolean = true,
    val statsShowAdaptationsCompleted: Boolean = true,
    val statsShowGenreDistribution: Boolean = true,
    val genreChartType: String = "DONUT", // "DONUT" (круговая / кольцевая), "RADAR" (лепестковая)
    val statsRadarShowItemCounts: Boolean = true,
    val statsShowTopBooks: Boolean = true,
    
    // Goals configuration
    val wordsTarget: Long = 10_000_000L,
    val volumesTarget: Int = 50,
    val webTarget: Int = 10,
    val seriesTarget: Int = 15,
    val singlesTarget: Int = 10,
    val vnTarget: Int = 5,
    val endingsTarget: Int = 20,
    
    // Goals item & trophy toggles
    val statsShowGoalsTrophy: Boolean = true,
    val statsGoalShowWords: Boolean = true,
    val statsGoalShowVolumes: Boolean = true,
    val statsGoalShowSeries: Boolean = true,
    val statsGoalShowSingles: Boolean = true,
    val statsGoalShowWeb: Boolean = true,
    
    // Stats singles display
    val statsShowSinglesCompleted: Boolean = true,
    
    // Session state
    val rememberLastTab: Boolean = false,
    val lastActiveTab: String = "library",
    
    // Tier list active mode
    val tierListIndependent: Boolean = false,
    
    // UI enhancements & Customization toggles
    val fractionalRatingEnabled: Boolean = true,
    val uniformHeadersEnabled: Boolean = true,
    val updatedEditorEnabled: Boolean = true,
    val hideWordsEquivalent: Boolean = false,
    val alignFormatWithTitle: Boolean = false,
    val roundedInputFields: Boolean = true,
    val tabletLayoutEnabled: Boolean = true,
    
    // Animation settings
    val disableAnimations: Boolean = false,
    val headerEnabled: Boolean = true,

    // Color Palette and Custom HEX Theme Customization
    val activePalette: String = "CLASSIC_DARK", // "CLASSIC_DARK", "MIDNIGHT_NEON", "OBSIDIAN_JADE", "ROYAL_AMETHYST", "VOLCANIC_SUNSET", "CUSTOM"
    val customPrimaryHex: String = "#9ECAFF",
    val customOnPrimaryHex: String = "#003258",
    val customPrimaryContainerHex: String = "#2196F3",
    val customOnPrimaryContainerHex: String = "#D1E4FF",
    val customSecondaryHex: String = "#78DC77",
    val customOnSecondaryHex: String = "#00390A",
    val customSecondaryContainerHex: String = "#00761F",
    val customOnSecondaryContainerHex: String = "#95FB92",
    val customTertiaryHex: String = "#FFB77B",
    val customOnTertiaryHex: String = "#4D2700",
    val customTertiaryContainerHex: String = "#DB7900",
    val customOnTertiaryContainerHex: String = "#FFDCC2",
    val customBackgroundHex: String = "#131313",
    val customOnBackgroundHex: String = "#E5E2E1",
    val customSurfaceHex: String = "#131313",
    val customOnSurfaceHex: String = "#E5E2E1",
    val customSurfaceVariantHex: String = "#404752",
    val customOnSurfaceVariantHex: String = "#BFC7D4",
    val customSurfaceContainerLowestHex: String = "#0E0E0E",
    val customSurfaceContainerLowHex: String = "#1C1B1B",
    val customSurfaceContainerHex: String = "#201F1F",
    val customSurfaceContainerHighHex: String = "#2A2A2A",
    val customSurfaceContainerHighestHex: String = "#353534",
    val customSurfaceBrightHex: String = "#393939",
    val customOutlineHex: String = "#89919D",
    val customOutlineVariantHex: String = "#404752",
    val customErrorHex: String = "#FFB4AB",
    val customStatusReadingHex: String = "#78DC77",
    val customStatusPlannedHex: String = "#FFB77B",
    val customStatusCompletedHex: String = "#9ECAFF",
    val customStatusPausedHex: String = "#BFC7D4",
    val customStatusDroppedHex: String = "#FFB4AB"
)
