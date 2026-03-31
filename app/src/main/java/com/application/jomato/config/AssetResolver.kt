package com.application.jomato.config

/**
 * Resolves asset paths from ui.json (e.g. "assets/food_rescue.svg") to loadable URIs.
 * Zero-telemetry fork: assets are bundled locally in app/src/main/assets/.
 * Returns file:///android_asset/ URIs that Coil can load directly.
 */
object AssetResolver {

    fun urlPrimary(relativePath: String): String {
        // Strip "assets/" prefix if present — bundled assets are at the root of the assets dir
        val path = relativePath.removePrefix("assets/").removePrefix("/")
        return "file:///android_asset/$path"
    }

    fun urlFallback(relativePath: String): String = urlPrimary(relativePath)
}
