package io.github.mambawow.appconfig.store

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * @author Frank
 * @created 5/20/25
 * Interface for abstracting the underlying storage mechanism (e.g., SharedPreferences, File, Database).
 * The generated ConfigManager will use an instance of this obtained.
 */
interface ConfigStore {
    // Getters now return Flow<T> to observe changes reactively
    fun getString(key: String, defaultValue: String): Flow<String>
    fun getBoolean(key: String, defaultValue: Boolean): Flow<Boolean>
    fun getInt(key: String, defaultValue: Int): Flow<Int>
    fun getLong(key: String, defaultValue: Long): Flow<Long>
    fun getFloat(key: String, defaultValue: Float): Flow<Float>
    fun getDouble(key: String, defaultValue: Double): Flow<Double>

    // Setters become suspend functions
    suspend fun putString(key: String, value: String)
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun putInt(key: String, value: Int)
    suspend fun putLong(key: String, value: Long)
    suspend fun putFloat(key: String, value: Float)
    suspend fun putDouble(key: String, value: Double)
    suspend fun remove(key: String, type: KClass<*>)
    suspend fun clearAll()
}