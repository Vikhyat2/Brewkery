package com.bookverse.brewkery.ui.cart


import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookverse.brewkery.domain.CartLine
import com.bookverse.brewkery.domain.PriceCalculator
import com.bookverse.brewkery.ui.components.BrewCard
import com.bookverse.brewkery.ui.components.PrimaryButton
import com.bookverse.brewkery.ui.components.PriceText
import com.bookverse.brewkery.ui.components.QuantityStepper
import com.bookverse.brewkery.ui.components.ScreenTopBar
import com.bookverse.brewkery.ui.theme.Brew
import java.math.BigDecimal

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onBrowseMenu: () -> Unit,
    onOrderPlaced: () -> Unit,
    viewModel: CartViewModel = viewModel(factory = CartViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val symbol = state.currencySymbol

    Column(
        Modifier
            .fillMaxSize()
            .background(Brew.Cream)
            .systemBarsPadding(),
    ) {
        ScreenTopBar(title = "YOUR CART", onBack = onBack) {
            if (!state.isEmpty) {
                Text(
                    "Clear Cart",
                    color = Brew.Danger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = viewModel::clearCart).padding(4.dp),
                )
            }
        }

        if (state.isEmpty) {
            EmptyCart(onBrowseMenu = onBrowseMenu, modifier = Modifier.weight(1f))
        } else {

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.lines, key = { it.key }) { line ->
                    CartLineCard(
                        line = line,
                        symbol = symbol,
                        onIncrement = { viewModel.increment(line.key) },
                        onDecrement = { viewModel.decrement(line.key) },
                    )
                }
            }

            Column(Modifier.padding(16.dp)) {
                BrewCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("Subtotal", PriceCalculator.format(state.summary.subtotalCents, symbol))
                        SummaryRow("Delivery Fee", PriceCalculator.format(state.summary.deliveryFeeCents, symbol))
                        SummaryRow(
                            "Est. Tax (${BigDecimal.valueOf(state.taxRatePercent).setScale(1)}%)",
                            PriceCalculator.format(state.summary.taxCents, symbol),
                        )
                        HorizontalDivider(color = Brew.Border)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total Payable", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            PriceText(PriceCalculator.format(state.summary.totalCents, symbol), fontSizeSp = 17)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "Place Order Now  •  " + PriceCalculator.format(state.summary.totalCents, symbol),
                    onClick = { if (viewModel.placeOrder() != null) onOrderPlaced() },
                    modifier = Modifier.fillMaxWidth(),
                    leading = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp)) },
                )
            }
        }
    }
}

@Composable
private fun CartLineCard(line: CartLine, symbol: String, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    BrewCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(line.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (line.optionsSummary.isNotBlank()) {
                    Text(line.optionsSummary, fontSize = 10.sp, color = Brew.Muted)
                }
                Spacer(Modifier.height(4.dp))
                PriceText(PriceCalculator.format(line.lineTotalCents, symbol), fontSizeSp = 11)
            }
            Spacer(Modifier.width(8.dp))
            QuantityStepper(quantity = line.quantity, onDecrement = onDecrement, onIncrement = onIncrement)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row {
        Text(label, color = Brew.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun EmptyCart(onBrowseMenu: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🥐", fontSize = 44.sp)
        Spacer(Modifier.height(12.dp))
        Text("Your cart is empty", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Pick a roast or a fresh bake to get started.",
            color = Brew.Muted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(20.dp))
        Box(Modifier.width(200.dp)) {
            PrimaryButton(text = "Browse Menu", onClick = onBrowseMenu, modifier = Modifier.fillMaxWidth())
        }
    }
}
