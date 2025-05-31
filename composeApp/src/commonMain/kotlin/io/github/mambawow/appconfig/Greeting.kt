package io.github.mambawow.appconfig

@Config(groupName = "Social2")
interface AppearanceConfig5 {
    @BooleanProperty(
        key = "Featureaab",
        defaultValue = false,
        description = "Featureaab"
    )
    val isFeature1Open: Boolean
}

@Config(groupName = "Social1")
interface AppearanceConfig4 {
    @BooleanProperty(
        key = "Featureaaa",
        defaultValue = false,
        description = "Featureaaa"
    )
    val isFeature1Open: Boolean
}

@Config(groupName = "Social")
interface AppearanceConfig3 {
    @BooleanProperty(
        key = "Feature1",
        defaultValue = false,
        description = "Enable Feature1"
    )
    val isFeature1Open: Boolean

    @StringProperty(
        key = "Feature1 type",
        defaultValue = "aaaaaa",
        description = "Feature1 type"
    )
    val feature1Type: String

    @BooleanProperty(
        key = "Feature2",
        defaultValue = false,
        description = "Enable Feature2"
    )
    val isFeature2Open: Boolean

    @StringProperty(
        key = "Feature2 type",
        defaultValue = "aaaaaa",
        description = "Feature2 type"
    )
    val feature2Type: String
}

@Config(groupName = "Appearance")
interface AppearanceConfig {
    @BooleanProperty(
        key = "DarkMode",
        defaultValue = false,
        description = "Enable dark mode theme"
    )
    val isDarkModeEnabled: Boolean

    @StringProperty(
        key = "WelcomeMessage",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage: String

    @FloatProperty(
        key = "AnimationScale",
        defaultValue = 1.0f,
        description = "Animation scale factor"
    )
    val animationScale: Float

    @IntProperty(
        key = "LogLevel",
        defaultValue = 2,
        description = "Logging level for internal diagnostics"
    )
    val logLevel: Int

    @OptionProperty(
        key = "Logoutput",
        description = "Target for debug logs"
    )
    val logOutputTarget: LogOutputTarget

    @StringProperty(
        key = "WelcomeMessage1",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage1: String

    @StringProperty(
        key = "WelcomeMessage2",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage2: String

    @StringProperty(
        key = "WelcomeMessage3",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage3: String

    @StringProperty(
        key = "WelcomeMessage4",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage4: String

    @StringProperty(
        key = "WelcomeMessage5",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage5: String

    @StringProperty(
        key = "WelcomeMessage6",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage6: String

    @StringProperty(
        key = "WelcomeMessage7",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage7: String

    @StringProperty(
        key = "WelcomeMessage8",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage8: String

    @StringProperty(
        key = "WelcomeMessage9",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage9: String

    @StringProperty(
        key = "WelcomeMessage10",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage10: String

    @StringProperty(
        key = "WelcomeMessage11",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage11: String

    @StringProperty(
        key = "WelcomeMessage12",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage12: String

    @StringProperty(
        key = "WelcomeMessage13",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage13: String

    @StringProperty(
        key = "WelcomeMessage14",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    val welcomeMessage14: String
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