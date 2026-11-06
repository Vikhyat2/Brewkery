package com.bookverse.brewkery.data

import com.bookverse.brewkery.data.remote.BrewkeryApi
import com.bookverse.brewkery.data.remote.MenuItem
import com.bookverse.brewkery.data.remote.MenuResponse
import com.bookverse.brewkery.data.remote.StoreMeta
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

interface MenuRepository {
    /** Last successfully loaded menu, or null if it hasn't loaded yet. */
    val cachedMenu: StateFlow<MenuResponse?>

    /** Store meta (fees, tax, ETA) – falls back to sane defaults before the menu loads. */
    val meta: StoreMeta get() = cachedMenu.value?.meta ?: StoreMeta()

    suspend fun getMenu(forceRefresh: Boolean = false): MenuResponse
    suspend fun getItem(id: Int): MenuItem
}

class NetworkMenuRepository(
    private val api: BrewkeryApi,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : MenuRepository {

    private val cache = MutableStateFlow<MenuResponse?>(null)
    override val cachedMenu: StateFlow<MenuResponse?> = cache.asStateFlow()

    override suspend fun getMenu(forceRefresh: Boolean): MenuResponse = withContext(io) {
        val cached = cache.value
        if (cached != null && !forceRefresh) cached
        else api.getMenu().also { cache.value = it }
    }

    /**
     * Uses the dedicated item endpoint so the detail screen always has fresh data,
     * but falls back to the copy from the menu we already have if that call fails.
     */
    override suspend fun getItem(id: Int): MenuItem = withContext(io) {
        try {
            api.getItem(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            cache.value?.items?.firstOrNull { it.id == id } ?: throw e
        }
    }
}
