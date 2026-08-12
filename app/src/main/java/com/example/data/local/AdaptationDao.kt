package com.example.data.local

import androidx.room.*
import com.example.data.models.Adaptation
import kotlinx.coroutines.flow.Flow

@Dao
interface AdaptationDao {
    @Query("SELECT * FROM adaptations ORDER BY updatedAt DESC")
    fun getAllAdaptations(): Flow<List<Adaptation>>

    @Query("SELECT * FROM adaptations ORDER BY updatedAt DESC")
    suspend fun getAllAdaptationsSync(): List<Adaptation>

    @Query("SELECT COUNT(*) FROM adaptations")
    suspend fun getAdaptationCount(): Int

    @Query("SELECT * FROM adaptations WHERE id = :id LIMIT 1")
    fun getAdaptationByIdFlow(id: String): Flow<Adaptation?>

    @Query("SELECT * FROM adaptations WHERE id = :id LIMIT 1")
    suspend fun getAdaptationById(id: String): Adaptation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdaptation(adaptation: Adaptation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdaptations(adaptations: List<Adaptation>)

    @Update
    suspend fun updateAdaptation(adaptation: Adaptation)

    @Delete
    suspend fun deleteAdaptation(adaptation: Adaptation)

    @Query("DELETE FROM adaptations WHERE id = :id")
    suspend fun deleteAdaptationById(id: String)

    @Query("DELETE FROM adaptations")
    suspend fun deleteAllAdaptations()
}
