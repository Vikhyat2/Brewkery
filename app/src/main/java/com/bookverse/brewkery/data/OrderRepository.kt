package com.bookverse.brewkery.data


import com.bookverse.brewkery.domain.Order
import com.bookverse.brewkery.domain.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/** Holds the most recently placed order so the menu can show live tracking. */
class OrderRepository(private val random: Random = Random.Default) {

    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    fun placeOrder(itemCount: Int, totalCents: Long, estimatedWait: String): Order {
        val order = Order(
            ticketId = "BK-${random.nextInt(10_000, 100_000)}",
            itemCount = itemCount,
            totalCents = totalCents,
            estimatedWait = estimatedWait,
            status = OrderStatus.PREPARING,
        )
        _activeOrder.value = order
        return order
    }
}
