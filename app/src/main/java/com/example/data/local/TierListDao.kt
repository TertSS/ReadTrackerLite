package com.example.data.local

import androidx.room.*
import com.example.data.models.TierListRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TierListDao {
    @Query("SELECT * FROM tier_rows ORDER BY orderIndex ASC")
    fun getAllRows(): Flow<List<TierListRow>>

    @Query("SELECT * FROM tier_rows WHERE mode = :mode ORDER BY orderIndex ASC")
    fun getRowsByMode(mode: String): Flow<List<TierListRow>>

    @Query("SELECT * FROM tier_rows ORDER BY orderIndex ASC")
    suspend fun getAllRowsList(): List<TierListRow>

    @Query("SELECT * FROM tier_rows WHERE mode = :mode ORDER BY orderIndex ASC")
    suspend fun getRowsByModeList(mode: String): List<TierListRow>

    @Query("SELECT COUNT(*) FROM tier_rows")
    suspend fun getRowCount(): Int

    @Query("SELECT COUNT(*) FROM tier_rows WHERE mode = :mode")
    suspend fun getRowCountByMode(mode: String): Int

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

    @Query("DELETE FROM tier_rows WHERE mode = :mode")
    suspend fun deleteRowsByMode(mode: String)

    @Query("DELETE FROM tier_rows")
    suspend fun deleteAllRows()
}
