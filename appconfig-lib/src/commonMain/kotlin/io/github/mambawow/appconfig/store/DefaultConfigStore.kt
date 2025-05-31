package io.github.mambawow.appconfig.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import kotlin.reflect.KClass

/**
 * @author Frank
 * @created 5/27/25
 */
class DefaultConfigStore(private val produceFilePath: () -> String) : ConfigStore {

    private val store: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { produceFilePath().toPath() }
    )

    override fun getString(key: String, defaultValue: String): Flow<String> {
        return store.data.map { preferences ->
            preferences[stringPreferencesKey(key)]  ?: defaultValue
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Flow<Boolean> {
        return store.data.map { preferences ->
            preferences[booleanPreferencesKey(key)]  ?: defaultValue
        }
    }

    override fun getInt(key: String, defaultValue: Int): Flow<Int> {
        return store.data.map { preferences ->
            preferences[intPreferencesKey(key)]  ?: defaultValue
        }
    }

    override fun getLong(key: String, defaultValue: Long): Flow<Long> {
        return store.data.map { preferences ->
            preferences[longPreferencesKey(key)]  ?: defaultValue
        }
    }

    override fun getFloat(key: String, defaultValue: Float): Flow<Float> {
        return store.data.map { preferences ->
            preferences[floatPreferencesKey(key)]  ?: defaultValue
        }
    }

    override fun getDouble(key: String, defaultValue: Double): Flow<Double> {
        return store.data.map { preferences ->
            preferences[doublePreferencesKey(key)]  ?: defaultValue
        }
    }

    override suspend fun putString(key: String, value: String) {
        store.edit { settings ->
            settings[stringPreferencesKey(key)] = value
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        store.edit { settings ->
            settings[booleanPreferencesKey(key)] = value
        }
    }

    override suspend fun putInt(key: String, value: Int) {
        store.edit { settings ->
            settings[intPreferencesKey(key)] = value
        }
    }

    override suspend fun putLong(key: String, value: Long) {
        store.edit { settings ->
            settings[longPreferencesKey(key)] = value
        }
    }

    override suspend fun putFloat(key: String, value: Float) {
        store.edit { settings ->
            settings[floatPreferencesKey(key)] = value
        }
    }

    override suspend fun putDouble(key: String, value: Double) {
        store.edit { settings ->
            settings[doublePreferencesKey(key)] = value
        }
    }

    override suspend fun remove(key: String, type: KClass<*>) {
        when (type) {
            String::class -> store.edit { settings -> settings.remove(stringPreferencesKey(key)) }
            Boolean::class -> store.edit { settings -> settings.remove(booleanPreferencesKey(key)) }
            Int::class -> store.edit { settings -> settings.remove(intPreferencesKey(key)) }
            Double::class -> store.edit { settings -> settings.remove(doublePreferencesKey(key)) }
            Float::class -> store.edit { settings -> settings.remove(floatPreferencesKey(key)) }
            Long::class -> store.edit { settings -> settings.remove(longPreferencesKey(key)) }
        }
    }

    override suspend fun clearAll() {
        store.edit { settings ->
            settings.clear()
        }
    }
}