# GitHub Actions Build Guide

This guide explains how to use GitHub Actions to automatically build your enhanced LLM Chat Client.

## 🚀 Quick Start

GitHub Actions automatically builds your app whenever you push code. No additional setup needed!

### Automatic Builds

The workflow automatically triggers on:
- **Push** to `main` branch
- **Push** to `enhanced-v1.0` branch  
- **Pull requests** to `main` branch

### Manual Trigger

To manually trigger a build:

1. Go to: https://github.com/satyam14126/llm-chat-android/actions
2. Click "Build Android APK" workflow
3. Click "Run workflow" button
4. Select the branch
5. Click "Run workflow"

## 📊 Build Workflow

The GitHub Actions workflow performs these steps:

```
1. Checkout code
2. Set up Java 17 (Temurin)
3. Restore debug keystore
4. Verify keystore
5. Make Gradle executable
6. Build debug APK
7. Build release APK
8. Run tests (non-blocking)
9. Upload debug APK artifact
10. Upload release APK artifact
11. Upload test reports (if available)
```

## 📦 Download Artifacts

After the build completes:

1. Go to: https://github.com/satyam14126/llm-chat-android/actions
2. Click the workflow run
3. Scroll to "Artifacts" section
4. Download:
   - `app-debug-apk` - Debug APK for testing
   - `app-release-apk` - Release APK for distribution
   - `test-reports` - Test execution reports (if available)

## 🔐 Secrets Configuration

The workflow requires these GitHub secrets to be configured:

| Secret | Value | Description |
|--------|-------|-------------|
| `ANDROID_DEBUG_KEYSTORE` | Base64 encoded keystore | Debug keystore for signing |
| `ANDROID_KEY_ALIAS` | `androiddebugkey` | Key alias in keystore |
| `ANDROID_KEYSTORE_PASSWORD` | `android` | Keystore password |

### How to Add Secrets

1. Go to: https://github.com/satyam14126/llm-chat-android/settings/secrets/actions
2. Click "New repository secret"
3. Add each secret with its value

### Encoding Keystore as Base64

```bash
# Create base64 encoded keystore
base64 ~/.android/debug.keystore | tr -d '\n' > keystore.base64

# Copy the output to GitHub secret
cat keystore.base64
```

## 📋 Workflow File

Location: `.github/workflows/android.yml`

### Key Configuration

```yaml
name: Build Android APK

on:
  workflow_dispatch:
  push:
    branches: [ main, enhanced-v1.0 ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    # ... build steps ...
```

### Build Steps

**Checkout:**
```yaml
- uses: actions/checkout@v4
```

**Java Setup:**
```yaml
- uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: '17'
    cache: gradle
```

**Build:**
```yaml
- run: ./gradlew assembleDebug
- run: ./gradlew assembleRelease
```

**Upload Artifacts:**
```yaml
- uses: actions/upload-artifact@v4
  with:
    name: app-debug-apk
    path: app/build/outputs/apk/debug/*.apk
```

## 🔍 Viewing Build Logs

### View Workflow Run

1. Go to: https://github.com/satyam14126/llm-chat-android/actions
2. Click the workflow run
3. Click "Build Android APK" job
4. Expand steps to view logs

### Common Log Sections

**Checkout:**
```
Fetching the repository
Cloning the full history
```

**Java Setup:**
```
Resolving Java 17 (temurin)
Setting up Java environment
```

**Build:**
```
Downloading Gradle
Compiling Kotlin
Building APK
```

**Upload:**
```
Uploading artifacts
```

## 🐛 Troubleshooting

### Build Failed

**Check logs:**
1. Go to the workflow run
2. Click "Build Android APK" job
3. Look for red "X" marks
4. Expand the failed step
5. Read the error message

**Common Errors:**

| Error | Cause | Solution |
|-------|-------|----------|
| "SDK location not found" | Android SDK not configured | Not applicable to GitHub Actions (has SDK) |
| "Keystore not found" | Missing secrets | Configure GitHub secrets |
| "Compilation error" | Code syntax error | Fix the Kotlin code and push |
| "Gradle sync failed" | Dependency issue | Check gradle.properties and build.gradle.kts |

### Keystore Errors

**Error:** "Failed to verify keystore"

**Solution:**
1. Verify secrets are configured correctly
2. Re-encode the keystore:
   ```bash
   base64 ~/.android/debug.keystore | tr -d '\n'
   ```
3. Update the `ANDROID_DEBUG_KEYSTORE` secret

### Timeout Errors

**Error:** "The operation timed out"

**Solution:**
- GitHub Actions has a 6-hour timeout
- Large projects may need optimization
- Check gradle.properties for memory settings

## 📈 Performance Optimization

### Faster Builds

**Enable Gradle cache:**
```yaml
- uses: actions/setup-java@v4
  with:
    cache: gradle  # Caches Gradle dependencies
```

**Parallel builds:**
```properties
# In gradle.properties
org.gradle.parallel=true
```

**Incremental compilation:**
```properties
# In gradle.properties
org.gradle.caching=true
```

### Build Time Estimates

| Build Type | Time | Notes |
|-----------|------|-------|
| First build | 10-15 min | Downloads all dependencies |
| Cached build | 5-8 min | Uses cached dependencies |
| Incremental | 3-5 min | Only changed files compiled |

## 🔄 Continuous Integration

### PR Checks

Builds automatically run on pull requests:
1. Code is checked out
2. Build is executed
3. Tests are run
4. Results shown in PR

### Merge Protection

You can require successful builds before merging:
1. Go to: Settings → Branches → Branch protection rules
2. Require status checks to pass before merging
3. Select "Build Android APK" workflow

## 📊 Build Status Badge

Add a build status badge to your README:

```markdown
[![Build Status](https://github.com/satyam14126/llm-chat-android/workflows/Build%20Android%20APK/badge.svg)](https://github.com/satyam14126/llm-chat-android/actions)
```

## 🚀 Release Workflow

### Automated Releases

To create automated releases:

1. Create a new workflow file: `.github/workflows/release.yml`
2. Trigger on tag creation
3. Build APK
4. Create GitHub Release
5. Upload APK as release asset

### Manual Release

1. Build APK via GitHub Actions
2. Download APK from artifacts
3. Go to: https://github.com/satyam14126/llm-chat-android/releases
4. Create new release
5. Upload APK
6. Publish release

## 📚 Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android Build System](https://developer.android.com/build)
- [Gradle Documentation](https://docs.gradle.org/)
- [Setup Java Action](https://github.com/actions/setup-java)
- [Upload Artifact Action](https://github.com/actions/upload-artifact)

## ✅ Checklist

- [ ] GitHub secrets configured
- [ ] Workflow file present at `.github/workflows/android.yml`
- [ ] Code pushed to main branch
- [ ] Build triggered automatically or manually
- [ ] APK downloaded from artifacts
- [ ] APK tested on device
- [ ] Release created with APK

## 📝 Notes

- GitHub Actions is free for public repositories
- Private repositories get 2,000 free minutes per month
- Builds run on Ubuntu Linux
- Android SDK is pre-installed on runners
- Java 17 is used for compilation
- APKs are available for 90 days

---

**Last Updated**: June 15, 2024  
**Version**: 1.0 Enhanced

For issues or questions, check the troubleshooting section or GitHub Actions documentation.
