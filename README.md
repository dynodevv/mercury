# Mercury

Mercury is a feature-rich, AI-centric Android web browser built with Kotlin, Jetpack Compose, and Material 3 Expressive. It brings artificial intelligence directly into your browsing experience while respecting your privacy.

## Features

- **Modern Material 3 Expressive UI** — Dynamic color, motion physics, and alive animations
- **Talk to Your Websites** — Chat with an AI that has full context of the current webpage
- **AI-Powered Search** — Search with DuckDuckGo and get real-time AI summaries with cited sources
- **BYOK AI** — Bring Your Own Key. Connect your OpenAI, Gemini, Groq, or any OpenAI-compatible API
- **Ad & Tracker Blocking** — Built-in OISD full blocklist support
- **Customizable** — Light/dark/theme, homepage, search engine, and more
- **Tab Management** — Multi-tab browsing with state persistence
- **History & Bookmarks** — Local Room database storage

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 Expressive
- **DI:** Hilt
- **Navigation:** Jetpack Navigation Compose
- **Database:** Room (History & Bookmarks)
- **Settings:** DataStore Preferences
- **Networking:** Ktor Client
- **HTML Parsing:** Jsoup
- **Browser Engine:** Android WebView

## Building Locally

This project uses Gradle 8.9. You can build locally if you have:

- Android Studio Ladybug or newer
- JDK 17
- Android SDK (compileSdk 35)

```bash
gradle assembleDebug
```

## Release Signing (GitHub Actions)

Release APKs are built and signed automatically via GitHub Actions. To set this up:

### 1. Generate a Keystore

If you don't have one, generate it locally:

```bash
keytool -genkey -v -keystore mercury-release.keystore -alias mercury -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Encode the Keystore for GitHub Secrets

```bash
base64 -w 0 mercury-release.keystore > keystore.b64
```

### 3. Add Secrets to Your GitHub Repository

Go to **Settings > Secrets and variables > Actions > New repository secret** and add:

| Secret Name | Value |
|-------------|-------|
| `KEYSTORE_BASE64` | The entire contents of `keystore.b64` |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | `mercury` (or whatever alias you chose) |
| `KEY_PASSWORD` | Your key password (can be same as keystore password) |

### 4. Trigger a Release

Push a version tag to automatically build and release a signed APK:

```bash
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions will build a signed release APK and attach it to the GitHub Release.

**Important:** Do not commit `mercury-release.keystore` or `keystore.b64` to the repository. Keep them private.

## License

See [LICENSE](./LICENSE).
