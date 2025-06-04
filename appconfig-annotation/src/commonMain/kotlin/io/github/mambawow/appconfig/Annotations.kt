package io.github.mambawow.appconfig

/**
 * Marks a class or interface as a configuration group.
 * 
 * Example:
 * ```kotlin
 * @Config(groupName = "Appearance")
 * interface AppearanceConfig {
 *     @BooleanProperty(key = "dark_mode", defaultValue = false)
 *     var isDarkModeEnabled: Boolean
 * }
 * ```
 * 
 * @param groupName The logical name for this configuration group. Defaults to class name if empty.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Config(val groupName: String = "")

/**
 * Declares an integer configuration property with automatic storage management.
 * 
 * @param key The unique storage key for this property. Must be non-empty.
 * @param defaultValue The default value when no stored value exists.
 * @param description Human-readable description of this property.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class IntProperty(
    val key: String = "",
    val defaultValue: Int,
    val description: String = ""
)

/**
 * Declares a long integer configuration property.
 * 
 * @param key The unique storage key for this property. Must be non-empty.
 * @param defaultValue The default value when no stored value exists.
 * @param description Human-readable description of this property.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class LongProperty(
    val key: String = "",
    val defaultValue: Long,
    val description: String = ""
)

/**
 * Declares a single-precision floating point configuration property.
 * 
 * @param key The unique storage key for this property. Must be non-empty.
 * @param defaultValue The default value when no stored value exists.
 * @param description Human-readable description of this property.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class FloatProperty(
    val key: String = "",
    val defaultValue: Float,
    val description: String = ""
)

/**
 * Declares a double-precision floating point configuration property.
 * 
 * @param key The unique storage key for this property. Must be non-empty.
 * @param defaultValue The default value when no stored value exists.
 * @param description Human-readable description of this property.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DoubleProperty(
    val key: String = "",
    val defaultValue: Double,
    val description: String = ""
)

/**
 * Declares a string configuration property with UTF-8 encoding.
 * 
 * @param key The unique storage key for this property. Must be non-empty.
 * @param defaultValue The default value when no stored value exists.
 * @param description Human-readable description of this property.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class StringProperty(
    val key: String = "",
    val defaultValue: String,
    val description: String = ""
)

/**
 * Declares a boolean configuration property for toggle switches and feature flags.
 * 
 * @param key The unique storage key for this property. Must be non-empty.
 * @param defaultValue The default value when no stored value exists.
 * @param description Human-readable description of this property.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class BooleanProperty(
    val key: String = "",
    val defaultValue: Boolean,
    val description: String = ""
)

/**
 * Declares an enumerated choice property using sealed classes with predefined options.
 * 
 * Example:
 * ```kotlin
 * @OptionProperty(key = "FeatureType")
 * var featureType: FeatureType
 * 
 * @Option
 * sealed class FeatureType {
 *     @OptionItem(optionId = 0, isDefault = true)
 *     object TypeA : FeatureType()
 *     
 *     @OptionItem(optionId = 1)
 *     object TypeB : FeatureType()
 *
 *     @OptionItem(optionId = 2)
 *     object TypeC : FeatureType()
 * }
 * ```
 * 
 * @param key The unique storage key for this property. Must be non-empty.
 * @param description Human-readable description of available options.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class OptionProperty(
    val key: String = "",
    val description: String = ""
)

/**
 * Marks a sealed class as an option type for @OptionProperty.
 * 
 * Requirements:
 * - Must be a sealed class or interface
 * - Must have subclasses annotated with @OptionItem
 * - Exactly one subclass must be marked as default
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Option

/**
 * Defines a selectable option within an @Option sealed class hierarchy.
 * 
 * @param optionId Unique integer identifier for storage. Must be unique within the hierarchy.
 * @param description Human-readable description for UI display.
 * @param isDefault Whether this option is the default. Exactly one must be true per hierarchy.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class OptionItem(
    val optionId: Int,
    val description: String = "",
    val isDefault: Boolean = false
)
