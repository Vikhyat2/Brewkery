package com.bookverse.brewkery

import com.bookverse.brewkery.data.CartRepository
import com.bookverse.brewkery.data.OrderRepository
import com.bookverse.brewkery.domain.CartLine
import com.bookverse.brewkery.domain.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CartRepositoryTest {

    private val cart = CartRepository()

    private fun macchiato(size: String, qty: Int = 1) = CartLine(
        key = CartLine.keyFor(1, size, "milk_oat", "50% Sweet"),
        itemId = 1,
        name = "Toasted Caramel Macchiato",
        imageUrl = "",
        sizeLabel = size,
        milkName = "Oat Milk",
        sugarLevel = "50% Sweet",
        unitPriceCents = 485,
        quantity = qty,
    )

    @Test
    fun `same item with same options merges into one line`() {
        cart.add(macchiato("sz_small"))
        cart.add(macchiato("sz_small", qty = 2))
        assertEquals(1, cart.lines.value.size)
        assertEquals(3, cart.lines.value.single().quantity)
    }

    @Test
    fun `different options create separate lines`() {
        cart.add(macchiato("sz_small"))
        cart.add(macchiato("sz_large"))
        assertEquals(2, cart.lines.value.size)
    }

    @Test
    fun `decrementing the last unit removes the line`() {
        val line = macchiato("sz_small")
        cart.add(line)
        cart.increment(line.key)
        assertEquals(2, cart.lines.value.single().quantity)
        cart.decrement(line.key)
        cart.decrement(line.key)
        assertTrue(cart.lines.value.isEmpty())
    }

    @Test
    fun `clear empties the cart`() {
        cart.add(macchiato("sz_small"))
        cart.clear()
        assertTrue(cart.lines.value.isEmpty())
    }

    @Test
    fun `placing an order creates a PREPARING ticket`() {
        val orders = OrderRepository(Random(seed = 42))
        val order = orders.placeOrder(itemCount = 2, totalCents = 1265, estimatedWait = "20-30 mins")
        assertTrue(order.ticketId.matches(Regex("BK-\\d{5}")))
        assertEquals(OrderStatus.PREPARING, order.status)
        assertEquals(order, orders.activeOrder.value)
    }
}
