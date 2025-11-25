package com.levelupgamer.levelup.repository

import com.levelupgamer.levelup.data.local.dao.RewardDao
import com.levelupgamer.levelup.model.Reward
import kotlinx.coroutines.flow.Flow

class RewardRepository(private val rewardDao: RewardDao) {

    fun getAllRewards(): Flow<List<Reward>> {
        return rewardDao.getAllRewards()
    }

    suspend fun getReward(id: String): Reward? {
        return rewardDao.getReward(id)
    }

    suspend fun addReward(reward: Reward) {
        rewardDao.addReward(reward)
    }

    suspend fun updateReward(reward: Reward) {
        rewardDao.updateReward(reward)
    }

    suspend fun deleteReward(reward: Reward) {
        rewardDao.deleteReward(reward)
    }
}
