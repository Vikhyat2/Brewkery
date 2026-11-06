package com.bookverse.brewkery.domain


/** One line in the cart. Prices are kept in cents to avoid floating-point drift. */
data class CartLine(
    val key: String,
    val itemId: Int,
    val name: String,
    val imageUrl: String,
    val sizeLabel: String?,
    val milkName: String?,
    val sugarLevel: String?,
    val unitPriceCents: Long,
    val quantity: Int,
) {
    val lineTotalCents: Long get() = unitPriceCents * quantity

    /** e.g. "Grande (12 oz) • Oat Milk (Barista Blend) • 50% Sweet" */
    val optionsSummary: String
        get() = listOfNotNull(sizeLabel, milkName, sugarLevel).joinToString(" • ")

    companion object {
        /** Same item with the same choices merges into a single line. */
        fun keyFor(itemId: Int, sizeId: String?, milkId: String?, sugar: String?) =
            "$itemId|${sizeId.orEmpty()}|${milkId.orEmpty()}|${sugar.orEmpty()}"
    }
}

data class OrderSummary(
    val subtotalCents: Long,
    val deliveryFeeCents: Long,
    val taxCents: Long,
    val totalCents: Long,
)

enum class OrderStatus(val label: String) { PREPARING("PREPARING") }

data class Order(
    val ticketId: String,
    val itemCount: Int,
    val totalCents: Long,
    val estimatedWait: String,
    val status: OrderStatus = OrderStatus.PREPARING,
)
