package io.github.mambawow.appconfig

/**
 * Supported data types for configuration properties.
 * 
 * Used internally by the AppConfig processor to determine storage mechanisms,
 * validation rules, and UI components for each property type.
 * 
 * Mapping:
 * - [STRING] → @StringProperty
 * - [BOOLEAN] → @BooleanProperty
 * - [INT] → @IntProperty
 * - [LONG] → @LongProperty
 * - [FLOAT] → @FloatProperty
 * - [DOUBLE] → @DoubleProperty
 * - [OPTION] → @OptionProperty with sealed classes
 */
enum class DataType {
    /** String values with UTF-8 encoding */
    STRING,
    
    /** Boolean toggle values for feature flags */
    BOOLEAN,
    
    /** 32-bit signed integers */
    INT,
    
    /** 64-bit signed integers */
    LONG,
    
    /** 32-bit floating point numbers */
    FLOAT,
    
    /** 64-bit floating point numbers */
    DOUBLE,
    
    /** Enumerated choices from sealed class hierarchies */
    OPTION
}