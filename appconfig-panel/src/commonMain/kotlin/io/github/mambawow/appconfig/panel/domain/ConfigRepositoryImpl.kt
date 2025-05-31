package io.github.mambawow.appconfig.panel.domain

import io.github.mambawow.appconfig.ConfigItemDescriptor
import io.github.mambawow.appconfig.OptionConfigItem
import io.github.mambawow.appconfig.StandardConfigItem
import io.github.mambawow.appconfig.DataType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Implementation of ConfigRepository
 * Following Single Responsibility Principle - only handles config data operations
 */
class ConfigRepositoryImpl(
    private val configItems: List<ConfigItemDescriptor<*>>
) : ConfigRepository {
    
    override fun getConfigItems(): List<ConfigItemDescriptor<*>> = configItems
    
    override fun observeConfigValue(key: String): Flow<Any?> {
        val item = configItems.find { it.key == key }
            ?: throw IllegalArgumentException("Config item with key '$key' not found")
        
        return item.getCurrentValue()
    }
    
    override fun observeAllConfigValues(): Flow<Map<String, Any?>> {
        if (configItems.isEmpty()) {
            return kotlinx.coroutines.flow.flowOf(emptyMap())
        }
        
        val configFlows = configItems.map { item ->
            item.getCurrentValue().let { flow ->
                kotlinx.coroutines.flow.flow {
                    flow.collect { value ->
                        emit(item.key to value)
                    }
                }
            }
        }
        
        return combine(configFlows) { keyValuePairs ->
            keyValuePairs.toMap()
        }
    }
    
    override suspend fun updateConfigValue(key: String, value: Any?) {
        val item = configItems.find { it.key == key }
            ?: throw IllegalArgumentException("Config item with key '$key' not found")
        
        if (value == null) return
        
        when (item) {
            is StandardConfigItem<*> -> updateStandardConfigItem(item, value)
            is OptionConfigItem<*> -> updateOptionConfigItem(item, value)
        }
    }
    
    override suspend fun resetConfigToDefault(key: String) {
        val item = configItems.find { it.key == key }
            ?: throw IllegalArgumentException("Config item with key '$key' not found")
        
        when (item) {
            is StandardConfigItem<*> -> {
                item.defaultValue?.let { defaultValue ->
                    updateStandardConfigItem(item, defaultValue)
                }
            }
            is OptionConfigItem<*> -> {
                val defaultChoice = item.choices.find { it.optionId == item.defaultOptionId }
                defaultChoice?.option?.let { option ->
                    updateOptionConfigItem(item, option)
                }
            }
        }
    }
    
    override suspend fun resetAllConfigs() {
        configItems.forEach { item ->
            resetConfigToDefault(item.key)
        }
    }
    
    private suspend fun updateStandardConfigItem(item: StandardConfigItem<*>, value: Any) {
        try {
            when (item.dataType) {
                DataType.BOOLEAN -> {
                    if (value is Boolean) {
                        updateTypedStandardConfigItem(item, value)
                    } else {
                        throw ClassCastException("Expected Boolean, got ${value::class.simpleName}")
                    }
                }
                DataType.STRING -> {
                    if (value is String) {
                        updateTypedStandardConfigItem(item, value)
                    } else {
                        throw ClassCastException("Expected String, got ${value::class.simpleName}")
                    }
                }
                DataType.INT -> {
                    if (value is Int) {
                        updateTypedStandardConfigItem(item, value)
                    } else {
                        throw ClassCastException("Expected Int, got ${value::class.simpleName}")
                    }
                }
                DataType.LONG -> {
                    if (value is Long) {
                        updateTypedStandardConfigItem(item, value)
                    } else {
                        throw ClassCastException("Expected Long, got ${value::class.simpleName}")
                    }
                }
                DataType.FLOAT -> {
                    if (value is Float) {
                        updateTypedStandardConfigItem(item, value)
                    } else {
                        throw ClassCastException("Expected Float, got ${value::class.simpleName}")
                    }
                }
                DataType.DOUBLE -> {
                    if (value is Double) {
                        updateTypedStandardConfigItem(item, value)
                    } else {
                        throw ClassCastException("Expected Double, got ${value::class.simpleName}")
                    }
                }
                else -> {
                    // Skip unsupported types
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to update standard config item ${item.key}: ${e.message}")
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> updateTypedStandardConfigItem(item: StandardConfigItem<*>, value: T) {
        (item as StandardConfigItem<T>).updateValue(value)
    }
    
    private suspend fun updateOptionConfigItem(item: OptionConfigItem<*>, value: Any) {
        try {
            updateTypedOptionConfigItem(item, value)
        } catch (e: Exception) {
            throw Exception("Failed to update option config item ${item.key}: ${e.message}")
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private suspend fun updateTypedOptionConfigItem(item: OptionConfigItem<*>, value: Any) {
        (item as OptionConfigItem<Any>).updateValue(value)
    }
} 