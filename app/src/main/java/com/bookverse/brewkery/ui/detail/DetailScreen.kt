package com.bookverse.brewkery.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bookverse.brewkery.data.remote.MenuItem
import com.bookverse.brewkery.domain.PriceCalculator
import com.bookverse.brewkery.ui.components.BadgeTag
import com.bookverse.brewkery.ui.components.BrewCard
import com.bookverse.brewkery.ui.components.CircleIconButton
import com.bookverse.brewkery.ui.components.ErrorView
import com.bookverse.brewkery.ui.components.LoadingView
import com.bookverse.brewkery.ui.components.PrimaryButton
import com.bookverse.brewkery.ui.components.PriceText
import com.bookverse.brewkery.ui.components.QuantityStepper
import com.bookverse.brewkery.ui.components.ScreenTopBar
import com.bookverse.brewkery.ui.theme.Brew

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onAddedToCart: () -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(Brew.Cream)
            .systemBarsPadding(),
    ) {
        ScreenTopBar(title = "ITEM CUSTOMIZER", onBack = onBack) {
            CircleIconButton(onClick = viewModel::toggleFavorite) {
                Icon(
                    if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = Brew.Danger,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        val item = state.item
        when {
            state.isLoading && item == null -> LoadingView(Modifier.weight(1f), message = "Warming up the details…")
            item == null -> ErrorView(state.error ?: "Item not found.", onRetry = viewModel::load, modifier = Modifier.weight(1f))
            else -> {
                DetailContent(state = state, item = item, viewModel = viewModel, modifier = Modifier.weight(1f))
                Box(Modifier.padding(16.dp)) {
                    PrimaryButton(
                        text = "Add to Cart  •  " + PriceCalculator.format(state.totalPriceCents, state.currencySymbol),
                        onClick = { if (viewModel.addToCart()) onAddedToCart() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(state: DetailUiState, item: MenuItem, viewModel: DetailViewModel, modifier: Modifier) {
    val symbol = state.currencySymbol
    val c = item.customizations

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        // Hero image with badge overlay
        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Brew.PeachDeep),
                error = ColorPainter(Brew.PeachDeep),
                modifier = Modifier.fillMaxSize(),
            )
            BadgeTag(item.badge, dark = true, modifier = Modifier.padding(10.dp))
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, fontSize = 18.sp, modifier = Modifier.weight(1f))
            PriceText(PriceCalculator.format(state.unitPriceCents, symbol), fontSizeSp = 16)
        }
        if (item.description.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(item.description, style = MaterialTheme.typography.bodyMedium, color = Brew.Muted)
        }
        val facts = listOfNotNull(
            item.prepTime.takeIf { it.isNotBlank() }?.let { "⏱ $it" },
            item.calories.takeIf { it > 0 }?.let { "🔥 $it kcal" },
        )
        if (facts.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(facts.joinToString("   "), fontSize = 11.sp, color = Brew.Muted)
        }

        if (item.ingredients.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text("KEY INGREDIENTS", style = MaterialTheme.typography.labelSmall, color = Brew.Muted)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item.ingredients.forEach { IngredientChip(it) }
            }
        }

        if (c.sizes.isNotEmpty()) {
            OptionSection("Size Selection") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    c.sizes.forEach { size ->
                        SizeOptionBox(
                            label = size.label,
                            extra = PriceCalculator.formatExtra(size.extraPrice, symbol),
                            selected = size.id == state.selectedSizeId,
                            onClick = { viewModel.selectSize(size.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        if (c.milkOptions.isNotEmpty()) {
            OptionSection("Milk Options / Spreads") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    c.milkOptions.forEach { milk ->
                        MilkOptionRow(
                            name = milk.name,
                            extra = PriceCalculator.formatExtra(milk.extraPrice, symbol),
                            selected = milk.id == state.selectedMilkId,
                            onClick = { viewModel.selectMilk(milk.id) },
                        )
                    }
                }
            }
        }

        if (c.sugarLevels.isNotEmpty()) {
            OptionSection("Sugar Levels / Serving") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    c.sugarLevels.forEach { level ->
                        SugarChip(level, selected = level == state.selectedSugar, onClick = { viewModel.selectSugar(level) })
                    }
                }
            }
        }

        OptionSection("Quantity") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    PriceCalculator.format(state.unitPriceCents, symbol) + " each",
                    fontSize = 12.sp,
                    color = Brew.Muted,
                    modifier = Modifier.weight(1f),
                )
                QuantityStepper(
                    quantity = state.quantity,
                    onDecrement = viewModel::decrementQuantity,
                    onIncrement = viewModel::incrementQuantity,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun OptionSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(14.dp))
    BrewCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun IngredientChip(text: String) {
    Text(
        text,
        fontSize = 10.sp,
        color = Brew.Muted,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Brew.Border, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun SizeOptionBox(label: String, extra: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) Brew.Peach else Color.White)
            .border(1.dp, if (selected) Brew.Accent else Brew.Border, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = if (selected) Brew.Accent else Brew.Espresso,
        )
        Text(extra, fontSize = 10.sp, color = if (selected) Brew.Accent else Brew.Muted)
    }
}

@Composable
private fun MilkOptionRow(name: String, extra: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Brew.Peach else Color.White)
            .border(1.dp, if (selected) Brew.Accent else Brew.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Brew.Accent else Brew.Muted,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(extra, style = Brew.PriceStyle, fontSize = 10.sp, color = if (selected) Brew.Accent else Brew.Muted)
    }
}

@Composable
private fun SugarChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Text(
        label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) Color.White else Brew.Espresso,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Brew.Espresso else Color.White)
            .border(1.dp, if (selected) Brew.Espresso else Brew.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
