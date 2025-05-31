package io.github.mambawow.appconfig

import kotlinx.coroutines.flow.Flow

/**
 * @author Frank
 * @created 5/20/25
 * Base sealed interface for all configuration item descriptors.
 * The generated ConfigManager provides a list of these for the UI to build itself.
 * @param T The data type of the configuration property itself (e.g., Boolean, String, LogOutputTarget).
 */
sealed interface ConfigItemDescriptor<T> {
    val key: String // Unique key within its group
    val groupName: String // Group this item belongs to
    val description: String
    val getCurrentValue: () -> Flow<T> // Lambda to get the current value
    val updateValue: suspend (newValue: T) -> Unit // Lambda to update the config value
    val resetToDefault: suspend () -> Unit // Lambda to reset this specific item to its default
}

/**
 * Configuration item for standard primitive types.
 */
data class StandardConfigItem<T>(
    override val key: String,
    override val groupName: String,
    override val description: String,
    val defaultValue: T,
    val panelType: PanelType,
    val dataType: DataType,
    override val getCurrentValue: () -> Flow<T>,
    override val updateValue: suspend (newValue: T) -> Unit,
    override val resetToDefault: suspend () -> Unit
) : ConfigItemDescriptor<T>

/**
 * Configuration item for properties based on custom sealed classes implementing Option<V>.
 * @param T The type of the configuration property (e.g., LogOutputTarget), which must implement Option<V>.
 * @param V The type of the 'value' field in Option<V> (e.g., String if LogOutputTarget implements Option<String>).
 */
data class OptionConfigItem<T>(
    override val key: String,
    override val groupName: String,
    override val description: String,
    val defaultOptionId: Int,
    val choices: List<OptionItemDescriptor<T>>, // List of available option instances (e.g., LogOutputTarget.A, LogOutputTarget.B)
    override val getCurrentValue: () -> Flow<T>,
    override val updateValue: suspend (newValue: T) -> Unit,
    override val resetToDefault: suspend () -> Unit
) : ConfigItemDescriptor<T>

data class OptionItemDescriptor<T>(
    val optionId: Int,
    val description: String,
    val option: T
)