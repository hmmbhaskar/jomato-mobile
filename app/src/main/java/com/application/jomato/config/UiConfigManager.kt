package com.application.jomato.config

import android.content.Context
import com.application.jomato.utils.FileLogger
import kotlinx.serialization.json.Json

/**
 * Loads UI config from the bundled assets/ui.json file.
 * Zero-telemetry fork: no network calls to any external config server.
 */
object UiConfigManager {

    private const val TAG = "UiConfigManager"
    private const val ASSET_FILE = "ui.json"

    private val json = Json { ignoreUnknownKeys = true }

    var config: UiConfig? = null
        private set

    /**
     * Loads config from the bundled asset file. No network calls.
     */
    suspend fun fetch(context: Context) {
        try {
            val body = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
            config = json.decodeFromString<UiConfig>(body)
            EntityRegistry.updateFrom(config)
            FileLogger.log(context, TAG, "ui.json loaded from bundled assets")
        } catch (e: Exception) {
            FileLogger.log(context, TAG, "Failed to load bundled ui.json: ${e.message}", e)
        }
    }
}