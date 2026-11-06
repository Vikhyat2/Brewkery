package com.bookverse.brewkery


import com.bookverse.brewkery.data.remote.Customizations
import com.bookverse.brewkery.data.remote.MenuItem
import com.bookverse.brewkery.data.remote.MilkOption
import com.bookverse.brewkery.data.remote.SizeOption
import com.bookverse.brewkery.domain.CartLine
import com.bookverse.brewkery.domain.PriceCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class PriceCalculatorTest {

    private val grande = SizeOption("sz_medium", "Grande (12 oz)", 0.65)
    private val almond = MilkOption("milk_almond", "Roasted Almond Milk", 0.50)
    private val macchiato = MenuItem(
        id = 1,
        name = "Toasted Caramel Macchiato",
        basePrice = 4.85,
        customizations = Customizations(sizes = listOf(grande), milkOptions = listOf(almond)),
    )

    private fun line(key: String, cents: Long, qty: Int = 1) =
        CartLine(key, 1, "x", "", null, null, null, cents, qty)

    @Test
    fun `toCents does not lose a cent to floating point`() {
        assertEquals(65L, PriceCalculator.toCents(0.65))
        assertEquals(485L, PriceCalculator.toCents(4.85))
        assertEquals(1265L, PriceCalculator.toCents(12.65))
    }

    @Test
    fun `unit price adds size and milk surcharges to base price`() {
        assertEquals(485L, PriceCalculator.unitPriceCents(macchiato, null, null))
        assertEquals(550L, PriceCalculator.unitPriceCents(macchiato, grande, null))
        assertEquals(600L, PriceCalculator.unitPriceCents(macchiato, grande, almond))
    }

    @Test
    fun `cart summary matches the checkout mock`() {
        // Grande oat macchiato $5.50 + croissant $3.90 -> $9.40 + $2.50 + 8% tax ($0.75) = $12.65
        val summary = PriceCalculator.summarize(
            lines = listOf(line("a", 550), line("b", 390)),
            deliveryFee = 2.50,
            taxRatePercent = 8.0,
        )
        assertEquals(940L, summary.subtotalCents)
        assertEquals(250L, summary.deliveryFeeCents)
        assertEquals(75L, summary.taxCents)
        assertEquals(1265L, summary.totalCents)
    }

    @Test
    fun `quantities multiply into the subtotal`() {
        val summary = PriceCalculator.summarize(listOf(line("a", 485, qty = 3)), 2.50, 8.0)
        assertEquals(1455L, summary.subtotalCents)
        assertEquals(116L, summary.taxCents) // 116.4 -> 116
        assertEquals(1821L, summary.totalCents)
    }

    @Test
    fun `empty cart costs nothing`() {
        val summary = PriceCalculator.summarize(emptyList(), 2.50, 8.0)
        assertEquals(0L, summary.totalCents)
    }

    @Test
    fun `format renders dollars and cents`() {
        assertEquals("$12.65", PriceCalculator.format(1265))
        assertEquals("$0.05", PriceCalculator.format(5))
        assertEquals("+$0.65", PriceCalculator.formatExtra(0.65))
    }
}
