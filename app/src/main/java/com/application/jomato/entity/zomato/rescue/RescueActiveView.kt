package com.application.jomato.entity.zomato.rescue

import android.content.Intent
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.application.jomato.BuildConfig
import com.application.jomato.entity.zomato.ZomatoManager
import com.application.jomato.entity.zomato.api.OrderDetails
import com.application.jomato.entity.zomato.service.FoodRescueService
import com.application.jomato.ui.theme.JomatoTheme
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private fun formatRupee(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
    nf.maximumFractionDigits = 0
    nf.minimumFractionDigits = 0
    return "₹${nf.format(amount)}"
}

@Composable
fun RescueActiveView(
    state: FoodRescueState,
    onStopClick: () -> Unit
) {
    val context = LocalContext.current
    val claimedOrders = remember { ZomatoManager.getFrClaimedOrders(context) }
    val totalSaved = remember { ZomatoManager.getFrTotalSaved(context) }
    val hasClaims = claimedOrders.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (hasClaims) {
            MonitoringHeader(
                state = state,
                startedAt = state.startedAtTimestamp,
                onStop = onStopClick
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (hasClaims) {
                SavingsHero(
                    totalSaved = totalSaved,
                    claimedCount = claimedOrders.size
                )
                Spacer(modifier = Modifier.height(16.dp))
                RecentClaimsSection(orders = claimedOrders)
            } else {
                EmptyClaimsView(
                    state = state,
                    onStop = onStopClick
                )
            }
        }
    }
}


// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun MonitoringHeader(state: FoodRescueState, startedAt: Long, onStop: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Outer pulse ring
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(pulseScale)
                    .graphicsLayer { alpha = pulseAlpha }
                    .clip(CircleShape)
                    .background(JomatoTheme.ActiveGreen)
            )
            // Core dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(JomatoTheme.ActiveGreen)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (state.locationCount == 1) {
                    state.primaryLocation.fullAddress
                } else {
                    "Monitoring ${state.locationCount} addresses"
                },
                fontSize = 13.sp,
                color = JomatoTheme.BrandBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
            RunTimer(startedAt = startedAt)
        }

        Spacer(modifier = Modifier.width(8.dp))

        FilledTonalButton(
            onClick = onStop,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = JomatoTheme.Error.copy(alpha = 0.1f),
                contentColor = JomatoTheme.Error
            ),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(Icons.Rounded.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Stop", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Live timer showing how long the service has been running */
@Composable
private fun RunTimer(startedAt: Long) {
    var elapsed by remember { mutableStateOf(0L) }

    LaunchedEffect(startedAt) {
        while (true) {
            elapsed = (System.currentTimeMillis() - startedAt) / 1000
            delay(1000)
        }
    }

    val hours = elapsed / 3600
    val minutes = (elapsed % 3600) / 60
    val seconds = elapsed % 60
    val timeStr = if (hours > 0) {
        String.format("%dh %02dm", hours, minutes)
    } else {
        String.format("%dm %02ds", minutes, seconds)
    }

    Text(
        text = "Monitoring for $timeStr",
        fontSize = 11.sp,
        color = JomatoTheme.TextGray,
        letterSpacing = 0.3.sp
    )
}

// ── Savings hero ──────────────────────────────────────────────────────────────

@Composable
private fun SavingsHero(totalSaved: Double, claimedCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(JomatoTheme.BrandGradient)
            .border(1.dp, JomatoTheme.Brand.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .padding(vertical = 28.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TOTAL SAVED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = JomatoTheme.Brand.copy(alpha = 0.7f),
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatRupee(totalSaved),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = JomatoTheme.Brand,
                letterSpacing = (-1.5).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$claimedCount ${if (claimedCount == 1) "order claimed" else "orders claimed"}",
                fontSize = 13.sp,
                color = JomatoTheme.TextGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Recent claims ─────────────────────────────────────────────────────────────

@Composable
private fun RecentClaimsSection(orders: List<OrderDetails>) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "RECENT CLAIMS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = JomatoTheme.TextMuted,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        orders.forEachIndexed { index, order ->
            ClaimedOrderRow(order = order)
            if (index < orders.lastIndex) {
                HorizontalDivider(
                    color = JomatoTheme.Divider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        if (BuildConfig.IS_DEV) {
            Spacer(modifier = Modifier.height(20.dp))
            TextButton(
                onClick = {
                    ZomatoManager.clearClaimedOrders(context)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Clear Claim History",
                    fontSize = 12.sp,
                    color = JomatoTheme.TextMuted
                )
            }
        }
    }
}

@Composable
private fun ClaimedOrderRow(order: OrderDetails) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(order.restaurantImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = order.restaurantName,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(Modifier.fillMaxSize().background(JomatoTheme.Divider))
            },
            error = {
                Box(Modifier.fillMaxSize().background(JomatoTheme.Divider))
            }
        )

        Column(modifier = Modifier.weight(1f)) {

            // Restaurant name + savings badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.restaurantName ?: "Unknown Restaurant",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = JomatoTheme.BrandBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (order.cartTotal != null && order.paidAmount != null) {
                    val saved = order.cartTotal - order.paidAmount
                    if (saved > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(JomatoTheme.ActiveGreen.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "saved ${formatRupee(saved)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JomatoTheme.ActiveGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Price row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (order.cartTotal != null && order.paidAmount != null && order.cartTotal != order.paidAmount) {
                    Text(
                        text = formatRupee(order.cartTotal),
                        fontSize = 12.sp,
                        color = JomatoTheme.TextMuted,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
                if (order.paidAmount != null) {
                    Text(
                        text = formatRupee(order.paidAmount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JomatoTheme.BrandBlack
                    )
                }
            }

            // Items — one per line
            if (order.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                order.items.take(3).forEach { item ->
                    Text(
                        text = "· $item",
                        fontSize = 12.sp,
                        color = JomatoTheme.TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                if (order.items.size > 3) {
                    Text(
                        text = "+${order.items.size - 3} more",
                        fontSize = 11.sp,
                        color = JomatoTheme.TextMuted
                    )
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyClaimsView(state: FoodRescueState, onStop: () -> Unit) {
    val context = LocalContext.current

    val locationDisplayText = if (state.locationCount == 1) {
        "near ${state.primaryLocation.name}"
    } else {
        "across ${state.locationCount} addresses"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "radar")

    // Three expanding rings for a sonar/radar effect
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseOut), RepeatMode.Restart),
        label = "ring1"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseOut), RepeatMode.Restart),
        label = "ring1a"
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseOut, delayMillis = 600), RepeatMode.Restart),
        label = "ring2"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseOut, delayMillis = 600), RepeatMode.Restart),
        label = "ring2a"
    )
    val ring3Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseOut, delayMillis = 1200), RepeatMode.Restart),
        label = "ring3"
    )
    val ring3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseOut, delayMillis = 1200), RepeatMode.Restart),
        label = "ring3a"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Radar animation
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ring 1
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(ring1Scale)
                    .graphicsLayer { alpha = ring1Alpha }
                    .clip(CircleShape)
                    .border(1.5.dp, JomatoTheme.Brand, CircleShape)
            )
            // Ring 2
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(ring2Scale)
                    .graphicsLayer { alpha = ring2Alpha }
                    .clip(CircleShape)
                    .border(1.5.dp, JomatoTheme.Brand, CircleShape)
            )
            // Ring 3
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(ring3Scale)
                    .graphicsLayer { alpha = ring3Alpha }
                    .clip(CircleShape)
                    .border(1.5.dp, JomatoTheme.Brand, CircleShape)
            )
            // Center icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(JomatoTheme.Brand.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = JomatoTheme.Brand,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Listening for Orders",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = JomatoTheme.BrandBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Watching for cancelled orders\n$locationDisplayText",
            fontSize = 14.sp,
            color = JomatoTheme.TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Run timer
        RunTimer(startedAt = state.startedAtTimestamp)

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = JomatoTheme.Brand,
                contentColor = JomatoTheme.Background
            )
        ) {
            Text(
                "STOP MONITORING",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                val testIntent = Intent(context, FoodRescueService::class.java).apply {
                    action = FoodRescueService.ACTION_TEST
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(testIntent)
                } else {
                    context.startService(testIntent)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                JomatoTheme.GlassBorder.copy(alpha = 0.5f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = JomatoTheme.TextGray
            )
        ) {
            Text(
                "SEND TEST NOTIFICATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
