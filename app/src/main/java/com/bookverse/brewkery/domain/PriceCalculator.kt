package com.bookverse.brewkery.domain

import kotlin.text.toLong

import com.bookverse.brewkery.data.remote.MenuItem
import com.bookverse.brewkery.data.remote.MilkOption
import com.bookverse.brewkery.data.remote.SizeOption
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object PriceCalculator {

    /** 4.85 -> 485. Goes through BigDecimal's string form so 0.65 doesn't become 64. */
    fun toCents(amount: Double): Long =
        BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).movePointRight(2).toLong()

    /** Base price + selected size and milk surcharges. */
    fun unitPriceCents(item: MenuItem, size: SizeOption?, milk: MilkOption?): Long =
        toCents(item.basePrice) +
                (size?.let { toCents(it.extraPrice) } ?: 0L) +
                (milk?.let { toCents(it.extraPrice) } ?: 0L)

    /**
     * Subtotal + flat delivery fee + tax on the subtotal (rounded half-up to the cent).
     * An empty cart costs nothing – no delivery fee.
     */
    fun summarize(lines: List<CartLine>, deliveryFee: Double, taxRatePercent: Double): OrderSummary {
        val subtotal = lines.sumOf { it.lineTotalCents }
        val delivery = if (lines.isEmpty()) 0L else toCents(deliveryFee)
        val tax = BigDecimal.valueOf(subtotal)
            .multiply(BigDecimal.valueOf(taxRatePercent))
            .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
            .toLong()
        return OrderSummary(
            subtotalCents = subtotal,
            deliveryFeeCents = delivery,
            taxCents = tax,
            totalCents = subtotal + delivery + tax,
        )
    }

    fun format(cents: Long, symbol: String = "$"): String =
        symbol + String.format(Locale.US, "%d.%02d", cents / 100, cents % 100)

    /** "+$0.65" for option surcharges. */
    fun formatExtra(amount: Double, symbol: String = "$"): String = "+" + format(toCents(amount), symbol)
}
