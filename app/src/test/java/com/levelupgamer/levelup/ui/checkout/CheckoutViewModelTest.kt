package com.levelupgamer.levelup.ui.checkout

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.levelupgamer.levelup.data.repository.*
import com.levelupgamer.levelup.model.*
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CheckoutViewModelTest {

    @Mock private lateinit var orderRepository: OrderRepository
    @Mock private lateinit var productRepository: ProductRepository
    @Mock private lateinit var addressRepository: AddressRepository
    @Mock private lateinit var rewardRepository: RewardRepository
    @Mock private lateinit var userRewardRepository: UserRewardRepository
    @Mock private lateinit var userRepository: UserRepository

    private lateinit var viewModel: CheckoutViewModel
    private lateinit var context: Context
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        UserManager.saveUser(context, 1, "Test User", "test@example.com")

        // Default mocks
        `when`(addressRepository.getAddressesForUser(1)).thenReturn(flowOf(emptyList()))
        `when`(userRewardRepository.getUserRewards(1)).thenReturn(flowOf(emptyList()))
        `when`(rewardRepository.getAllRewards()).thenReturn(flowOf(emptyList()))

        viewModel = CheckoutViewModel(
            orderRepository,
            productRepository,
            addressRepository,
            rewardRepository,
            userRewardRepository,
            userRepository,
            context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        if (::context.isInitialized) {
            UserManager.logout(context)
        }
    }

    @Test
    fun `calcular totales correctamente con items en el carrito`() = runTest {
        val product = Product(
            code = "P001",
            name = "Test Product",
            category = "Category",
            price = 1000,
            description = "Description",
            quantity = 10,
            imageUrl = "",
            imageResId = null,
            averageRating = 0f
        )
        val items = listOf(Pair(product, 2)) // 2 * 1000 = 2000

        viewModel.initCartItems(items)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2000.0, viewModel.uiState.value.subtotal, 0.0)
        assertEquals(2000.0, viewModel.uiState.value.total, 0.0)
    }

    @Test
    fun `aplicar descuento de recompensa correctamente`() = runTest {
        val product = Product(
            code = "P001",
            name = "Test Product",
            category = "Category",
            price = 10000,
            description = "Description",
            quantity = 10,
            imageUrl = "",
            imageResId = null,
            averageRating = 0f
        )
        val items = listOf(Pair(product, 1)) // 10000
        val reward = Reward(
            id = "1",
            title = "10% Off",
            description = "",
            pointsCost = 100,
            type = RewardType.DISCOUNT_PERCENTAGE.name,
            value = 10.0
        )

        viewModel.initCartItems(items)
        viewModel.onRewardSelected(reward)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(10000.0, viewModel.uiState.value.subtotal, 0.0)
        assertEquals(1000.0, viewModel.uiState.value.discountAmount, 0.0) // 10% of 10000
        assertEquals(9000.0, viewModel.uiState.value.total, 0.0)
    }

    @Test
    fun `realizar pedido exitosamente limpia el carrito y navega`() = runTest {
        val product = Product(
            code = "P001",
            name = "Test Product",
            category = "Category",
            price = 1000,
            description = "Description",
            quantity = 10,
            imageUrl = "",
            imageResId = null,
            averageRating = 0f
        )
        val items = listOf(Pair(product, 1))
        val address = UserAddress(
            id = "1",
            userId = 1,
            street = "Street",
            numberOrApt = "123",
            region = "Metropolitana",
            commune = "Santiago",
            isPrimary = true
        )
        val user = User(id = 1, name = "Test", email = "test@test.com", rut = "", birthDate = "", phone = "", passwordHash = "", isActive = true, points = 0)

        `when`(userRepository.getUserById(1)).thenReturn(user)
        
        viewModel.initCartItems(items)
        viewModel.onAddressSelected(address)
        viewModel.onPaymentMethodSelected("WebPay")
        
        var cartCleared = false
        viewModel.onPlaceOrderClick(onCartCleared = { cartCleared = true })
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.orderPlacedId)
        assertEquals(true, cartCleared)
        verify(orderRepository).addOrder(any(), any())
    }

    @Test
    fun `no permitir compra si usuario esta suspendido`() = runTest {
        val product = Product(
            code = "P001",
            name = "Test Product",
            category = "Category",
            price = 1000,
            description = "Description",
            quantity = 10,
            imageUrl = "",
            imageResId = null,
            averageRating = 0f
        )
        val items = listOf(Pair(product, 1))
        val address = UserAddress(
            id = "1",
            userId = 1,
            street = "Street",
            numberOrApt = "123",
            region = "Metropolitana",
            commune = "Santiago",
            isPrimary = true
        )
        val user = User(id = 1, name = "Test", email = "test@test.com", rut = "", birthDate = "", phone = "", passwordHash = "", isActive = false, points = 0) // Suspended

        `when`(userRepository.getUserById(1)).thenReturn(user)

        viewModel.initCartItems(items)
        viewModel.onAddressSelected(address)
        viewModel.onPaymentMethodSelected("WebPay")

        viewModel.onPlaceOrderClick(onCartCleared = {})
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Cuenta suspendida", viewModel.uiState.value.error)
    }
}
