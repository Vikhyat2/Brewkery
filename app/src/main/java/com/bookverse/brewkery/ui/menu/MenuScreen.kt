package com.bookverse.brewkery.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bookverse.brewkery.data.remote.Category
import com.bookverse.brewkery.data.remote.MenuItem
import com.bookverse.brewkery.data.remote.StoreMeta
import com.bookverse.brewkery.domain.Order
import com.bookverse.brewkery.domain.PriceCalculator
import com.bookverse.brewkery.ui.components.BadgeTag
import com.bookverse.brewkery.ui.components.BrewCard
import com.bookverse.brewkery.ui.components.CircleIconButton
import com.bookverse.brewkery.ui.components.ErrorView
import com.bookverse.brewkery.ui.components.LoadingView
import com.bookverse.brewkery.ui.components.PriceText
import com.bookverse.brewkery.ui.theme.Brew
import java.math.BigDecimal

@Composable
fun MenuScreen(
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onActiveOrderClick: () -> Unit,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
    val activeOrder by viewModel.activeOrder.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(Brew.Cream)
            .systemBarsPadding(),
    ) {
        MenuHeader(cartCount = cartCount, onCartClick = onCartClick)

        when {
            state.isLoading && !state.hasData -> LoadingView(Modifier.weight(1f))
            state.error != null && !state.hasData ->
                ErrorView(message = state.error!!, onRetry = { viewModel.load(forceRefresh = true) }, modifier = Modifier.weight(1f))
            else -> MenuContent(
                state = state,
                activeOrder = activeOrder,
                onQueryChange = viewModel::onQueryChange,
                onCategorySelected = viewModel::selectCategory,
                onItemClick = onItemClick,
                onActiveOrderClick = onActiveOrderClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MenuContent(
    state: MenuUiState,
    activeOrder: Order?,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onItemClick: (Int) -> Unit,
    onActiveOrderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = state.visibleItems
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (activeOrder != null) {
            item(key = "active_order") { ActiveOrderCard(activeOrder, onActiveOrderClick) }
        }
        item(key = "store") { StoreInfoCard(state.meta) }
        item(key = "search") { SearchField(state.query, onQueryChange) }
        item(key = "categories") {
            CategoryRow(state.categories, state.selectedCategoryId, onCategorySelected)
        }
        if (visible.isEmpty()) {
            item(key = "empty") {
                Text(
                    "Nothing matches “${state.query.trim()}”. Try another search.",
                    color = Brew.Muted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                )
            }
        }
        items(visible, key = { it.id }) { item ->
            MenuItemCard(item = item, currencySymbol = state.meta.currencySymbol, onClick = { onItemClick(item.id) })
        }
    }
}

@Composable
private fun MenuHeader(cartCount: Int, onCartClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(Brew.Espresso),
            contentAlignment = Alignment.Center,
        ) {
            Text("BK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Fresh Roast & Bakes", fontSize = 11.sp, color = Brew.Muted)
            Text("Brewkery Artisans", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Box {
            CircleIconButton(onClick = onCartClick) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart", modifier = Modifier.size(18.dp))
            }
            if (cartCount > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Brew.Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (cartCount > 9) "9+" else cartCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreInfoCard(meta: StoreMeta) {
    BrewCard(modifier = Modifier.fillMaxWidth(), background = Brew.Peach) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White),
                contentAlignment = Alignment.Center,
            ) { Text("🛵", fontSize = 16.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("STORE INFO", style = MaterialTheme.typography.labelSmall, color = Brew.Accent)
                    Spacer(Modifier.width(4.dp))
                    Box(Modifier.size(5.dp).clip(CircleShape).background(Brew.Accent))
                }
                Text("Delivery in ${meta.estimatedDeliveryTime}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    PriceCalculator.format(PriceCalculator.toCents(meta.deliveryFee), meta.currencySymbol) + " flat fee",
                    fontSize = 11.sp,
                    color = Brew.Muted,
                )
            }
            Text(
                "Open",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Brew.Border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

/** Shown on the menu once an order has been placed – tapping opens the status screen. */
@Composable
private fun ActiveOrderCard(order: Order, onClick: () -> Unit) {
    BrewCard(modifier = Modifier.fillMaxWidth(), background = Brew.Espresso, onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("☕", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("ACTIVE ORDER", style = MaterialTheme.typography.labelSmall, color = Brew.Gold)
                Text(
                    "#${order.ticketId} · ${order.status.label}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
                Text(
                    "Barista accepted your order · ETA ${order.estimatedWait}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Track order", tint = Color.White)
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Brew.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = Brew.Muted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Search roast, cold brew, pastry...", color = Brew.Muted, fontSize = 13.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = Brew.Espresso),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Clear search",
                tint = Brew.Muted,
                modifier = Modifier.size(18.dp).clickable { onQueryChange("") },
            )
        }
    }
}

@Composable
private fun CategoryRow(categories: List<Category>, selectedId: String?, onSelect: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { CategoryChip(label = "All Items", selected = selectedId == null, onClick = { onSelect(null) }) }
        items(categories, key = { it.id }) { category ->
            CategoryChip(
                label = listOf(category.icon, category.name).filter { it.isNotBlank() }.joinToString(" "),
                selected = selectedId == category.id,
                onClick = { onSelect(category.id) },
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Text(
        label,
        color = if (selected) Color.White else Brew.Espresso,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Brew.Espresso else Color.White)
            .border(1.dp, if (selected) Brew.Espresso else Brew.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun MenuItemCard(item: MenuItem, currencySymbol: String, onClick: () -> Unit) {
    BrewCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Brew.PeachDeep),
                error = ColorPainter(Brew.PeachDeep),
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                BadgeTag(item.badge)
                Spacer(Modifier.height(4.dp))
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Brew.Gold, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(formatRating(item.rating), fontSize = 11.sp, color = Brew.Muted)
                    Text(" (${item.reviewCount})", fontSize = 11.sp, color = Brew.Muted)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriceText(PriceCalculator.format(PriceCalculator.toCents(item.basePrice), currencySymbol))
                    Spacer(Modifier.weight(1f))
                    Text(
                        "+ Customize",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brew.Accent)
                            .clickable(onClick = onClick)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

/** 4.9 -> "4.9", 4.95 -> "4.95", 5.0 -> "5.0" */
private fun formatRating(rating: Double): String {
    val bd = BigDecimal.valueOf(rating).stripTrailingZeros()
    return (if (bd.scale() < 1) bd.setScale(1) else bd).toPlainString()
}
