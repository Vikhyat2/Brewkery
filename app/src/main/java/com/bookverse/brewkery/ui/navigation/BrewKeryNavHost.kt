package com.bookverse.brewkery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookverse.brewkery.ui.cart.CartScreen
import com.bookverse.brewkery.ui.detail.DetailScreen
import com.bookverse.brewkery.ui.detail.DetailViewModel
import com.bookverse.brewkery.ui.menu.MenuScreen
import com.bookverse.brewkery.ui.order.OrderScreen

private object Routes {
    const val MENU = "menu"
    const val DETAIL = "detail/{${DetailViewModel.ARG_ITEM_ID}}"
    const val CART = "cart"
    const val ORDER = "order"
    fun detail(id: Int) = "detail/$id"
}

/** Menu → (tap item) → Detail → (add to cart) → Cart → (place order) → Order status. */
@Composable
fun BrewkeryNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            MenuScreen(
                onItemClick = { id -> nav.navigate(Routes.detail(id)) },
                onCartClick = { nav.navigate(Routes.CART) },
                onActiveOrderClick = { nav.navigate(Routes.ORDER) },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(DetailViewModel.ARG_ITEM_ID) { type = NavType.IntType }),
        ) {
            DetailScreen(
                onBack = { nav.popBackStack() },
                // Back from the cart should land on the menu, not the customizer again.
                onAddedToCart = { nav.navigate(Routes.CART) { popUpTo(Routes.MENU) } },
            )
        }
        composable(Routes.CART) {
            CartScreen(
                onBack = { nav.popBackStack() },
                onBrowseMenu = { nav.popBackStack(Routes.MENU, inclusive = false) },
                onOrderPlaced = { nav.navigate(Routes.ORDER) { popUpTo(Routes.MENU) } },
            )
        }
        composable(Routes.ORDER) {
            OrderScreen(onBackToMenu = { nav.popBackStack(Routes.MENU, inclusive = false) })
        }
    }
}
