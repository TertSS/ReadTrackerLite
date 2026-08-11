package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class TierItem(
    val id: String = UUID.randomUUID().toString(),
    val sourceId: String? = null,
    val title: String,
    val coverUrl: String? = null,
    val colorPlaceholder: Long = 0xFF201F1FL
)

@Entity(tableName = "tier_rows")
data class TierListRow(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long,
    val textColor: Long = 0xFFFFFFFFL,
    val orderIndex: Int,
    val items: List<TierItem> = emptyList()
)
