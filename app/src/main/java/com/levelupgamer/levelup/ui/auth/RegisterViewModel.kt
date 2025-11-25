package com.levelupgamer.levelup.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelupgamer.levelup.repository.UserRepository
import com.levelupgamer.levelup.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val rut: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val referralCode: String = "",
    val error: String? = null,
    val registrationSuccess: Boolean = false
)

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name) } }
    fun onEmailChange(email: String) { _uiState.update { it.copy(email = email) } }
    fun onRutChange(rut: String) { _uiState.update { it.copy(rut = rut) } }
    fun onBirthDateChange(birthDate: String) { _uiState.update { it.copy(birthDate = birthDate) } }
    fun onPhoneChange(phone: String) { _uiState.update { it.copy(phone = phone) } }
    fun onPasswordChange(password: String) { _uiState.update { it.copy(password = password) } }
    fun onConfirmPasswordChange(confirmPassword: String) { _uiState.update { it.copy(confirmPassword = confirmPassword) } }
    fun onReferralCodeChange(referralCode: String) { _uiState.update { it.copy(referralCode = referralCode) } }

    fun onRegisterClick() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!isValid(state)) return@launch

            var referringUser: User? = null
            if (state.referralCode.isNotBlank()) {
                referringUser = userRepository.findByReferralCode(state.referralCode.uppercase())
                if (referringUser == null) {
                    _uiState.update { it.copy(error = "El código de referido no es válido.") }
                    return@launch
                }
            }

            val existingUser = userRepository.getUserByEmail(state.email)
            if (existingUser != null) {
                _uiState.update { it.copy(error = "El email ya está registrado.") }
                return@launch
            }

            val newUser = User(
                name = state.name,
                email = state.email,
                rut = state.rut,
                birthDate = state.birthDate,
                phone = state.phone,
                passwordHash = state.password, // En una app real, esto debería ser un hash
                profileImageUri = null
            )
            userRepository.insert(newUser) // CORREGIDO

            referringUser?.let {
                val updatedUser = it.copy(points = it.points + 100) // Recompensa por referir
                userRepository.update(updatedUser) // CORREGIDO
            }

            _uiState.update { it.copy(registrationSuccess = true) }
        }
    }

    private fun isValid(state: RegisterUiState): Boolean {
        return when {
            state.name.isBlank() || state.email.isBlank() || state.rut.isBlank() || state.birthDate.isBlank() || state.phone.isBlank() || state.password.isBlank() -> {
                _uiState.update { it.copy(error = "Todos los campos son obligatorios") }
                false
            }
            !isValidRut(state.rut) -> {
                 _uiState.update { it.copy(error = "El RUT ingresado no es válido") }
                false
            }
            !isOfLegalAge(state.birthDate) -> {
                _uiState.update { it.copy(error = "Debes ser mayor de 18 años para registrarte") }
                false
            }
            state.password.length < 6 -> {
                _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
                false
            }
            state.password != state.confirmPassword -> {
                _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
                false
            }
            else -> {
                _uiState.update { it.copy(error = null) } // Limpiar errores
                true
            }
        }
    }

    private fun isValidRut(rut: String): Boolean {
        if (rut.length !in 8..9) return false
        try {
            var rutAux = rut.uppercase().replace(".", "").replace("-", "")
            val dv = rutAux.last()
            val numbers = rutAux.substring(0, rutAux.length - 1).toInt()
            var m = 0
            var s = 1
            var t = numbers
            while (t != 0) {
                s = (s + t % 10 * (9 - m++ % 6)) % 11
                t /= 10
            }
            val calculatedDv = if (s != 0) (s + 47).toChar() else 'K'
            return dv == calculatedDv
        } catch (e: Exception) {
            return false
        }
    }

    private fun isOfLegalAge(birthDateStr: String): Boolean {
        if (birthDateStr.length != 8) return false
        return try {
            val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
            val birthDate = sdf.parse(birthDateStr) ?: return false

            val dob = Calendar.getInstance()
            dob.time = birthDate

            val today = Calendar.getInstance()

            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            
            age >= 18
        } catch (e: Exception) {
            false
        }
    }
}
