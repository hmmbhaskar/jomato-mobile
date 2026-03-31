# Jomato — Zero Telemetry Fork

<div align="center">

### The Unofficial Zomato Client — Privacy-First Edition
**Built from source with all telemetry, analytics, tracking, and remote mechanisms stripped.**

</div>

> [!IMPORTANT]
> This is a **hard fork** of [jatin-dot-py/jomato-mobile](https://github.com/jatin-dot-py/jomato-mobile).
> All analytics, tracking, remote APK updates, external config fetching, and original-author attribution links have been removed. This fork is maintained independently.

---

## About

Jomato is an unofficial, reverse-engineered client for Zomato that delivers utility features not available in the official app. This fork removes all phone-home telemetry from the original project while keeping full functionality.

## Features

- **Instant Food Rescue Notifications**: Monitors Food Rescue events in real time within your area. Delivers immediate alerts and allows direct access via notification tap.

---

## What Was Removed

### Telemetry & Analytics

| Component | Original Behavior | This Fork |
|---|---|---|
| **App-open analytics** | Sent install ID, app version, Android version to developer's server on every launch | ❌ Removed |
| **Order data exfiltration** | Sent order ID, cart total, and amount paid to developer's server on Food Rescue claim | ❌ Removed |
| **Install ID generation** | Created a persistent 16-char hex tracking ID on first launch, preserved across session resets | ❌ Removed |
| **Install ID migration** | `SessionMigration` preserved the tracking ID even after wipe | ❌ Removed |

### Remote Update & Sideloading

| Component | Original Behavior | This Fork |
|---|---|---|
| **UpdateWidget** | Could download and sideload APKs from developer's server | ❌ Deleted |
| **`REQUEST_INSTALL_PACKAGES`** | Android permission allowing APK sideloading | ❌ Removed |
| **`FileProvider` + `file_paths.xml`** | Used by the updater to serve downloaded APK files | ❌ Deleted |

### External Config Server Dependency

| Component | Original Behavior | This Fork |
|---|---|---|
| **UiConfigManager** | Fetched `ui.json` from `jomato.pages.dev` on every app launch (leaks IP/timing) | ❌ Replaced with bundled local config |
| **AssetResolver** | Built image URLs pointing to `jomato.pages.dev/assets/` | ❌ Loads from local `assets/` directory |
| **`UI_JSON_HOST` build config** | Required external host URLs in `local.properties` | ❌ Removed entirely |
| **Integrity check** | Compared APK hash against author's known digests, showed warning dialog | ❌ Removed |

### Original Author References

| Component | Original Behavior | This Fork |
|---|---|---|
| **AttributionWidget** | Displayed "Star us on GitHub" linking to `jatin-dot-py/jomato` | ❌ Deleted |
| **SupportRow / DashboardTopBar** | Hardcoded "Report Issue" and "Request Feature" URLs to original author's GitHub | ❌ Cleared |
| **DashboardBottomBar** | Looked up and displayed update/attribution widgets | ❌ Stripped |

### Dead Code & Unsafe Patterns

| Component | Issue | This Fork |
|---|---|---|
| **RescueServiceOld.kt** | 490 lines of dead code containing a trust-all-certs SSL bypass (`getUnsafeSocketFactory`) | ❌ Deleted |
| **IntegrityDialog.kt** | Dead UI component after integrity check removal | ❌ Deleted |
| **Prefs integrity methods** | Dead `getHideIntegrity()` / `setHideIntegrity()` after dialog removal | ❌ Removed |

---

## Anti-Detection Hardening

The original app had several static fingerprint patterns that could be used by Zomato to identify and block unofficial clients. This fork addresses all of them:

| Vector | Original | This Fork |
|---|---|---|
| **Device fingerprint** | Hardcoded emulator string (`Android SDK built for x86_64`) — every user sent the same value | Uses real `android.os.Build` values from the actual device |
| **Installer package** | `cm.aptoide.pt` (Aptoide third-party store) | `com.android.vending` (Google Play Store) |
| **Firebase Instance ID** | Random hex (wrong format, detectable) | Realistic FCM token format (`xxx:APA91b...`) |
| **GPS location headers** | Hardcoded `0.0, 0.0` (Gulf of Guinea, Atlantic Ocean) | Removed defaults; real coordinates sent per-request |
| **VPN header** | `X-VPN-Active: 1` (flagged VPN usage) | `X-VPN-Active: 0` |
| **Network type** | `mobile_UNKNOWN` (suspicious) | `mobile_LTE` (normal) |
| **MQTT client ID** | `user{timestamp}` (recognizable pattern on broker) | UUID-based (random, no fingerprint) |
| **Location headers in cart API** | Only sent `X-User-Defined-Lat/Long` | Sends both `X-Present-*` and `X-User-Defined-*` (matches real app) |

---

## Privacy

**This fork has zero telemetry. No data leaves your device except to Zomato's own servers.**

- ✅ No analytics pings of any kind
- ✅ No install ID generation or tracking
- ✅ No order/cart data exfiltration
- ✅ No remote update mechanism
- ✅ No external config server calls — UI config is bundled locally
- ✅ No original-author attribution links or GitHub redirects
- ✅ All user data (sessions, preferences) stays on-device in local storage
- ✅ Sensitive data (tokens, phone numbers, OTPs) sanitized in local logs

### Network Connections

The app **only** communicates with:

| Server | Purpose | Protocol |
|---|---|---|
| `accounts.zomato.com` | OTP login and session management | HTTPS |
| `api.zomato.com` | Food Rescue cart and order APIs | HTTPS |
| `hedwig.zomato.com:443` | Real-time Food Rescue notifications | MQTT over TLS |

No other domains are contacted. Period.

---

## How to Install

### Option A: Download the APK

- Navigate to the [Releases Page](../../releases) of this fork
- Download the latest APK and install it on your device
- You will see a Play Protect warning — this is expected for apps not on the Play Store

### Option B: Build from Source

1. Install [Android Studio](https://developer.android.com/studio)
2. Clone this repository
3. Open the project in Android Studio (it will set up `local.properties` automatically)
4. Wait for Gradle sync to complete (~5 min first time)
5. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
6. Or from terminal: `./gradlew assembleDebug`

Output: `app/build/outputs/apk/debug/app-debug.apk`

> **Note:** All UI configuration is bundled locally in `assets/ui.json` — the app makes zero network calls to any config server. No `local.properties` configuration is needed beyond the SDK path.

---

## Setup After Install

1. Open the app and log in with your Zomato phone number + OTP
2. Tap on the **Food Rescue** feature from the home screen
3. Select your **location** to monitor
4. Allow **notification permissions** when prompted
5. Disable **battery optimization** for reliable background notifications
6. Done! You'll receive instant alerts for Food Rescue deals near you.

---

## Known Limitations

| Issue | Details |
|---|---|
| **Frozen API version** | The app sends `X-Zomato-App-Version: 931` (v19.3.1). If Zomato deprecates this API version, the app will stop working until the headers are updated from a newer Zomato APK. |
| **Tokens in plaintext** | Session tokens are stored in Android SharedPreferences (plaintext). On rooted devices, they could be extracted. |
| **ProGuard disabled** | Release builds are not obfuscated (`minifyEnabled false`). The APK can be fully decompiled. |

---

## Disclaimer & Legal

This project is intended for **educational purposes only**.

- This application is **not affiliated with, endorsed by, or connected to Zomato** in any manner
- "Zomato" and the Zomato logo are trademarks of Zomato Ltd.
- Usage of this software is at your own discretion and risk
- The developer assumes **no responsibility** for any account restrictions or bans imposed by the official service provider

---

## License

MIT License — see [LICENSE](LICENSE) for details.

Original project by [jatin-dot-py](https://github.com/jatin-dot-py/jomato-mobile). This fork strips telemetry and is maintained independently.
