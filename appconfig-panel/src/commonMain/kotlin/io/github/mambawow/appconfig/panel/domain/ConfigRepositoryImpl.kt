package io.github.mambawow.appconfig.panel.domain

import io.github.mambawow.appconfig.ConfigItemDescriptor
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.OptionConfigItem
import io.github.mambawow.appconfig.StandardConfigItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of ConfigRepository
 */
class ConfigRepositoryImpl(
    private val configItems: List<ConfigItemDescriptor<*>>
) : ConfigRepository {

    // 使用 StateFlow 来模拟响应式数据
    private val _configValues = MutableStateFlow<Map<String, Any?>>(emptyMap())

    init {
        // 初始化时获取所有当前值
        refreshAllValues()
    }

    override fun getConfigItems(): List<ConfigItemDescriptor<*>> = configItems

    override fun observeAllConfigValues(): Flow<Map<String, Any?>> {
        return _configValues
    }

    override suspend fun updateConfigValue(key: String, value: Any?) {
        val item = configItems.find { it.key == key }
            ?: throw IllegalArgumentException("Config item with key '$key' not found")

        if (value == null) return

        when (item) {
            is StandardConfigItem<*> -> updateStandardConfigItem(item, value)
            is OptionConfigItem<*> -> updateOptionConfigItem(item, value)
        }

        // 只更新这个特定的配置项
        updateSingleConfigValue(key, item.getCurrentValue())
    }

    override suspend fun resetAllConfigs() {
        configItems.forEach { item ->
            item.resetToDefault()
        }

        // 重置所有配置时才需要刷新全部值
        refreshAllValues()
    }

    private fun updateSingleConfigValue(key: String, newValue: Any?) {
        val currentValues = _configValues.value.toMutableMap()
        currentValues[key] = newValue
        _configValues.value = currentValues
    }

    private fun refreshAllValues() {
        val values = configItems.associate { item ->
            item.key to item.getCurrentValue()
        }
        _configValues.value = values
    }

    private fun updateStandardConfigItem(item: StandardConfigItem<*>, value: Any) {
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
    private fun <T> updateTypedStandardConfigItem(item: StandardConfigItem<*>, value: T) {
        (item as StandardConfigItem<T>).updateValue(value)
    }

    private fun updateOptionConfigItem(item: OptionConfigItem<*>, value: Any) {
        try {
            updateTypedOptionConfigItem(item, value)
        } catch (e: Exception) {
            throw Exception("Failed to update option config item ${item.key}: ${e.message}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateTypedOptionConfigItem(item: OptionConfigItem<*>, value: Any) {
        (item as OptionConfigItem<Any>).updateValue(value)
    }
} 