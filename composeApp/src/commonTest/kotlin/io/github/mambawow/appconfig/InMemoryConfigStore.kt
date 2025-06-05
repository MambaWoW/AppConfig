package io.github.mambawow.appconfig

import com.russhwolf.settings.MapSettings
import io.github.mambawow.appconfig.store.ConfigStore

class IsolatedInMemoryConfigStore: ConfigStore {

    private val settings: MapSettings = MapSettings()

    override fun getString(key: String, defaultValue: String): String {
        return settings.getString(key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return settings.getBoolean(key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return settings.getInt(key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return settings.getLong(key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return settings.getFloat(key, defaultValue)
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return settings.getDouble(key, defaultValue)
    }

    override fun putString(key: String, value: String) {
        settings.putString(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        settings.putBoolean(key, value)
    }

    override fun putInt(key: String, value: Int) {
        settings.putInt(key, value)
    }

    override fun putLong(key: String, value: Long) {
        settings.putLong(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        settings.putFloat(key, value)
    }

    override fun putDouble(key: String, value: Double) {
        settings.putDouble(key, value)
    }

    override fun remove(key: String) {
        settings.remove(key)
    }

    override suspend fun clearAll() {
        settings.clear()
    }
}