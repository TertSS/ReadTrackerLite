package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val targetId: String,
    val targetTitle: String,
    val targetType: String, // "book" or "adaptation"
    val reviewType: ReviewType = ReviewType.OVERALL,
    val targetNumber: Int = 1,
    val chapterStart: Int? = null,
    val chapterEnd: Int? = null,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val subtitle: String
        get() = when (reviewType) {
            ReviewType.VOLUME -> "Том $targetNumber"
            ReviewType.SEASON -> "Сезон $targetNumber"
            ReviewType.MOVIE -> "Фильм $targetNumber"
            ReviewType.CHAPTERS -> {
                if (chapterStart != null && chapterEnd != null) {
                    if (chapterStart == chapterEnd) "Глава $chapterStart"
                    else "Главы $chapterStart–$chapterEnd"
                } else if (chapterStart != null) "Глава $chapterStart"
                else "Главы"
            }
            ReviewType.OVERALL -> "Общее впечатление"
        }
}
