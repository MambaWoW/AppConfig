package io.github.mambawow.appconfig.panel.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.mambawow.appconfig.ConfigItemDescriptor
import io.github.mambawow.appconfig.panel.domain.ConfigRepository
import io.github.mambawow.appconfig.panel.domain.ConfigRepositoryImpl

/**
 * Factory for creating ViewModels with proper dependency injection
 */
object ViewModelFactory {
    
    /**
     * Create ConfigPanelViewModel with default repository implementation
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