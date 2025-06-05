package io.github.mambawow.appconfig

@Config
interface NetworkConfig {
    @StringProperty(
        key = "timeout",
        defaultValue = "60s",
        description = "Network timeout duration"
    )
    var networkTimeout: String

    @IntProperty(
        defaultValue = 3,
        description = "Maximum retry attempts"
    )
    var maxRetries: Int
}