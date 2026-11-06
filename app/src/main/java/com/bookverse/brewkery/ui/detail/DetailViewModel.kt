package com.bookverse.brewkery.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookverse.brewkery.BrewkeryApp
import com.bookverse.brewkery.data.CartRepository
import com.bookverse.brewkery.data.MenuRepository
import com.bookverse.brewkery.data.remote.MenuItem
import com.bookverse.brewkery.data.remote.MilkOption
import com.bookverse.brewkery.data.remote.SizeOption
import com.bookverse.brewkery.data.toUserMessage
import com.bookverse.brewkery.domain.CartLine
import com.bookverse.brewkery.domain.PriceCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val item: MenuItem? = null,
    val currencySymbol: String = "$",
    val selectedSizeId: String? = null,
    val selectedMilkId: String? = null,
    val selectedSugar: String? = null,
    val quantity: Int = 1,
    val isFavorite: Boolean = false,
) {
    val selectedSize: SizeOption? get() = item?.customizations?.sizes?.firstOrNull { it.id == selectedSizeId }
    val selectedMilk: MilkOption? get() = item?.customizations?.milkOptions?.firstOrNull { it.id == selectedMilkId }

    /** Price of one unit with the current selections – updates live as options change. */
    val unitPriceCents: Long get() = item?.let { PriceCalculator.unitPriceCents(it, selectedSize, selectedMilk) } ?: 0L
    val totalPriceCents: Long get() = unitPriceCents * quantity
}

class DetailViewModel(
    private val itemId: Int,
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val item = menuRepository.getItem(itemId)
                val c = item.customizations
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        item = item,
                        currencySymbol = menuRepository.meta.currencySymbol,
                        // Default to the first option of each group, like the mock.
                        selectedSizeId = it.selectedSizeId ?: c.sizes.firstOrNull()?.id,
                        selectedMilkId = it.selectedMilkId ?: c.milkOptions.firstOrNull()?.id,
                        selectedSugar = it.selectedSugar ?: c.sugarLevels.firstOrNull(),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }

    fun selectSize(id: String) = _uiState.update { it.copy(selectedSizeId = id) }
    fun selectMilk(id: String) = _uiState.update { it.copy(selectedMilkId = id) }
    fun selectSugar(level: String) = _uiState.update { it.copy(selectedSugar = level) }
    fun incrementQuantity() = _uiState.update { it.copy(quantity = (it.quantity + 1).coerceAtMost(20)) }
    fun decrementQuantity() = _uiState.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }
    fun toggleFavorite() = _uiState.update { it.copy(isFavorite = !it.isFavorite) }

    /** Returns false if the item hasn't loaded yet. */
    fun addToCart(): Boolean {
        val state = _uiState.value
        val item = state.item ?: return false
        cartRepository.add(
            CartLine(
                key = CartLine.keyFor(item.id, state.selectedSizeId, state.selectedMilkId, state.selectedSugar),
                itemId = item.id,
                name = item.name,
                imageUrl = item.imageUrl,
                sizeLabel = state.selectedSize?.label,
                milkName = state.selectedMilk?.name,
                sugarLevel = state.selectedSugar,
                unitPriceCents = state.unitPriceCents,
                quantity = state.quantity,
            ),
        )
        return true
    }

    companion object {
        const val ARG_ITEM_ID = "itemId"

        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as BrewkeryApp).container
                val handle: SavedStateHandle = createSavedStateHandle()
                DetailViewModel(
                    itemId = checkNotNull(handle.get<Int>(ARG_ITEM_ID)) { "itemId nav argument missing" },
                    menuRepository = c.menuRepository,
                    cartRepository = c.cartRepository,
                )
            }
        }
    }
}
