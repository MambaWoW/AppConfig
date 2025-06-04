package io.github.mambawow.appconfig.panel.domain

import io.github.mambawow.appconfig.ConfigItemDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository interface for configuration operations
 */
interface ConfigRepository {
    
    /**
     * Get all configuration items
     */
    fun getConfigItems(): List<ConfigItemDescriptor<*>>

    /**
     * Observe all config values
     */
    fun observeAllConfigValues(): Flow<Map<String, Any?>>
    
    /**
     * Update a configuration value
     */
    suspend fun updateConfigValue(key: String, value: Any?)
    
    /**
     * Reset all configs to default values
     */
    suspend fun resetAllConfigs()

} 