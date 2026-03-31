---
description: How to build the Jomato APK from source on Windows
---

# Build Jomato APK from Source (Windows)

## Prerequisites (One-Time Setup)

### Step 1: Install Android Studio

Download and install **Android Studio** from:
https://developer.android.com/studio

During installation:
- ✅ Keep all default options checked
- ✅ Let it install the Android SDK (default location: `C:\Users\<you>\AppData\Local\Android\Sdk`)
- ✅ Let it download SDK Platform 34 (API 34)

Android Studio automatically installs Java (JDK 17) bundled with it — you do NOT need to install Java separately.

> **Time estimate**: ~20-30 minutes (large download, ~1.2 GB)

### Step 2: Open the Project in Android Studio

1. Launch Android Studio
2. Click **"Open"** (not "New Project")
3. Navigate to `D:\dev\lab\jomato-mobile` and select the folder
4. Click **OK**

Android Studio will:
- Detect the Gradle project automatically
- Start downloading all dependencies (first time takes 5-10 minutes)
- You'll see a progress bar at the bottom

### Step 3: Wait for Gradle Sync

- A bar at the bottom will say **"Gradle sync..."**
- Wait until it says **"BUILD SUCCESSFUL"** or the progress bar disappears
- If you see errors about `local.properties`, the file has already been created for you (see below)

---

## Building the APK

### Option A: Via Android Studio UI (Recommended for Beginners)

1. In the menu bar: **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. Wait for the build to complete (1-3 minutes first time)
3. A notification will appear: **"APK(s) generated successfully"**
4. Click **"locate"** to open the folder containing your APK

The APK will be at:
```
D:\dev\lab\jomato-mobile\app\build\outputs\apk\debug\app-debug.apk
```

### Option B: Via Terminal (Command Line)

Open a terminal in Android Studio (View → Tool Windows → Terminal) and run:

```bash
# Debug build (unsigned, for testing)
./gradlew assembleDebug

# Release build (unsigned)
./gradlew assembleRelease
```

Output locations:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## Installing on Your Phone

### Method 1: USB Cable
1. On your Android phone: **Settings** → **About Phone** → Tap **Build Number** 7 times to enable Developer Mode
2. Go to **Settings** → **Developer Options** → Enable **USB Debugging**
3. Connect phone via USB
4. In Android Studio, click the green **▶ Run** button (top toolbar)
5. Select your phone from the device list
6. The app will install and launch automatically

### Method 2: Transfer APK
1. Copy the built APK to your phone (USB, email, cloud storage, etc.)
2. Open the APK file on your phone
3. Allow "Install from unknown sources" when prompted
4. Install and open

---

## Troubleshooting

### "SDK location not found"
The `local.properties` file should already exist. If not, create it in the project root:
```properties
sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
```

### "local.properties: UI_JSON_HOST not found"
The `local.properties` file must contain the UI config hosts. They've been pre-configured.

### "Gradle sync failed"
1. File → Invalidate Caches → Restart
2. After restart, let Gradle sync again

### Build takes very long
First build downloads ~500MB of dependencies. Subsequent builds are much faster (30-60 seconds).
