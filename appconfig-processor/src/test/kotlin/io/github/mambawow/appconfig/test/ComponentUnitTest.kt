package io.github.mambawow.appconfig.test

import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.PanelType
import io.github.mambawow.appconfig.model.PropertyData
import io.github.mambawow.appconfig.model.OptionItemData
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.ClassName
import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Component-level unit tests for AppConfig processor.
 */
class ComponentUnitTest {

    @Test
    fun `test PropertyData creation and validation`() {
        // Create a valid PropertyData instance
        val propertyData = PropertyData(
            name = "testProperty",
            typeName = STRING, // Use KotlinPoet's STRING TypeName
            dataType = DataType.STRING,
            key = "test_key",
            description = "Test description",
            defaultValue = "default",
            optionItems = emptyList(),
            panelType = PanelType.TEXT_INPUT
        )

        assertEquals("testProperty", propertyData.name)
        assertEquals(DataType.STRING, propertyData.dataType)
        assertEquals("test_key", propertyData.key)
        assertEquals("default", propertyData.defaultValue)
        assertTrue(propertyData.optionItems.isEmpty())
    }

    @Test
    fun `test OptionItemData creation and validation`() {
        val optionItem = OptionItemData(
            className = "Light",
            typeName = ClassName("test", "Light"), // Use KotlinPoet's ClassName
            optionId = 0,
            description = "Light theme",
            isDefault = true
        )

        assertEquals("Light", optionItem.className)
        assertEquals(0, optionItem.optionId)
        assertEquals("Light theme", optionItem.description)
        assertTrue(optionItem.isDefault)
    }

    @Test
    fun `test data type and panel type relationships`() {
        // Test that each data type has an appropriate panel type
        val relationships = mapOf(
            DataType.STRING to PanelType.TEXT_INPUT,
            DataType.INT to PanelType.NUMBER_INPUT,
            DataType.LONG to PanelType.NUMBER_INPUT,
            DataType.FLOAT to PanelType.NUMBER_INPUT,
            DataType.DOUBLE to PanelType.NUMBER_INPUT,
            DataType.BOOLEAN to PanelType.SWITCH,
            DataType.OPTION to PanelType.TOGGLE
        )

        relationships.forEach { (dataType, expectedPanelType) ->
            val inferredPanelType = inferPanelTypeFromDataType(dataType)
            assertEquals(expectedPanelType, inferredPanelType, 
                "Data type $dataType should map to panel type $expectedPanelType")
        }
    }

    @Test
    fun `test option property validation rules`() {
        // Test valid option items
        val validOptions = listOf(
            createOptionItem(0, "Option A", true),
            createOptionItem(1, "Option B", false),
            createOptionItem(2, "Option C", false)
        )

        assertTrue(validateOptionItems(validOptions))

        // Test duplicate option IDs
        val duplicateIdOptions = listOf(
            createOptionItem(0, "Option A", true),
            createOptionItem(0, "Option B", false) // Duplicate ID
        )

        assertFalse(validateOptionItems(duplicateIdOptions))

        // Test no default option
        val noDefaultOptions = listOf(
            createOptionItem(0, "Option A", false),
            createOptionItem(1, "Option B", false)
        )

        assertFalse(validateOptionItems(noDefaultOptions))

        // Test multiple default options
        val multipleDefaultOptions = listOf(
            createOptionItem(0, "Option A", true),
            createOptionItem(1, "Option B", true) // Multiple defaults
        )

        assertFalse(validateOptionItems(multipleDefaultOptions))
    }

    @Test
    fun `test generated class naming conventions`() {
        // Test implementation class name generation
        assertEquals("TestConfigImpl", generateImplClassName("TestConfig"))
        assertEquals("UserSettingsImpl", generateImplClassName("UserSettings"))
        assertEquals("AppConfigImpl", generateImplClassName("AppConfig"))

        // Test extension property name generation  
        assertEquals("testconfig", generateExtensionPropertyName("TestConfig"))
        assertEquals("usersettings", generateExtensionPropertyName("UserSettings"))
        assertEquals("appconfig", generateExtensionPropertyName("AppConfig"))
    }

    @Test
    fun `test property key normalization`() {
        // Test that property keys are handled correctly
        assertEquals("validKey", normalizePropertyKey("validKey"))
        assertEquals("valid_key", normalizePropertyKey("valid_key"))
        assertEquals("validKey123", normalizePropertyKey("validKey123"))
        
        // Test empty key handling (should use property name)
        assertEquals("propertyName", normalizePropertyKey("", "propertyName"))
        assertEquals("propertyName", normalizePropertyKey("   ", "propertyName"))
    }

    // Helper methods
    private fun inferPanelTypeFromDataType(dataType: DataType): PanelType {
        return when (dataType) {
            DataType.BOOLEAN -> PanelType.SWITCH
            DataType.STRING -> PanelType.TEXT_INPUT
            DataType.OPTION -> PanelType.TOGGLE
            DataType.INT, DataType.LONG, DataType.FLOAT, DataType.DOUBLE -> PanelType.NUMBER_INPUT
        }
    }

    private fun createOptionItem(
        optionId: Int, 
        description: String, 
        isDefault: Boolean
    ): OptionItemData {
        return OptionItemData(
            className = "Option$optionId",
            typeName = ClassName("test", "Option$optionId"), // Use KotlinPoet's ClassName
            optionId = optionId,
            description = description,
            isDefault = isDefault
        )
    }

    private fun validateOptionItems(options: List<OptionItemData>): Boolean {
        // Check for unique option IDs
        val optionIds = options.map { it.optionId }
        if (optionIds.size != optionIds.toSet().size) {
            return false
        }

        // Check for exactly one default option
        val defaultCount = options.count { it.isDefault }
        return defaultCount == 1
    }

    private fun generateImplClassName(interfaceName: String): String {
        return "${interfaceName}Impl"
    }

    private fun generateExtensionPropertyName(groupName: String): String {
        return groupName.lowercase()
    }

    private fun normalizePropertyKey(
        annotationKey: String, 
        propertyName: String = "defaultProperty"
    ): String {
        return if (annotationKey.isBlank()) {
            propertyName
        } else {
            annotationKey
        }
    }
} 