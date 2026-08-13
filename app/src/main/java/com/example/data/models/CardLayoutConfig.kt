package com.example.data.models

import org.json.JSONObject

/**
 * Configuration for library card customization.
 * Allows users to customize card geometry, cover styling,
 * element positions (status, format, words, rating, progress, bookmark, author),
 * and display styles.
 */
data class CardLayoutConfig(
    // Geometry & Dimensions (Grid Mode)
    val gridCoverStyle: String = "TOP", // "TOP", "FULL_BACKGROUND", "COMPACT_BANNER", "NONE"
    val gridCoverAspect: String = "2:3", // "2:3", "3:4", "1:1", "16:9", "WIDE"
    val cardCornerRadiusDp: Int = 16, // 0..28
    val coverCornerRadiusDp: Int = 0, // 0..20
    val borderWidthDp: Float = 1f, // 0f, 0.5f, 1f, 1.5f, 2f
    val surfaceStyle: String = "SURFACE_LOW", // "SURFACE_LOW", "SURFACE_CONTAINER", "SURFACE_HIGH", "GLASS_GRADIENT", "OUTLINE", "TONAL"
    
    // Geometry & Dimensions (List Mode)
    val listCoverWidthDp: Int = 62, // 45..100
    val listCoverAspect: String = "2:3", // "2:3", "1:1", "3:4"
    val listCoverPosition: String = "LEFT", // "LEFT", "RIGHT", "NONE"
    val listCardPaddingDp: Int = 10, // 6..16

    // Element: STATUS
    val statusVisible: Boolean = true,
    val statusPosition: String = "COVER_TOP_END", // "COVER_TOP_END", "COVER_TOP_START", "COVER_BOTTOM_END", "BODY_TOP_END", "INFO_ROW", "FOOTER_START", "FOOTER_END"
    val statusStyle: String = "PILL", // "PILL", "DOT_TEXT", "MINIMAL_DOT", "ACCENT_BAR", "OUTLINE_GLOW", "GLASS_FROSTED", "ICON_CHIP"

    // Element: FORMAT (LN / WN / VN / Series / Movie)
    val formatVisible: Boolean = true,
    val formatPosition: String = "COVER_TOP_START", // "COVER_TOP_START", "COVER_BOTTOM_START", "BODY_TOP_START", "INFO_ROW", "FOOTER_START"
    val formatStyle: String = "BADGE", // "BADGE", "SOLID", "OUTLINE", "PLAIN_TEXT"

    // Element: PROGRESS (e.g. "Том 3, Гл. 45" / "75%")
    val progressVisible: Boolean = true,
    val progressPosition: String = "FOOTER_END", // "FOOTER_END", "FOOTER_START", "INFO_ROW", "BODY_TOP_END"
    val progressStyle: String = "DETAILED", // "DETAILED", "PERCENTAGE_ONLY", "SHORT"

    // Element: PROGRESS BAR
    val progressBarVisible: Boolean = true,
    val progressBarPosition: String = "BOTTOM_OF_COVER", // "BOTTOM_OF_COVER", "BOTTOM_OF_CARD", "INSIDE_BODY"
    val progressBarHeightDp: Int = 4, // 2, 3, 4, 6

    // Element: WORDS COUNT
    val wordsVisible: Boolean = true,
    val wordsPosition: String = "INFO_ROW", // "INFO_ROW", "FOOTER_START", "FOOTER_END", "BELOW_TITLE"

    // Element: RATING
    val ratingVisible: Boolean = true,
    val ratingPosition: String = "FOOTER_START", // "FOOTER_START", "FOOTER_END", "COVER_TOP_START", "BODY_TOP_END", "INFO_ROW"

    // Element: BOOKMARK
    val bookmarkVisible: Boolean = true,
    val bookmarkPosition: String = "INFO_ROW", // "INFO_ROW", "FOOTER_START", "BELOW_TITLE"

    // Element: AUTHOR
    val authorVisible: Boolean = true,
    val authorPosition: String = "BELOW_TITLE", // "BELOW_TITLE", "FOOTER_START"
    val authorMaxLines: Int = 1,

    // Element: TITLE
    val titleMaxLines: Int = 2, // 1, 2, 3
    val titleTextSize: String = "MEDIUM" // "SMALL", "MEDIUM", "LARGE"
) {
    val coverAspectRatioValue: Float
        get() = when (gridCoverAspect) {
            "2:3" -> 0.67f
            "3:4" -> 0.75f
            "1:1" -> 1.0f
            "16:9" -> 1.78f
            "WIDE" -> 1.33f
            else -> 0.72f
        }

    val listCoverAspectRatioValue: Float
        get() = when (listCoverAspect) {
            "1:1" -> 1.0f
            "3:4" -> 0.75f
            else -> 0.70f // 2:3
        }

    fun toJson(): String {
        val obj = JSONObject()
        obj.put("gridCoverStyle", gridCoverStyle)
        obj.put("gridCoverAspect", gridCoverAspect)
        obj.put("cardCornerRadiusDp", cardCornerRadiusDp)
        obj.put("coverCornerRadiusDp", coverCornerRadiusDp)
        obj.put("borderWidthDp", borderWidthDp.toDouble())
        obj.put("surfaceStyle", surfaceStyle)
        obj.put("listCoverWidthDp", listCoverWidthDp)
        obj.put("listCoverAspect", listCoverAspect)
        obj.put("listCoverPosition", listCoverPosition)
        obj.put("listCardPaddingDp", listCardPaddingDp)
        
        obj.put("statusVisible", statusVisible)
        obj.put("statusPosition", statusPosition)
        obj.put("statusStyle", statusStyle)

        obj.put("formatVisible", formatVisible)
        obj.put("formatPosition", formatPosition)
        obj.put("formatStyle", formatStyle)

        obj.put("progressVisible", progressVisible)
        obj.put("progressPosition", progressPosition)
        obj.put("progressStyle", progressStyle)

        obj.put("progressBarVisible", progressBarVisible)
        obj.put("progressBarPosition", progressBarPosition)
        obj.put("progressBarHeightDp", progressBarHeightDp)

        obj.put("wordsVisible", wordsVisible)
        obj.put("wordsPosition", wordsPosition)

        obj.put("ratingVisible", ratingVisible)
        obj.put("ratingPosition", ratingPosition)

        obj.put("bookmarkVisible", bookmarkVisible)
        obj.put("bookmarkPosition", bookmarkPosition)

        obj.put("authorVisible", authorVisible)
        obj.put("authorPosition", authorPosition)
        obj.put("authorMaxLines", authorMaxLines)

        obj.put("titleMaxLines", titleMaxLines)
        obj.put("titleTextSize", titleTextSize)
        return obj.toString()
    }

    companion object {
        val DEFAULT = CardLayoutConfig()

        // Preset 1: Classic Poster (Классический постер)
        val PRESET_CLASSIC_POSTER = CardLayoutConfig(
            gridCoverStyle = "TOP",
            gridCoverAspect = "2:3",
            cardCornerRadiusDp = 16,
            coverCornerRadiusDp = 0,
            borderWidthDp = 1f,
            surfaceStyle = "SURFACE_LOW",
            statusPosition = "COVER_TOP_END",
            statusStyle = "PILL",
            formatPosition = "COVER_TOP_START",
            formatStyle = "BADGE",
            progressPosition = "FOOTER_END",
            progressBarPosition = "BOTTOM_OF_COVER",
            progressBarHeightDp = 4,
            wordsPosition = "INFO_ROW",
            ratingPosition = "FOOTER_START",
            bookmarkPosition = "INFO_ROW",
            authorPosition = "BELOW_TITLE"
        )

        // Preset 2: Glass Full Background (Стеклянный арт / Неоновый постер)
        val PRESET_GLASS_POSTER = CardLayoutConfig(
            gridCoverStyle = "FULL_BACKGROUND",
            gridCoverAspect = "2:3",
            cardCornerRadiusDp = 18,
            coverCornerRadiusDp = 18,
            borderWidthDp = 1.2f,
            surfaceStyle = "GLASS_GRADIENT",
            statusPosition = "COVER_TOP_END",
            statusStyle = "PILL",
            formatPosition = "COVER_TOP_START",
            formatStyle = "SOLID",
            progressPosition = "FOOTER_END",
            progressBarPosition = "BOTTOM_OF_CARD",
            progressBarHeightDp = 4,
            wordsPosition = "INFO_ROW",
            ratingPosition = "FOOTER_START",
            bookmarkPosition = "INFO_ROW",
            authorPosition = "BELOW_TITLE"
        )

        // Preset 3: Minimal Zen (Чистый минимализм)
        val PRESET_MINIMAL = CardLayoutConfig(
            gridCoverStyle = "TOP",
            gridCoverAspect = "3:4",
            cardCornerRadiusDp = 12,
            coverCornerRadiusDp = 0,
            borderWidthDp = 0.5f,
            surfaceStyle = "SURFACE_LOW",
            statusPosition = "COVER_TOP_END",
            statusStyle = "MINIMAL_DOT",
            formatPosition = "COVER_TOP_START",
            formatStyle = "PLAIN_TEXT",
            progressPosition = "FOOTER_END",
            progressBarPosition = "BOTTOM_OF_CARD",
            progressBarHeightDp = 3,
            wordsPosition = "INFO_ROW",
            ratingPosition = "FOOTER_START",
            bookmarkPosition = "INFO_ROW",
            authorPosition = "BELOW_TITLE"
        )

        // Preset 4: Info Rich (Максимум деталей)
        val PRESET_INFO_RICH = CardLayoutConfig(
            gridCoverStyle = "TOP",
            gridCoverAspect = "2:3",
            cardCornerRadiusDp = 16,
            coverCornerRadiusDp = 0,
            borderWidthDp = 1f,
            surfaceStyle = "SURFACE_CONTAINER",
            statusPosition = "COVER_TOP_END",
            statusStyle = "PILL",
            formatPosition = "COVER_TOP_START",
            formatStyle = "BADGE",
            progressPosition = "FOOTER_END",
            progressBarPosition = "INSIDE_BODY",
            progressBarHeightDp = 4,
            wordsPosition = "INFO_ROW",
            ratingPosition = "FOOTER_START",
            bookmarkPosition = "INFO_ROW",
            authorPosition = "BELOW_TITLE",
            authorMaxLines = 1,
            titleMaxLines = 2
        )

        // Preset 5: Compact Grid (Ультракомпакт)
        val PRESET_COMPACT = CardLayoutConfig(
            gridCoverStyle = "COMPACT_BANNER",
            gridCoverAspect = "16:9",
            cardCornerRadiusDp = 12,
            coverCornerRadiusDp = 0,
            borderWidthDp = 0.5f,
            surfaceStyle = "SURFACE_LOW",
            statusPosition = "BODY_TOP_END",
            statusStyle = "PILL",
            formatPosition = "COVER_TOP_START",
            formatStyle = "SOLID",
            progressPosition = "FOOTER_END",
            progressBarPosition = "BOTTOM_OF_CARD",
            progressBarHeightDp = 2,
            wordsPosition = "INFO_ROW",
            ratingPosition = "FOOTER_START",
            bookmarkPosition = "INFO_ROW",
            authorPosition = "BELOW_TITLE",
            titleMaxLines = 1
        )

        // Preset 6: Book Shelf (Книжная полка 3:4)
        val PRESET_BOOK_SHELF = CardLayoutConfig(
            gridCoverStyle = "TOP",
            gridCoverAspect = "3:4",
            cardCornerRadiusDp = 14,
            coverCornerRadiusDp = 0,
            borderWidthDp = 1f,
            surfaceStyle = "SURFACE_LOW",
            statusPosition = "COVER_TOP_END",
            statusStyle = "DOT_TEXT",
            formatPosition = "COVER_TOP_START",
            formatStyle = "SOLID",
            progressPosition = "FOOTER_END",
            progressBarPosition = "BOTTOM_OF_COVER",
            progressBarHeightDp = 3,
            wordsPosition = "INFO_ROW",
            ratingPosition = "FOOTER_START",
            bookmarkPosition = "INFO_ROW",
            authorPosition = "BELOW_TITLE"
        )

        fun fromJson(jsonStr: String?): CardLayoutConfig {
            if (jsonStr.isNullOrBlank()) return DEFAULT
            return try {
                val obj = JSONObject(jsonStr)
                CardLayoutConfig(
                    gridCoverStyle = obj.optString("gridCoverStyle", DEFAULT.gridCoverStyle),
                    gridCoverAspect = obj.optString("gridCoverAspect", DEFAULT.gridCoverAspect),
                    cardCornerRadiusDp = obj.optInt("cardCornerRadiusDp", DEFAULT.cardCornerRadiusDp),
                    coverCornerRadiusDp = obj.optInt("coverCornerRadiusDp", DEFAULT.coverCornerRadiusDp),
                    borderWidthDp = obj.optDouble("borderWidthDp", DEFAULT.borderWidthDp.toDouble()).toFloat(),
                    surfaceStyle = obj.optString("surfaceStyle", DEFAULT.surfaceStyle),
                    listCoverWidthDp = obj.optInt("listCoverWidthDp", DEFAULT.listCoverWidthDp),
                    listCoverAspect = obj.optString("listCoverAspect", DEFAULT.listCoverAspect),
                    listCoverPosition = obj.optString("listCoverPosition", DEFAULT.listCoverPosition),
                    listCardPaddingDp = obj.optInt("listCardPaddingDp", DEFAULT.listCardPaddingDp),

                    statusVisible = obj.optBoolean("statusVisible", DEFAULT.statusVisible),
                    statusPosition = obj.optString("statusPosition", DEFAULT.statusPosition),
                    statusStyle = obj.optString("statusStyle", DEFAULT.statusStyle),

                    formatVisible = obj.optBoolean("formatVisible", DEFAULT.formatVisible),
                    formatPosition = obj.optString("formatPosition", DEFAULT.formatPosition),
                    formatStyle = obj.optString("formatStyle", DEFAULT.formatStyle),

                    progressVisible = obj.optBoolean("progressVisible", DEFAULT.progressVisible),
                    progressPosition = obj.optString("progressPosition", DEFAULT.progressPosition),
                    progressStyle = obj.optString("progressStyle", DEFAULT.progressStyle),

                    progressBarVisible = obj.optBoolean("progressBarVisible", DEFAULT.progressBarVisible),
                    progressBarPosition = obj.optString("progressBarPosition", DEFAULT.progressBarPosition),
                    progressBarHeightDp = obj.optInt("progressBarHeightDp", DEFAULT.progressBarHeightDp),

                    wordsVisible = obj.optBoolean("wordsVisible", DEFAULT.wordsVisible),
                    wordsPosition = obj.optString("wordsPosition", DEFAULT.wordsPosition),

                    ratingVisible = obj.optBoolean("ratingVisible", DEFAULT.ratingVisible),
                    ratingPosition = obj.optString("ratingPosition", DEFAULT.ratingPosition),

                    bookmarkVisible = obj.optBoolean("bookmarkVisible", DEFAULT.bookmarkVisible),
                    bookmarkPosition = obj.optString("bookmarkPosition", DEFAULT.bookmarkPosition),

                    authorVisible = obj.optBoolean("authorVisible", DEFAULT.authorVisible),
                    authorPosition = obj.optString("authorPosition", DEFAULT.authorPosition),
                    authorMaxLines = obj.optInt("authorMaxLines", DEFAULT.authorMaxLines),

                    titleMaxLines = obj.optInt("titleMaxLines", DEFAULT.titleMaxLines),
                    titleTextSize = obj.optString("titleTextSize", DEFAULT.titleTextSize)
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }
    }
}
