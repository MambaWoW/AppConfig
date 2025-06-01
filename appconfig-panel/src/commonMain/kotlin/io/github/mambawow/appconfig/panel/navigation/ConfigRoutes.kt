package io.github.mambawow.appconfig.panel.navigation

import io.github.mambawow.appconfig.DataType
import kotlinx.serialization.Serializable

/**
 * Navigation routes for configuration panel using Kotlin Serialization
 * Provides type-safe navigation with compile-time route validation
 */

@Serializable
object ConfigPanel

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

/**
 * Extension functions for DataType and Int conversion
 * 用于 DataType 枚举和 Int ordinal 值之间的转换
 */

/**
 * Convert DataType to ordinal value for navigation
 * 将 DataType 枚举转换为 ordinal 值，用于导航
 */
fun DataType.toOrdinal(): Int = this.ordinal

/**
 * Convert ordinal value back to DataType enum
 * 将 ordinal 值转换回 DataType 枚举
 */
fun Int.toDataType(): DataType = DataType.entries.getOrElse(this) { DataType.STRING }

/**
 * Safe conversion from ordinal to DataType with default fallback
 * 安全地将 ordinal 转换为 DataType，带有默认值回退
 */
fun Int.toDataTypeOrDefault(default: DataType = DataType.STRING): DataType {
    return DataType.entries.getOrNull(this) ?: default
} 