package com.levelupgamer.levelup.repository

import com.levelupgamer.levelup.data.local.dao.UserRewardDao
import com.levelupgamer.levelup.model.UserReward
import kotlinx.coroutines.flow.Flow

class UserRewardRepository(private val userRewardDao: UserRewardDao) {

    fun getUserRewards(userId: Int): Flow<List<UserReward>> {
        return userRewardDao.getUserRewards(userId)
    }

    suspend fun addRewardForUser(userId: Int, rewardId: String) {
        val userReward = UserReward(userId = userId, rewardId = rewardId)
        userRewardDao.insert(userReward)
    }

    suspend fun delete(userReward: UserReward) {
        userRewardDao.delete(userReward)
    }
}
