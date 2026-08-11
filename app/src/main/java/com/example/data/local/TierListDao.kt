package com.example.data.local

import androidx.room.*
import com.example.data.models.CustomTierItem
import com.example.data.models.TierListRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TierListDao {
    @Query("SELECT * FROM tier_rows ORDER BY orderIndex ASC")
    fun getAllRows(): Flow<List<TierListRow>>

    @Query("SELECT * FROM tier_rows ORDER BY orderIndex ASC")
    suspend fun getAllRowsList(): List<TierListRow>

    @Query("SELECT COUNT(*) FROM tier_rows")
    suspend fun getRowCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRow(row: TierListRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(rows: List<TierListRow>)

    @Update
    suspend fun updateRow(row: TierListRow)

    @Delete
    suspend fun deleteRow(row: TierListRow)

    @Query("DELETE FROM tier_rows WHERE id = :id")
    suspend fun deleteRowById(id: String)

    @Query("DELETE FROM tier_rows")
    suspend fun deleteAllRows()

    // Custom Tier Items operations
    @Query("SELECT * FROM custom_tier_items")
    fun getAllCustomTierItems(): Flow<List<CustomTierItem>>

    @Query("SELECT * FROM custom_tier_items")
    suspend fun getAllCustomTierItemsList(): List<CustomTierItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTierItem(item: CustomTierItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTierItems(items: List<CustomTierItem>)

    @Update
    suspend fun updateCustomTierItem(item: CustomTierItem)

    @Delete
    suspend fun deleteCustomTierItem(item: CustomTierItem)

    @Query("DELETE FROM custom_tier_items WHERE id = :id")
    suspend fun deleteCustomTierItemById(id: String)

    @Query("DELETE FROM custom_tier_items")
    suspend fun deleteAllCustomTierItems()
}
