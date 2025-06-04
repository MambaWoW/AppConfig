package io.github.mambawow.appconfig.store

/**
 * Interface for configuration storage operations.
 * 
 * Provides type-safe methods for storing and retrieving configuration values
 * across different data types. Implementations handle platform-specific
 * storage mechanisms while maintaining a consistent API.
 * 
 * All get methods return the provided default value when no stored value
 * exists for the given key. Put methods immediately persist the value
 * to the underlying storage system.
 */
interface ConfigStore {
    
    // Getter methods
    
    /**
     * Retrieves a string value from storage.
     * 
     * @param key The storage key for the value.
     * @param defaultValue Value returned when key doesn't exist.
     * @return The stored string value or defaultValue if not found.
     */
    fun getString(key: String, defaultValue: String): String
    
    /**
     * Retrieves a boolean value from storage.
     * 
     * @param key The storage key for the value.
     * @param defaultValue Value returned when key doesn't exist.
     * @return The stored boolean value or defaultValue if not found.
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    
    /**
     * Retrieves an integer value from storage.
     * 
     * @param key The storage key for the value.
     * @param defaultValue Value returned when key doesn't exist.
     * @return The stored integer value or defaultValue if not found.
     */
    fun getInt(key: String, defaultValue: Int): Int
    
    /**
     * Retrieves a long integer value from storage.
     * 
     * @param key The storage key for the value.
     * @param defaultValue Value returned when key doesn't exist.
     * @return The stored long value or defaultValue if not found.
     */
    fun getLong(key: String, defaultValue: Long): Long
    
    /**
     * Retrieves a float value from storage.
     * 
     * @param key The storage key for the value.
     * @param defaultValue Value returned when key doesn't exist.
     * @return The stored float value or defaultValue if not found.
     */
    fun getFloat(key: String, defaultValue: Float): Float
    
    /**
     * Retrieves a double value from storage.
     * 
     * @param key The storage key for the value.
     * @param defaultValue Value returned when key doesn't exist.
     * @return The stored double value or defaultValue if not found.
     */
    fun getDouble(key: String, defaultValue: Double): Double

    // Setter methods
    
    /**
     * Stores a string value.
     * 
     * @param key The storage key for the value.
     * @param value The string value to store.
     */
    fun putString(key: String, value: String)
    
    /**
     * Stores a boolean value.
     * 
     * @param key The storage key for the value.
     * @param value The boolean value to store.
     */
    fun putBoolean(key: String, value: Boolean)
    
    /**
     * Stores an integer value.
     * 
     * @param key The storage key for the value.
     * @param value The integer value to store.
     */
    fun putInt(key: String, value: Int)
    
    /**
     * Stores a long integer value.
     * 
     * @param key The storage key for the value.
     * @param value The long value to store.
     */
    fun putLong(key: String, value: Long)
    
    /**
     * Stores a float value.
     * 
     * @param key The storage key for the value.
     * @param value The float value to store.
     */
    fun putFloat(key: String, value: Float)
    
    /**
     * Stores a double value.
     * 
     * @param key The storage key for the value.
     * @param value The double value to store.
     */
    fun putDouble(key: String, value: Double)
    
    /**
     * Removes a value from storage.
     * 
     * @param key The storage key of the value to remove.
     */
    fun remove(key: String)
    
    /**
     * Clears all stored values in this configuration store.
     * 
     * This operation is irreversible and will remove all configuration
     * data associated with this store's group.
     */
    suspend fun clearAll()
}