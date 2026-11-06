package com.bookverse.brewkery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookverse.brewkery.ui.theme.Brew

@Composable
fun LoadingView(modifier: Modifier = Modifier, message: String = "Brewing the menu…") {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = Brew.Accent, strokeWidth = 3.dp)
        Spacer(Modifier.height(12.dp))
        Text(message, color = Brew.Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("☕", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text("Couldn't load", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(message, color = Brew.Muted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = "Try again", onClick = onRetry, modifier = Modifier.width(180.dp))
    }
}

/** Small yellow "BESTSELLER" style tag. */
@Composable
fun BadgeTag(text: String, modifier: Modifier = Modifier, dark: Boolean = false) {
    if (text.isBlank()) return
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 8.sp,
        color = if (dark) Brew.Gold else Brew.BadgeText,
        modifier = modifier
            .clip(RoundedCornerShape(if (dark) 50 else 4))
            .background(if (dark) Brew.Espresso.copy(alpha = 0.85f) else Brew.BadgeBg)
            .padding(horizontal = if (dark) 10.dp else 6.dp, vertical = 3.dp),
    )
}

@Composable
fun PriceText(text: String, modifier: Modifier = Modifier, fontSizeSp: Int = 12) {
    Text(text, style = Brew.PriceStyle, fontSize = fontSizeSp.sp, modifier = modifier)
}

@Composable
fun CircleIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Brew.Border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Back arrow + centered uppercase title + optional trailing slot. */
@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    trailing: @Composable () -> Unit = { Spacer(Modifier.size(36.dp)) },
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            letterSpacing = 0.8.sp,
        )
        Box(Modifier.width(72.dp), contentAlignment = Alignment.CenterEnd) { trailing() }
    }
}

/** Big rounded CTA with the brand gradient. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) Brew.CtaGradient else androidx.compose.ui.graphics.SolidColor(Brew.Muted.copy(alpha = 0.4f)))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(8.dp))
            }
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

/** "−  1  +" pill used on the cart and detail screens. */
@Composable
fun QuantityStepper(quantity: Int, onDecrement: () -> Unit, onIncrement: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brew.PeachDeep)
            .border(1.dp, Brew.Border, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton("−", onDecrement)
        Text(
            quantity.toString(),
            style = Brew.PriceStyle.copy(color = Brew.Espresso),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        StepperButton("+", onIncrement)
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(30.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Brew.Espresso)
    }
}

/** White rounded card with the warm border used everywhere. */
@Composable
fun BrewCard(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(1.dp, Brew.Border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) { content() }
}
