# LLM Chat Client — Android Setup Guide

## Prerequisites

- **Android Studio Ladybug (2024.2.1) or newer**
- **JDK 17** (bundled with Android Studio)
- **Android SDK API 35** (install via SDK Manager)
- **Minimum device/emulator: API 26 (Android 8.0)**

---

## Quick Start

### 1. Open in Android Studio

```
File → Open → select the `LLMChat` folder
```

Wait for Gradle sync to complete (first sync downloads dependencies, ~1-2 min).

### 2. Run the app

- Select a device/emulator
- Press **Run ▶** or `Shift+F10`

### 3. Add your first provider

1. Tap **Settings** (gear icon) on the sessions screen
2. Tap **Add Provider Profile**
3. Fill in:
   - **Name**: OpenAI (or any label)
   - **Base URL**: `https://api.openai.com`
   - **API Key**: your `sk-...` key
   - **Model**: `gpt-4o-mini` (or `gpt-4o`)
   - **Streaming**: enable for real-time responses
4. Tap **Save**, then **Set Default**
5. Go back and tap **New Chat**

---

## Supported Providers (OpenAI-compatible)

| Provider | Base URL |
|----------|----------|
| OpenAI | `https://api.openai.com` |
| Anthropic (via proxy) | `https://api.anthropic.com` |
| Groq | `https://api.groq.com/openai` |
| Together AI | `https://api.together.xyz` |
| Mistral | `https://api.mistral.ai` |
| Local Ollama | `http://10.0.2.2:11434` (emulator) / `http://192.168.x.x:11434` (device) |
| LM Studio | `http://10.0.2.2:1234` |
| OpenRouter | `https://openrouter.ai/api` |
| Perplexity | `https://api.perplexity.ai` |
| Anyscale | `https://api.endpoints.anyscale.com/v1` |

> **Note for Ollama on device**: Use your computer's LAN IP, not `localhost`.
> Ollama must be started with `OLLAMA_HOST=0.0.0.0 ollama serve`.

---

## Features Overview

### Chat
- **Multiple sessions**: create, rename, delete, search chats
- **Streaming**: real-time token-by-token responses with animated cursor
- **Message actions**: tap a message to copy, edit, delete, or regenerate
- **File attachments**: attach .txt, .md, .pdf, .json, .csv, code files — text is extracted and injected as context
- **Context management**: automatic warning when approaching token limits; one-tap summarization of older messages
- **Auto-title**: session title is set automatically from your first message

### Settings
- **Multiple provider profiles**: configure different providers/models/settings
- **Per-profile**: API key, base URL, model, temperature, max tokens, streaming toggle, system prompt
- **Dark mode**: persistent preference

### Export / Import
- **Export**: share chat as JSON, Markdown, or plain text
- **Import**: restore a previously exported JSON backup

---

## Project Structure

```
app/src/main/java/com/llmchat/app/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database
│   │   ├── dao/                    # DAOs: sessions, messages, profiles, files
│   │   └── entities/               # Room entities
│   ├── remote/
│   │   ├── api/
│   │   │   ├── LLMApiService.kt    # Retrofit API interface (OpenAI-compatible)
│   │   │   └── StreamingApiClient.kt # SSE streaming via OkHttp
│   │   └── dto/                    # Request/response data classes
│   └── repository/                 # Repositories: Chat, Provider, LLM, Import
├── di/
│   ├── AppModule.kt                # Hilt DI: OkHttp, Room, JSON
│   └── ApiServiceFactory.kt        # Builds Retrofit per provider profile
├── domain/
│   ├── model/                      # Domain models + mappers
│   └── usecase/
│       ├── SendMessageUseCase.kt   # Orchestrates message sending + streaming
│       └── SummarizeContextUseCase.kt # Context overflow summarization
├── ui/
│   ├── navigation/AppNavigation.kt
│   ├── sessions/                   # Sessions list + ViewModel
│   ├── chat/                       # Chat screen, message bubbles + ViewModel
│   ├── settings/                   # Settings + provider management + ViewModel
│   └── common/                     # Shared UI components
└── util/
    ├── FileExtractor.kt            # Text extraction from files + relevant chunk selection
    ├── TokenCounter.kt             # Token estimation (no external API needed)
    └── ExportImportManager.kt      # Export to JSON/MD/TXT, import from JSON
```

### Architecture

```
UI (Compose) → ViewModel → UseCase → Repository → [Room DB | Retrofit | OkHttp SSE]
                                 ↘ Hilt DI wires everything
```

**Pattern**: MVVM + Repository + Clean Architecture layers + Hilt + Coroutines/Flow

---

## Adding a New LLM Provider

The API layer is provider-agnostic. To support a new OpenAI-compatible provider:

1. Open **Settings → Add Provider Profile**
2. Enter the provider's base URL and API key
3. Done — no code changes needed

For providers with non-standard APIs (e.g., custom authentication headers):

1. Modify `ApiServiceFactory.kt` to conditionally add custom headers based on the `baseUrl`
2. Add any extra DTO fields in `ChatDtos.kt`
3. Handle the response format differences in `LLMRepository.kt`

---

## Security Notes

- API keys are stored in the **local Room database** on-device (not transmitted anywhere except the target provider)
- The database is private to the app and not backed up to cloud by default (`allowBackup="true"` is for Android backup — consider setting it to `false` for maximum security)
- For production use, consider migrating API key storage to Android Keystore via EncryptedSharedPreferences

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Gradle sync fails | Check internet, update Android Studio, try File → Invalidate Caches |
| "No provider configured" | Add a provider in Settings and set it as default |
| Streaming not working | Some providers don't support SSE; disable streaming in the profile |
| Ollama connection refused | Use your machine's LAN IP, not localhost; ensure `OLLAMA_HOST=0.0.0.0` |
| PDF extraction fails | PDFBox may not support all PDF versions; try a different file |
| Rate limit errors | App shows retry button; wait and retry |

---

## Building a Release APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

You'll need to set up a signing keystore. See [Android docs](https://developer.android.com/studio/publish/app-signing).
