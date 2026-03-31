# Jomato — Zero Telemetry Fork

<div align="center">

### The Unofficial Zomato Client — Privacy-First Edition
**Built from source with all telemetry, analytics, and remote update mechanisms removed.**

</div>

> [!IMPORTANT]
> This is a **hard fork** of [jatin-dot-py/jomato-mobile](https://github.com/jatin-dot-py/jomato-mobile).
> All analytics, tracking, and remote APK update capabilities have been stripped. See [What Was Removed](#what-was-removed) for details.

---

## About

Jomato is an unofficial, reverse-engineered client for Zomato that delivers utility features not available in the official app. This fork removes all phone-home telemetry from the original project while keeping full functionality.

## Features

- **Instant Food Rescue Notifications**: Monitors Food Rescue events in real time within your area. Delivers immediate alerts and allows direct access via notification tap.

---

## What Was Removed

| Component | Original Behavior | This Fork |
|---|---|---|
| **App-open analytics** | Sent install ID, app version, Android version to developer's server on every launch | ❌ Removed |
| **Order tracking** | Sent order ID, cart total, and amount paid to developer's server on Food Rescue claim | ❌ Removed |
| **Install ID generation** | Created a persistent 16-char hex tracking ID on first launch | ❌ Removed |
| **Remote APK updater** | Could download and sideload APKs from developer's server | ❌ Removed |
| **`REQUEST_INSTALL_PACKAGES`** | Android permission allowing APK sideloading | ❌ Removed |
| **`FileProvider`** | Used by the updater to serve downloaded APKs to the installer | ❌ Removed |

---

## Privacy

**This fork has zero telemetry. No data leaves your device.**

- No analytics pings of any kind
- No install ID generation or tracking
- No order/cart data exfiltration
- No remote update mechanism
- All user data (sessions, preferences) stays on-device in local storage
- The app only communicates with Zomato's own servers for login and Food Rescue monitoring

---

## How to Install

### Option A: Download the APK

- Navigate to the [Releases Page](../../releases) of this fork
- Download the latest APK and install it on your device
- You will see a Play Protect warning — this is expected for apps not on the Play Store

### Option B: Build from Source

1. Clone this repository
2. Open the project in **Android Studio**
3. Android Studio will automatically create `local.properties` with your SDK path
4. Let Gradle sync complete
5. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
6. Or from terminal: `./gradlew assembleRelease`

> **Note:** All UI configuration is bundled locally — the app makes zero network calls to any config server.

---

## Setup After Install

1. Open the app and log in with your Zomato phone number + OTP
2. Tap on the **Food Rescue widget** on the home screen
3. Select your **location** to monitor
4. Allow **notification permissions** when prompted
5. Disable **battery optimization** for reliable background notifications
6. Done! You'll receive instant alerts for Food Rescue deals near you.

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
