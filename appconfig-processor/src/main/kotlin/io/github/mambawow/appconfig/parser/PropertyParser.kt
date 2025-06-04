package io.github.mambawow.appconfig.parser

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.mambawow.appconfig.*
import io.github.mambawow.appconfig.ConfigProcessor.Companion.PropertyAnnotationQualifiedNames
import io.github.mambawow.appconfig.model.*

/**
 * Parser for analyzing individual configuration properties.
 * 
 * This parser transforms KSPropertyDeclaration instances annotated with configuration
 * property annotations into PropertyData models.
 * 
 * The parser supports all configuration property types:
 * - StringProperty, BooleanProperty, IntProperty, LongProperty, FloatProperty, DoubleProperty
 * - OptionProperty (for sealed class-based enumeration types)
 */
class PropertyParser(
    private val logger: KSPLogger
) {

    /**
     * Parses a configuration property into a PropertyData model.
     * 
     * This method performs comprehensive analysis of a property annotated with
     * a configuration property annotation:
     * 1. Identifies the annotation type and extracts parameters
     * 2. Applies convention over configuration for the storage key
     * 3. Determines the appropriate data type
     * 4. Parses default values and option items (for option properties)
     * 5. Infers the appropriate UI panel type
     * 
     * @param property The property declaration to parse
     * @param containingClass The class containing this property (for context)
     * @return PropertyData model with all parsed metadata, or null if parsing fails
     */
    fun parseProperty(property: KSPropertyDeclaration, containingClass: KSClassDeclaration): PropertyData? {
        val propertyAnnotation = property.annotations.first { 
            it.annotationType.resolve().declaration.qualifiedName?.asString() in PropertyAnnotationQualifiedNames
        }
        
        val annotationName = propertyAnnotation.shortName.asString()
        val annotationArguments = propertyAnnotation.arguments.associateBy { it.name!!.asString() }
        
        // Apply convention over configuration: use property name as key if annotation key is empty
        val storageKey = extractStorageKey(annotationArguments, property)
        val description = annotationArguments["description"]?.value as? String ?: ""
        val propertyName = property.simpleName.asString()
        val propertyKSType = property.type.resolve()
        val propertyTypeName = propertyKSType.toTypeName()
        
        // Determine the internal data type classification
        val dataType = determineDataType(annotationName, propertyName) ?: return null
        
        // Parse default value and option items based on data type
        val (defaultValue, optionItems) = when (dataType) {
            DataType.OPTION -> parseOptionProperty(property, propertyKSType, propertyName)
            else -> parsePrimitiveProperty(annotationArguments, annotationName, propertyName)
        } ?: return null
        
        val panelType = inferPanelTypeFromDataType(dataType)
        
        return PropertyData(
            name = propertyName,
            typeName = propertyTypeName,
            dataType = dataType,
            key = storageKey,
            description = description,
            defaultValue = defaultValue,
            optionItems = optionItems,
            panelType = panelType
        )
    }
    
    /**
     * Extracts the storage key for the property, applying convention over configuration.
     * 
     * If the 'key' parameter in the annotation is empty or blank, the property name
     * is used as the default key. This follows convention over configuration principles.
     * 
     * @param annotationArguments Map of annotation parameter name to value
     * @param property The property declaration for fallback naming
     * @return The storage key to use for this property
     */
    private fun extractStorageKey(
        annotationArguments: Map<String, Any?>, 
        property: KSPropertyDeclaration
    ): String {
        val keyArgument = annotationArguments["key"]
        val annotationKey = keyArgument as? String ?: ""
        
        return if (annotationKey.isBlank()) {
            property.simpleName.asString()  // Convention: use property name as default
        } else {
            annotationKey
        }
    }
    
    /**
     * Determines the internal data type classification from the annotation name.
     * @param annotationName The simple name of the property annotation
     * @param propertyName The property name for error reporting
     * @return DataType enum value, or null if annotation is unrecognized
     */
    private fun determineDataType(annotationName: String, propertyName: String): DataType? {
        return when (annotationName) {
            StringProperty::class.simpleName -> DataType.STRING
            BooleanProperty::class.simpleName -> DataType.BOOLEAN
            IntProperty::class.simpleName -> DataType.INT
            LongProperty::class.simpleName -> DataType.LONG
            FloatProperty::class.simpleName -> DataType.FLOAT
            DoubleProperty::class.simpleName -> DataType.DOUBLE
            OptionProperty::class.simpleName -> DataType.OPTION
            else -> {
                logger.error(ConfigError.unrecognizedAnnotation(annotationName, propertyName))
                null
            }
        }
    }
    
    /**
     * Parses an option property based on a sealed class structure.
     *
     * The default value returned is the optionId of the default option, which
     * is stored as an integer for efficiency.
     * 
     * @param property The property declaration with @OptionProperty
     * @param propertyKSType The resolved type of the property
     * @param propertyName The property name for error reporting
     * @return Pair of (defaultOptionId, optionItemsList) or null if validation fails
     */
    private fun parseOptionProperty(
        property: KSPropertyDeclaration,
        propertyKSType: KSType,
        propertyName: String
    ): Pair<Int, List<OptionItemData>>? {
        val classDeclaration = propertyKSType.declaration as? KSClassDeclaration
        
        // Validate that the type is a sealed class
        if (classDeclaration == null || !classDeclaration.isSealed()) {
            logger.error(
                ConfigError.mustBeSealedClass(propertyName, propertyKSType.toTypeName().toString()),
                property
            )
            return null
        }
        
        // Validate that the sealed class has @Option annotation
        val hasOptionAnnotation = classDeclaration.annotations.any { 
            it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.mambawow.appconfig.Option"
        }
        if (!hasOptionAnnotation) {
            logger.error(
                ConfigError.missingOptionAnnotation(propertyName, classDeclaration.simpleName.asString()),
                property
            )
            return null
        }
        
        // Parse subclasses as option items
        val subclasses = classDeclaration.getSealedSubclasses().toList()
        if (subclasses.isEmpty()) {
            logger.error(ConfigError.noSubclasses(propertyName), property)
            return null
        }
        
        return parseOptionItems(subclasses, propertyName, property)
    }
    
    /**
     * Parses the subclasses of a sealed class into option items.
     *
     * @param subclasses List of sealed class subclasses
     * @param propertyName Property name for error reporting
     * @param property Property declaration for error location
     * @return Pair of (defaultOptionId, optionItemsList) or null if validation fails
     */
    private fun parseOptionItems(
        subclasses: List<KSClassDeclaration>,
        propertyName: String,
        property: KSPropertyDeclaration
    ): Pair<Int, List<OptionItemData>>? {
        val optionItems = mutableListOf<OptionItemData>()
        var defaultOptionId: Int? = null
        var hasDefaultOption = false
        
        for (subclass in subclasses) {
            val optionItemAnnotation = subclass.annotations.find { 
                it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.mambawow.appconfig.OptionItem"
            }
            
            if (optionItemAnnotation != null) {
                val optionItemData = parseOptionItem(optionItemAnnotation, subclass, propertyName, property)
                    ?: return null
                
                optionItems.add(optionItemData)
                
                // Track default option
                if (optionItemData.isDefault) {
                    if (hasDefaultOption) {
                        logger.error(ConfigError.multipleDefaultOptions(propertyName), property)
                        return null
                    }
                    hasDefaultOption = true
                    defaultOptionId = optionItemData.optionId
                }
            }
        }
        
        // Validate that exactly one default option exists
        if (!hasDefaultOption) {
            logger.error(ConfigError.noDefaultOption(propertyName), property)
            return null
        }
        
        // Validate that all option IDs are unique
        val optionIds = optionItems.map { it.optionId }
        if (optionIds.size != optionIds.toSet().size) {
            logger.error(ConfigError.duplicateOptionIds(propertyName), property)
            return null
        }
        
        return defaultOptionId!! to optionItems
    }
    
    /**
     * Parses a single @OptionItem annotation into an OptionItemData model.
     * 
     * @param annotation The @OptionItem annotation
     * @param subclass The sealed class subclass with this annotation
     * @param propertyName Property name for error reporting
     * @param property Property declaration for error location
     * @return OptionItemData model or null if parsing fails
     */
    private fun parseOptionItem(
        annotation: com.google.devtools.ksp.symbol.KSAnnotation,
        subclass: KSClassDeclaration,
        propertyName: String,
        property: KSPropertyDeclaration
    ): OptionItemData? {
        val annotationArguments = annotation.arguments.associateBy { it.name?.asString() }
        val optionId = annotationArguments["optionId"]?.value as? Int
        val isDefault = annotationArguments["isDefault"]?.value as? Boolean ?: false
        val description = annotationArguments["description"]?.value as? String ?: ""

        if (optionId == null) {
            logger.error(
                ConfigError.missingOptionId(propertyName, subclass.simpleName.asString()),
                property
            )
            return null
        }

        return OptionItemData(
            className = subclass.simpleName.asString(),
            typeName = subclass.asType(emptyList()).toTypeName(),
            optionId = optionId,
            description = description,
            isDefault = isDefault
        )
    }
    
    /**
     * Parses primitive type properties (non-option properties).
     * 
     * Extracts the defaultValue parameter from the annotation and validates
     * that it's present and non-null.
     * 
     * @param annotationArguments Map of annotation parameter name to value
     * @param annotationName The annotation type name for error reporting
     * @param propertyName The property name for error reporting
     * @return Pair of (defaultValue, emptyList) or null if parsing fails
     */
    private fun parsePrimitiveProperty(
        annotationArguments: Map<String, Any?>,
        annotationName: String,
        propertyName: String
    ): Pair<Any, List<OptionItemData>>? {
        val defaultValueArgument = annotationArguments["defaultValue"]
        if (defaultValueArgument == null) {
            logger.error(ConfigError.missingDefaultValue(propertyName, annotationName))
            return null
        }
        
        // Extract the actual default value from the KSP value argument
        val defaultValue = (defaultValueArgument as? com.google.devtools.ksp.symbol.KSValueArgument)?.value
            ?: defaultValueArgument
        
        return defaultValue to emptyList()
    }

    /**
     * Infers the appropriate UI panel type based on the data type.
     * 
     * Maps internal data types to recommended UI panel types for
     * configuration management interfaces.
     * 
     * @param dataType The internal data type classification
     * @return Recommended PanelType for UI presentation
     */
    private fun inferPanelTypeFromDataType(dataType: DataType): PanelType {
        return when (dataType) {
            DataType.BOOLEAN -> PanelType.SWITCH
            DataType.STRING -> PanelType.TEXT_INPUT
            DataType.OPTION -> PanelType.TOGGLE
            DataType.INT, DataType.LONG, DataType.FLOAT, DataType.DOUBLE -> PanelType.NUMBER_INPUT
        }
    }
} 