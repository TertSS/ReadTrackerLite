package com.example.data.models

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Represents a user-created card styling preset.
 */
data class CustomCardPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val config: CardLayoutConfig,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("configJson", config.toJson())
        obj.put("createdAt", createdAt)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): CustomCardPreset {
            val id = obj.optString("id", UUID.randomUUID().toString())
            val name = obj.optString("name", "Мой стиль")
            val configJson = obj.optString("configJson", "")
            val config = CardLayoutConfig.fromJson(configJson)
            val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            return CustomCardPreset(id, name, config, createdAt)
        }

        fun fromJsonList(jsonStr: String?): List<CustomCardPreset> {
            if (jsonStr.isNullOrBlank()) return emptyList()
            return try {
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<CustomCardPreset>()
                for (i in 0 until arr.length()) {
                    list.add(fromJson(arr.getJSONObject(i)))
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun listToJson(list: List<CustomCardPreset>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }
    }
}
