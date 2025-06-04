package io.github.mambawow.appconfig

// 测试配置组1 - 使用默认groupName "NetworkConfig"
@Config
interface NetworkConfig {
    // 这个key可以与其他组中的相同key共存
    @StringProperty(
        key = "timeout",
        defaultValue = "60s",
        description = "Network timeout duration"
    )
    var networkTimeout: String

    // 测试约定优于配置 - key将使用属性名 "maxRetries"
    @IntProperty(
        defaultValue = 3,
        description = "Maximum retry attempts"
    )
    var maxRetries: Int
}

// 测试配置组2 - 显式指定groupName
@Config(groupName = "UserInterface")
interface UserInterfaceConfig {
    // 这个key与NetworkConfig中的"timeout"不冲突，因为在不同组中
    @StringProperty(
        key = "timeout",
        defaultValue = "5s",
        description = "UI animation timeout"
    )
    var animationTimeout: String

    // 测试约定优于配置 - key将使用属性名 "theme"
    @StringProperty(
        defaultValue = "light",
        description = "Application theme"
    )
    var theme: String
} 