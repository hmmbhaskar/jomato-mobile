package com.application.jomato.entity.zomato.api

import android.os.Build
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.brotli.BrotliInterceptor
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

internal object ApiBase {
    const val TAG = "ZomatoApiClient"

    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(BrotliInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    // ---- Per-session identifiers (regenerated each app launch) ----
    private val sessionUuid = UUID.randomUUID().toString()
    private val appSessionId = UUID.randomUUID().toString()
    private val accessUuid = UUID.randomUUID().toString()
    private val androidId = randomHex(16)
    private val jumboSessionId = "${UUID.randomUUID()}${System.currentTimeMillis()}"
    private val appsflyerUid = "${System.currentTimeMillis()}-${Random.nextLong(1_000_000_000_000_000_000, Long.MAX_VALUE)}"

    // ---- Real device fingerprint from android.os.Build ----
    // Each install sends its ACTUAL device info — no shared static pattern to flag.
    private val realManufacturer = Build.MANUFACTURER
    private val realBrand = Build.BRAND
    private val realModel = Build.MODEL
    private val realAndroidVersion = Build.VERSION.RELEASE

    // ---- Realistic Firebase Instance ID ----
    // Real FCM tokens follow format: <11 alphanum>:APA91b<134 chars>
    // We generate a plausible-looking token per session.
    private val firebaseInstanceId: String = run {
        val prefix = randomAlphanumeric(11)
        val suffix = randomAlphanumeric(134)
        "${prefix}:APA91b${suffix}"
    }

    val commonHeaders: Headers = Headers.Builder()
        // Connection & encoding
        .add("Accept", "image/webp")
        .add("Connection", "Keep-Alive")

        // Zomato core
        .add("X-Zomato-API-Key", "7749b19667964b87a3efc739e254ada2")
        .add("X-Zomato-App-Version", "931")
        .add("X-Zomato-App-Version-Code", "1710019310")
        .add("X-Zomato-Client-Id", "5276d7f1-910b-4243-92ea-d27e758ad02b")
        .add("X-Zomato-UUID", sessionUuid)
        .add("X-Client-Id", "zomato_android_v2")

        // Device fingerprint — uses REAL device values, not a hardcoded emulator string
        .add("User-Agent",
            "&source=android_market" +
            "&version=${realAndroidVersion}" +
            "&device_manufacturer=${realManufacturer}" +
            "&device_brand=${realBrand}" +
            "&device_model=${realModel.replace(" ", "+")}" +
            "&api_version=931" +
            "&app_version=v19.3.1"
        )
        .add("X-Android-Id", androidId)
        .add("X-Device-Height", "2400")
        .add("X-Device-Width", "1080")
        .add("X-Device-Pixel-Ratio", "2.75")
        .add("X-Device-Language", "en")

        // App state
        .add("X-APP-APPEARANCE", "LIGHT")
        .add("X-APP-THEME", "default")
        .add("X-SYSTEM-APPEARANCE", "UNSPECIFIED")
        .add("X-App-Language", "&lang=en&android_language=en&android_country=")
        .add("X-App-Session-Id", appSessionId)

        // Session & tracking IDs
        .add("X-Access-UUID", accessUuid)
        .add("X-Request-Id", UUID.randomUUID().toString())
        .add("X-Jumbo-Session-Id", jumboSessionId)
        .add("X-Appsflyer-UID", appsflyerUid)
        .add("X-FIREBASE-INSTANCE-ID", firebaseInstanceId)
        .add("X-Installer-Package-Name", "com.android.vending")

        // Location defaults — not 0,0 (Gulf of Guinea).
        // Omitted so per-request overrides take precedence.
        .add("X-City-Id", "-1")
        .add("X-O2-City-Id", "-1")
        .add("X-Present-Horizontal-Accuracy", "-1")

        // Network & device state
        .add("X-Network-Type", "mobile_LTE")
        .add("X-Bluetooth-On", "false")
        .add("X-VPN-Active", "0")

        // Accessibility
        .add("X-Accessibility-Dynamic-Text-Scale-Factor", "1.0")
        .add("X-Accessibility-Voice-Over-Enabled", "0")

        // Feature flags
        .add("X-BLINKIT-INSTALLED", "false")
        .add("X-DISTRICT-INSTALLED", "false")
        .add("X-RIDER-INSTALLED", "false")

        // Akamai CDN
        .add("is-akamai-video-optimisation-enabled", "0")
        .add("pragma", "akamai-x-get-request-id,akamai-x-cache-on, akamai-x-check-cacheable")

        // Priority
        .add("USER-BUCKET", "0")
        .add("USER-HIGH-PRIORITY", "0")
        .add("x-perf-class", "PERFORMANCE_AVERAGE")

        .build()

    val jsonParser: Json = Json { ignoreUnknownKeys = true }
    val jsonMediaType: MediaType = "application/json; charset=UTF-8".toMediaType()

    fun authenticatedHeaders(accessToken: String): Headers =
        commonHeaders.newBuilder()
            .add("X-Zomato-Access-Token", accessToken)
            .build()

    /**
     * Builds headers with the user's actual selected location coordinates.
     * Use this instead of [authenticatedHeaders] for location-sensitive API calls.
     */
    fun authenticatedHeadersWithLocation(
        accessToken: String,
        lat: Double?,
        lng: Double?,
        cityId: Int? = null
    ): Headers {
        val builder = commonHeaders.newBuilder()
            .add("X-Zomato-Access-Token", accessToken)
        if (lat != null) {
            builder.set("X-Present-Lat", lat.toString())
            builder.set("X-User-Defined-Lat", lat.toString())
        }
        if (lng != null) {
            builder.set("X-Present-Long", lng.toString())
            builder.set("X-User-Defined-Long", lng.toString())
        }
        if (cityId != null) {
            builder.set("X-City-Id", cityId.toString())
            builder.set("X-O2-City-Id", cityId.toString())
        }
        return builder.build()
    }

    private fun randomHex(length: Int): String =
        (1..length).joinToString("") { Random.nextInt(16).toString(16) }

    private fun randomAlphanumeric(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
        return (1..length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
