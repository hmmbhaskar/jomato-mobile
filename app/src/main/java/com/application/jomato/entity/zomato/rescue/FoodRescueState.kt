package com.application.jomato.entity.zomato.rescue

import com.application.jomato.entity.zomato.api.TabbedHomeEssentials
import com.application.jomato.entity.zomato.api.UserLocation

/**
 * A single location being monitored, with its Zomato API essentials
 * (including the MQTT channel for that zone).
 */
data class MonitoredAddress(
    val essentials: TabbedHomeEssentials,
    val location: UserLocation
)

/**
 * Full state of an active Food Rescue monitoring session.
 * Supports monitoring multiple addresses simultaneously.
 */
data class FoodRescueState(
    val addresses: List<MonitoredAddress>,
    val startedAtTimestamp: Long,
) {
    /** Convenience: first address (used for display fallbacks) */
    val primaryLocation: UserLocation get() = addresses.first().location

    /** All unique MQTT channel names across monitored addresses */
    val uniqueChannels: List<String>
        get() = addresses.mapNotNull { it.essentials.foodRescue?.channelName }.distinct()

    /** Number of locations being monitored */
    val locationCount: Int get() = addresses.size

    // ── Backward-compatible accessors ─────────────────────────────────────
    // These allow existing code to keep working with minimal changes.

    @Deprecated("Use addresses list", ReplaceWith("addresses.first().essentials"))
    val essentials: TabbedHomeEssentials get() = addresses.first().essentials

    @Deprecated("Use addresses list or primaryLocation", ReplaceWith("primaryLocation"))
    val location: UserLocation get() = primaryLocation
}