package com.application.jomato

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object Prefs {

    private const val PREFS_NAME = "jomato_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_ALERT_SOUND_URI = "alert_sound_uri"
    private const val KEY_ALERT_CHANNEL_VERSION = "alert_channel_version"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Theme ────────────────────────────────────────────────────────────────

    /** "system" | "dark" | "light" */
    private val _themeMode = MutableStateFlow("system")
    val themeMode = _themeMode.asStateFlow()

    fun loadThemeMode(context: Context) {
        _themeMode.value = prefs(context).getString(KEY_THEME_MODE, "system") ?: "system"
    }

    fun cycleThemeMode(context: Context) {
        val next = when (_themeMode.value) {
            "system" -> "dark"
            "dark" -> "light"
            else -> "system"
        }
        _themeMode.value = next
        prefs(context).edit().putString(KEY_THEME_MODE, next).apply()
    }

    // ── Alert Sound ──────────────────────────────────────────────────────────

    /** Returns the saved alert sound URI string, or null for the built-in default. */
    fun getAlertSoundUri(context: Context): String? =
        prefs(context).getString(KEY_ALERT_SOUND_URI, null)

    /** Save a custom alert sound URI. Pass null to reset to default. */
    fun setAlertSoundUri(context: Context, uri: String?) {
        prefs(context).edit().apply {
            if (uri != null) putString(KEY_ALERT_SOUND_URI, uri)
            else remove(KEY_ALERT_SOUND_URI)
        }.apply()
    }

    /**
     * Returns the current alert channel version. Incremented each time the user
     * changes the notification sound, so RescueService creates a new channel
     * (Android caches channel settings and won't update sound on an existing channel).
     */
    fun getAlertChannelVersion(context: Context): Int =
        prefs(context).getInt(KEY_ALERT_CHANNEL_VERSION, 1)

    fun incrementAlertChannelVersion(context: Context) {
        val current = getAlertChannelVersion(context)
        prefs(context).edit().putInt(KEY_ALERT_CHANNEL_VERSION, current + 1).apply()
    }
}