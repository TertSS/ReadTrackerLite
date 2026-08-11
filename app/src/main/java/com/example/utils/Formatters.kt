package com.example.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatters {
    private val symbols = DecimalFormatSymbols(Locale("ru", "RU")).apply {
        groupingSeparator = ' '
    }
    private val decimalFormat = DecimalFormat("#,###", symbols)

    fun formatNumber(number: Long, shorten: Boolean = false): String {
        if (number < 0) return "0"
        if (!shorten) {
            return decimalFormat.format(number)
        }

        return when {
            number >= 1_000_000_000L -> {
                val value = number.toDouble() / 1_000_000_000.0
                if (value >= 10) String.format(Locale.US, "%.0fB", value) else String.format(Locale.US, "%.1fB", value).replace(".0B", "B")
            }
            number >= 1_000_000L -> {
                val value = number.toDouble() / 1_000_000.0
                if (value >= 10) String.format(Locale.US, "%.0fM", value) else String.format(Locale.US, "%.1fM", value).replace(".0M", "M")
            }
            number >= 10_000L -> {
                val value = number.toDouble() / 1_000.0
                String.format(Locale.US, "%.0fK", value)
            }
            number >= 1_000L -> {
                val value = number.toDouble() / 1_000.0
                String.format(Locale.US, "%.1fK", value).replace(".0K", "K")
            }
            else -> number.toString()
        }
    }

    fun formatDuration(minutes: Int): String {
        if (minutes <= 0) return "0 мин"
        val hours = minutes / 60
        val remainingMins = minutes % 60

        return when {
            hours > 0 && remainingMins > 0 -> "$hours ч $remainingMins мин"
            hours > 0 -> "$hours ч"
            else -> "$remainingMins мин"
        }
    }

    fun formatRating(rating: Float, scale: com.example.data.models.RatingScale, allowDecimal: Boolean = true): String {
        if (rating <= 0f) return "—"
        return if (scale == com.example.data.models.RatingScale.STARS_5) {
            val star5 = (rating / 2f).coerceIn(0.1f, 5f)
            if (allowDecimal && (star5 % 1f != 0f)) {
                String.format(Locale.US, "%.1f/5", star5)
            } else {
                "${Math.round(star5)}/5"
            }
        } else {
            val star10 = rating.coerceIn(0.1f, 10f)
            if (allowDecimal && (star10 % 1f != 0f)) {
                String.format(Locale.US, "%.1f/10", star10)
            } else {
                "${Math.round(star10)}/10"
            }
        }
    }
}
