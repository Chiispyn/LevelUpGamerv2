package com.levelupgamer.levelup.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.repository.AddressRepository
import com.levelupgamer.levelup.repository.UserRepository
import com.levelupgamer.levelup.model.User
import com.levelupgamer.levelup.model.UserAddress
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LoginDestination { NONE, MAIN, ADMIN }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val navigateTo: LoginDestination = LoginDestination.NONE
)

class LoginViewModel(private val userRepository: UserRepository, private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onLoginClick() {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.email == "admin@levelupgamer.com" && state.password == "admin123") {
                UserManager.saveUser(context, -1, "Admin", state.email)
                _uiState.update { it.copy(navigateTo = LoginDestination.ADMIN) }
                return@launch
            }

            when {
                state.email.isBlank() -> _uiState.update { it.copy(error = "Ingresa tu email") }
                state.password.length < 6 -> _uiState.update { it.copy(error = "Contraseña mínima 6 caracteres") }
                else -> {
                    val user = userRepository.getUserByEmail(state.email)
                    if (user != null) {
                        UserManager.saveUser(context, user.id, user.name, user.email)
                        _uiState.update { it.copy(navigateTo = LoginDestination.MAIN, error = null) }
                    } else {
                        _uiState.update { it.copy(error = "Usuario o contraseña incorrectos") }
                    }
                }
            }
        }
    }

    fun onTestUserClick(email: String, name: String, addresses: List<UserAddress> = emptyList()) {
        viewModelScope.launch {
            var user = userRepository.getUserByEmail(email)
            if (user == null) {
                val newUser = User(name = name, email = email, rut = "", birthDate = "", phone = "912345678", passwordHash = "password")
                userRepository.insert(newUser)
                user = userRepository.getUserByEmail(email)
            }
            
            if (user != null) {
                UserManager.saveUser(context, user.id, user.name, user.email)
                
                val addressRepository = AddressRepository((context.applicationContext as MyApp).database.addressDao())
                val existingAddresses = addressRepository.getAddressesForUser(user.id).first()

                if (existingAddresses.isEmpty()) {
                    addresses.forEach { address ->
                        val userSpecificAddress = address.copy(userId = user.id)
                        addressRepository.addAddress(userSpecificAddress)
                    }
                }

                _uiState.update { it.copy(navigateTo = LoginDestination.MAIN) }
            } 
        }
    }
    
    fun onAdminShortcutClick() {
        viewModelScope.launch {
            UserManager.saveUser(context, -1, "Admin", "admin@levelupgamer.com")
            _uiState.update { it.copy(navigateTo = LoginDestination.ADMIN) }
        }
    }
}
