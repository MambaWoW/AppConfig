package io.github.mambawow.appconfig.store

import com.russhwolf.settings.Settings
import io.github.mambawow.appconfig.createSettings

/**
 * Default implementation of [ConfigStore] using multiplatform-settings library.
 * 
 * This implementation provides persistent storage across different platforms:
 * - **Android**: Uses SharedPreferences for reliable key-value storage
 * - **iOS**: Uses NSUserDefaults for native iOS preference storage
 *
 * The store handles automatic type conversion and persistence, ensuring that
 * configuration values survive application restarts and are synchronized
 * across the application lifecycle.
 * 
 * @param path The configuration group identifier, used to create isolated
 *            storage namespaces and prevent key collisions between different
 *            configuration groups.
 * 
 * @see ConfigStore
 * @see Settings
 */
class DefaultConfigStore(path: String) : ConfigStore {

    private val store: Settings = createSettings(path)

    override fun getString(key: String, defaultValue: String): String {
        return store.getString(key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return store.getBoolean(key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return store.getInt(key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return store.getLong(key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return store.getFloat(key, defaultValue)
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return store.getDouble(key, defaultValue)
    }

    override fun putString(key: String, value: String) {
        store.putString(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        store.putBoolean(key, value)
    }

    override fun putInt(key: String, value: Int) {
        store.putInt(key, value)
    }

    override fun putLong(key: String, value: Long) {
        store.putLong(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        store.putFloat(key, value)
    }

    override fun putDouble(key: String, value: Double) {
        store.putDouble(key, value)
    }

    override fun remove(key: String) {
        store.remove(key)
    }

    override suspend fun clearAll() {
        store.clear()
    }

}