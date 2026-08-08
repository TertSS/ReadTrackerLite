package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.models.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class Converters {

    // Strings list
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        value.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = ArrayList<String>(array.length())
            for (i in 0 until array.length()) {
                list.add(array.optString(i))
            }
            list
        } catch (e: Throwable) {
            emptyList()
        }
    }

    // VolumeEntry list
    @TypeConverter
    fun fromVolumeEntryList(value: List<VolumeEntry>?): String {
        if (value.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        value.forEach {
            val obj = JSONObject()
            obj.put("volumeNumber", it.volumeNumber)
            obj.put("wordCount", it.wordCount)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toVolumeEntryList(value: String?): List<VolumeEntry> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = ArrayList<VolumeEntry>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    VolumeEntry(
                        volumeNumber = obj.optInt("volumeNumber", i + 1),
                        wordCount = obj.optLong("wordCount", 0L)
                    )
                )
            }
            list
        } catch (e: Throwable) {
            emptyList()
        }
    }

    // SeasonEntry list
    @TypeConverter
    fun fromSeasonEntryList(value: List<SeasonEntry>?): String {
        if (value.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        value.forEach {
            val obj = JSONObject()
            obj.put("seasonNumber", it.seasonNumber)
            obj.put("totalEpisodes", it.totalEpisodes)
            obj.put("watchedEpisodes", it.watchedEpisodes)
            obj.put("defaultEpisodeDurationMinutes", it.defaultEpisodeDurationMinutes)
            val durObj = JSONObject()
            it.episodeDurations.forEach { (k, v) -> durObj.put(k, v) }
            obj.put("episodeDurations", durObj)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toSeasonEntryList(value: String?): List<SeasonEntry> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = ArrayList<SeasonEntry>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val durMap = mutableMapOf<String, Int>()
                val durObj = obj.optJSONObject("episodeDurations")
                if (durObj != null) {
                    val keys = durObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        durMap[key] = durObj.optInt(key, 24)
                    }
                }
                list.add(
                    SeasonEntry(
                        seasonNumber = obj.optInt("seasonNumber", i + 1),
                        totalEpisodes = obj.optInt("totalEpisodes", 12),
                        watchedEpisodes = obj.optInt("watchedEpisodes", 0),
                        defaultEpisodeDurationMinutes = obj.optInt("defaultEpisodeDurationMinutes", 24),
                        episodeDurations = durMap
                    )
                )
            }
            list
        } catch (e: Throwable) {
            emptyList()
        }
    }

    // MovieEntry list
    @TypeConverter
    fun fromMovieEntryList(value: List<MovieEntry>?): String {
        if (value.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        value.forEach {
            val obj = JSONObject()
            obj.put("movieNumber", it.movieNumber)
            obj.put("title", it.title)
            obj.put("isWatched", it.isWatched)
            obj.put("durationMinutes", it.durationMinutes)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toMovieEntryList(value: String?): List<MovieEntry> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = ArrayList<MovieEntry>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    MovieEntry(
                        movieNumber = obj.optInt("movieNumber", i + 1),
                        title = obj.optString("title", ""),
                        isWatched = obj.optBoolean("isWatched", false),
                        durationMinutes = obj.optInt("durationMinutes", 100)
                    )
                )
            }
            list
        } catch (e: Throwable) {
            emptyList()
        }
    }

    // TierItem list
    @TypeConverter
    fun fromTierItemList(value: List<TierItem>?): String {
        if (value.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        value.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            if (it.sourceId != null) obj.put("sourceId", it.sourceId)
            obj.put("title", it.title)
            if (it.coverUrl != null) obj.put("coverUrl", it.coverUrl)
            obj.put("colorPlaceholder", it.colorPlaceholder)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toTierItemList(value: String?): List<TierItem> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = ArrayList<TierItem>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    TierItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        sourceId = if (obj.has("sourceId") && !obj.isNull("sourceId")) obj.optString("sourceId") else null,
                        title = obj.optString("title", ""),
                        coverUrl = if (obj.has("coverUrl") && !obj.isNull("coverUrl")) obj.optString("coverUrl") else null,
                        colorPlaceholder = obj.optLong("colorPlaceholder", 0xFF201F1FL)
                    )
                )
            }
            list
        } catch (e: Throwable) {
            emptyList()
        }
    }

    // Enums
    @TypeConverter
    fun fromTitleStatus(status: TitleStatus?): String = status?.id ?: TitleStatus.READING.id

    @TypeConverter
    fun toTitleStatus(value: String?): TitleStatus = if (value.isNullOrBlank()) TitleStatus.READING else TitleStatus.fromId(value)

    @TypeConverter
    fun fromTitleFormat(format: TitleFormat?): String = format?.id ?: TitleFormat.SERIES.id

    @TypeConverter
    fun toTitleFormat(value: String?): TitleFormat = if (value.isNullOrBlank()) TitleFormat.SERIES else TitleFormat.fromId(value)

    @TypeConverter
    fun fromAdaptationType(type: AdaptationType?): String = type?.id ?: AdaptationType.SERIES.id

    @TypeConverter
    fun toAdaptationType(value: String?): AdaptationType = if (value.isNullOrBlank()) AdaptationType.SERIES else AdaptationType.fromId(value)

    @TypeConverter
    fun fromReviewType(type: ReviewType?): String = type?.id ?: ReviewType.OVERALL.id

    @TypeConverter
    fun toReviewType(value: String?): ReviewType = if (value.isNullOrBlank()) ReviewType.OVERALL else ReviewType.fromId(value)

    @TypeConverter
    fun fromRatingScale(scale: RatingScale?): String = scale?.name ?: RatingScale.STARS_10.name

    @TypeConverter
    fun toRatingScale(value: String?): RatingScale = if (value.isNullOrBlank()) RatingScale.STARS_10 else try {
        RatingScale.valueOf(value)
    } catch (e: Throwable) {
        RatingScale.STARS_10
    }

    @TypeConverter
    fun fromLibraryMode(mode: LibraryMode?): String = mode?.name ?: LibraryMode.BOOKS.name

    @TypeConverter
    fun toLibraryMode(value: String?): LibraryMode = if (value.isNullOrBlank()) LibraryMode.BOOKS else try {
        LibraryMode.valueOf(value)
    } catch (e: Throwable) {
        LibraryMode.BOOKS
    }
}
