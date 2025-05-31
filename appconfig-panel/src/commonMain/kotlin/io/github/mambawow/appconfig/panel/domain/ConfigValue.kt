package io.github.mambawow.appconfig.panel.domain

import io.github.mambawow.appconfig.DataType

/**
 * Type-safe configuration value holder
 * Provides compile-time type safety for configuration values
 */
sealed class ConfigValue<out T> {
    
    /**
     * Successful configuration value
     */
    data class Success<T>(val value: T) : ConfigValue<T>()
    
    /**
     * Configuration value with validation error
     */
    data class Error(val message: String, val originalValue: Any?) : ConfigValue<Nothing>()
    
    /**
     * Loading state for configuration value
     */
    object Loading : ConfigValue<Nothing>()
    
    /**
     * Get the value or null if error/loading
     */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        else -> null
    }
    
    /**
     * Get the value or provide a default
     */
    fun getOrDefault(default: @UnsafeVariance T): @UnsafeVariance T = when (this) {
        is Success -> value
        else -> default
    }
    
    /**
     * Transform the value if successful
     */
    inline fun <R> map(transform: (T) -> R): ConfigValue<R> = when (this) {
        is Success -> Success(transform(value))
        is Error -> Error(message, originalValue)
        is Loading -> Loading
    }
    
    /**
     * Flat map for chaining operations
     */
    inline fun <R> flatMap(transform: (T) -> ConfigValue<R>): ConfigValue<R> = when (this) {
        is Success -> transform(value)
        is Error -> Error(message, originalValue)
        is Loading -> Loading
    }
    
    companion object {
        /**
         * Create a ConfigValue from any value with type validation
         */
        fun <T> fromAny(value: Any?, expectedType: DataType): ConfigValue<T> {
            return try {
                when (expectedType) {
                    DataType.BOOLEAN -> {
                        val boolValue = when (value) {
                            is Boolean -> value
                            is String -> value.toBoolean()
                            is Number -> value.toInt() != 0
                            else -> false
                        }
                        @Suppress("UNCHECKED_CAST")
                        Success(boolValue as T)
                    }
                    DataType.STRING -> {
                        @Suppress("UNCHECKED_CAST")
                        Success((value?.toString() ?: "") as T)
                    }
                    DataType.INT -> {
                        val intValue = when (value) {
                            is Int -> value
                            is Number -> value.toInt()
                            is String -> value.toIntOrNull() ?: 0
                            else -> 0
                        }
                        @Suppress("UNCHECKED_CAST")
                        Success(intValue as T)
                    }
                    DataType.LONG -> {
                        val longValue = when (value) {
                            is Long -> value
                            is Number -> value.toLong()
                            is String -> value.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        @Suppress("UNCHECKED_CAST")
                        Success(longValue as T)
                    }
                    DataType.FLOAT -> {
                        val floatValue = when (value) {
                            is Float -> value
                            is Number -> value.toFloat()
                            is String -> value.toFloatOrNull() ?: 0f
                            else -> 0f
                        }
                        @Suppress("UNCHECKED_CAST")
                        Success(floatValue as T)
                    }
                    DataType.DOUBLE -> {
                        val doubleValue = when (value) {
                            is Double -> value
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        @Suppress("UNCHECKED_CAST")
                        Success(doubleValue as T)
                    }
                    else -> {
                        @Suppress("UNCHECKED_CAST")
                        Success(value as T)
                    }
                }
            } catch (e: Exception) {
                Error("Type conversion failed: ${e.message}", value)
            }
        }
        
        /**
         * Create a success value
         */
        fun <T> success(value: T): ConfigValue<T> = Success(value)
        
        /**
         * Create an error value
         */
        fun <T> error(message: String, originalValue: Any? = null): ConfigValue<T> = Error(message, originalValue)
        
        /**
         * Create a loading value
         */
        fun <T> loading(): ConfigValue<T> = Loading
    }
} 