package io.github.mambawow.appconfig.panel.ui.screen.adapters

import androidx.compose.runtime.Composable
import io.github.mambawow.appconfig.ConfigItemDescriptor
import io.github.mambawow.appconfig.OptionConfigItem
import io.github.mambawow.appconfig.StandardConfigItem
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.panel.ui.components.ConfigSwitchItem
import io.github.mambawow.appconfig.panel.ui.components.ConfigInputItem
import io.github.mambawow.appconfig.panel.ui.components.ConfigOptionItem
import io.github.mambawow.appconfig.panel.ui.components.ConfigOption

/**
 * Adapter component that renders the appropriate UI for different config item types
 * Uses navigation-based UI for input and option items
 * Following Adapter Pattern and Single Responsibility Principle
 */
@Composable
fun ConfigItemAdapter(
    item: ConfigItemDescriptor<*>,
    currentValue: Any?,
    onValueChange: (Any?) -> Unit,
    onNavigateToInput: (title: String, currentValue: String, isNumeric: Boolean, dataType: DataType, key: String) -> Unit = { _, _, _, _, _ -> },
    onNavigateToOption: (title: String, currentOptionId: Int, key: String) -> Unit = { _, _, _ -> }
) {
    when (item) {
        is StandardConfigItem<*> -> {
            StandardConfigItemAdapter(
                item = item,
                currentValue = currentValue,
                onValueChange = onValueChange,
                onNavigateToInput = onNavigateToInput
            )
        }
        is OptionConfigItem<*> -> {
            OptionConfigItemAdapter(
                item = item,
                currentValue = currentValue,
                onValueChange = onValueChange,
                onNavigateToOption = onNavigateToOption
            )
        }
    }
}

/**
 * Adapter for standard configuration items (boolean, string, numeric)
 */
@Composable
private fun StandardConfigItemAdapter(
    item: StandardConfigItem<*>,
    currentValue: Any?,
    onValueChange: (Any?) -> Unit,
    onNavigateToInput: (title: String, currentValue: String, isNumeric: Boolean, dataType: DataType, key: String) -> Unit
) {
    when (item.dataType) {
        DataType.BOOLEAN -> {
            ConfigSwitchItem(
                title = item.key,
                description = item.description,
                currentValue = currentValue as? Boolean ?: false,
                onValueChange = onValueChange
            )
        }
        
        DataType.STRING, DataType.INT, DataType.LONG, 
        DataType.FLOAT, DataType.DOUBLE -> {
            val isNumeric = item.dataType in listOf(
                DataType.INT, DataType.LONG, DataType.FLOAT, DataType.DOUBLE
            )
            
            ConfigInputItem(
                title = item.key,
                description = item.description,
                currentValue = currentValue?.toString() ?: "",
                isNumeric = isNumeric,
                onNavigateToInput = { title, currentValue, isNumeric ->
                    onNavigateToInput(title, currentValue, isNumeric, item.dataType, item.key)
                }
            )
        }
        
        else -> {
            // Handle unsupported types with a simple text input
            ConfigInputItem(
                title = item.key,
                description = item.description,
                currentValue = currentValue?.toString() ?: "",
                isNumeric = false,
                onNavigateToInput = { title, currentValue, isNumeric ->
                    onNavigateToInput(title, currentValue, isNumeric, DataType.STRING, item.key)
                }
            )
        }
    }
}

/**
 * Adapter for option configuration items
 */
@Composable
private fun <T> OptionConfigItemAdapter(
    item: OptionConfigItem<T>,
    currentValue: Any?,
    onValueChange: (Any?) -> Unit,
    onNavigateToOption: (title: String, currentOptionId: Int, key: String) -> Unit
) {
    val currentOptionId = item.choices.find { it.option == currentValue  }?.optionId ?: item.defaultOptionId
    val options = ConfigOption.fromOptionItemDescriptors(item.choices)
    val currentOption = options.find { it.id == currentOptionId }
    
    ConfigOptionItem(
        title = item.key,
        description = item.description,
        currentOptionId = currentOptionId,
        currentOptionDescription = currentOption?.description ?: "",
        onNavigateToOption = { title, currentOptionId ->
            onNavigateToOption(title, currentOptionId, item.key)
        }
    )
}

/**
 * Convert string input to the appropriate type based on DataType
 * Enhanced with better type safety and validation
 */
fun convertStringToType(value: String, dataType: DataType): Any? {
    if (value.isBlank() && dataType != DataType.STRING) {
        return getDefaultValueForDataType(dataType)
    }
    
    return try {
        when (dataType) {
            DataType.BOOLEAN -> {
                when (value.lowercase()) {
                    "true", "1", "yes", "on" -> true
                    "false", "0", "no", "off" -> false
                    else -> value.toBooleanStrictOrNull() ?: false
                }
            }
            DataType.STRING -> value
            DataType.INT -> {
                value.toIntOrNull() ?: run {
                    // Try to parse as double first, then convert to int
                    value.toDoubleOrNull()?.toInt() ?: 0
                }
            }
            DataType.LONG -> {
                value.toLongOrNull() ?: run {
                    // Try to parse as double first, then convert to long
                    value.toDoubleOrNull()?.toLong() ?: 0L
                }
            }
            DataType.FLOAT -> {
                value.toFloatOrNull() ?: run {
                    // Handle case where user might input with different decimal separator
                    value.replace(",", ".").toFloatOrNull() ?: 0f
                }
            }
            DataType.DOUBLE -> {
                value.toDoubleOrNull() ?: run {
                    // Handle case where user might input with different decimal separator
                    value.replace(",", ".").toDoubleOrNull() ?: 0.0
                }
            }
            else -> value
        }
    } catch (e: NumberFormatException) {
        // Return sensible default for the type
        getDefaultValueForDataType(dataType)
    } catch (e: Exception) {
        // For any other exception, return the original string
        value
    }
}

/**
 * Get default value for a given DataType
 */
private fun getDefaultValueForDataType(dataType: DataType): Any {
    return when (dataType) {
        DataType.BOOLEAN -> false
        DataType.STRING -> ""
        DataType.INT -> 0
        DataType.LONG -> 0L
        DataType.FLOAT -> 0f
        DataType.DOUBLE -> 0.0
        else -> ""
    }
} 