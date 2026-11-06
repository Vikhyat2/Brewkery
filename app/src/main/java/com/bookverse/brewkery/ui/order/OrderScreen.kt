package com.bookverse.brewkery.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bookverse.brewkery.BrewkeryApp
import com.bookverse.brewkery.data.OrderRepository
import com.bookverse.brewkery.domain.Order
import com.bookverse.brewkery.ui.components.BrewCard
import com.bookverse.brewkery.ui.theme.Brew

class OrderViewModel(orderRepository: OrderRepository) : ViewModel() {
    val order = orderRepository.activeOrder

    companion object {
        val Factory = viewModelFactory {
            initializer { OrderViewModel((this[APPLICATION_KEY] as BrewkeryApp).container.orderRepository) }
        }
    }
}

@Composable
fun OrderScreen(
    onBackToMenu: () -> Unit,
    viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory),
) {
    val order by viewModel.order.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(Brew.Cream)
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Box(
            Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape, ambientColor = Brew.Accent, spotColor = Brew.Accent)
                .clip(CircleShape)
                .background(Brew.Peach)
                .border(1.5.dp, Brew.Accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text("☕", fontSize = 30.sp) }

        Spacer(Modifier.height(14.dp))
        Text("ORDER DISPATCHED", style = MaterialTheme.typography.labelSmall, color = Brew.Accent)
        Spacer(Modifier.height(4.dp))
        Text("Brewing in Progress!", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text("Your ticket was dispatched to our barista.", color = Brew.Muted, fontSize = 12.sp)

        Spacer(Modifier.weight(1f))

        val current = order
        if (current != null) {
            TicketCard(current)
        } else {
            Text("No active order.", color = Brew.Muted, fontSize = 13.sp)
        }

        Spacer(Modifier.weight(1.2f))

        Box(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brew.Espresso)
                .clickable(onClick = onBackToMenu),
            contentAlignment = Alignment.Center,
        ) {
            Text("Back to Menu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun TicketCard(order: Order) {
    BrewCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("ORDER TICKET", style = MaterialTheme.typography.labelSmall, color = Brew.Muted, fontSize = 9.sp)
                    Text(
                        "#${order.ticketId}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
                Text(
                    order.status.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Brew.BadgeText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Brew.BadgeBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
            HorizontalDivider(color = Brew.Border)
            TicketRow("Estimated Wait:", order.estimatedWait, valueColor = Brew.Accent)
            TicketRow("Items Ordered:", "${order.itemCount} ${if (order.itemCount == 1) "Item" else "Items"}")
            HorizontalDivider(color = Brew.Border)
            Column {
                Text("Status:", fontSize = 10.sp, color = Brew.Muted)
                Text("Barista accepted your order!", color = Brew.Success, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TicketRow(label: String, value: String, valueColor: Color = Brew.Espresso) {
    Row {
        Text(label, fontSize = 12.sp, color = Brew.Muted, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
