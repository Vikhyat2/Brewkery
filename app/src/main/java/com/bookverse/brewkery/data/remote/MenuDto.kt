package com.bookverse.brewkery.data.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors https://raw.githubusercontent.com/VivekShah138/Brewkery/main/data.json
 *
 * Every field has a default so a missing / null key (e.g. an item with no milk options)
 * never crashes parsing. Combined with `coerceInputValues = true` in the Json config.
 */
@Serializable
data class MenuResponse(
    val meta: StoreMeta = StoreMeta(),
    val categories: List<Category> = emptyList(),
    val items: List<MenuItem> = emptyList(),
)

@Serializable
data class StoreMeta(
    val app: String = "Brewkery",
    val version: String = "",
    val tagline: String = "",
    val currency: String = "USD",
    @SerialName("currency_symbol") val currencySymbol: String = "$",
    @SerialName("delivery_fee") val deliveryFee: Double = 2.50,
    @SerialName("tax_rate_percent") val taxRatePercent: Double = 8.0,
    @SerialName("estimated_delivery_time") val estimatedDeliveryTime: String = "20-30 mins",
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val icon: String = "",
    @SerialName("item_count") val itemCount: Int = 0,
)

@Serializable
data class MenuItem(
    val id: Int,
    @SerialName("category_id") val categoryId: String = "",
    val name: String,
    val tagline: String = "",
    val description: String = "",
    @SerialName("base_price") val basePrice: Double = 0.0,
    val rating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0,
    @SerialName("prep_time") val prepTime: String = "",
    val calories: Int = 0,
    @SerialName("image_url") val imageUrl: String = "",
    val badge: String = "",
    val ingredients: List<String> = emptyList(),
    val customizations: Customizations = Customizations(),
)

@Serializable
data class Customizations(
    val sizes: List<SizeOption> = emptyList(),
    @SerialName("milk_options") val milkOptions: List<MilkOption> = emptyList(),
    @SerialName("sugar_levels") val sugarLevels: List<String> = emptyList(),
)

@Serializable
data class SizeOption(
    val id: String,
    val label: String,
    @SerialName("extra_price") val extraPrice: Double = 0.0,
)

@Serializable
data class MilkOption(
    val id: String,
    val name: String,
    @SerialName("extra_price") val extraPrice: Double = 0.0,
)
