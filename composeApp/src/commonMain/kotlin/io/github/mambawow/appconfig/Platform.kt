package io.github.mambawow.appconfig

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform