package com.bookverse.brewkery


import com.bookverse.brewkery.data.CartRepository
import com.bookverse.brewkery.data.MenuRepository
import com.bookverse.brewkery.data.OrderRepository
import com.bookverse.brewkery.data.remote.Category
import com.bookverse.brewkery.data.remote.MenuItem
import com.bookverse.brewkery.data.remote.MenuResponse
import com.bookverse.brewkery.ui.menu.MenuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    private val menu = MenuResponse(
        categories = listOf(Category("cat_hot_coffee", "Hot Coffee"), Category("cat_bakery", "Artisan Bakery")),
        items = listOf(
            MenuItem(id = 1, categoryId = "cat_hot_coffee", name = "Toasted Caramel Macchiato", basePrice = 4.85),
            MenuItem(id = 3, categoryId = "cat_bakery", name = "Golden Normandy Croissant", basePrice = 3.90),
        ),
    )

    /** Fails the first [failures] calls, then succeeds. */
    private class FakeMenuRepository(private val menu: MenuResponse, var failures: Int = 0) : MenuRepository {
        private val cache = MutableStateFlow<MenuResponse?>(null)
        override val cachedMenu: StateFlow<MenuResponse?> = cache
        override suspend fun getMenu(forceRefresh: Boolean): MenuResponse {
            if (failures > 0) {
                failures--
                throw IOException("offline")
            }
            return menu.also { cache.value = it }
        }
        override suspend fun getItem(id: Int) = menu.items.first { it.id == id }
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: MenuRepository) = MenuViewModel(repo, CartRepository(), OrderRepository())

    @Test
    fun `loads menu on start`() {
        val state = viewModel(FakeMenuRepository(menu)).uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.visibleItems.size)
    }

    @Test
    fun `category chip and search filter the list`() {
        val vm = viewModel(FakeMenuRepository(menu))

        vm.selectCategory("cat_bakery")
        assertEquals(listOf(3), vm.uiState.value.visibleItems.map { it.id })

        vm.selectCategory(null)
        vm.onQueryChange("caramel")
        assertEquals(listOf(1), vm.uiState.value.visibleItems.map { it.id })
    }

    @Test
    fun `network failure shows an error and retry recovers`() {
        val repo = FakeMenuRepository(menu, failures = 1)
        val vm = viewModel(repo)
        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.hasData)

        vm.load(forceRefresh = true)
        assertNull(vm.uiState.value.error)
        assertEquals(2, vm.uiState.value.visibleItems.size)
    }
}
