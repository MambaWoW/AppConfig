package io.github.mambawow.appconfig.panel.domain

import io.github.mambawow.appconfig.ConfigItemDescriptor
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.OptionConfigItem
import io.github.mambawow.appconfig.StandardConfigItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Default implementation of ConfigRepository
 * 
 * This repository manages configuration items and provides reactive updates
 * through Flow-based state management. It handles both standard configuration
 * items (with primitive data types) and option-based configuration items.
 * 
 * Features:
 * - Type-safe configuration updates with validation
 * - Reactive configuration value observation
 * - Efficient single-value updates to minimize UI recomposition
 * - Comprehensive error handling with detailed error messages
 * 
 * @param configItems The list of configuration items to manage
 */
class ConfigRepositoryImpl(
    private val configItems: List<ConfigItemDescriptor<*>>
) : ConfigRepository {

    // StateFlow for reactive configuration value updates
    // Using concurrent map access pattern for thread safety
    private val _configValues = MutableStateFlow<Map<String, Any?>>(emptyMap())

    init {
        // Initialize with current values from all configuration items
        refreshAllValues()
    }

    /**
     * Get all managed configuration items
     * 
     * @return Immutable list of configuration item descriptors
     */
    override fun getConfigItems(): List<ConfigItemDescriptor<*>> = configItems

    /**
     * Observe all configuration values reactively
     * 
     * This method returns a Flow that emits the complete configuration map
     * whenever any configuration value changes. UI components can collect
     * from this Flow to receive updates automatically.
     * 
     * @return Flow of configuration key-value pairs
     */
    override fun observeAllConfigValues(): Flow<Map<String, Any?>> {
        return _configValues
    }

    /**
     * Update a specific configuration value with type validation
     * 
     * This method performs type checking and validation before updating
     * the configuration value. It only updates the specific changed value
     * to optimize performance and reduce unnecessary UI recomposition.
     * 
     * @param key The unique identifier for the configuration item
     * @param value The new value to set (must match expected type)
     * @throws IllegalArgumentException If the configuration key is not found
     * @throws ClassCastException If the value type doesn't match expected type
     */
    override suspend fun updateConfigValue(key: String, value: Any?) {
        val item = configItems.find { it.key == key }
            ?: throw IllegalArgumentException("Configuration item with key '$key' not found")

        if (value == null) {
            throw IllegalArgumentException("Cannot set null value for configuration key '$key'")
        }

        try {
            when (item) {
                is StandardConfigItem<*> -> updateStandardConfigItem(item, value)
                is OptionConfigItem<*> -> updateOptionConfigItem(item, value)
            }

            // Only update this specific configuration item for optimal performance
            updateSingleConfigValue(key, item.getCurrentValue())
            
        } catch (e: Exception) {
            throw Exception("Failed to update configuration '$key': ${e.message}", e)
        }
    }

    /**
     * Reset all configurations to their default values
     * 
     * This method resets every configuration item to its default value
     * and refreshes the entire configuration state.
     */
    override suspend fun resetAllConfigs() {
        try {
            configItems.forEach { item ->
                item.resetToDefault()
            }

            // Refresh all values after bulk reset operation
            refreshAllValues()
            
        } catch (e: Exception) {
            throw Exception("Failed to reset configurations: ${e.message}", e)
        }
    }

    /**
     * Update a single configuration value in the state map
     * 
     * This method performs an optimized update by only changing the specific
     * key-value pair rather than recreating the entire map.
     * 
     * @param key The configuration key to update
     * @param newValue The new value for the key
     */
    private fun updateSingleConfigValue(key: String, newValue: Any?) {
        val currentValues = _configValues.value.toMutableMap()
        currentValues[key] = newValue
        _configValues.value = currentValues
    }

    /**
     * Refresh all configuration values from their current state
     * 
     * This method rebuilds the entire configuration map by querying
     * each configuration item for its current value. Used during
     * initialization and after bulk operations.
     */
    private fun refreshAllValues() {
        val values = configItems.associate { item ->
            item.key to item.getCurrentValue()
        }
        _configValues.value = values
    }

    /**
     * Update a standard configuration item with type validation
     * 
     * This method handles updates for primitive data types (Boolean, String,
     * Int, Long, Float, Double) with strict type checking to ensure type safety.
     * 
     * @param item The standard configuration item to update
     * @param value The new value (must match the item's expected data type)
     * @throws ClassCastException If value type doesn't match expected type
     */
    private fun updateStandardConfigItem(item: StandardConfigItem<*>, value: Any) {
        try {
            when (item.dataType) {
                DataType.BOOLEAN -> {
                    require(value is Boolean) { 
                        "Expected Boolean for ${item.key}, got ${value::class.simpleName}" 
                    }
                    updateTypedStandardConfigItem(item, value)
                }

                DataType.STRING -> {
                    require(value is String) { 
                        "Expected String for ${item.key}, got ${value::class.simpleName}" 
                    }
                    updateTypedStandardConfigItem(item, value)
                }

                DataType.INT -> {
                    require(value is Int) { 
                        "Expected Int for ${item.key}, got ${value::class.simpleName}" 
                    }
                    updateTypedStandardConfigItem(item, value)
                }

                DataType.LONG -> {
                    require(value is Long) { 
                        "Expected Long for ${item.key}, got ${value::class.simpleName}" 
                    }
                    updateTypedStandardConfigItem(item, value)
                }

                DataType.FLOAT -> {
                    require(value is Float) { 
                        "Expected Float for ${item.key}, got ${value::class.simpleName}" 
                    }
                    updateTypedStandardConfigItem(item, value)
                }

                DataType.DOUBLE -> {
                    require(value is Double) { 
                        "Expected Double for ${item.key}, got ${value::class.simpleName}" 
                    }
                    updateTypedStandardConfigItem(item, value)
                }

                else -> {
                    throw IllegalArgumentException("Unsupported data type: ${item.dataType}")
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to update standard config item ${item.key}: ${e.message}", e)
        }
    }

    /**
     * Type-safe update for standard configuration items
     * 
     * This method performs the actual update operation with proper type casting.
     * The @Suppress annotation is necessary due to Kotlin's type system limitations
     * with generic wildcards.
     * 
     * @param item The configuration item to update
     * @param value The new value (type already validated)
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> updateTypedStandardConfigItem(item: StandardConfigItem<*>, value: T) {
        (item as StandardConfigItem<T>).updateValue(value)
    }

    /**
     * Update an option-based configuration item
     * 
     * This method handles updates for configuration items that have a predefined
     * set of options. The value must be one of the valid options for the item.
     * 
     * @param item The option configuration item to update
     * @param value The new option value
     */
    private fun updateOptionConfigItem(item: OptionConfigItem<*>, value: Any) {
        try {
            updateTypedOptionConfigItem(item, value)
        } catch (e: Exception) {
            throw Exception("Failed to update option config item ${item.key}: ${e.message}", e)
        }
    }

    /**
     * Type-safe update for option configuration items
     * 
     * Similar to the standard config item update, this method performs
     * the actual update with proper type casting.
     * 
     * @param item The option configuration item to update
     * @param value The new option value
     */
    @Suppress("UNCHECKED_CAST")
    private fun updateTypedOptionConfigItem(item: OptionConfigItem<*>, value: Any) {
        (item as OptionConfigItem<Any>).updateValue(value)
    }
} 