# AppConfig 🚀

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mambawow/appconfig-lib.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.mambawow%22%20AND%20a:%22appconfig-lib%22)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/platform-android%20%7C%20ios%20%7C%20desktop%20%7C%20web-lightgrey.svg)](https://kotlinlang.org/docs/multiplatform.html)

A powerful, type-safe configuration management library for Kotlin Multiplatform that transforms how you handle app settings with zero boilerplate code.

---

## ✨ Features

- 🎯 **Zero Boilerplate**: Define configurations with simple annotations
- 🔒 **Type-Safe**: Compile-time validation and type safety
- 🌍 **Multiplatform**: Works on Android, iOS
- 🎨 **Auto-Generated Admin UI**: Built-in configuration panel for easy testing
- 📱 **Convention over Configuration**: Sensible defaults with minimal setup

## 🚀 Quick Start

### 1. Add Dependencies

```kotlin
// build.gradle.kts (Module level)
plugins {
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"
}

dependencies {
    implementation("io.github.mambawow:appconfig-lib:0.0.2-SNAPSHOT")
    implementation("io.github.mambawow:appconfig-annotation:0.0.2-SNAPSHOT")
    ksp("io.github.mambawow:appconfig-processor:0.0.2-SNAPSHOT")
    
    // Optional: Admin UI panel (Compose Multiplatform)
    implementation("io.github.mambawow:appconfig-panel:0.0.2-SNAPSHOT")
}
```

### 2. Define Your Configuration

```kotlin
@Config(groupName = "UserSettings")
interface UserSettings {
    @BooleanProperty(
        key = "dark_mode",
        defaultValue = false,
        description = "Enable dark mode theme"
    )
    var isDarkModeEnabled: Boolean
    
    @StringProperty(
        key = "api_endpoint",
        defaultValue = "https://api.example.com",
        description = "API base URL"
    )
    var apiEndpoint: String
    
    @IntProperty(
        key = "timeout_seconds",
        defaultValue = 30,
        description = "Network timeout in seconds"
    )
    var timeoutSeconds: Int
    
    @OptionProperty(
        key = "log_level",
        description = "Application log level"
    )
    var logLevel: LogLevel
}

@Option
sealed class LogLevel {
    @OptionItem(optionId = 0, description = "Debug", isDefault = true)
    object Debug : LogLevel()
    
    @OptionItem(optionId = 1, description = "Info")
    object Info : LogLevel()
    
    @OptionItem(optionId = 2, description = "Error")
    object Error : LogLevel()
}
```

### 3. Use Your Configuration

```kotlin
// Access your configuration anywhere in your app
val settings = AppConfig.usersettings

// Read values
println("Dark mode: ${settings.isDarkModeEnabled}")
println("API: ${settings.apiEndpoint}")
println("Timeout: ${settings.timeoutSeconds}")
println("Log level: ${settings.logLevel}")

// Update values (automatically persisted)
settings.isDarkModeEnabled = true
settings.timeoutSeconds = 60
settings.logLevel = LogLevel.Error
```

### 4. Add Admin Panel (Optional)

```kotlin
@Composable
fun DebugScreen() {
    if (BuildConfig.DEBUG) {
        ConfigPanel(
            configItems = AppConfig.getAllConfigItems()
        )
    }
}
```

## 📱 Supported Types

AppConfig supports all common configuration types:

| Annotation | Kotlin Type | Description |
|------------|-------------|-------------|
| `@StringProperty` | `String` | Text values |
| `@BooleanProperty` | `Boolean` | True/false toggles |
| `@IntProperty` | `Int` | 32-bit integers |
| `@LongProperty` | `Long` | 64-bit integers |
| `@FloatProperty` | `Float` | 32-bit floating point |
| `@DoubleProperty` | `Double` | 64-bit floating point |
| `@OptionProperty` | `Sealed Class` | Enum-like choices |

## 🎯 Advanced Examples

### Feature Flags

```kotlin
@Config(groupName = "FeatureFlags")
interface FeatureFlags {
    @BooleanProperty(defaultValue = false)
    var enableBetaFeatures: Boolean
    
    @BooleanProperty(defaultValue = true)
    var enableAnalytics: Boolean
    
    @BooleanProperty(defaultValue = false)
    var showDebugInfo: Boolean
}

// Usage
val features = AppConfig.featureflags
if (features.enableBetaFeatures) {
    showBetaUI()
}
```

### Custom Storage Backend

```kotlin
// Use your own storage implementation
AppConfig.configure { groupName ->
    CustomConfigStore(groupName, yourCustomStorage)
}
```

## 🏗️ Architecture

AppConfig uses Kotlin Symbol Processing (KSP) for compile-time code generation:

```
┌─────────────────┐    KSP     ┌──────────────────┐
│ @Config         │ ────────→  │ Generated        │
│ Interface       │            │ Implementation   │
└─────────────────┘            └──────────────────┘
                                        │
                                        ▼
┌─────────────────┐            ┌──────────────────┐
│ Platform        │ ←──────────│ ConfigStore      │
│ Storage         │            │ Abstraction      │
└─────────────────┘            └──────────────────┘
```

**Key Components:**
- **Annotations**: Define configuration schema
- **Processor**: Generates implementation code at compile-time
- **ConfigStore**: Abstracts platform-specific storage
- **AppConfig**: Main API for accessing configurations

## 🌍 Platform Support

| Platform | Storage Backend | Status |
|----------|----------------|--------|
| Android | SharedPreferences | ✅ Supported |
| iOS | NSUserDefaults | ✅ Supported |

## 📦 Modules

- **appconfig-annotation**: Core annotations and data types
- **appconfig-processor**: KSP processor for code generation
- **appconfig-lib**: Runtime library and core functionality
- **appconfig-panel**: Optional Compose Multiplatform admin UI
- **appconfig-gradle-plugin**: Build tools and utilities

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Reporting Issues

Please use our [Issue Tracker](https://github.com/MambaWoW/AppConfig/issues) to report bugs or request features.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- Code generation powered by [KSP](https://github.com/google/ksp)
- UI components built with [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
---

## 📈 Roadmap

- [ ] Support for custom serialization formats (JSON, TOML, etc.)
- [ ] Integration with remote configuration services
- [ ] Import/export functionality

---

**Star ⭐ this repository if you find AppConfig useful!**

Made with ❤️ for the Kotlin Multiplatform community.
