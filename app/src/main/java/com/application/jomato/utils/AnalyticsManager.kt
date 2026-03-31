package com.application.jomato.utils

import android.content.Context

/**
 * Analytics has been fully stripped from this fork.
 * Both methods are kept as no-ops so call sites compile without changes.
 */
object AnalyticsManager {

    @Suppress("UNUSED_PARAMETER")
    suspend fun pingAppOpen(context: Context) {
        // No-op: zero telemetry fork
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun pingFoodRescue(
        context: Context,
        orderId: String,
        totalCart: Double,
        totalPaid: Double
    ) {
        // No-op: zero telemetry fork
    }
}