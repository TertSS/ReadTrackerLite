package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "books")
data class BookTitle(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String = "",
    val description: String = "",
    val status: TitleStatus = TitleStatus.READING,
    val format: TitleFormat = TitleFormat.SERIES,
    val genres: List<String> = emptyList(),
    val rating: Float = 0f, // 0..10
    val bookmark: String = "",
    val droppedReason: String = "",
    val showInReviews: Boolean = false,
    
    // Progress fields
    val words: Long = 0L,
    val totalWords: Long = 0L,
    val volumes: Int = 0,
    val totalVolumes: Int = 0,
    val isOngoing: Boolean = false,
    val chapters: Int = 0,
    val totalChapters: Int = 0,
    val webChapters: Int = 0,
    val totalWebChapters: Int = 0,
    val endings: Int = 0,
    val totalEndings: Int = 0,
    
    // Adaptation reading offset
    val startVolume: Int? = null,
    val startChapter: Int? = null,
    
    // Detailed volume breakdown
    val hasDetailedVolumes: Boolean = false,
    val detailedVolumes: List<VolumeEntry> = emptyList(),
    
    // Cover
    val coverUrl: String? = null,
    val coverColor: Long = 0xFF2A2A2AL,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val effectiveWords: Long
        get() = if (hasDetailedVolumes && format != TitleFormat.WEB_NOVEL) {
            detailedVolumes.sumOf { it.wordCount }
        } else {
            words
        }

    val effectiveVolumes: Int
        get() = if (hasDetailedVolumes) {
            detailedVolumes.size
        } else {
            volumes
        }

    val progressFraction: Float
        get() {
            return when {
                format == TitleFormat.VISUAL_NOVEL && totalEndings > 0 -> {
                    (endings.toFloat() / totalEndings.toFloat()).coerceIn(0f, 1f)
                }
                totalWords > 0 -> {
                    (effectiveWords.toFloat() / totalWords.toFloat()).coerceIn(0f, 1f)
                }
                totalVolumes > 0 -> {
                    (effectiveVolumes.toFloat() / totalVolumes.toFloat()).coerceIn(0f, 1f)
                }
                (format == TitleFormat.WEB_NOVEL || format == TitleFormat.HYBRID) && totalChapters > 0 -> {
                    (chapters.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f)
                }
                status == TitleStatus.COMPLETED -> 1f
                else -> 0f
            }
        }

    val progressDisplay: String
        get() {
            return when (format) {
                TitleFormat.VISUAL_NOVEL -> {
                    if (totalEndings > 0) "$endings/$totalEndings кон." else "$endings кон."
                }
                TitleFormat.WEB_NOVEL -> {
                    if (isOngoing) {
                        "$chapters/? гл."
                    } else if (totalChapters > 0) {
                        "$chapters/$totalChapters гл."
                    } else {
                        "$chapters гл."
                    }
                }
                TitleFormat.HYBRID -> {
                    val volStr = if (isOngoing) "$effectiveVolumes/? т." else if (totalVolumes > 0) "$effectiveVolumes/$totalVolumes т." else "$effectiveVolumes т."
                    val chStr = if (totalChapters > 0) "$chapters/$totalChapters гл." else if (chapters > 0) "$chapters гл." else ""
                    if (chStr.isNotEmpty()) "$volStr, $chStr" else volStr
                }
                TitleFormat.SERIES, TitleFormat.NOVEL, TitleFormat.SINGLE -> {
                    if (isOngoing) {
                        "$effectiveVolumes/? т."
                    } else if (totalVolumes > 0) {
                        "$effectiveVolumes/$totalVolumes т."
                    } else {
                        "$effectiveVolumes т."
                    }
                }
            }
        }
}
