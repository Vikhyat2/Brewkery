package com.bookverse.brewkery.ui.menu


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookverse.brewkery.BrewkeryApp
import com.bookverse.brewkery.data.CartRepository
import com.bookverse.brewkery.data.MenuRepository
import com.bookverse.brewkery.data.OrderRepository
import com.bookverse.brewkery.data.remote.Category
import com.bookverse.brewkery.data.remote.MenuItem
import com.bookverse.brewkery.data.remote.StoreMeta
import com.bookverse.brewkery.data.toUserMessage
import com.bookverse.brewkery.domain.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val meta: StoreMeta = StoreMeta(),
    val categories: List<Category> = emptyList(),
    val allItems: List<MenuItem> = emptyList(),
    val selectedCategoryId: String? = null, // null = "All Items"
    val query: String = "",
) {
    val hasData: Boolean get() = allItems.isNotEmpty()

    /** Items after the category chip + search box are applied. */
    val visibleItems: List<MenuItem>
        get() {
            val q = query.trim()
            return allItems.filter { item ->
                (selectedCategoryId == null || item.categoryId == selectedCategoryId) &&
                        (q.isEmpty() ||
                                item.name.contains(q, ignoreCase = true) ||
                                item.tagline.contains(q, ignoreCase = true) ||
                                item.ingredients.any { it.contains(q, ignoreCase = true) } ||
                                categoryName(item.categoryId).contains(q, ignoreCase = true))
            }
        }

    private fun categoryName(id: String) = categories.firstOrNull { it.id == id }?.name.orEmpty()
}

class MenuViewModel(
    private val menuRepository: MenuRepository,
    cartRepository: CartRepository,
    orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    val cartCount: StateFlow<Int> =
        cartRepository.itemCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val activeOrder: StateFlow<Order?> = orderRepository.activeOrder

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val menu = menuRepository.getMenu(forceRefresh)
                _uiState.update {
                    it.copy(isLoading = false, meta = menu.meta, categories = menu.categories, allItems = menu.items)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }

    fun selectCategory(categoryId: String?) = _uiState.update { it.copy(selectedCategoryId = categoryId) }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as BrewkeryApp).container
                MenuViewModel(c.menuRepository, c.cartRepository, c.orderRepository)
            }
        }
    }
}
