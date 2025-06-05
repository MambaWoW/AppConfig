package io.github.mambawow.appconfig.panel.navigation

import io.github.mambawow.appconfig.DataType
import kotlinx.serialization.Serializable

/**
 * Navigation routes for configuration panel using Kotlin Serialization
 * Provides type-safe navigation with compile-time route validation
 */

@Serializable
internal object ConfigPanel

@Serializable
data class ConfigInput(
    val title: String,
    val currentValue: String,
    val isNumeric: Boolean,
    val dataTypeOrdinal: Int,
    val key: String
)

@Serializable
data class ConfigOption(
    val title: String,
    val currentOptionId: Int,
    val key: String
)

fun DataType.toOrdinal(): Int = this.ordinal

fun Int.toDataType(): DataType = DataType.entries.getOrElse(this) { DataType.STRING }

fun Int.toDataTypeOrDefault(default: DataType = DataType.STRING): DataType {
    return DataType.entries.getOrNull(this) ?: default
} 