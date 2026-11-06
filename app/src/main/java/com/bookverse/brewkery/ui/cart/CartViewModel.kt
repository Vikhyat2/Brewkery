package com.bookverse.brewkery.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookverse.brewkery.BrewkeryApp
import com.bookverse.brewkery.data.CartRepository
import com.bookverse.brewkery.data.MenuRepository
import com.bookverse.brewkery.data.OrderRepository
import com.bookverse.brewkery.domain.CartLine
import com.bookverse.brewkery.domain.Order
import com.bookverse.brewkery.domain.OrderSummary
import com.bookverse.brewkery.domain.PriceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CartUiState(
    val lines: List<CartLine> = emptyList(),
    val summary: OrderSummary = OrderSummary(0, 0, 0, 0),
    val taxRatePercent: Double = 8.0,
    val currencySymbol: String = "$",
) {
    val isEmpty: Boolean get() = lines.isEmpty()
}

class CartViewModel(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    val uiState: StateFlow<CartUiState> =
        combine(cartRepository.lines, menuRepository.cachedMenu) { lines, menu ->
            val meta = menu?.meta ?: menuRepository.meta
            CartUiState(
                lines = lines,
                summary = PriceCalculator.summarize(lines, meta.deliveryFee, meta.taxRatePercent),
                taxRatePercent = meta.taxRatePercent,
                currencySymbol = meta.currencySymbol,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState())

    private fun initialState(): CartUiState {
        val meta = menuRepository.meta
        val lines = cartRepository.lines.value
        return CartUiState(
            lines = lines,
            summary = PriceCalculator.summarize(lines, meta.deliveryFee, meta.taxRatePercent),
            taxRatePercent = meta.taxRatePercent,
            currencySymbol = meta.currencySymbol,
        )
    }

    fun increment(key: String) = cartRepository.increment(key)
    fun decrement(key: String) = cartRepository.decrement(key)
    fun clearCart() = cartRepository.clear()

    /** Creates the order ticket, clears the cart, and returns the order (null if the cart was empty). */
    fun placeOrder(): Order? {
        val lines = cartRepository.lines.value
        if (lines.isEmpty()) return null
        val meta = menuRepository.meta
        val summary = PriceCalculator.summarize(lines, meta.deliveryFee, meta.taxRatePercent)
        val order = orderRepository.placeOrder(
            itemCount = lines.sumOf { it.quantity },
            totalCents = summary.totalCents,
            estimatedWait = meta.estimatedDeliveryTime,
        )
        cartRepository.clear()
        return order
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as BrewkeryApp).container
                CartViewModel(c.menuRepository, c.cartRepository, c.orderRepository)
            }
        }
    }
}
