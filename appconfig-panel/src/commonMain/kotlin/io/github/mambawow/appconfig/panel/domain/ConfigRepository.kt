package io.github.mambawow.appconfig.panel.domain

import io.github.mambawow.appconfig.ConfigItemDescriptor
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for configuration operations
 */
interface ConfigRepository {

    fun getConfigItems(): List<ConfigItemDescriptor<*>>

    fun observeAllConfigValues(): Flow<Map<String, Any?>>

    suspend fun updateConfigValue(key: String, value: Any?)

    suspend fun resetAllConfigs()
} 