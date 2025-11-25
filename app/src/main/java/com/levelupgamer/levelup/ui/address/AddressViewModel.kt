package com.levelupgamer.levelup.address

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelupgamer.levelup.repository.AddressRepository
import com.levelupgamer.levelup.model.UserAddress
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddressUiState(
    val addresses: List<UserAddress> = emptyList(),
    val isLoading: Boolean = true
)

class AddressViewModel(
    private val addressRepository: AddressRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    init {
        loadAddresses()
    }

    private fun loadAddresses() {
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context)
            if (userId != null) {
                addressRepository.getAddressesForUser(userId).collect { addresses ->
                    _uiState.update { it.copy(addresses = addresses, isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) } // No user, no addresses
            }
        }
    }

    fun addOrUpdateAddress(street: String, number: String, commune: String, region: String, isPrimary: Boolean, id: String? = null) {
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context) ?: return@launch
            val address = UserAddress(
                id = id ?: UUID.randomUUID().toString(),
                userId = userId,
                street = street,
                numberOrApt = number,
                commune = commune,
                region = region,
                isPrimary = isPrimary
            )
            if (id != null) {
                addressRepository.updateAddress(address)
            } else {
                addressRepository.addAddress(address)
            }
        }
    }

    fun deleteAddress(address: UserAddress) {
        viewModelScope.launch {
            addressRepository.deleteAddress(address)
        }
    }

    fun setPrimaryAddress(address: UserAddress) {
        viewModelScope.launch {
            addressRepository.updateAddress(address.copy(isPrimary = true))
        }
    }
}
