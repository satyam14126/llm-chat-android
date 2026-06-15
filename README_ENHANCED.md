# LLM Chat Client - Enhanced Edition

A powerful, feature-rich Android chat application for interacting with multiple LLM providers. This enhanced version includes modern UI/UX improvements, advanced features, and better performance.

## 🌟 What's New in the Enhanced Version

This enhanced edition builds upon the solid foundation of the original LLM Chat Client with significant improvements:

### Visual Enhancements
- **4 Beautiful Color Themes**: Default, Ocean, Forest, and Sunset themes with light and dark variants
- **Improved Message Bubbles**: Enhanced shadows, code block highlighting, and better animations
- **Modern UI Components**: New search bars, quick reply buttons, error states, and info banners
- **Better Visual Hierarchy**: Improved spacing, typography, and component organization

### Advanced Features
- **Message Search Utility**: Comprehensive search functionality with context and snippets
- **Quick Reply Manager**: 10 pre-defined quick reply templates for faster interaction
- **Code Highlighting**: Automatic syntax highlighting for code blocks in messages
- **Enhanced Settings**: Theme selection, animation toggle, and auto-scroll preferences

### Technical Improvements
- **Reactive Architecture**: Enhanced with Flow-based state management
- **Better Error Handling**: Improved error display and recovery flows
- **Performance Optimized**: Efficient rendering and lazy loading
- **Modular Design**: Self-contained, reusable components

## 🚀 Features

### Core Features
- **Multiple LLM Providers**: Support for OpenAI, Anthropic, Groq, Ollama, and more
- **Real-time Streaming**: Token-by-token responses with animated cursor
- **Chat Management**: Create, rename, delete, and search conversations
- **File Attachments**: Support for .txt, .md, .pdf, .json, .csv, and code files
- **Context Management**: Automatic warnings and summarization for token limits
- **Export/Import**: Share and restore conversations in JSON, Markdown, or text format

### Enhanced Features
- **Theme Customization**: Choose from 4 beautiful color themes
- **Message Search**: Find messages across conversations
- **Quick Replies**: Fast access to common prompts
- **Code Highlighting**: Beautiful code block formatting
- **Accessibility**: Animation toggle for users with motion sensitivity

## 📱 Screenshots & UI

### Themes Available
| Theme | Description |
|-------|-------------|
| Default | Modern blue theme with Material 3 design |
| Ocean | Cool blues and teals for a calming experience |
| Forest | Green tones for a natural, earthy feel |
| Sunset | Warm oranges and reds for a vibrant experience |

### Key Screens
- **Chat Screen**: Conversations with streaming responses and message actions
- **Sessions Screen**: Manage multiple chat sessions with search
- **Settings Screen**: Configure providers, themes, and preferences
- **Provider Management**: Add and configure LLM providers

## 🛠️ Installation & Setup

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK 17**: Bundled with Android Studio
- **Android SDK**: API 35 (install via SDK Manager)
- **Minimum Device**: API 26 (Android 8.0)

### Quick Start

1. **Clone the Repository**
   ```bash
   git clone https://github.com/satyam14126/llm-chat-android.git
   cd llm-chat-android
   ```

2. **Open in Android Studio**
   - File → Open → select the project folder
   - Wait for Gradle sync to complete

3. **Configure Your First Provider**
   - Launch the app
   - Tap Settings (gear icon)
   - Tap "Add Provider Profile"
   - Enter your provider details:
     - **Name**: OpenAI (or your choice)
     - **Base URL**: `https://api.openai.com`
     - **API Key**: Your API key (e.g., `sk-...`)
     - **Model**: `gpt-4o-mini` or `gpt-4o`
     - **Streaming**: Enable for real-time responses
   - Tap Save, then Set Default
   - Start chatting!

4. **Run the App**
   - Select a device/emulator
   - Press Run (Shift+F10)

## 🔌 Supported LLM Providers

The app supports any OpenAI-compatible API. Here are popular providers:

| Provider | Base URL | Notes |
|----------|----------|-------|
| OpenAI | `https://api.openai.com` | Official OpenAI API |
| Anthropic | `https://api.anthropic.com` | Via proxy |
| Groq | `https://api.groq.com/openai` | Fast inference |
| Together AI | `https://api.together.xyz` | Open source models |
| Mistral | `https://api.mistral.ai` | European AI provider |
| Local Ollama | `http://10.0.2.2:11434` | Local models (emulator) |
| LM Studio | `http://10.0.2.2:1234` | Local models |
| OpenRouter | `https://openrouter.ai/api` | Model aggregator |
| Perplexity | `https://api.perplexity.ai` | Search-focused AI |
| Anyscale | `https://api.endpoints.anyscale.com/v1` | Ray-based inference |

## 📚 Usage Guide

### Creating a Chat
1. Tap "New Chat" on the sessions screen
2. Type your message
3. Press send to start the conversation
4. The AI will respond with streaming text

### Message Actions
- **Tap a message** to reveal action buttons:
  - **Copy**: Copy message to clipboard
  - **Edit** (user messages): Edit and resend
  - **Regenerate** (AI messages): Get a new response
  - **Delete**: Remove the message

### File Attachments
1. Tap the attachment icon in the input area
2. Select a file (.txt, .md, .pdf, .json, .csv, or code)
3. The file content is automatically extracted and included as context

### Search Messages
- Use the search functionality to find messages across conversations
- Results show snippets with context

### Quick Replies
- Tap quick reply buttons for common prompts
- Templates include: Explain, Summarize, Expand, Example, Code, etc.

### Customize Appearance
1. Go to Settings
2. Select your preferred theme
3. Toggle animations if needed
4. Changes apply immediately

## 🏗️ Project Architecture

The app follows clean architecture principles with MVVM + Repository pattern:

```
UI (Compose) → ViewModel → UseCase → Repository → [Room DB | Retrofit | OkHttp]
                                 ↘ Hilt DI wires everything
```

### Key Components

| Component | Purpose |
|-----------|---------|
| **UI Layer** | Jetpack Compose screens and components |
| **ViewModel** | State management and business logic |
| **Repository** | Data abstraction and aggregation |
| **UseCase** | Complex business logic orchestration |
| **Local DB** | Room database for message and session storage |
| **Remote API** | Retrofit + OkHttp for LLM provider communication |
| **DI** | Hilt for dependency injection |

### Key Files

```
app/src/main/java/com/llmchat/app/
├── data/
│   ├── local/                    # Room database
│   ├── remote/                   # API clients
│   └── repository/               # Data repositories
├── domain/
│   ├── model/                    # Domain models
│   └── usecase/                  # Business logic
├── ui/
│   ├── chat/                     # Chat screen
│   ├── sessions/                 # Sessions list
│   ├── settings/                 # Settings screen
│   ├── theme/                    # Theme system
│   └── common/                   # Shared components
└── util/
    ├── SearchUtil.kt             # Message search
    ├── QuickReplyManager.kt       # Quick replies
    └── FileExtractor.kt           # File handling
```

## 🎨 Theming System

The enhanced theme system allows users to personalize their experience:

### Available Themes
- **Default**: Original blue theme
- **Ocean**: Cool blues and teals
- **Forest**: Natural green tones
- **Sunset**: Warm oranges and reds

### Each Theme Includes
- Light mode variant
- Dark mode variant
- Optimized contrast and readability
- Consistent Material 3 design

### Customization
Users can switch themes in Settings → Appearance → Theme.

## 🔒 Security & Privacy

- **Local Storage**: All data is stored locally on your device
- **API Keys**: Stored securely in the Room database
- **No Cloud Sync**: Your conversations stay on your device
- **No Tracking**: No telemetry or analytics
- **Open Source**: Full transparency of code and functionality

### Recommendations
- Use strong API keys
- Keep your device updated
- Consider enabling device encryption
- Review provider privacy policies

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Gradle sync fails | Check internet, update Android Studio, File → Invalidate Caches |
| "No provider configured" | Add a provider in Settings and set as default |
| Streaming not working | Some providers don't support SSE; disable streaming in profile |
| Ollama connection refused | Use your machine's LAN IP, not localhost; ensure `OLLAMA_HOST=0.0.0.0` |
| PDF extraction fails | PDFBox may not support all PDF versions; try a different file |
| Rate limit errors | Wait and retry; consider using a different provider |
| Theme not changing | Restart the app; ensure DataStore is initialized |

## 📖 Documentation

- **SETUP.md**: Original setup and configuration guide
- **ENHANCEMENTS.md**: Detailed list of all enhancements
- **IMPLEMENTATION_GUIDE.md**: Developer guide for using new features

## 🚀 Building a Release APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

You'll need to set up a signing keystore. See [Android docs](https://developer.android.com/studio/publish/app-signing).

## 🤝 Contributing

Contributions are welcome! Feel free to:
- Report bugs
- Suggest features
- Submit pull requests
- Improve documentation

## 📝 License

This project is provided as-is. Check the original repository for license information.

## 🙏 Credits

- **Original Developer**: [satyam14126](https://github.com/satyam14126)
- **Enhanced By**: Manus AI Assistant
- **Built With**: Kotlin, Jetpack Compose, Room, Retrofit, Hilt, Coroutines

## 📞 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review the documentation files
3. Check the original repository for existing issues
4. Open a new issue with detailed information

## 🎯 Roadmap

Future enhancements being considered:
- Voice input/output (speech-to-text, text-to-speech)
- Message reactions and emojis
- Conversation bookmarks
- Better export formatting
- Offline message caching
- Cloud synchronization
- Custom theme creation
- Message pinning
- Conversation templates
- Usage analytics

---

**Version**: 1.0 Enhanced  
**Last Updated**: June 2024  
**Status**: Production Ready

Enjoy your enhanced LLM Chat Client! 🚀
