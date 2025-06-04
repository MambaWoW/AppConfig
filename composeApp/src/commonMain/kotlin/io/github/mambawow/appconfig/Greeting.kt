package io.github.mambawow.appconfig

@Config
interface AppearanceConfig {
    @BooleanProperty(
        defaultValue = false,
        description = "Enable dark mode theme"
    )
    var isDarkModeEnabled: Boolean

    @StringProperty(
        key = "WelcomeMessage",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    var welcomeMessage: String

    @FloatProperty(
        defaultValue = 1.0f,
        description = "Animation scale factor"
    )
    var animationScale: Float

    @IntProperty(
        defaultValue = 2,
        description = "Logging level for internal diagnostics"
    )
    var logLevel: Int

    @OptionProperty(
        key = "Logoutput",
        description = "Target for debug logs"
    )
    var logOutputTarget: LogOutputTarget
}

@Option
sealed class LogOutputTarget(
    val value: String
) {
    @OptionItem(optionId = 0, description = "Option A")
    data object A : LogOutputTarget("A")

    @OptionItem(4, "Option B", isDefault = true)
    data object B : LogOutputTarget("B")

    @OptionItem(5, "Option C")
    data object C : LogOutputTarget("C")
}