package io.github.mambawow.appconfig

/**
 * Marks a data class as a configuration container.
 *
 * @param groupName The display name for this group of settings in the configuration UI.
 *                  This name will apply to all configuration items within this class.
 *                  It also serves as a unique identifier for this configuration set (e.g., for storage scoping).
 *                  Must be unique across all @Config annotated classes.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Config(val groupName: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class IntProperty(
    val key: String,
    val defaultValue: Int,
    val description: String = ""
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class LongProperty(
    val key: String,
    val defaultValue: Long,
    val description: String = ""
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class FloatProperty(
    val key: String,
    val defaultValue: Float,
    val description: String = ""
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DoubleProperty(
    val key: String,
    val defaultValue: Double,
    val description: String = ""
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class StringProperty(
    val key: String,
    val defaultValue: String,
    val description: String = ""
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class BooleanProperty(
    val key: String,
    val defaultValue: Boolean,
    val description: String = ""
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class OptionProperty(
    val key: String,
    val description: String = ""
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Option

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class OptionItem(
    val optionId: Int,
    val description: String = "",
    val isDefault: Boolean = false
)
