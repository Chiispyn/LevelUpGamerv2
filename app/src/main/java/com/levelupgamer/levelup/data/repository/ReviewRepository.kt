package com.levelupgamer.levelup.data.repository

import com.levelupgamer.levelup.data.local.dao.ReviewDao
import com.levelupgamer.levelup.model.Review
import kotlinx.coroutines.flow.Flow

class ReviewRepository(private val reviewDao: ReviewDao) {

    fun getReviewsForProduct(productCode: String): Flow<List<Review>> {
        return reviewDao.getReviewsForProduct(productCode)
    }

    fun getAverageRatingForProduct(productCode: String): Flow<Double?> {
        return reviewDao.getAverageRatingForProduct(productCode)
    }

    suspend fun addReview(review: Review) {
        reviewDao.insert(review)
    }
}
