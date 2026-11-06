package com.bookverse.brewkery.data


import com.bookverse.brewkery.domain.CartLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory cart shared by every screen. Per the brief, it resets when the process dies.
 */
class CartRepository {

    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    val itemCount: Flow<Int> = lines.map { list -> list.sumOf { it.quantity } }

    fun add(line: CartLine) = _lines.update { current ->
        val index = current.indexOfFirst { it.key == line.key }
        if (index == -1) {
            current + line
        } else {
            current.toMutableList().also {
                it[index] = it[index].copy(quantity = it[index].quantity + line.quantity)
            }
        }
    }

    fun increment(key: String) = changeQuantity(key, +1)

    /** Removes the line when its quantity would drop to zero. */
    fun decrement(key: String) = changeQuantity(key, -1)

    fun remove(key: String) = _lines.update { current -> current.filterNot { it.key == key } }

    fun clear() {
        _lines.value = emptyList()
    }

    private fun changeQuantity(key: String, delta: Int) = _lines.update { current ->
        current.mapNotNull { line ->
            if (line.key != key) line
            else (line.quantity + delta).takeIf { it > 0 }?.let { line.copy(quantity = it) }
        }
    }
}
