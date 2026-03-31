package com.application.jomato

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object Prefs {

    private const val PREFS_NAME = "jomato_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
}