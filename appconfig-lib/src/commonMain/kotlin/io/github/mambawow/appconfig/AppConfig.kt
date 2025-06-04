package io.github.mambawow.appconfig

import io.github.mambawow.appconfig.store.ConfigStore
import io.github.mambawow.appconfig.store.ConfigStoreFactory
import io.github.mambawow.appconfig.store.DefaultConfigStore

/**
 * Central configuration manager for the AppConfig library.
 * 
 * This object serves as the main entry point for configuration management,
 * providing factory methods for creating configuration stores and allowing
 * customization of the underlying storage implementation.
 * 
 * Features:
 * - Store caching to prevent duplicate instances
 * - Configurable storage backend through factory pattern
 * - Automatic store lifecycle management
 * 
 * Example usage:
 * ```kotlin
 * 
 * // Using custom storage implementation
 * AppConfig.initWithFactory { groupName -> CustomConfigStore(groupName) }
 * val customStore = AppConfig.createConfigStore("CustomGroup")
 * ```
 */
object AppConfig {

    private var configStoreFactory: ConfigStoreFactory = { name -> DefaultConfigStore(name) }

    /**
     * Initializes AppConfig with a custom store factory.
     * 
     * This method allows you to provide a custom implementation of [ConfigStore]
     * for specialized storage requirements such as encrypted storage, remote
     * synchronization, or in-memory testing stores.
     * 
     * @param configStoreFactory Factory function that creates [ConfigStore] instances
     *                          for a given configuration group name.
     * 
     * @see ConfigStoreFactory
     * @see DefaultConfigStore
     */
    fun initWithFactory(configStoreFactory: ConfigStoreFactory) {
        this.configStoreFactory = configStoreFactory
    }

    private val cachedStore: MutableMap<String, ConfigStore> = mutableMapOf()

    /**
     * Creates or retrieves a cached [ConfigStore] for the specified group.
     * 
     * This method implements a caching strategy to ensure that each configuration
     * group uses the same store instance throughout the application lifecycle.
     * Stores are created lazily on first access.
     * 
     * @param configGroupName The unique name for the configuration group.
     *                       This typically corresponds to the groupName parameter
     *                       in @Config annotations.
     * 
     * @return A [ConfigStore] instance for the specified group.
     * 
     * @see Config
     */
    fun createConfigStore(configGroupName: String): ConfigStore {
        if (cachedStore.containsKey(configGroupName)) {
            return cachedStore[configGroupName]!!
        }
        cachedStore[configGroupName] = configStoreFactory(configGroupName)
        return cachedStore[configGroupName]!!
    }

}
