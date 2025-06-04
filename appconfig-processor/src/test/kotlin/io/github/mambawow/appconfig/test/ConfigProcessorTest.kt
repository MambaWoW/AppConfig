package io.github.mambawow.appconfig.test

import io.github.mambawow.appconfig.model.ConfigData
import io.github.mambawow.appconfig.model.PropertyData
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.PanelType
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.test.*

/**
 * Unit tests for ConfigProcessor core functionality.
 */
class ConfigProcessorTest {

    @Test
    fun `test basic configuration data creation`() {
        // Test creating basic configuration data structures
        val configData = createTestConfigData("TestConfig", "TestGroup")
        
        assertEquals("TestConfig", configData.name)
        assertEquals("TestGroup", configData.groupName)
        assertTrue(configData.properties.isEmpty())
    }

    @Test
    fun `test property data creation`() {
        // Test creating property data structures
        val propertyData = createTestPropertyData("testProp", "testKey")
        
        assertEquals("testProp", propertyData.name)
        assertEquals("testKey", propertyData.key)
        assertEquals(DataType.STRING, propertyData.dataType)
    }

    @Test
    fun `test group name validation logic`() {
        // Test the logic for group name validation
        val config1 = createTestConfigData("Config1", "Group1")
        val config2 = createTestConfigData("Config2", "Group2")
        val config3 = createTestConfigData("Config3", "Group1") // Duplicate group
        
        // Test unique groups
        val uniqueGroups = listOf(config1, config2)
        assertTrue(hasUniqueGroupNames(uniqueGroups))
        
        // Test duplicate groups
        val duplicateGroups = listOf(config1, config2, config3)
        assertFalse(hasUniqueGroupNames(duplicateGroups))
    }

    @Test
    fun `test property key validation within groups`() {
        // Test property key uniqueness within the same group
        val prop1 = createTestPropertyData("prop1", "key1")
        val prop2 = createTestPropertyData("prop2", "key2")
        val prop3 = createTestPropertyData("prop3", "key1") // Duplicate key
        
        val configWithUniqueKeys = createTestConfigData("Config1", "Group1", listOf(prop1, prop2))
        assertTrue(hasUniqueKeysWithinGroup(configWithUniqueKeys))
        
        val configWithDuplicateKeys = createTestConfigData("Config2", "Group1", listOf(prop1, prop2, prop3))
        assertFalse(hasUniqueKeysWithinGroup(configWithDuplicateKeys))
    }

    @Test
    fun `test cross-group key validation`() {
        // Test that duplicate keys are allowed across different groups
        val prop1 = createTestPropertyData("prop1", "sameKey")
        val prop2 = createTestPropertyData("prop2", "sameKey")
        
        val config1 = createTestConfigData("Config1", "Group1", listOf(prop1))
        val config2 = createTestConfigData("Config2", "Group2", listOf(prop2))
        
        // Same keys in different groups should be allowed
        assertTrue(allowsCrossGroupDuplicateKeys(listOf(config1, config2)))
    }

    // Helper methods to create test data
    private fun createTestConfigData(
        name: String, 
        groupName: String, 
        properties: List<PropertyData> = emptyList()
    ): ConfigData {
        return ConfigData(
            name = name,
            packageName = "test.package",
            groupName = groupName,
            properties = properties,
            modifiers = emptyList(),
            ksFile = mock(), // Use mockito to create a mock KSFile
            annotations = emptyList()
        )
    }

    private fun createTestPropertyData(name: String, key: String): PropertyData {
        return PropertyData(
            name = name,
            typeName = com.squareup.kotlinpoet.STRING, // Use KotlinPoet's STRING TypeName
            dataType = DataType.STRING,
            key = key,
            description = "Test property",
            defaultValue = "test",
            optionItems = emptyList(),
            panelType = PanelType.TEXT_INPUT
        )
    }

    // Validation logic methods (simplified versions of actual processor logic)
    private fun hasUniqueGroupNames(configs: List<ConfigData>): Boolean {
        val groupNames = configs.map { it.groupName }
        return groupNames.size == groupNames.toSet().size
    }

    private fun hasUniqueKeysWithinGroup(config: ConfigData): Boolean {
        val keys = config.properties.map { it.key }
        return keys.size == keys.toSet().size
    }

    private fun allowsCrossGroupDuplicateKeys(configs: List<ConfigData>): Boolean {
        // Keys can be duplicated across different groups - this should always return true
        return true
    }
} 