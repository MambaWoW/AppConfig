package io.github.mambawow.appconfig.parser

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toKModifier
import io.github.mambawow.appconfig.*
import io.github.mambawow.appconfig.model.*
import io.github.mambawow.appconfig.ConfigProcessor.Companion.PropertyAnnotationQualifiedNames

/**
 * Parser for analyzing configuration classes annotated with @Config.
 * 
 * This parser transforms KSClassDeclaration instances into ConfigData models
 * that contain all the metadata needed for code generation. It handles:
 * 
 * - Validation of class structure (must be interface)
 * - Extraction of group name from @Config annotation
 * - Convention over configuration (class name as default group name)
 * - Property parsing and validation
 * - Kotlin identifier validation for group names
 * 
 * The parser applies convention over configuration principles - when the
 * groupName in @Config is empty, it defaults to using the class name.
 */
class ConfigClassParser(
    private val logger: KSPLogger,
    private val propertyParser: PropertyParser
) {

    companion object {
        /**
         * Set of reserved Kotlin keywords that cannot be used as identifiers.
         */
        private val KOTLIN_KEYWORDS = setOf(
            // Hard keywords
            "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if",
            "in", "interface", "is", "null", "object", "package", "return", "super", "this",
            "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while",
            
            // Soft keywords and modifiers
            "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally",
            "get", "import", "init", "param", "property", "receiver", "set", "setparam",
            "where", "actual", "abstract", "annotation", "companion", "const", "crossinline",
            "data", "enum", "expect", "external", "final", "infix", "inline", "inner",
            "internal", "lateinit", "noinline", "open", "operator", "out", "override",
            "private", "protected", "public", "reified", "sealed", "suspend", "tailrec",
            "vararg"
        )

        /**
         * Validates whether a string is a valid Kotlin identifier.
         * 
         * @param identifier The string to validate as a Kotlin identifier
         * @return true if the identifier is valid, false otherwise
         */
        fun isValidKotlinIdentifier(identifier: String): Boolean {
            if (identifier.isBlank()) return false
            
            // Check if it's a Kotlin keyword
            if (identifier in KOTLIN_KEYWORDS) return false
            
            // Check first character: must be letter or underscore
            val firstChar = identifier.first()
            if (!firstChar.isLetter() && firstChar != '_') return false
            
            // Check remaining characters: must be letters, digits, or underscores
            return identifier.drop(1).all { char ->
                char.isLetterOrDigit() || char == '_'
            }
        }
    }

    /**
     * Parses a configuration class into a ConfigData model.
     * @param configClass The KSClassDeclaration to parse
     * @return ConfigData model containing all parsed metadata, or null if validation fails
     */
    fun parseConfigClass(configClass: KSClassDeclaration): ConfigData? {
        // Validate basic class requirements
        if (!validateClassStructure(configClass)) {
            return null
        }

        val groupName = extractAndValidateGroupName(configClass) ?: return null
        val packageName = configClass.packageName.asString()
        val className = configClass.simpleName.asString()

        // Parse all annotated properties in the interface
        val properties = parseConfigurationProperties(configClass)
        if (properties.isEmpty()) {
            logger.error("No valid properties found in class '$className'")
        }

        return ConfigData(
            name = className,
            packageName = packageName,
            groupName = groupName,
            properties = properties,
            modifiers = configClass.modifiers.mapNotNull { it.toKModifier() },
            ksFile = configClass.containingFile!!,
            annotations = configClass.annotations.toList()
        )
    }

    /**
     * Validates the basic structure requirements for configuration classes.
     * 
     * Configuration classes must:
     * - Be interfaces (not classes, objects, etc.)
     * - Have a non-empty package name
     * 
     * @param configClass The class declaration to validate
     * @return true if the class meets structural requirements, false otherwise
     */
    private fun validateClassStructure(configClass: KSClassDeclaration): Boolean {
        // Must be an interface
        if (configClass.classKind != ClassKind.INTERFACE) {
            logger.error(ConfigError.ONLY_INTERFACES_CAN_BE_ANNOTATED, configClass)
            return false
        }

        // Must have a package name
        if (configClass.packageName.asString().isEmpty()) {
            logger.error("Interface needs to have a package", configClass)
            return false
        }

        return true
    }

    /**
     * Extracts and validates the group name from the @Config annotation.
     * @param configClass The configuration class with @Config annotation
     * @return Valid group name, or null if validation fails
     */
    private fun extractAndValidateGroupName(configClass: KSClassDeclaration): String? {
        val configAnnotation = configClass.annotations.first { 
            it.shortName.asString() == Config::class.simpleName 
        }
        
        val annotationGroupName = configAnnotation.arguments.firstOrNull { 
            it.name?.asString() == "groupName" 
        }?.value as? String ?: ""

        // Apply convention over configuration: use class name if groupName is empty
        val groupName = if (annotationGroupName.isBlank()) {
            configClass.simpleName.asString()
        } else {
            annotationGroupName
        }

        // Validate that the group name is a valid Kotlin identifier
        if (!isValidKotlinIdentifier(groupName)) {
            logger.error(
                ConfigError.invalidGroupName(configClass.simpleName.asString(), groupName), 
                configClass
            )
            return null
        }

        return groupName
    }

    /**
     * Parses all configuration properties declared in the interface.
     *
     * @param configClass The configuration interface to parse properties from
     * @return List of successfully parsed PropertyData models
     */
    private fun parseConfigurationProperties(configClass: KSClassDeclaration): List<PropertyData> {
        val annotatedProperties = configClass.getDeclaredProperties()
            .filter { property -> 
                // Find properties with configuration property annotations
                property.annotations.any { annotation ->
                    annotation.annotationType.resolve().declaration.qualifiedName?.asString() in PropertyAnnotationQualifiedNames
                }
            }
            .filter { property ->
                // Filter out properties with unresolvable types
                val type = property.type.resolve()
                if (type.isError) {
                    logger.warn(
                        "Property '${property.simpleName.asString()}' in '${configClass.simpleName.asString()}' has a type that could not be resolved. Skipping.",
                        property
                    )
                    false
                } else {
                    true
                }
            }

        // Parse each valid property using the property parser
        return annotatedProperties.mapNotNull { property ->
            propertyParser.parseProperty(property, configClass)
        }.toList()
    }
} 