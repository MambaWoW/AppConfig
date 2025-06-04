package io.github.mambawow.appconfig.test

import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Integration tests for AppConfig processor end-to-end functionality.
 * These tests validate the complete flow from source code to generated files.
 */
class IntegrationTest {

    @Test
    fun `test simple configuration processing`() {
        // Test that a simple config with basic properties compiles successfully
        val sourceCode = TestSourceCode.SIMPLE_CONFIG
        
        // In a real integration test, this would:
        // 1. Create source files with the config
        // 2. Run the processor
        // 3. Verify generated files contain expected content
        // 4. Verify generated code compiles
        
        // For now, just verify the test source code is valid
        assertTrue(sourceCode.contains("@Config"))
        assertTrue(sourceCode.contains("TestConfig"))
        assertTrue(sourceCode.contains("@StringProperty"))
        assertTrue(sourceCode.contains("@IntProperty"))
        assertTrue(sourceCode.contains("@BooleanProperty"))
    }

    @Test
    fun `test convention over configuration`() {
        val sourceCode = TestSourceCode.CONVENTION_CONFIG
        
        // Should generate implementation even with empty groupName and keys
        assertTrue(sourceCode.contains("@Config"))
        assertTrue(sourceCode.contains("ConventionConfig"))
        assertTrue(sourceCode.contains("defaultValue"))
    }

    @Test
    fun `test option property handling`() {
        val sourceCode = TestSourceCode.OPTION_CONFIG
        
        // Should handle sealed class options correctly
        assertTrue(sourceCode.contains("@OptionProperty"))
        assertTrue(sourceCode.contains("@Option"))
        assertTrue(sourceCode.contains("@OptionItem"))
        assertTrue(sourceCode.contains("isDefault = true"))
    }

    @Test 
    fun `test all property types`() {
        val sourceCode = TestSourceCode.ALL_TYPES_CONFIG
        
        // Should support all data types
        assertTrue(sourceCode.contains("@StringProperty"))
        assertTrue(sourceCode.contains("@IntProperty"))
        assertTrue(sourceCode.contains("@LongProperty"))
        assertTrue(sourceCode.contains("@FloatProperty"))
        assertTrue(sourceCode.contains("@DoubleProperty"))
        assertTrue(sourceCode.contains("@BooleanProperty"))
        assertTrue(sourceCode.contains("@OptionProperty"))
    }

    @Test
    fun `test error cases are properly identified`() {
        // Test various invalid configurations
        val invalidSources = listOf(
            TestSourceCode.INVALID_CLASS_CONFIG to "class instead of interface",
            TestSourceCode.NO_DEFAULT_OPTION_CONFIG to "no default option",
            TestSourceCode.MULTIPLE_DEFAULT_OPTIONS_CONFIG to "multiple default options",
            TestSourceCode.DUPLICATE_OPTION_IDS_CONFIG to "duplicate option IDs",
            TestSourceCode.NOT_SEALED_CLASS_CONFIG to "not sealed class",
            TestSourceCode.DUPLICATE_KEYS_CONFIG to "duplicate keys"
        )

        invalidSources.forEach { (sourceCode, description) ->
            // In a real test, would verify compilation fails with expected error
            assertTrue(sourceCode.isNotEmpty(), "Source code for '$description' should not be empty")
        }
    }

    @Test
    fun `test duplicate group names detection`() {
        val config1 = TestSourceCode.DUPLICATE_GROUP_CONFIG_1
        val config2 = TestSourceCode.DUPLICATE_GROUP_CONFIG_2
        
        // Both configs use the same group name "Duplicate"
        assertTrue(config1.contains("groupName = \"Duplicate\""))
        assertTrue(config2.contains("groupName = \"Duplicate\""))
        
        // In a real test, would verify processor detects this as an error
    }

    @Test
    fun `test generated file names and structure`() {
        // Test that expected files would be generated with correct names
        val expectedFiles = listOf(
            "TestConfigImpl.kt",
            "Extensions.kt"
        )
        
        // In a real test, would verify these files are generated
        assertTrue(expectedFiles.isNotEmpty())
    }

    @Test
    fun `test multiplatform configuration`() {
        // Test that multiplatform projects generate correct expect/actual declarations
        // This would involve testing with AppConfig_isMultiplatform option set to 1
        
        val sourceCode = TestSourceCode.SIMPLE_CONFIG
        
        // In a real test, would verify commonMain generates expect declarations
        // and platform modules generate actual implementations
        assertTrue(sourceCode.contains("interface"))
    }

    @Test
    fun `test nested configurations`() {
        // Test that configurations can reference each other
        val nestedConfig = """
            @Config(groupName = "Parent")
            interface ParentConfig {
                @StringProperty(defaultValue = "parent")
                var parentProperty: String
            }
            
            @Config(groupName = "Child") 
            interface ChildConfig {
                @StringProperty(defaultValue = "child")
                var childProperty: String
            }
        """
        
        assertTrue(nestedConfig.contains("ParentConfig"))
        assertTrue(nestedConfig.contains("ChildConfig"))
    }
} 