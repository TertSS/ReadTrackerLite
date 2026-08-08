package com.example.utils

object PluralRu {
    /**
     * Russian pluralization logic:
     * - Ends with 1 (except 11) -> one
     * - Ends with 2..4 (except 12..14) -> few
     * - Else -> many
     */
    fun form(count: Long, one: String, few: String, many: String): String {
        val absCount = Math.abs(count)
        val rem100 = (absCount % 100).toInt()
        val rem10 = (absCount % 10).toInt()

        return when {
            rem100 in 11..19 -> many
            rem10 == 1 -> one
            rem10 in 2..4 -> few
            else -> many
        }
    }

    fun countAndForm(count: Long, one: String, few: String, many: String, shorten: Boolean = false): String {
        val numStr = if (shorten) Formatters.formatNumber(count, shorten = true) else Formatters.formatNumber(count, shorten = false)
        return "$numStr ${form(count, one, few, many)}"
    }

    fun pluralSeason(count: Int): String = countAndForm(count.toLong(), "сезон", "сезона", "сезонов")
    fun pluralEpisode(count: Int): String = countAndForm(count.toLong(), "серия", "серии", "серий")
    fun pluralMovie(count: Int): String = countAndForm(count.toLong(), "фильм", "фильма", "фильмов")
    fun pluralVolume(count: Int): String = countAndForm(count.toLong(), "том", "тома", "томов")
    fun pluralChapter(count: Int): String = countAndForm(count.toLong(), "глава", "главы", "глав")
    fun pluralEnding(count: Int): String = countAndForm(count.toLong(), "концовка", "концовки", "концовок")
    fun pluralReview(count: Int): String = countAndForm(count.toLong(), "отзыв", "отзыва", "отзывов")
    fun pluralBook(count: Int): String = countAndForm(count.toLong(), "книга", "книги", "книг")
    fun pluralAdaptation(count: Int): String = countAndForm(count.toLong(), "экранизация", "экранизации", "экранизаций")
    fun pluralWord(count: Long, shorten: Boolean = false): String = countAndForm(count, "слово", "слова", "слов", shorten = shorten)
}
