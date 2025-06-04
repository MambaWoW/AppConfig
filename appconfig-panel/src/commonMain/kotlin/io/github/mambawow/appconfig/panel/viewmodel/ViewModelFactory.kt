package io.github.mambawow.appconfig.panel.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.mambawow.appconfig.ConfigItemDescriptor
import io.github.mambawow.appconfig.panel.domain.ConfigRepository
import io.github.mambawow.appconfig.panel.domain.ConfigRepositoryImpl

/**
 * Factory for creating ViewModels with proper dependency injection
 * 
 * Usage:
 * ```kotlin
 * val viewModel = ViewModelFactory.createConfigPanelViewModel(configItems)
 * ```
 */
object ViewModelFactory {
    
    /**
     * Create ConfigPanelViewModel with default repository implementation
     * 
     * This method creates a ConfigPanelViewModel instance with a default
     * ConfigRepositoryImpl. The ViewModel and repository are automatically
     * remembered by Compose to prevent unnecessary recreations during
     * recomposition.
     * @param configItems List of configuration items to manage
     * @return ConfigPanelViewModel instance configured with default repository
     */
    @Composable
    fun createConfigPanelViewModel(
        configItems: List<ConfigItemDescriptor<*>>
    ): ConfigPanelViewModel {
        return remember(configItems) {
            val repository: ConfigRepository = ConfigRepositoryImpl(configItems)
            ConfigPanelViewModel(repository)
        }
    }

} 