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
    val totalWordsEnabled: Boolean = true,
    val startAfterAdaptationEnabled: Boolean = true,
    val shortenNumbers: Boolean = false,
    val sortByStatus: Boolean = true,
    val showCoversInLibrary: Boolean = true,
    val showStatusFiltersInLibrary: Boolean = false,
    val libraryStatusBarStyle: String = "PILLS", // PILLS, SEGMENTED, CARDS_COUNT
    val showStatusBarItemCounts: Boolean = true,
    val coverlessCardStyle: String = "CLASSIC", // CLASSIC, MINIMAL, GRADIENT, COMPACT
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
    val disableAnimations: Boolean = false
)
