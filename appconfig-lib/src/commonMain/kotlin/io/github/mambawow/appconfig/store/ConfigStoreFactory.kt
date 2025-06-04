package io.github.mambawow.appconfig.store

/**
 * Factory function type for creating [ConfigStore] instances.
 * 
 * This typealias defines the signature for factory functions that create
 * configuration stores for specific configuration groups. The factory
 * pattern allows for dependency injection and customization of storage
 * backends without modifying the core AppConfig logic.
 * 
 * Example implementations:
 * ```kotlin
 * // Default factory using multiplatform-settings
 * val defaultFactory: ConfigStoreFactory = { groupName -> DefaultConfigStore(groupName) }
 * 
 * @param configGroupName The name of the configuration group requiring a store.
 * @return A [ConfigStore] instance for the specified group.
 * 
 * @see AppConfig.initWithFactory
 * @see DefaultConfigStore
 */
typealias ConfigStoreFactory = (configGroupName: String) -> ConfigStore