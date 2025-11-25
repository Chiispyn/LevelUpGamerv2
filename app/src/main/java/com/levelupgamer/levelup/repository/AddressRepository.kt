package com.levelupgamer.levelup.repository

import com.levelupgamer.levelup.data.local.dao.AddressDao
import com.levelupgamer.levelup.model.UserAddress
import kotlinx.coroutines.flow.Flow

class AddressRepository(private val addressDao: AddressDao) {

    fun getAddressesForUser(userId: Int): Flow<List<UserAddress>> {
        return addressDao.getAddressesForUser(userId)
    }

    suspend fun addAddress(address: UserAddress) {
        if (address.isPrimary) {
            addressDao.clearPrimaryFlags(address.userId)
        }
        addressDao.insert(address)
    }

    suspend fun updateAddress(address: UserAddress) {
        if (address.isPrimary) {
            addressDao.clearPrimaryFlags(address.userId)
        }
        addressDao.update(address)
    }

    suspend fun deleteAddress(address: UserAddress) {
        addressDao.delete(address)
    }
    
    suspend fun setPrimary(userId: Int, addressId: String) {
        addressDao.clearPrimaryFlags(userId)
        addressDao.setPrimary(addressId)
    }
}
