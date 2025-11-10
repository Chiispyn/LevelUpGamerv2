package com.levelupgamer.levelup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.levelupgamer.levelup.model.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE productCode = :productCode ORDER BY date DESC")
    fun getReviewsForProduct(productCode: String): Flow<List<Review>>

    @Query("SELECT AVG(rating) FROM reviews WHERE productCode = :productCode")
    fun getAverageRatingForProduct(productCode: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<Review>)
}
