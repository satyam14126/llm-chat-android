# Build Guide - Enhanced LLM Chat Client

This guide covers building the enhanced LLM Chat Client locally and on GitHub Actions.

## 🚀 Quick Start - GitHub Actions (Recommended)

The easiest way to build the app is to use GitHub Actions, which has all dependencies pre-installed.

### Automatic Builds

The app automatically builds on:
- **Push** to `main` branch
- **Push** to `enhanced-v1.0` branch
- **Pull requests** to `main` branch

### Manual Build Trigger

1. Go to: https://github.com/satyam14126/llm-chat-android/actions
2. Select "Build Android APK" workflow
3. Click "Run workflow"
4. Select the branch and click "Run workflow"

### Download APKs

After the build completes:
1. Click the workflow run
2. Scroll to "Artifacts"
3. Download:
   - `app-debug-apk` - Debug APK for testing
   - `app-release-apk` - Release APK for distribution

---

## 💻 Local Build Setup

### Prerequisites

**Required Software:**
- Android Studio (Ladybug 2024.2.1 or newer)
- JDK 21 (OpenJDK or Oracle)
- Android SDK API 35
- Gradle 9.4.1 (included with Android Studio)

**System Requirements:**
- 8GB RAM minimum (16GB recommended)
- 10GB free disk space
- Ubuntu 20.04+ / macOS 10.14+ / Windows 10+

### Installation Steps

#### 1. Install Android Studio

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install android-studio
```

**macOS:**
```bash
brew install android-studio
```

**Windows:**
Download from: https://developer.android.com/studio

#### 2. Install JDK 21

**Ubuntu/Debian:**
```bash
sudo apt-get install openjdk-21-jdk
```

**macOS:**
```bash
brew install openjdk@21
```

**Windows:**
Download from: https://www.oracle.com/java/technologies/downloads/

#### 3. Configure Android SDK

1. Open Android Studio
2. Go to: Settings → SDK Manager
3. Install:
   - Android SDK Platform 35
   - Android SDK Build-Tools 35.x.x
   - Android Emulator (optional)
   - Android SDK Platform-Tools

#### 4. Set Environment Variables

**Ubuntu/Debian/macOS:**
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

Add to `~/.bashrc` or `~/.zshrc` for persistence.

**Windows:**
1. Right-click "This PC" → Properties
2. Click "Advanced system settings"
3. Click "Environment Variables"
4. Add `ANDROID_HOME` = `C:\Users\YourUsername\AppData\Local\Android\Sdk`
5. Add to PATH: `%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools`

### Build Commands

#### Clean Build
```bash
./gradlew clean assembleDebug
```

#### Debug APK
```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

#### Release APK
```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

#### Run Tests
```bash
./gradlew test
```

#### Run on Emulator/Device
```bash
./gradlew installDebug
./gradlew runDebug
```

#### View Build Report
```bash
./gradlew build --scan
```

---

## 🐛 Troubleshooting

### Build Errors

#### Error: "SDK location not found"
**Solution:**
```bash
# Create local.properties file
echo "sdk.dir=$ANDROID_HOME" > local.properties

# Or set ANDROID_HOME environment variable
export ANDROID_HOME=$HOME/Android/Sdk
```

#### Error: "Could not find com.android.tools.build:gradle:8.7.3"
**Solution:**
- Update Android Studio to latest version
- Run: `./gradlew --refresh-dependencies`

#### Error: "Compilation error. See log for more details"
**Solution:**
```bash
# Run with verbose output
./gradlew assembleDebug --info

# Or with stack trace
./gradlew assembleDebug --stacktrace
```

#### Error: "Gradle version 9.4.1 is required"
**Solution:**
- The project uses Gradle 9.4.1
- Android Studio will download it automatically
- Or manually: `./gradlew wrapper --gradle-version 9.4.1`

### Memory Issues

If you get "Out of memory" errors:

**Increase Gradle heap size in `gradle.properties`:**
```properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
```

### Slow Builds

**Speed up builds:**
```bash
# Enable build cache
org.gradle.caching=true

# Enable parallel builds
org.gradle.parallel=true

# Use daemon
org.gradle.daemon=true
```

---

## 📊 Build Configuration

### Current Configuration

| Setting | Value |
|---------|-------|
| Gradle Version | 9.4.1 |
| JDK Version | 21 |
| Kotlin Version | 2.3.0 |
| Android SDK | 35 |
| Min SDK | 26 |
| Target SDK | 35 |
| Compile SDK | 35 |

### Gradle Properties

Located in `gradle.properties`:
```properties
android.useAndroidX=true
android.enableJetifier=true
org.gradle.jvmargs=-Xmx1536m -XX:MaxMetaspaceSize=512m
org.gradle.daemon=false
kotlin.compiler.execution.strategy=in-process
org.gradle.java.installations.paths=/usr/lib/jvm/java-21-openjdk-amd64
```

### Build Variants

**Debug Build:**
- Debuggable
- Unoptimized
- Faster build time
- Larger APK size

**Release Build:**
- Not debuggable
- Optimized with ProGuard
- Slower build time
- Smaller APK size
- Requires signing

---

## 🔐 Signing Release APK

### Generate Signing Key

```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

### Configure Signing

Edit `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("path/to/my-release-key.jks")
        storePassword = "your-password"
        keyAlias = "my-key-alias"
        keyPassword = "your-password"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
    }
}
```

### Build Signed Release APK

```bash
./gradlew assembleRelease
```

---

## 📦 Artifact Output

### Debug APK
- **Location**: `app/build/outputs/apk/debug/app-debug.apk`
- **Size**: ~50-80 MB
- **Use**: Testing on devices/emulators
- **Installation**: `adb install app-debug.apk`

### Release APK
- **Location**: `app/build/outputs/apk/release/app-release.apk`
- **Size**: ~30-50 MB
- **Use**: Distribution on Play Store
- **Installation**: `adb install app-release.apk`

### Test Reports
- **Location**: `app/build/reports/tests/`
- **Format**: HTML
- **View**: Open `index.html` in browser

---

## 🔄 CI/CD with GitHub Actions

### Workflow File

Located at: `.github/workflows/android.yml`

**Triggers:**
- Push to `main` branch
- Push to `enhanced-v1.0` branch
- Pull requests to `main`
- Manual trigger (workflow_dispatch)

**Steps:**
1. Checkout code
2. Set up Java 21
3. Restore debug keystore
4. Build debug APK
5. Build release APK
6. Run tests
7. Upload artifacts

### Secrets Required

The workflow requires these GitHub secrets:
- `ANDROID_DEBUG_KEYSTORE` - Base64 encoded debug keystore
- `ANDROID_KEY_ALIAS` - Key alias (default: androiddebugkey)
- `ANDROID_KEYSTORE_PASSWORD` - Keystore password (default: android)

### View Build Logs

1. Go to: https://github.com/satyam14126/llm-chat-android/actions
2. Click the workflow run
3. Click "Build Android APK" job
4. Expand steps to view logs

---

## 📈 Performance Tips

### Faster Builds

1. **Use Build Cache**
   ```bash
   org.gradle.caching=true
   ```

2. **Enable Parallel Builds**
   ```bash
   org.gradle.parallel=true
   ```

3. **Increase Heap Size**
   ```bash
   org.gradle.jvmargs=-Xmx2048m
   ```

4. **Use Daemon**
   ```bash
   org.gradle.daemon=true
   ```

5. **Skip Tests**
   ```bash
   ./gradlew assembleDebug -x test
   ```

### Smaller APK

1. **Enable ProGuard** (Release only)
   ```kotlin
   isMinifyEnabled = true
   ```

2. **Remove Unused Resources**
   ```kotlin
   isShrinkResources = true
   ```

3. **Use App Bundle**
   ```bash
   ./gradlew bundleRelease
   ```

---

## 📚 Resources

- [Android Build System](https://developer.android.com/build)
- [Gradle Documentation](https://docs.gradle.org/)
- [Kotlin Compiler Options](https://kotlinlang.org/docs/gradle-compiler-options.html)
- [GitHub Actions for Android](https://github.com/actions/setup-java)

---

## ✅ Build Checklist

- [ ] Android Studio installed
- [ ] JDK 21 installed
- [ ] Android SDK API 35 installed
- [ ] ANDROID_HOME environment variable set
- [ ] `local.properties` created
- [ ] `gradle.properties` configured
- [ ] Clean build successful
- [ ] Debug APK generated
- [ ] Release APK generated
- [ ] Tests passing
- [ ] GitHub Actions workflow running

---

**Last Updated**: June 15, 2024  
**Version**: 1.0 Enhanced

For issues or questions, refer to the troubleshooting section or check GitHub Actions logs.
