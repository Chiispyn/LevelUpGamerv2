package com.levelupgamer.levelup.ui.auth

import com.levelupgamer.levelup.data.repository.UserRepository
import com.levelupgamer.levelup.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class RegisterViewModelTest {

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: RegisterViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onRegisterClick with empty fields shows error`() = runTest {
        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Todos los campos son obligatorios", viewModel.uiState.value.error)
    }

    @Test
    fun `onRegisterClick with password mismatch shows error`() = runTest {
        viewModel.onNameChange("Test User")
        viewModel.onEmailChange("test@example.com")
        viewModel.onRutChange("100000040") // Verified Valid RUT
        viewModel.onBirthDateChange("01012000")
        viewModel.onPhoneChange("912345678")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password456") // Mismatch

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.error)
    }

    @Test
    fun `onRegisterClick with underage birthdate shows error`() = runTest {
        // Assuming current year is 2025 (from system prompt), 2010 would be 15 years old
        viewModel.onNameChange("Test User")
        viewModel.onEmailChange("test@example.com")
        viewModel.onRutChange("100000040")
        viewModel.onBirthDateChange("01012015") // Underage
        viewModel.onPhoneChange("912345678")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Debes ser mayor de 18 años para registrarte", viewModel.uiState.value.error)
    }

    @Test
    fun `onRegisterClick with existing email shows error`() = runTest {
        val email = "existing@example.com"
        `when`(userRepository.getUserByEmail(email)).thenReturn(User(name="Existing", email=email, rut="", birthDate="", phone="", passwordHash=""))

        viewModel.onNameChange("Test User")
        viewModel.onEmailChange(email)
        viewModel.onRutChange("100000040") 
        viewModel.onBirthDateChange("01012000")
        viewModel.onPhoneChange("912345678")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("El email ya está registrado.", viewModel.uiState.value.error)
    }

    @Test
    fun `onRegisterClick with valid data registers user`() = runTest {
        val email = "new@example.com"
        `when`(userRepository.getUserByEmail(email)).thenReturn(null)

        viewModel.onNameChange("Test User")
        viewModel.onEmailChange(email)
        viewModel.onRutChange("100000040") // Valid RUT
        viewModel.onBirthDateChange("01012000")
        viewModel.onPhoneChange("912345678")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")

        viewModel.onRegisterClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.registrationSuccess)
        assertEquals(null, viewModel.uiState.value.error)
    }
}
