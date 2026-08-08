package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "adaptations")
data class Adaptation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val status: TitleStatus = TitleStatus.READING,
    val type: AdaptationType = AdaptationType.SERIES,
    val genres: List<String> = emptyList(),
    val rating: Float = 0f,
    val bookmark: String = "",
    val droppedReason: String = "",
    val showInReviews: Boolean = false,
    val coverUrl: String? = null,
    val coverColor: Long = 0xFF201F1FL,
    val seasons: List<SeasonEntry> = emptyList(),
    val movies: List<MovieEntry> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val watchedEpisodes: Int
        get() = seasons.sumOf { it.watchedEpisodes }

    val totalEpisodes: Int
        get() = seasons.sumOf { it.totalEpisodes }

    val watchedMovies: Int
        get() = movies.count { it.isWatched }

    val totalMovies: Int
        get() = movies.size

    val watchTimeMinutes: Int
        get() {
            return if (type == AdaptationType.SERIES) {
                seasons.sumOf { it.calculateWatchTimeMinutes() }
            } else {
                movies.filter { it.isWatched }.sumOf { it.durationMinutes }
            }
        }

    val completedSeasons: Int
        get() = seasons.count { it.isCompleted }

    val progressFraction: Float
        get() {
            return when (type) {
                AdaptationType.SERIES -> {
                    if (totalEpisodes > 0) (watchedEpisodes.toFloat() / totalEpisodes.toFloat()).coerceIn(0f, 1f)
                    else if (status == TitleStatus.COMPLETED) 1f else 0f
                }
                AdaptationType.MOVIE -> {
                    if (totalMovies > 0) (watchedMovies.toFloat() / totalMovies.toFloat()).coerceIn(0f, 1f)
                    else if (status == TitleStatus.COMPLETED) 1f else 0f
                }
            }
        }

    val progressDisplay: String
        get() {
            return when (type) {
                AdaptationType.SERIES -> {
                    if (totalEpisodes > 0) "$watchedEpisodes/$totalEpisodes сер." else "$watchedEpisodes сер."
                }
                AdaptationType.MOVIE -> {
                    if (totalMovies > 0) "$watchedMovies/$totalMovies фил." else "$watchedMovies фил."
                }
            }
        }
}
