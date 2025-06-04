package io.github.mambawow.appconfig.test

import io.github.mambawow.appconfig.model.ConfigError
import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Unit tests for ConfigProcessor error handling and validation.
 */
class ConfigProcessorErrorTest {

    @Test
    fun `test ConfigError messages contain expected content`() {
        // Test duplicate group name error
        val duplicateGroupError = ConfigError.duplicateGroupName("TestGroup", "TestConfig")
        assertTrue(duplicateGroupError.contains("TestGroup"))
        assertTrue(duplicateGroupError.contains("TestConfig"))
        assertTrue(duplicateGroupError.contains("Duplicate"))

        // Test missing default value error
        val missingDefaultError = ConfigError.missingDefaultValue("testProperty", "StringProperty")
        assertTrue(missingDefaultError.contains("testProperty"))
        assertTrue(missingDefaultError.contains("defaultValue"))

        // Test duplicate property key error
        val duplicateKeyError = ConfigError.duplicatePropertyKeyInGroup(
            key = "duplicateKey",
            groupName = "TestGroup", 
            propertyName = "property1",
            existingPropertyName = "property2"
        )
        assertTrue(duplicateKeyError.contains("duplicateKey"))
        assertTrue(duplicateKeyError.contains("TestGroup"))
        assertTrue(duplicateKeyError.contains("property1"))
        assertTrue(duplicateKeyError.contains("property2"))
    }

    @Test
    fun `test invalid Kotlin identifier validation`() {
        // Test valid identifiers
        assertTrue(isValidKotlinIdentifier("validName"))
        assertTrue(isValidKotlinIdentifier("_validName"))
        assertTrue(isValidKotlinIdentifier("valid123"))
        assertTrue(isValidKotlinIdentifier("valid_name_123"))

        // Test invalid identifiers
        assertFalse(isValidKotlinIdentifier(""))
        assertFalse(isValidKotlinIdentifier(" "))
        assertFalse(isValidKotlinIdentifier("123invalid"))
        assertFalse(isValidKotlinIdentifier("invalid-name"))
        assertFalse(isValidKotlinIdentifier("invalid.name"))
        assertFalse(isValidKotlinIdentifier("invalid name"))

        // Test Kotlin keywords
        assertFalse(isValidKotlinIdentifier("class"))
        assertFalse(isValidKotlinIdentifier("fun"))
        assertFalse(isValidKotlinIdentifier("var"))
        assertFalse(isValidKotlinIdentifier("val"))
        assertFalse(isValidKotlinIdentifier("if"))
        assertFalse(isValidKotlinIdentifier("when"))
        assertFalse(isValidKotlinIdentifier("object"))
        assertFalse(isValidKotlinIdentifier("interface"))
    }

    @Test
    fun `test option property error messages`() {
        // Test sealed class validation errors
        val notSealedError = ConfigError.mustBeSealedClass("optionProp", "String")
        assertTrue(notSealedError.contains("optionProp"))
        assertTrue(notSealedError.contains("sealed class"))

        val missingOptionError = ConfigError.missingOptionAnnotation("optionProp", "TestOption")
        assertTrue(missingOptionError.contains("optionProp"))
        assertTrue(missingOptionError.contains("TestOption"))
        assertTrue(missingOptionError.contains("@Option"))

        val noDefaultError = ConfigError.noDefaultOption("optionProp")
        assertTrue(noDefaultError.contains("optionProp"))
        assertTrue(noDefaultError.contains("isDefault=true"))

        val multipleDefaultError = ConfigError.multipleDefaultOptions("optionProp")
        assertTrue(multipleDefaultError.contains("optionProp"))
        assertTrue(multipleDefaultError.contains("Only one"))

        val duplicateIdsError = ConfigError.duplicateOptionIds("optionProp")
        assertTrue(duplicateIdsError.contains("optionProp"))
        assertTrue(duplicateIdsError.contains("unique"))
    }

    // Helper method - would be extracted from ConfigClassParser in real implementation
    private fun isValidKotlinIdentifier(identifier: String): Boolean {
        if (identifier.isBlank()) return false
        
        // Kotlin keywords
        val keywords = setOf(
            "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if",
            "in", "interface", "is", "null", "object", "package", "return", "super", "this",
            "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while",
            "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally",
            "get", "import", "init", "param", "property", "receiver", "set", "setparam",
            "where", "actual", "abstract", "annotation", "companion", "const", "crossinline",
            "data", "enum", "expect", "external", "final", "infix", "inline", "inner",
            "internal", "lateinit", "noinline", "open", "operator", "out", "override",
            "private", "protected", "public", "reified", "sealed", "suspend", "tailrec",
            "vararg"
        )
        
        if (identifier in keywords) return false
        
        // Check first character: must be letter or underscore
        val firstChar = identifier.first()
        if (!firstChar.isLetter() && firstChar != '_') return false
        
        // Check remaining characters: must be letters, digits, or underscores
        return identifier.drop(1).all { char ->
            char.isLetterOrDigit() || char == '_'
        }
    }
} 