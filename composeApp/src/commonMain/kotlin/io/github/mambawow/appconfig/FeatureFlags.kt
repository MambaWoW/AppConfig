package io.github.mambawow.appconfig

@Config
interface FeatureFlags {

    @BooleanProperty(
        defaultValue = false,
        description = "Enable Chat feature"
    )
    var isChatEnabled: Boolean

    @BooleanProperty(
        defaultValue = true,
        description = "Enable Welcome Page"
    )
    var isWelcomePageEnabled: Boolean

    @OptionProperty(
        description = "Login Page Style"
    )
    var loginPageStyle: LoginPageStyle

    @OptionProperty(
        key = "ProtocolType",
        description = "Network Protocol type"
    )
    var protocolType: ProtocolType
}

@Option
sealed interface LoginPageStyle {

    @OptionItem(optionId = 0, description = "Style1", isDefault = true)
    data object Style1 : LoginPageStyle

    @OptionItem(1, description = "Style2")
    data object Style2 : LoginPageStyle

    @OptionItem(2, description = "Style3")
    data object Style3 : LoginPageStyle
}

@Option
sealed class ProtocolType(
    val value: String
) {
    @OptionItem(optionId = 0, description = "HTTP1.1 Protocol")
    data object HTTP1_1 : ProtocolType("HTTP1.1")

    @OptionItem(1, description = "HTTP2 Protocol", isDefault = true)
    data object HTTP2 : ProtocolType("HTTP2")

    @OptionItem(2, description = "QUIC Protocol")
    data object QUIC : ProtocolType("QUIC")
}