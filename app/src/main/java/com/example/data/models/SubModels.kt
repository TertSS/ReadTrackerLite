package com.example.data.models

data class VolumeEntry(
    val volumeNumber: Int,
    val wordCount: Long
)

data class SeasonEntry(
    val seasonNumber: Int,
    val totalEpisodes: Int = 12,
    val watchedEpisodes: Int = 0,
    val defaultEpisodeDurationMinutes: Int = 24,
    val episodeDurations: Map<String, Int> = emptyMap() // "1" -> 24, "2" -> 45
) {
    val isCompleted: Boolean
        get() = totalEpisodes > 0 && watchedEpisodes >= totalEpisodes

    fun getEpisodeDuration(episodeNumber: Int): Int {
        return episodeDurations[episodeNumber.toString()] ?: defaultEpisodeDurationMinutes
    }

    fun calculateWatchTimeMinutes(): Int {
        var total = 0
        for (ep in 1..watchedEpisodes) {
            val dur = episodeDurations[ep.toString()] ?: defaultEpisodeDurationMinutes
            total += dur
        }
        return total
    }

    fun calculateTotalSeasonDurationMinutes(): Int {
        var total = 0
        val count = if (totalEpisodes > 0) totalEpisodes else watchedEpisodes
        for (ep in 1..count) {
            val dur = episodeDurations[ep.toString()] ?: defaultEpisodeDurationMinutes
            total += dur
        }
        return total
    }
}

data class MovieEntry(
    val movieNumber: Int,
    val title: String = "",
    val isWatched: Boolean = false,
    val durationMinutes: Int = 100
)
