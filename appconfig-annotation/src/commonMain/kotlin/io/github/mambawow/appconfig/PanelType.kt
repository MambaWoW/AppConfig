package io.github.mambawow.appconfig

/**
 * @author Frank
 * @created 5/25/25
 */
enum class PanelType {
    /** Typically for Boolean types, rendered as a toggle switch. */
    SWITCH,

    TOGGLE,
    /** For general single-line string input. */
    TEXT_INPUT,
    /** For numeric types (Int, Long, Float, Double), often with validation or specific keyboards. */
    NUMBER_INPUT
}