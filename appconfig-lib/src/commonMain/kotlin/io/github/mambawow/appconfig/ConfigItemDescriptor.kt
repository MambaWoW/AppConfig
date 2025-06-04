package io.github.mambawow.appconfig

/**
 * Base interface for configuration item descriptors.
 * 
 * Provides metadata and access methods for configuration properties.
 * This interface is implemented by specific descriptor types for different
 * configuration patterns (standard values, option selections).
 * 
 * @param T The type of the configuration value.
 */
sealed interface ConfigItemDescriptor<T> {
    /** Unique identifier for this configuration item within its group. */
    val key: String
    
    /** Name of the configuration group this item belongs to. */
    val groupName: String
    
    /** Human-readable description of this configuration item. */
    val description: String
    
    /** Lambda function to retrieve the current value of this configuration item. */
    val getCurrentValue: () -> T
    
    /** Lambda function to update the value of this configuration item. */
    val updateValue: (newValue: T) -> Unit
    
    /** Lambda function to reset this configuration item to its default value. */
    val resetToDefault: () -> Unit
}

/**
 * Descriptor for standard configuration items with primitive or simple types.
 * 
 * Represents configuration properties declared with annotations like @StringProperty,
 * @IntProperty, @BooleanProperty, etc. These items have direct default values
 * and straightforward storage semantics.
 * 
 * @param T The type of the configuration value (String, Int, Boolean, etc.).
 * @property defaultValue The default value used when no stored value exists.
 * @property panelType The UI panel type for rendering this item in admin interfaces.
 * @property dataType The underlying data type for storage and validation.
 */
data class StandardConfigItem<T>(
    override val key: String,
    override val groupName: String,
    override val description: String,
    val defaultValue: T,
    val panelType: PanelType,
    val dataType: DataType,
    override val getCurrentValue: () -> T,
    override val updateValue: (newValue: T) -> Unit,
    override val resetToDefault: () -> Unit
) : ConfigItemDescriptor<T>

/**
 * Descriptor for option-based configuration items using sealed class hierarchies.
 * 
 * Represents configuration properties declared with @OptionProperty that provide
 * a finite set of predefined choices. Each choice is identified by a unique
 * integer ID for efficient storage and comparison.
 * 
 * @param T The sealed class type representing the available options.
 * @property defaultOptionId The ID of the default option when no value is stored.
 * @property choices List of available option descriptors with their metadata.
 */
data class OptionConfigItem<T>(
    override val key: String,
    override val groupName: String,
    override val description: String,
    val defaultOptionId: Int,
    val choices: List<OptionItemDescriptor<T>>,
    override val getCurrentValue: () -> T,
    override val updateValue: (newValue: T) -> Unit,
    override val resetToDefault: () -> Unit
) : ConfigItemDescriptor<T>

/**
 * Descriptor for individual options within an option-based configuration item.
 * 
 * Represents a single selectable choice within a sealed class hierarchy,
 * providing the mapping between storage IDs and actual option instances.
 * 
 * @param T The type of the option instance.
 * @property optionId Unique integer identifier for storage and comparison.
 * @property description Human-readable description for UI display.
 * @property option The actual option instance (e.g., Theme.Light, Theme.Dark).
 */
data class OptionItemDescriptor<T>(
    val optionId: Int,
    val description: String,
    val option: T
)