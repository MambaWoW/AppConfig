package io.github.mambawow.appconfig

/**
 * UI panel types for rendering configuration properties in admin interfaces.
 * 
 * Determines how properties are displayed and interacted with in generated
 * configuration panels. Automatically selected based on data type:
 * - BOOLEAN → [SWITCH]
 * - STRING → [TEXT_INPUT] 
 * - Numeric types → [NUMBER_INPUT]
 * - OPTION → [TOGGLE]
 */
enum class PanelType {
    /**
     * Toggle switch for boolean values.
     * Rendered as iOS-style switch or material toggle.
     */
    SWITCH,

    /**
     * Multi-option selector for enumerated choices.
     * Rendered as segmented control or radio button group.
     */
    TOGGLE,
    
    /**
     * Single-line text input for string values.
     * Supports validation and specialized keyboards.
     */
    TEXT_INPUT,
    
    /**
     * Numeric input with validation and increment/decrement controls.
     * Optimized for numeric entry with appropriate keyboards.
     */
    NUMBER_INPUT
}