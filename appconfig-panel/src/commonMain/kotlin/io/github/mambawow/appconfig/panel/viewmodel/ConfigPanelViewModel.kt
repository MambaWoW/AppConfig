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
 * ViewModel for managing configuration panel state and operations
 * 
 * @param repository The repository for configuration data operations
 */
class ConfigPanelViewModel(
    private val repository: ConfigRepository
) : ViewModel() {
    
    // Configuration values state - holds current values for all config items
    private val _configValues = MutableStateFlow<Map<String, Any?>>(emptyMap())
    val configValues: StateFlow<Map<String, Any?>> = _configValues.asStateFlow()
    
    // Loading state - indicates when operations are in progress
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state - holds error messages for display to user
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // Start observing configuration values immediately
        observeConfigValues()
    }
    
    /**
     * Start observing all configuration values from the repository
     */
    private fun observeConfigValues() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.observeAllConfigValues()
                .catch { throwable ->
                    handleError("Failed to observe config values", throwable)
                    _isLoading.value = false
                }
                .collect { values ->
                    _configValues.value = values
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Update a specific configuration value
     * 
     * @param key The unique identifier for the configuration item
     * @param value The new value to set (null values are ignored)
     */
    fun updateConfigValue(key: String, value: Any?) {
        if (value == null) return
        
        viewModelScope.launch {
            try {
                repository.updateConfigValue(key, value)
                clearError() // Clear any previous errors on successful update
            } catch (e: Exception) {
                handleError("Failed to update config value for key '$key'", e)
            }
        }
    }
    
    /**
     * Reset all configurations to their default values
     */
    fun resetAllConfigs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.resetAllConfigs()
                clearError() // Clear any previous errors on successful reset
            } catch (e: Exception) {
                handleError("Failed to reset all configurations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear the current error state
     * 
     * This method can be called when the user has acknowledged an error
     * or when a new operation succeeds.
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Handle errors in a consistent way throughout the ViewModel
     * 
     * @param message A user-friendly error message
     * @param throwable The original exception (optional)
     */
    private fun handleError(message: String, throwable: Throwable? = null) {
        val errorMessage = buildString {
            append(message)
            throwable?.let { error ->
                error.message?.let { errorDetail ->
                    append(": $errorDetail")
                }
            }
        }
        _error.value = errorMessage
    }

} 