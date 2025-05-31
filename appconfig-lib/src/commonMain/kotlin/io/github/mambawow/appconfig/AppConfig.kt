package io.github.mambawow.appconfig

import io.github.mambawow.appconfig.store.ConfigStore
import io.github.mambawow.appconfig.store.ConfigStoreFactory

/**
 * @author Frank
 * @created 5/24/25
 */

/**
 * AppConfig单例对象，提供友好的配置管理API
 * 使用方式：
 * ```
 * // 注入配置存储工厂
 * AppConfig.configStoreFactory = myConfigStoreFactory
 * 
 * // 直接使用
 * val store = AppConfig.createConfigStore("user_settings")
 * ```
 */
object AppConfig {
    
    /**
     * 配置存储工厂，需要在使用前设置
     */
    private lateinit var configStoreFactory: ConfigStoreFactory

    fun initWithFactory(configStoreFactory: ConfigStoreFactory) {
        this.configStoreFactory = configStoreFactory
    }

    val cachedStore: MutableMap<String, ConfigStore> = mutableMapOf()
    /**
     * 为指定的配置组创建配置存储
     * @param configGroupName 配置组名称
     * @return ConfigStore实例
     * @throws UninitializedPropertyAccessException 如果configStoreFactory未设置
     */
    fun createConfigStore(configGroupName: String): ConfigStore {
        if (!::configStoreFactory.isInitialized) {
            throw RuntimeException("ConfigStoreFactory must be initialized before creating a ConfigStore.")
        }
        if (cachedStore.containsKey(configGroupName)) {
            return cachedStore[configGroupName]!!
        }
        cachedStore[configGroupName] = configStoreFactory(configGroupName)
        return cachedStore[configGroupName]!!
    }
    
    /**
     * 检查是否已经设置了configStoreFactory
     * @return true如果已设置，false否则
     */
    internal fun isInitialized(): Boolean {
        return ::configStoreFactory.isInitialized
    }

}
