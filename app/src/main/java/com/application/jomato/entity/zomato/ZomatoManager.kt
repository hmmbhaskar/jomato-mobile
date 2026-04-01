package com.application.jomato.entity.zomato

import android.content.Context
import com.application.jomato.entity.zomato.api.OrderDetails
import com.application.jomato.entity.zomato.api.TabbedHomeEssentials
import com.application.jomato.entity.zomato.api.UserLocation
import com.application.jomato.entity.zomato.rescue.FoodRescueState
import com.application.jomato.entity.zomato.rescue.MonitoredAddress
import com.application.jomato.sessions.BaseSessionManager
import com.application.jomato.sessions.Entity
import com.application.jomato.utils.FileLogger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

object ZomatoManager : BaseSessionManager<ZomatoSession>() {

    override val entity: Entity get() = Entity.ZOMATO

    private const val TAG = "ZomatoManager"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun sessionKey(sessionId: String, field: String) = "${Entity.ZOMATO.keyPrefix}-$sessionId-$field"
    private fun frKey(field: String) = "${Entity.ZOMATO.keyPrefix}-$field"


    override fun saveSession(context: Context, session: ZomatoSession) {
        FileLogger.log(context, TAG, "Saving session: ${session.sessionId} | user: ${session.userName}")
        prefs(context).edit()
            .putString(sessionKey(session.sessionId, "access_token"), session.accessToken)
            .putString(sessionKey(session.sessionId, "refresh_token"), session.refreshToken)
            .putString(sessionKey(session.sessionId, "user_name"), session.userName)
            .putString(sessionKey(session.sessionId, "user_id"), session.userId)
            .putLong(sessionKey(session.sessionId, "created_at"), session.createdAt)
            .apply()
    }

    override fun getSession(context: Context, sessionId: String): ZomatoSession? {
        val p = prefs(context)
        val accessToken = p.getString(sessionKey(sessionId, "access_token"), null) ?: return null
        val refreshToken = p.getString(sessionKey(sessionId, "refresh_token"), null) ?: return null
        val userName = p.getString(sessionKey(sessionId, "user_name"), null) ?: return null
        val userId = p.getString(sessionKey(sessionId, "user_id"), null) ?: return null
        val createdAt = p.getLong(sessionKey(sessionId, "created_at"), 0)
        return ZomatoSession(sessionId, createdAt, accessToken, refreshToken, userName, userId)
    }

    override fun listSessions(context: Context): List<ZomatoSession> {
        val prefix = Entity.ZOMATO.keyPrefix
        val sessions = prefs(context).all.keys
            .filter { it.startsWith("$prefix-") && it.endsWith("-user_name") }
            .mapNotNull { k ->
                val sessionId = k.removePrefix("$prefix-").removeSuffix("-user_name")
                getSession(context, sessionId)
            }
        FileLogger.log(context, TAG, "Listed ${sessions.size} Zomato sessions")
        return sessions
    }

    override fun deleteSession(context: Context, sessionId: String) {
        FileLogger.log(context, TAG, "Deleting session: $sessionId")
        val keysToRemove = prefs(context).all.keys.filter { it.startsWith("${Entity.ZOMATO.keyPrefix}-$sessionId-") }
        prefs(context).edit().apply {
            keysToRemove.forEach { remove(it) }
        }.apply()
        FileLogger.log(context, TAG, "Deleted ${keysToRemove.size} keys for session: $sessionId")
    }


    /**
     * Saves multi-address Food Rescue state.
     * Each pair of (essentials, location) represents one monitored zone.
     */
    fun saveFoodRescueState(context: Context, addresses: List<MonitoredAddress>) {
        FileLogger.log(context, TAG, "Saving FR state | ${addresses.size} addresses")
        try {
            val essentialsList = addresses.map { json.encodeToString(it.essentials) }
            val locationsList = addresses.map { json.encodeToString(it.location) }
            prefs(context).edit()
                .putString(frKey("fr_essentials_list"), json.encodeToString(essentialsList))
                .putString(frKey("fr_locations_list"), json.encodeToString(locationsList))
                .putLong(frKey("fr_started_at"), System.currentTimeMillis())
                .putLong(frKey("fr_last_notification_at"), 0)
                // Clean up legacy single-address keys
                .remove(frKey("fr_essentials"))
                .remove(frKey("fr_location"))
                .apply()
            FileLogger.log(context, TAG, "FR state saved for: ${addresses.map { it.location.name }}")
        } catch (e: Exception) {
            FileLogger.log(context, TAG, "Failed to save FR state | ${e.message}", e)
        }
    }

    fun getFoodRescueState(context: Context): FoodRescueState? {
        val p = prefs(context)

        // Try new multi-address format first
        val essListJson = p.getString(frKey("fr_essentials_list"), null)
        val locListJson = p.getString(frKey("fr_locations_list"), null)

        if (essListJson != null && locListJson != null) {
            return try {
                val essList: List<String> = json.decodeFromString(essListJson)
                val locList: List<String> = json.decodeFromString(locListJson)
                val addresses = essList.zip(locList).map { (essJson, locJson) ->
                    MonitoredAddress(
                        essentials = json.decodeFromString(essJson),
                        location = json.decodeFromString(locJson)
                    )
                }
                if (addresses.isEmpty()) return null
                val state = FoodRescueState(
                    addresses = addresses,
                    startedAtTimestamp = p.getLong(frKey("fr_started_at"), 0)
                )
                FileLogger.log(context, TAG, "FR state retrieved | ${state.locationCount} locations")
                state
            } catch (e: Exception) {
                FileLogger.log(context, TAG, "Error parsing multi-address FR state | ${e.message}", e)
                stopFoodRescue(context)
                null
            }
        }

        // Fallback: migrate legacy single-address format
        val essJson = p.getString(frKey("fr_essentials"), null) ?: return null
        val locJson = p.getString(frKey("fr_location"), null) ?: return null
        return try {
            val address = MonitoredAddress(
                essentials = json.decodeFromString(essJson),
                location = json.decodeFromString(locJson)
            )
            val state = FoodRescueState(
                addresses = listOf(address),
                startedAtTimestamp = p.getLong(frKey("fr_started_at"), 0)
            )
            FileLogger.log(context, TAG, "FR state (legacy) retrieved | location: ${address.location.name}")
            // Auto-migrate to new format
            saveFoodRescueState(context, state.addresses)
            state
        } catch (e: Exception) {
            FileLogger.log(context, TAG, "Error parsing FR state, resetting | ${e.message}", e)
            stopFoodRescue(context)
            null
        }
    }

    fun stopFoodRescue(context: Context) {
        FileLogger.log(context, TAG, "Stopping FR")
        prefs(context).edit()
            .remove(frKey("fr_essentials_list"))
            .remove(frKey("fr_locations_list"))
            .remove(frKey("fr_essentials"))
            .remove(frKey("fr_location"))
            .remove(frKey("fr_started_at"))
            .remove(frKey("fr_last_notification_at"))
            .remove(frKey("fr_session_id"))
            .remove(frKey("fr_missed_alerts"))
            .apply()
        FileLogger.log(context, TAG, "FR stopped")
    }

    fun isFoodRescueActive(context: Context): Boolean {
        val active = prefs(context).contains(frKey("fr_essentials_list")) ||
                     prefs(context).contains(frKey("fr_essentials"))
        FileLogger.log(context, TAG, "FR active check | active: $active")
        return active
    }

    fun saveLastNotification(context: Context, timestamp: Long) {
        prefs(context).edit().putLong(frKey("fr_last_notification_at"), timestamp).apply()
        FileLogger.log(context, TAG, "Last notification saved | timestamp: $timestamp")
    }

    fun getLastNotificationTime(context: Context): Long {
        val timestamp = prefs(context).getLong(frKey("fr_last_notification_at"), 0)
        FileLogger.log(context, TAG, "Last notification retrieved | timestamp: $timestamp")
        return timestamp
    }

    fun saveFoodRescueSessionId(context: Context, sessionId: String) {
        FileLogger.log(context, TAG, "Saving FR session ID: $sessionId")
        prefs(context).edit().putString(frKey("fr_session_id"), sessionId).apply()
    }

    fun getFoodRescueSessionId(context: Context): String? {
        val sessionId = prefs(context).getString(frKey("fr_session_id"), null)
        FileLogger.log(context, TAG, "FR session ID retrieved: $sessionId")
        return sessionId
    }

    fun saveOrderClaimedState(context: Context, identifier: String, orderDetails: OrderDetails?) {
        FileLogger.log(context, TAG, "Saving order claimed state | id: $identifier | hasPayload: ${orderDetails != null}")
        prefs(context).edit()
            .putString(frKey("fr_order_claimed_$identifier"), if (orderDetails != null) json.encodeToString(orderDetails) else "")
            .apply()
    }

    fun getFrClaimedOrders(context: Context): List<OrderDetails> {
        val keyPrefix = frKey("fr_order_claimed_")
        return prefs(context).all.entries
            .filter { it.key.startsWith(keyPrefix) }
            .mapNotNull { entry ->
                val payload = entry.value as? String ?: return@mapNotNull null
                if (payload.isEmpty()) return@mapNotNull null
                try {
                    json.decodeFromString<OrderDetails>(payload)
                } catch (e: Exception) {
                    FileLogger.log(context, TAG, "Failed to parse claimed order: ${e.message}")
                    null
                }
            }
            .sortedByDescending { it.orderId }
    }

    fun getFrTotalSaved(context: Context): Double {
        return getFrClaimedOrders(context).sumOf { order ->
            val cart = order.cartTotal ?: 0.0
            val paid = order.paidAmount ?: 0.0
            (cart - paid).coerceAtLeast(0.0)
        }
    }

    fun clearClaimedOrders(context: Context) {
        val keyPrefix = frKey("fr_order_claimed_")
        val keysToRemove = prefs(context).all.keys.filter { it.startsWith(keyPrefix) }
        prefs(context).edit().apply {
            keysToRemove.forEach { remove(it) }
        }.apply()
        FileLogger.log(context, TAG, "Cleared ${keysToRemove.size} claimed order entries")
    }

    // ── Missed Alerts ─────────────────────────────────────────────────────────

    private const val MAX_MISSED_ALERTS = 50

    /**
     * Saves a missed alert (notification suppressed by cooldown).
     * Stores: timestamp, address name, short address.
     */
    fun saveMissedAlert(context: Context, timestamp: Long, addressName: String, addressShort: String) {
        val existing = getMissedAlertsRaw(context).toMutableList()
        val entry = buildJsonObject {
            put("t", timestamp)
            put("n", addressName)
            put("a", addressShort)
        }.toString()
        existing.add(entry)
        // Keep only the latest MAX_MISSED_ALERTS
        val trimmed = if (existing.size > MAX_MISSED_ALERTS) existing.takeLast(MAX_MISSED_ALERTS) else existing
        prefs(context).edit()
            .putString(frKey("fr_missed_alerts"), json.encodeToString(trimmed))
            .apply()
        FileLogger.log(context, TAG, "Missed alert saved | $addressName | total: ${trimmed.size}")
    }

    data class MissedAlert(val timestamp: Long, val addressName: String, val addressShort: String)

    fun getMissedAlerts(context: Context): List<MissedAlert> {
        return getMissedAlertsRaw(context).mapNotNull { raw ->
            try {
                val obj = json.parseToJsonElement(raw).jsonObject
                val t = obj["t"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
                val n = obj["n"]?.jsonPrimitive?.content ?: "Unknown"
                val a = obj["a"]?.jsonPrimitive?.content ?: ""
                MissedAlert(timestamp = t, addressName = n, addressShort = a)
            } catch (_: Exception) { null }
        }.sortedByDescending { it.timestamp }
    }

    fun getMissedAlertCount(context: Context): Int = getMissedAlertsRaw(context).size

    fun clearMissedAlerts(context: Context) {
        prefs(context).edit().remove(frKey("fr_missed_alerts")).apply()
        FileLogger.log(context, TAG, "Missed alerts cleared")
    }

    private fun getMissedAlertsRaw(context: Context): List<String> {
        val raw = prefs(context).getString(frKey("fr_missed_alerts"), null) ?: return emptyList()
        return try {
            json.decodeFromString<List<String>>(raw)
        } catch (_: Exception) { emptyList() }
    }

}
