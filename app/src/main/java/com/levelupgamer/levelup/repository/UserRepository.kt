package com.levelupgamer.levelup.repository

import com.levelupgamer.levelup.data.local.dao.UserDao
import com.levelupgamer.levelup.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun insert(user: User) {
        userDao.insert(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    suspend fun findByReferralCode(code: String): User? {
        return userDao.findByReferralCode(code)
    }

    suspend fun update(user: User) {
        userDao.update(user)
    }
}
