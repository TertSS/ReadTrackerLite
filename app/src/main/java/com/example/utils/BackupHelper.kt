package com.example.utils

import com.example.data.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray
import org.json.JSONObject

object BackupHelper {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    data class ExportPayload(
        val version: Int = 2,
        val exportedAt: Long = System.currentTimeMillis(),
        val books: List<BookTitle> = emptyList(),
        val adaptations: List<Adaptation> = emptyList(),
        val reviews: List<Review> = emptyList(),
        val tierRows: List<TierListRow> = emptyList()
    )

    fun exportLibraryToJson(
        books: List<BookTitle>,
        adaptations: List<Adaptation>,
        reviews: List<Review>,
        tierRows: List<TierListRow>
    ): String {
        val payload = ExportPayload(
            version = 2,
            exportedAt = System.currentTimeMillis(),
            books = books,
            adaptations = adaptations,
            reviews = reviews,
            tierRows = tierRows
        )
        return exportPayloadToJson(payload)
    }

    fun exportPayloadToJson(payload: ExportPayload): String {
        val adapter = moshi.adapter(ExportPayload::class.java).indent("  ")
        return adapter.toJson(payload)
    }

    fun exportSettingsToJson(settings: AppSettings): String {
        val adapter = moshi.adapter(AppSettings::class.java).indent("  ")
        return adapter.toJson(settings)
    }

    data class ImportResult(
        val success: Boolean,
        val books: List<BookTitle> = emptyList(),
        val adaptations: List<Adaptation> = emptyList(),
        val reviews: List<Review> = emptyList(),
        val tierRows: List<TierListRow> = emptyList(),
        val errorMessage: String? = null
    )

    fun parseLibraryJson(jsonString: String): ImportResult {
        return try {
            val trimmed = jsonString.trim()
            if (trimmed.startsWith("[")) {
                // Legacy plain array of books
                val bookListType = Types.newParameterizedType(List::class.java, BookTitle::class.java)
                val bookAdapter = moshi.adapter<List<BookTitle>>(bookListType)
                val books = bookAdapter.fromJson(trimmed) ?: emptyList()
                ImportResult(success = true, books = books)
            } else {
                val jsonObject = JSONObject(trimmed)
                val books = if (jsonObject.has("books")) {
                    val bookListType = Types.newParameterizedType(List::class.java, BookTitle::class.java)
                    val bookAdapter = moshi.adapter<List<BookTitle>>(bookListType)
                    bookAdapter.fromJson(jsonObject.getJSONArray("books").toString()) ?: emptyList()
                } else emptyList()

                val adaptations = if (jsonObject.has("adaptations")) {
                    val adListType = Types.newParameterizedType(List::class.java, Adaptation::class.java)
                    val adAdapter = moshi.adapter<List<Adaptation>>(adListType)
                    adAdapter.fromJson(jsonObject.getJSONArray("adaptations").toString()) ?: emptyList()
                } else if (jsonObject.has("franchises")) {
                    val adListType = Types.newParameterizedType(List::class.java, Adaptation::class.java)
                    val adAdapter = moshi.adapter<List<Adaptation>>(adListType)
                    adAdapter.fromJson(jsonObject.getJSONArray("franchises").toString()) ?: emptyList()
                } else emptyList()

                val reviews = if (jsonObject.has("reviews")) {
                    val revListType = Types.newParameterizedType(List::class.java, Review::class.java)
                    val revAdapter = moshi.adapter<List<Review>>(revListType)
                    revAdapter.fromJson(jsonObject.getJSONArray("reviews").toString()) ?: emptyList()
                } else emptyList()

                val tierRows = if (jsonObject.has("tierRows")) {
                    val rowListType = Types.newParameterizedType(List::class.java, TierListRow::class.java)
                    val rowAdapter = moshi.adapter<List<TierListRow>>(rowListType)
                    rowAdapter.fromJson(jsonObject.getJSONArray("tierRows").toString()) ?: emptyList()
                } else emptyList()

                ImportResult(
                    success = true,
                    books = books,
                    adaptations = adaptations,
                    reviews = reviews,
                    tierRows = tierRows
                )
            }
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errorMessage = "Ошибка парсинга JSON: ${e.localizedMessage ?: "Некорректный формат данных"}"
            )
        }
    }

    fun parseSettingsJson(jsonString: String): Pair<Boolean, AppSettings?> {
        return try {
            val adapter = moshi.adapter(AppSettings::class.java)
            val settings = adapter.fromJson(jsonString)
            if (settings != null) true to settings else false to null
        } catch (e: Exception) {
            false to null
        }
    }
}
