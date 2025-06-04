package io.github.mambawow.appconfig.panel.domain

import io.github.mambawow.appconfig.ConfigItemDescriptor
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for configuration operations
 * 
 * This interface defines the contract for configuration data management
 * and provides reactive access to configuration values. It abstracts
 * the underlying data source and provides a clean API for the ViewModel layer.
 * 
 * The repository follows these principles:
 * - Reactive programming with Flow-based data streams
 * - Separation of concerns between data access and business logic
 * - Type-safe configuration value management
 * - Comprehensive error handling and validation
 * 
 * Implementation considerations:
 * - Thread-safe operations for concurrent access
 * - Efficient updates to minimize UI recomposition
 * - Proper error propagation to the presentation layer
 * - Memory-efficient data structures for large configuration sets
 */
interface ConfigRepository {
    
    /**
     * Get all configuration items managed by this repository
     * 
     * This method returns the complete list of configuration item descriptors
     * that are currently managed by the repository. The list is immutable
     * and represents the static configuration schema.
     * 
     * @return Immutable list of configuration item descriptors
     */
    fun getConfigItems(): List<ConfigItemDescriptor<*>>

    /**
     * Observe all configuration values reactively
     * 
     * This method returns a Flow that emits the complete configuration
     * state whenever any configuration value changes. The Flow is designed
     * to be collected by UI components for automatic updates.
     * 
     * Features:
     * - Hot Flow that maintains current state
     * - Emits complete configuration map on each change
     * - Suitable for UI state collection with collectAsStateWithLifecycle
     * - Handles backpressure and conflated emissions
     * 
     * @return Flow of configuration key-value pairs
     * @throws Exception if configuration observation fails
     */
    fun observeAllConfigValues(): Flow<Map<String, Any?>>
    
    /**
     * Update a specific configuration value
     * 
     * This method updates a single configuration item with type validation
     * and error handling. The update is performed asynchronously and will
     * trigger emissions to observers through the Flow returned by
     * observeAllConfigValues().
     * 
     * Validation performed:
     * - Configuration key existence validation
     * - Type compatibility checking
     * - Value range validation (where applicable)
     * - Business rule validation
     * 
     * @param key The unique identifier for the configuration item
     * @param value The new value to set (must be compatible with item type)
     * @throws IllegalArgumentException if the key is not found
     * @throws ClassCastException if the value type is incompatible
     * @throws Exception if the update operation fails
     */
    suspend fun updateConfigValue(key: String, value: Any?)
    
    /**
     * Reset all configurations to their default values
     * 
     * This method performs a bulk reset operation that restores all
     * configuration items to their original default values. The operation
     * is atomic and will trigger a single emission to observers with all
     * updated values.
     * 
     * Use cases:
     * - User-initiated reset functionality
     * - Error recovery scenarios
     * - Testing and development workflows
     * - Factory reset implementations
     * 
     * @throws Exception if the reset operation fails
     */
    suspend fun resetAllConfigs()
} 