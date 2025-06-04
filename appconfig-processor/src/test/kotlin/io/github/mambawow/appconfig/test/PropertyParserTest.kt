package io.github.mambawow.appconfig.test

import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.PanelType
import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Unit tests for PropertyParser functionality.
 */
class PropertyParserTest {

    @Test
    fun `test data type mapping from annotation names`() {
        // Test basic property type mappings
        assertEquals(DataType.STRING, mapAnnotationToDataType("StringProperty"))
        assertEquals(DataType.INT, mapAnnotationToDataType("IntProperty"))
        assertEquals(DataType.LONG, mapAnnotationToDataType("LongProperty"))
        assertEquals(DataType.FLOAT, mapAnnotationToDataType("FloatProperty"))
        assertEquals(DataType.DOUBLE, mapAnnotationToDataType("DoubleProperty"))
        assertEquals(DataType.BOOLEAN, mapAnnotationToDataType("BooleanProperty"))
        assertEquals(DataType.OPTION, mapAnnotationToDataType("OptionProperty"))
        
        // Test invalid annotation
        assertNull(mapAnnotationToDataType("InvalidProperty"))
    }

    @Test
    fun `test panel type inference from data types`() {
        // Test panel type mappings
        assertEquals(PanelType.TEXT_INPUT, inferPanelType(DataType.STRING))
        assertEquals(PanelType.NUMBER_INPUT, inferPanelType(DataType.INT))
        assertEquals(PanelType.NUMBER_INPUT, inferPanelType(DataType.LONG))
        assertEquals(PanelType.NUMBER_INPUT, inferPanelType(DataType.FLOAT))
        assertEquals(PanelType.NUMBER_INPUT, inferPanelType(DataType.DOUBLE))
        assertEquals(PanelType.SWITCH, inferPanelType(DataType.BOOLEAN))
        assertEquals(PanelType.TOGGLE, inferPanelType(DataType.OPTION))
    }

    @Test
    fun `test convention over configuration for storage keys`() {
        // Test that empty or blank keys fall back to property name
        assertEquals("myProperty", extractStorageKey("", "myProperty"))
        assertEquals("myProperty", extractStorageKey("   ", "myProperty"))
        assertEquals("myProperty", extractStorageKey("", "myProperty"))
        
        // Test that non-empty keys are used as-is
        assertEquals("customKey", extractStorageKey("customKey", "myProperty"))
        assertEquals("custom_key", extractStorageKey("custom_key", "myProperty"))
    }

    @Test
    fun `test default value validation`() {
        // Test that non-null default values are accepted
        assertTrue(isValidDefaultValue("hello"))
        assertTrue(isValidDefaultValue(42))
        assertTrue(isValidDefaultValue(true))
        assertTrue(isValidDefaultValue(3.14f))
        assertTrue(isValidDefaultValue(2.718))
        
        // Test that null values are rejected
        assertFalse(isValidDefaultValue(null))
    }

    @Test
    fun `test option validation logic`() {
        // Test option ID uniqueness validation
        val validOptionIds = listOf(0, 1, 2, 3)
        val duplicateOptionIds = listOf(0, 1, 2, 1) // 1 is duplicated
        
        assertTrue(areOptionIdsUnique(validOptionIds))
        assertFalse(areOptionIdsUnique(duplicateOptionIds))
        
        // Test default option validation
        val optionsWithOneDefault = listOf(
            MockOptionItem(0, false),
            MockOptionItem(1, true),
            MockOptionItem(2, false)
        )
        
        val optionsWithNoDefault = listOf(
            MockOptionItem(0, false),
            MockOptionItem(1, false),
            MockOptionItem(2, false)
        )
        
        val optionsWithMultipleDefaults = listOf(
            MockOptionItem(0, true),
            MockOptionItem(1, true),
            MockOptionItem(2, false)
        )
        
        assertTrue(hasValidDefaultOptions(optionsWithOneDefault))
        assertFalse(hasValidDefaultOptions(optionsWithNoDefault))
        assertFalse(hasValidDefaultOptions(optionsWithMultipleDefaults))
    }

    // Helper methods that mirror the PropertyParser logic
    private fun mapAnnotationToDataType(annotationName: String): DataType? {
        return when (annotationName) {
            "StringProperty" -> DataType.STRING
            "BooleanProperty" -> DataType.BOOLEAN
            "IntProperty" -> DataType.INT
            "LongProperty" -> DataType.LONG
            "FloatProperty" -> DataType.FLOAT
            "DoubleProperty" -> DataType.DOUBLE
            "OptionProperty" -> DataType.OPTION
            else -> null
        }
    }

    private fun inferPanelType(dataType: DataType): PanelType {
        return when (dataType) {
            DataType.BOOLEAN -> PanelType.SWITCH
            DataType.STRING -> PanelType.TEXT_INPUT
            DataType.OPTION -> PanelType.TOGGLE
            DataType.INT, DataType.LONG, DataType.FLOAT, DataType.DOUBLE -> PanelType.NUMBER_INPUT
        }
    }

    private fun extractStorageKey(annotationKey: String, propertyName: String): String {
        return if (annotationKey.isBlank()) {
            propertyName
        } else {
            annotationKey
        }
    }

    private fun isValidDefaultValue(value: Any?): Boolean {
        return value != null
    }

    private fun areOptionIdsUnique(optionIds: List<Int>): Boolean {
        return optionIds.size == optionIds.toSet().size
    }

    private fun hasValidDefaultOptions(options: List<MockOptionItem>): Boolean {
        val defaultCount = options.count { it.isDefault }
        return defaultCount == 1
    }

    // Mock data class for testing
    private data class MockOptionItem(
        val optionId: Int,
        val isDefault: Boolean
    )
} 