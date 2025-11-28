package com.levelupgamer.levelup.ui.cart

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.levelupgamer.levelup.data.repository.CartRepository
import com.levelupgamer.levelup.data.repository.ProductRepository
import com.levelupgamer.levelup.model.CartItem
import com.levelupgamer.levelup.model.Product
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
class CartViewModelTest {

    @Mock
    private lateinit var cartRepository: CartRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    private lateinit var viewModel: CartViewModel
    private lateinit var context: Context
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        
        // Simulate logged in user
        UserManager.saveUser(context, 1, "Test User", "test@example.com")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        UserManager.logout(context)
    }

    @Test
    fun `cargar items del carrito carga items correctamente`() = runTest {
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
        val cartItem = CartItem(userId = 1, productCode = "P001", quantity = 2)

        `when`(cartRepository.getCartItems(1)).thenReturn(flowOf(listOf(cartItem)))
        `when`(productRepository.getAllProducts()).thenReturn(flowOf(listOf(product)))

        viewModel = CartViewModel(cartRepository, productRepository, context)

        viewModel.uiState.test {
            val state = awaitItem()
            if (state.cartItems.isEmpty()) {
                 val loadedState = awaitItem()
                 assertEquals(1, loadedState.cartItems.size)
                 assertEquals("P001", loadedState.cartItems[0].first.code)
                 assertEquals(2, loadedState.cartItems[0].second)
            } else {
                 assertEquals(1, state.cartItems.size)
                 assertEquals("P001", state.cartItems[0].first.code)
                 assertEquals(2, state.cartItems[0].second)
            }
        }
    }

    @Test
    fun `agregar producto agrega item cuando hay stock disponible`() = runTest {
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
        
        `when`(cartRepository.getCartItems(1)).thenReturn(flowOf(emptyList()))
        `when`(productRepository.getAllProducts()).thenReturn(flowOf(listOf(product)))
        `when`(productRepository.getProductByCode("P001")).thenReturn(product)

        viewModel = CartViewModel(cartRepository, productRepository, context)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onProductAdded("P001")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(cartRepository).addToCart(1, "P001", 1)
    }

    @Test
    fun `agregar producto emite toast cuando stock es insuficiente`() = runTest {
        val product = Product(
            code = "P001",
            name = "Test Product",
            category = "Category",
            price = 1000,
            description = "Description",
            quantity = 0, // No stock
            imageUrl = "",
            imageResId = null,
            averageRating = 0f
        )
        
        `when`(cartRepository.getCartItems(1)).thenReturn(flowOf(emptyList()))
        `when`(productRepository.getAllProducts()).thenReturn(flowOf(listOf(product)))
        `when`(productRepository.getProductByCode("P001")).thenReturn(product)

        viewModel = CartViewModel(cartRepository, productRepository, context)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toastMessage.test {
            viewModel.onProductAdded("P001")
            assertEquals("Este producto no tiene stock", awaitItem())
        }
    }
    
    @Test
    fun `eliminar producto elimina item del carrito`() = runTest {
        `when`(cartRepository.getCartItems(1)).thenReturn(flowOf(emptyList()))
        `when`(productRepository.getAllProducts()).thenReturn(flowOf(emptyList()))
        
        viewModel = CartViewModel(cartRepository, productRepository, context)
        
        viewModel.onProductRemoved("P001")
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(cartRepository).removeFromCart(1, "P001")
    }
}
