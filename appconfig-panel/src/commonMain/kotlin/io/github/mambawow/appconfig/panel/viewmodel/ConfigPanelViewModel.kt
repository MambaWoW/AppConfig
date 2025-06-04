package io.github.mambawow.appconfig.panel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.panel.domain.ConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for configuration panel
 */
class ConfigPanelViewModel(
    private val repository: ConfigRepository
) : ViewModel() {
    
    // Configuration values state
    private val _configValues = MutableStateFlow<Map<String, Any?>>(emptyMap())
    val configValues: StateFlow<Map<String, Any?>> = _configValues.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        observeConfigValues()
    }
    
    /**
     * Start observing all configuration values
     */
    private fun observeConfigValues() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.observeAllConfigValues()
                .catch { throwable ->
                    _error.value = "Failed to observe config values: ${throwable.message}"
                    _isLoading.value = false
                }
                .collect { values ->
                    _configValues.value = values
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Update a configuration value
     */
    fun updateConfigValue(key: String, value: Any?) {
        viewModelScope.launch {
            try {
                repository.updateConfigValue(key, value)
            } catch (e: Exception) {
                _error.value = "Failed to update config value: ${e.message}"
            }
        }
    }
    
    /**
     * Reset all configurations to their default values
     */
    fun resetAllConfigs() {
        viewModelScope.launch {
            try {
                repository.resetAllConfigs()
            } catch (e: Exception) {
                _error.value = "Failed to reset all configs: ${e.message}"
            }
        }
    }
    
    /**
     * Refresh all configuration values
     */
    fun refresh() {
        observeConfigValues()
    }
} 