package io.github.mambawow.appconfig.panel.domain

import io.github.mambawow.appconfig.ConfigItemDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository interface for configuration operations
 * Following Dependency Inversion Principle - depend on abstractions
 */
interface ConfigRepository {
    
    /**
     * Get all configuration items
     */
    fun getConfigItems(): List<ConfigItemDescriptor<*>>
    
    /**
     * Observe value changes for a specific config key
     */
    fun observeConfigValue(key: String): Flow<Any?>
    
    /**
     * Observe all config values
     */
    fun observeAllConfigValues(): Flow<Map<String, Any?>>
    
    /**
     * Update a configuration value
     */
    suspend fun updateConfigValue(key: String, value: Any?)
    
    /**
     * Reset a specific config to default value
     */
    suspend fun resetConfigToDefault(key: String)
    
    /**
     * Reset all configs to default values
     */
    suspend fun resetAllConfigs()
    
    /**
     * Type-safe helper method to get a specific configuration item by type
     */
    fun <T> getConfigItem(key: String): ConfigItemDescriptor<T>? {
        @Suppress("UNCHECKED_CAST")
        return getConfigItems().find { it.key == key } as? ConfigItemDescriptor<T>
    }
    
    /**
     * Type-safe helper method to observe a specific configuration value by type
     */
    fun <T> observeTypedConfigValue(key: String): Flow<T?> {
        return observeConfigValue(key).map { value ->
            @Suppress("UNCHECKED_CAST")
            value as? T
        }
    }
    
    /**
     * Type-safe helper method to update a configuration value with type checking
     */
    suspend fun <T> updateTypedConfigValue(key: String, value: T) {
        updateConfigValue(key, value)
    }
} 