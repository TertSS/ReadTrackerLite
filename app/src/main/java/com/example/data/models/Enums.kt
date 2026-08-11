package com.example.data.models

enum class TitleStatus(val id: String, val labelBook: String, val labelAdaptation: String, val sortOrder: Int) {
    PLANNED("planned", "В планах", "В планах", 1),
    READING("reading", "Читаю", "Смотрю", 2),
    PAUSED("paused", "На паузе", "На паузе", 3),
    DROPPED("dropped", "Брошено", "Брошено", 4),
    COMPLETED("completed", "Завершено", "Просмотрено", 5);

    companion object {
        fun fromId(id: String): TitleStatus = entries.find { it.id.equals(id, ignoreCase = true) } ?: READING
    }
}

enum class TitleFormat(val id: String, val label: String, val shortLabel: String) {
    SERIES("series", "Серия томов (LN)", "LN"),
    NOVEL("novel", "Роман", "Роман"),
    WEB_NOVEL("web_novel", "Веб-новелла (WN)", "WN"),
    SINGLE("single", "Сингл (одиночная)", "Сингл"),
    HYBRID("hybrid", "Гибрид (LN+WN)", "LN+WN"),
    VISUAL_NOVEL("visual_novel", "Визуальная новелла (VN)", "VN");

    companion object {
        fun fromId(id: String): TitleFormat = entries.find { it.id.equals(id, ignoreCase = true) } ?: SERIES
    }
}

enum class AdaptationType(val id: String, val label: String, val shortLabel: String) {
    SERIES("series", "Сериал", "Сериал"),
    MOVIE("movie", "Фильм / Франшиза", "Фильм");

    companion object {
        fun fromId(id: String): AdaptationType = entries.find { it.id.equals(id, ignoreCase = true) } ?: SERIES
    }
}

enum class ReviewType(val id: String, val label: String) {
    VOLUME("volume", "По тому"),
    CHAPTERS("chapters", "По главам"),
    SEASON("season", "По сезону"),
    MOVIE("movie", "По фильму"),
    OVERALL("overall", "Общее впечатление");

    companion object {
        fun fromId(id: String): ReviewType = entries.find { it.id.equals(id, ignoreCase = true) } ?: OVERALL
    }
}

enum class RatingScale(val maxStars: Int, val label: String) {
    STARS_10(10, "10 звёзд (10★)"),
    STARS_5(5, "5 звёзд (5★)")
}

enum class LibraryMode(val label: String) {
    BOOKS("Книги"),
    ADAPTATIONS("Экранизации")
}

enum class TierPreset(val label: String) {
    CLASSIC("Classic (Peak, Mid, Weak, Trash)"),
    LETTERS("Letters (S, A, B, C, D, F)"),
    NUMBERS("Numbers (10 .. 1)")
}
