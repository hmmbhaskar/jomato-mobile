<div align="center">

# 🍕 Jomato

### Zero Telemetry · Multi-Zone · Privacy First

**The unofficial Zomato client that respects your privacy.**<br/>
All telemetry, analytics, and tracking stripped. Your data stays on your device.

[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<br/>

<img src="screenshots/home_screen.jpeg" width="200"/>  <img src="screenshots/post_setup.jpeg" width="200"/>

</div>

---

## ✨ Features

### 🔔 Instant Food Rescue Alerts
Real-time monitoring of cancelled Zomato orders near you. Get notified the moment food becomes available at steep discounts.

- **Two-stage rich notifications** — instant alert sound, then silent update with restaurant name, price breakdown (~~₹450~~ → ₹149), and viewer count
- **Custom alert sound** — pick any notification sound from your device so you never miss a deal
- **5-minute cooldown** — prevents notification spam while keeping you informed

### 📍 Multi-Address Monitoring
Zomato restricts Food Rescue to a ~3km radius. Jomato lets you **monitor multiple addresses simultaneously**, covering far more ground.

- Select any combination of your saved Zomato addresses
- Each address gets its own MQTT channel subscription
- Cart fetch tries all locations to find the available order
- Foreground notification shows how many zones are being monitored

### 🛡️ Zero Telemetry
Not a single byte of data leaves your device except to Zomato's own servers.

| What's removed | Details |
|---|---|
| Analytics pings | Install ID, app version, device info — all stripped |
| Order data exfiltration | Cart totals and order IDs are never sent to third parties |
| Remote update mechanism | No APK sideloading from developer servers |
| External config server | UI config is bundled locally in `assets/ui.json` |
| Integrity checks | No APK hash comparison against author's server |
| Attribution links | No forced redirects to original author's GitHub |

### 🎨 Premium UI
Modern Material 3 design with Jetpack Compose, featuring:

- Branded splash screen with animated "JOMATO" logotype
- Dynamic time-of-day greetings (Good morning / afternoon / evening)
- Gradient hero cards for savings tracking
- Sonar/radar pulse animation while listening for orders
- Live monitoring timer showing uptime duration
- Dark mode support throughout

### 💾 Session Persistence
Your logged-in accounts survive app reinstalls through two mechanisms:

- **`hasFragileUserData`** — Android 10+ shows "Keep app data?" checkbox during uninstall
- **Auto Backup** — sessions backed up to Google Drive, restored on new install
- **Device Transfer** — sessions migrate when switching phones

---

## 📱 Quick Start

### Install

**Option A** — Download the latest APK from the [Releases page](../../releases)

**Option B** — Build from source:

```bash
git clone https://github.com/hmmbhaskar/jomato-mobile.git
cd jomato-mobile
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

> [!TIP]
> You can also open the project in [Android Studio](https://developer.android.com/studio) and use **Build → Build APK(s)** for a one-click build.

### Setup

<table>
<tr>
<td width="33%">

**1. Log in**

Open the app and enter your Zomato phone number. Verify with OTP.

<img src="screenshots/login.jpeg" width="150"/>

</td>
<td width="33%">

**2. Select addresses**

Choose one or more Zomato addresses to monitor. More addresses = more coverage.

<img src="screenshots/home_screen.jpeg" width="150"/>

</td>
<td width="33%">

**3. Grant permissions**

Allow notifications and disable battery optimization for reliable alerts.

<img src="screenshots/allow_permissions.jpeg" width="150"/>

</td>
</tr>
</table>

> [!IMPORTANT]
> **Disable battery optimization** for Jomato. Without this, Android may kill the background service and you'll miss alerts.
>
> <img src="screenshots/power_settings_dialog.jpeg" width="200"/>

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Jomato App                        │
│                                                     │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │  Dashboard   │  │  Setup View  │  │ Monitoring│  │
│  │  (Compose)   │  │ (Multi-addr) │  │   View    │  │
│  └──────┬───────┘  └──────┬───────┘  └─────┬─────┘  │
│         │                 │                │        │
│  ┌──────┴─────────────────┴────────────────┴─────┐  │
│  │            FoodRescueService                   │  │
│  │  ┌───────────┐ ┌──────────┐ ┌──────────────┐  │  │
│  │  │MQTT Client│ │ Dedup    │ │ Cart Fetch   │  │  │
│  │  │(multi-ch) │ │ Engine   │ │ (multi-addr) │  │  │
│  │  └─────┬─────┘ └──────────┘ └──────┬───────┘  │  │
│  └────────┼───────────────────────────┼──────────┘  │
│           │                           │             │
└───────────┼───────────────────────────┼─────────────┘
            │                           │
   ┌────────▼──────┐          ┌─────────▼────────┐
   │hedwig.zomato  │          │ api.zomato.com   │
   │  .com:443     │          │                  │
   │  (MQTT/TLS)   │          │  (HTTPS REST)    │
   └───────────────┘          └──────────────────┘
```

### Key Components

| Component | Description |
|---|---|
| `FoodRescueService` | Foreground service with MQTT subscription to Zomato's real-time event broker. Subscribes to multiple channels for multi-address monitoring. |
| `ZomatoManager` | Session and state persistence layer using SharedPreferences. Handles multi-address state serialization with legacy migration. |
| `FoodRescueCartApi` | Reverse-engineered Zomato cart API. Fetches restaurant name, prices, items, and viewer count for rich notifications. |
| `RescueActiveView` | Monitoring UI with radar animation, live timer, savings hero card, and claimed order history. |
| `JomatoTheme` | Custom Material 3 theme system with gradient brushes, glass borders, and light/dark mode support. |

### Network Connections

The app **only** communicates with Zomato's own servers:

| Server | Purpose | Protocol |
|---|---|---|
| `accounts.zomato.com` | OTP login and session management | HTTPS |
| `api.zomato.com` | Food Rescue cart, locations, restaurant data | HTTPS |
| `hedwig.zomato.com:443` | Real-time order cancellation events | MQTT over TLS |

**No other domains are contacted. Period.**

---

## 🔒 Anti-Detection Hardening

The original project had several static fingerprint patterns that Zomato could use to identify unofficial clients. This fork addresses all of them:

| Vector | Original (Detectable) | This Fork (Stealth) |
|---|---|---|
| Device fingerprint | Hardcoded emulator string | Real `android.os.Build` values |
| Installer package | `cm.aptoide.pt` (Aptoide) | `com.android.vending` (Play Store) |
| Firebase Instance ID | Random hex (wrong format) | Realistic FCM token format |
| GPS headers | Hardcoded `0.0, 0.0` (Atlantic Ocean) | Real device coordinates per-request |
| VPN header | `X-VPN-Active: 1` | `X-VPN-Active: 0` |
| Network type | `mobile_UNKNOWN` | `mobile_LTE` |
| MQTT client ID | `user{timestamp}` pattern | UUID-based (random) |
| Location headers | Missing `X-Present-*` headers | Both `X-Present-*` and `X-User-Defined-*` |

---

## 🧹 What Was Stripped

<details>
<summary><strong>Telemetry & Analytics</strong> — Click to expand</summary>

| Component | Original Behavior | Status |
|---|---|---|
| App-open analytics | Sent install ID, app version, Android info to developer's server on every launch | ❌ Removed |
| Order data exfiltration | Sent order ID, cart total, paid amount to developer's server | ❌ Removed |
| Install ID generation | Persistent 16-char hex tracking ID created on first launch | ❌ Removed |
| Install ID migration | `SessionMigration` preserved tracking ID even after wipe | ❌ Removed |

</details>

<details>
<summary><strong>Remote Update & Sideloading</strong> — Click to expand</summary>

| Component | Original Behavior | Status |
|---|---|---|
| UpdateWidget | Downloaded and sideloaded APKs from developer's server | ❌ Deleted |
| `REQUEST_INSTALL_PACKAGES` | Android permission for APK sideloading | ❌ Removed |
| `FileProvider` + `file_paths.xml` | Served downloaded APK files to installer | ❌ Deleted |

</details>

<details>
<summary><strong>External Config Server</strong> — Click to expand</summary>

| Component | Original Behavior | Status |
|---|---|---|
| UiConfigManager | Fetched `ui.json` from `jomato.pages.dev` on every launch | ❌ Bundled locally |
| AssetResolver | Image URLs pointing to `jomato.pages.dev/assets/` | ❌ Local `assets/` |
| `UI_JSON_HOST` build config | Required external host URLs in `local.properties` | ❌ Removed |
| Integrity check | Compared APK hash against author's known digests | ❌ Removed |

</details>

<details>
<summary><strong>Dead Code & Unsafe Patterns</strong> — Click to expand</summary>

| Component | Issue | Status |
|---|---|---|
| `RescueServiceOld.kt` | 490 lines of dead code with trust-all-certs SSL bypass | ❌ Deleted |
| `IntegrityDialog.kt` | Dead UI component | ❌ Deleted |
| Prefs integrity methods | Dead `getHideIntegrity()` / `setHideIntegrity()` | ❌ Removed |
| `DashboardBottomBar.kt` | Invisible spacer rendering empty UI | ❌ Deleted |
| Bug report button | Crashed app with empty URL `ACTION_VIEW` intent | ❌ Removed |

</details>

---

## ⚠️ Known Limitations

| Limitation | Details |
|---|---|
| **Frozen API headers** | Sends `X-Zomato-App-Version: 931` (v19.3.1). If Zomato deprecates this version, headers need updating from a newer Zomato APK. |
| **Plaintext tokens** | Session tokens are in SharedPreferences (plaintext). Extractable on rooted devices. |
| **ProGuard disabled** | Release builds are not obfuscated (`minifyEnabled false`). APK is fully decompilable. |
| **3km radius** | Zomato's server-side restriction per address. Multi-address monitoring is the workaround, not a bypass. |
| **Google backup dependency** | Session persistence via cloud backup requires Google backup enabled on the device. |

---

## 🤝 Contributing

Contributions are welcome! Here's how:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

- **Android Studio** Hedgehog or newer
- **JDK 11** (set via `compileOptions` in `build.gradle`)
- **Android SDK 34** (compileSdk)
- **Min SDK 26** (Android 8.0 Oreo)

> [!NOTE]
> All UI configuration is self-contained in `assets/ui.json`. No `local.properties` configuration is needed beyond the SDK path. No external API keys or service accounts required.

---

## 📃 Disclaimer & Legal

> [!CAUTION]
> This project is intended for **educational purposes only**.
>
> - This application is **not affiliated with, endorsed by, or connected to Zomato** in any manner
> - "Zomato" and the Zomato logo are trademarks of Zomato Ltd.
> - Usage of this software is at your own discretion and risk
> - The developer assumes **no responsibility** for any account restrictions or bans imposed by the official service provider

---

## 📄 License

[MIT License](LICENSE) — see the LICENSE file for details.

This is a hard fork of [jatin-dot-py/jomato-mobile](https://github.com/jatin-dot-py/jomato-mobile) with all telemetry stripped. Maintained independently.

---

<div align="center">
<sub>Built with ❤️ and a healthy distrust of telemetry.</sub>
</div>
