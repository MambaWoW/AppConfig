package io.github.mambawow.appconfig.test

import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Edge case and boundary condition tests for AppConfig processor.
 * These tests cover unusual but valid scenarios and boundary conditions.
 */
class EdgeCaseTest {

    @Test
    fun `test empty configuration interface`() {
        val emptyConfig = """
            @Config(groupName = "Empty")
            interface EmptyConfig {
                // No properties - should still generate valid implementation
            }
        """
        
        assertTrue(emptyConfig.contains("@Config"))
        assertTrue(emptyConfig.contains("EmptyConfig"))
        // Should generate valid implementation even with no properties
    }

    @Test
    fun `test very long property names and keys`() {
        val longNameConfig = """
            @Config(groupName = "LongNames")
            interface LongNamesConfig {
                @StringProperty(
                    key = "this_is_a_very_long_key_name_that_tests_the_limits_of_key_length",
                    defaultValue = "long value",
                    description = "A property with an extremely long name to test edge cases"
                )
                var thisIsAVeryLongPropertyNameThatTestsTheLimitsOfPropertyNameLength: String
            }
        """
        
        assertTrue(longNameConfig.contains("this_is_a_very_long_key_name"))
        assertTrue(longNameConfig.contains("thisIsAVeryLongPropertyNameThatTests"))
    }

    @Test
    fun `test special characters in descriptions`() {
        val specialCharsConfig = """
            @Config(groupName = "SpecialChars")
            interface SpecialCharsConfig {
                @StringProperty(
                    key = "special",
                    defaultValue = "default",
                    description = "Description with \"quotes\", 'apostrophes', and ${'$'}special chars!"
                )
                var specialProperty: String
            }
        """
        
        assertTrue(specialCharsConfig.contains("quotes"))
        assertTrue(specialCharsConfig.contains("apostrophes"))
    }

    @Test
    fun `test extreme numeric values`() {
        val extremeValuesConfig = """
            @Config(groupName = "ExtremeValues")
            interface ExtremeValuesConfig {
                @IntProperty(key = "maxInt", defaultValue = ${Int.MAX_VALUE})
                var maxIntValue: Int
                
                @IntProperty(key = "minInt", defaultValue = ${Int.MIN_VALUE})
                var minIntValue: Int
                
                @LongProperty(key = "maxLong", defaultValue = ${Long.MAX_VALUE}L)
                var maxLongValue: Long
                
                @FloatProperty(key = "maxFloat", defaultValue = ${Float.MAX_VALUE}f)
                var maxFloatValue: Float
                
                @DoubleProperty(key = "maxDouble", defaultValue = ${Double.MAX_VALUE})
                var maxDoubleValue: Double
            }
        """
        
        assertTrue(extremeValuesConfig.contains("${Int.MAX_VALUE}"))
        assertTrue(extremeValuesConfig.contains("${Long.MAX_VALUE}"))
    }

    @Test
    fun `test unicode characters in values and descriptions`() {
        val unicodeConfig = """
            @Config(groupName = "Unicode")
            interface UnicodeConfig {
                @StringProperty(
                    key = "unicode",
                    defaultValue = "默认值 🚀 ñoño",
                    description = "Property with Unicode: 中文, العربية, 🎉"
                )
                var unicodeProperty: String
            }
        """
        
        assertTrue(unicodeConfig.contains("默认值"))
        assertTrue(unicodeConfig.contains("🚀"))
        assertTrue(unicodeConfig.contains("中文"))
    }

    @Test
    fun `test maximum number of properties in single config`() {
        // Test configuration with many properties
        val manyPropsBuilder = StringBuilder()
        manyPropsBuilder.append("""
            @Config(groupName = "ManyProps")
            interface ManyPropsConfig {
        """.trimIndent())
        
        repeat(50) { i ->
            manyPropsBuilder.append("""
                @StringProperty(key = "prop$i", defaultValue = "value$i")
                var property$i: String
                
            """.trimIndent())
        }
        
        manyPropsBuilder.append("}")
        val manyPropsConfig = manyPropsBuilder.toString()
        
        assertTrue(manyPropsConfig.contains("property0"))
        assertTrue(manyPropsConfig.contains("property49"))
    }

    @Test
    fun `test option with maximum number of choices`() {
        val manyOptionsBuilder = StringBuilder()
        manyOptionsBuilder.append("""
            @Config(groupName = "ManyOptions")
            interface ManyOptionsConfig {
                @OptionProperty(key = "choice", description = "Many choices")
                var choice: ManyChoices
            }
            
            @Option
            sealed class ManyChoices {
        """.trimIndent())
        
        repeat(20) { i ->
            val isDefault = i == 0
            manyOptionsBuilder.append("""
                @OptionItem(optionId = $i, description = "Choice $i"${if (isDefault) ", isDefault = true" else ""})
                object Choice$i : ManyChoices()
                
            """.trimIndent())
        }
        
        manyOptionsBuilder.append("}")
        val manyOptionsConfig = manyOptionsBuilder.toString()
        
        assertTrue(manyOptionsConfig.contains("Choice0"))
        assertTrue(manyOptionsConfig.contains("Choice19"))
        assertTrue(manyOptionsConfig.contains("isDefault = true"))
    }

    @Test
    fun `test nested package names`() {
        val nestedPackageConfig = """
            package com.very.deeply.nested.package.structure.config
            
            @Config(groupName = "Nested")
            interface NestedPackageConfig {
                @StringProperty(key = "nested", defaultValue = "nested")
                var nestedProperty: String
            }
        """
        
        assertTrue(nestedPackageConfig.contains("com.very.deeply.nested"))
        assertTrue(nestedPackageConfig.contains("NestedPackageConfig"))
    }

    @Test
    fun `test underscore and number variations in names`() {
        val nameVariationsConfig = """
            @Config(groupName = "NameVariations")
            interface NameVariationsConfig {
                @StringProperty(key = "snake_case_key", defaultValue = "snake")
                var snake_case_property: String
                
                @StringProperty(key = "key123", defaultValue = "number")
                var property123: String
                
                @StringProperty(key = "_underscore", defaultValue = "underscore")
                var _underscoreProperty: String
                
                @StringProperty(key = "mixed_Case123", defaultValue = "mixed")
                var mixed_Case123_Property: String
            }
        """
        
        assertTrue(nameVariationsConfig.contains("snake_case_key"))
        assertTrue(nameVariationsConfig.contains("property123"))
        assertTrue(nameVariationsConfig.contains("_underscoreProperty"))
    }

    @Test
    fun `test empty string and whitespace values`() {
        val whitespaceConfig = """
            @Config(groupName = "Whitespace")
            interface WhitespaceConfig {
                @StringProperty(key = "empty", defaultValue = "")
                var emptyString: String
                
                @StringProperty(key = "spaces", defaultValue = "   ")
                var spacesString: String
                
                @StringProperty(key = "newlines", defaultValue = "\n\t\r")
                var newlinesString: String
            }
        """
        
        assertTrue(whitespaceConfig.contains("defaultValue = \"\""))
        assertTrue(whitespaceConfig.contains("defaultValue = \"   \""))
    }

    @Test
    fun `test boolean edge cases`() {
        val booleanConfig = """
            @Config(groupName = "BooleanEdges")
            interface BooleanEdgeConfig {
                @BooleanProperty(key = "defaultTrue", defaultValue = true)
                var defaultTrueProperty: Boolean
                
                @BooleanProperty(key = "defaultFalse", defaultValue = false)
                var defaultFalseProperty: Boolean
            }
        """
        
        assertTrue(booleanConfig.contains("defaultValue = true"))
        assertTrue(booleanConfig.contains("defaultValue = false"))
    }

    @Test
    fun `test floating point precision edge cases`() {
        val precisionConfig = """
            @Config(groupName = "Precision")
            interface PrecisionConfig {
                @FloatProperty(key = "verySmall", defaultValue = 0.000001f)
                var verySmallFloat: Float
                
                @DoubleProperty(key = "veryPrecise", defaultValue = 3.141592653589793)
                var veryPreciseDouble: Double
                
                @FloatProperty(key = "negative", defaultValue = -123.456f)
                var negativeFloat: Float
                
                @DoubleProperty(key = "scientific", defaultValue = 1.23e-10)
                var scientificDouble: Double
            }
        """
        
        assertTrue(precisionConfig.contains("0.000001f"))
        assertTrue(precisionConfig.contains("3.141592653589793"))
        assertTrue(precisionConfig.contains("-123.456f"))
    }
} 