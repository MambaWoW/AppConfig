package io.github.mambawow.appconfig

import io.github.mambawow.appconfig.store.ConfigStore
import io.github.mambawow.appconfig.store.ConfigStoreFactory
import io.github.mambawow.appconfig.store.DefaultConfigStore
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.atomicfu.update
import kotlinx.atomicfu.updateAndGet

/**
 * Central configuration manager using kotlinx-atomicfu for lock-free concurrency.
 *
 * Example usage:
 * ```kotlin
 * val store = AppConfig.createConfigStore("MyGroup")
 * AppConfig.configure { groupName -> CustomConfigStore(groupName) }
 * AppConfig.resetCache(clearStoredData = true)
 * ```
 */
object AppConfig {

    private val factoryRef = atomic<ConfigStoreFactory> { name -> DefaultConfigStore(name) }
    private val cacheRef = atomic<Map<String, ConfigStore>>(emptyMap())

    /**
     * Configures AppConfig with a custom store factory
     *
     * @param factory Factory function that creates [ConfigStore] instances
     */
    fun configure(factory: ConfigStoreFactory) {
        factoryRef.update { factory }
        cacheRef.update { emptyMap() }
    }

    /**
     * Retrieves or creates a [ConfigStore] for the specified group.
     * @param configGroupName The unique name for the configuration group
     * @return A [ConfigStore] instance for the specified group
     */
    fun getConfigStore(configGroupName: String): ConfigStore {
        return getOrCreateStoreAtomic(configGroupName)
    }

    private fun getOrCreateStoreAtomic(groupName: String): ConfigStore {
        cacheRef.value[groupName]?.let { return it }
        
        return cacheRef.updateAndGet { currentCache ->
            currentCache[groupName]?.let { currentCache }
                ?: run {
                    val newStore = factoryRef.value(groupName)
                    currentCache + (groupName to newStore)
                }
        }[groupName]!!
    }

    /**
     * Resets all cached stores and optionally their stored data.
     *
     * @param clearStoredData If true, calls clearAll() on each store
     */
     @InternalAPI
     suspend fun clear(clearStoredData: Boolean = true) {
        if (clearStoredData) {
            val oldCache = cacheRef.getAndUpdate { emptyMap() }
            oldCache.values.forEach { store ->
                runCatching { store.clearAll() }
                    .onFailure {  }
            }
        } else {
            cacheRef.update { emptyMap() }
        }
    }
}
