package com.levelupgamer.levelup.ui.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LoginViewModelTest {

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: LoginViewModel
    private lateinit var context: Context

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        viewModel = LoginViewModel(userRepository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEmailChange updates email in uiState`() = runTest {
        val newEmail = "test@example.com"
        viewModel.onEmailChange(newEmail)
        assertEquals(newEmail, viewModel.uiState.value.email)
    }

    @Test
    fun `onPasswordChange updates password in uiState`() = runTest {
        val newPassword = "password123"
        viewModel.onPasswordChange(newPassword)
        assertEquals(newPassword, viewModel.uiState.value.password)
    }

    @Test
    fun `onLoginClick with empty email shows error`() = runTest {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()
        
        // Advance coroutines
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("Ingresa tu email", viewModel.uiState.value.error)
    }

    @Test
    fun `onLoginClick with short password shows error`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("123")
        viewModel.onLoginClick()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("Contraseña mínima 6 caracteres", viewModel.uiState.value.error)
    }

    @Test
    fun `onLoginClick with valid credentials navigates to MAIN`() = runTest {
        val email = "user@example.com"
        val password = "password123"
        val user = User(id = 1, name = "Test User", email = email, passwordHash = "hash", rut = "", birthDate = "", phone = "")

        `when`(userRepository.getUserByEmail(email)).thenReturn(user)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onLoginClick()

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginDestination.MAIN, viewModel.uiState.value.navigateTo)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `onLoginClick with invalid credentials shows error`() = runTest {
        val email = "wrong@example.com"
        val password = "password123"

        `when`(userRepository.getUserByEmail(email)).thenReturn(null)

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onLoginClick()

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Usuario o contraseña incorrectos", viewModel.uiState.value.error)
    }
}
