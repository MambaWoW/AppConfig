package io.github.mambawow.appconfig.panel.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.mambawow.appconfig.ConfigItemDescriptor
import io.github.mambawow.appconfig.panel.domain.ConfigRepository
import io.github.mambawow.appconfig.panel.domain.ConfigRepositoryImpl

/**
 * Factory for creating ViewModels with proper dependency injection
 * 
 * This factory provides a centralized way to create ViewModels with their
 * required dependencies. It follows the Factory pattern to encapsulate
 * the creation logic and ensure proper dependency injection.
 * 
 * Benefits:
 * - Centralized ViewModel creation logic
 * - Proper dependency injection setup
 * - Easy testing with mock repositories
 * - Consistent ViewModel lifecycle management
 * - Memory optimization through Compose remember
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
     * 
     * The factory handles:
     * - Repository instantiation with provided configuration items
     * - ViewModel creation with repository dependency injection
     * - Compose remember optimization for lifecycle management
     * 
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
    
    /**
     * Create ConfigPanelViewModel with custom repository implementation
     * 
     * This method allows injection of a custom repository implementation,
     * useful for testing scenarios or when using different data sources.
     * 
     * @param repository Custom repository implementation to use
     * @return ConfigPanelViewModel instance configured with custom repository
     */
    @Composable
    fun createConfigPanelViewModel(
        repository: ConfigRepository
    ): ConfigPanelViewModel {
        return remember(repository) {
            ConfigPanelViewModel(repository)
        }
    }
} 