package com.example.data.local

import androidx.room.*
import com.example.data.models.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<Review>>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    suspend fun getAllReviewsSync(): List<Review>

    @Query("SELECT COUNT(*) FROM reviews")
    suspend fun getReviewCount(): Int

    @Query("SELECT * FROM reviews WHERE targetId = :targetId ORDER BY createdAt DESC")
    fun getReviewsForTarget(targetId: String): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReviewById(id: String)

    @Query("DELETE FROM reviews WHERE targetId = :targetId")
    suspend fun deleteReviewsByTargetId(targetId: String)

    @Query("DELETE FROM reviews")
    suspend fun deleteAllReviews()
}
