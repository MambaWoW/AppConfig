package io.github.mambawow.appconfig


@Config(groupName = "UserInterface")
interface UserInterfaceConfig {
    @StringProperty(
        key = "timeout",
        defaultValue = "5s",
        description = "UI animation timeout"
    )
    var animationTimeout: String

    @StringProperty(
        defaultValue = "light",
        description = "Application theme"
    )
    var theme: String
} 