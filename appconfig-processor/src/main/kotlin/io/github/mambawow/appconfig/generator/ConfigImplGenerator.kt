package io.github.mambawow.appconfig.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.model.*

/**
 * Generator for configuration implementation classes.
 * 
 * This generator creates concrete implementation classes for configuration interfaces
 * annotated with @Config. The generated classes include:
 * 
 * - Property implementations with custom getters and setters
 * - Integration with ConfigStore for persistent storage
 * - Default value management and update methods
 * - Configuration item descriptors for UI generation
 * - Bulk update and reset functionality
 * 
 * For option properties, the generator implements a dual-storage approach:
 * - Internal storage uses integer option IDs for efficiency
 * - Public interface uses strongly-typed sealed class instances
 * - Automatic conversion between IDs and type instances
 * 
 * Generated implementation classes are thread-safe and handle all type
 * conversions, validation, and storage operations transparently.
 */
class ConfigImplGenerator {
    
    private val configStoreClass = ClassName("io.github.mambawow.appconfig.store", "ConfigStore")
    private val appConfigClass = ClassName("io.github.mambawow.appconfig", "AppConfig")
    
    /**
     * Generates a complete implementation class for a configuration interface.
     *
     * @param configData Complete configuration metadata
     * @return TypeSpec for the implementation class
     */
    fun generateConfigImpl(configData: ConfigData): TypeSpec {
        val implementationBuilder = TypeSpec.classBuilder(configData.implName)
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(ClassName(configData.packageName, configData.name))
        
        // Add ConfigStore property for persistent storage
        implementationBuilder.addProperty(
            PropertySpec.builder("store", configStoreClass)
                .addModifiers(KModifier.PRIVATE)
                .initializer("AppConfig.getConfigStore(%S)", configData.groupName)
                .build()
        )
        
        // Generate property implementations
        configData.properties.forEach { property ->
            implementationBuilder.addProperty(generateDefaultValueProperty(property))
            implementationBuilder.addFunction(generateDefaultValueUpdateMethod(property))
            implementationBuilder.addProperty(generatePropertyImplementation(property))
        }
        
        // Generate utility methods
        implementationBuilder.addFunction(generateConfigItemsMethod(configData))
        implementationBuilder.addFunction(generateBulkUpdateMethod(configData))
        implementationBuilder.addFunction(generateResetMethod(configData))
        
        return implementationBuilder.build()
    }
    
    /**
     * Generates a default value storage property for a configuration property.
     *
     * @param property Property metadata
     * @return PropertySpec for the default value property
     */
    private fun generateDefaultValueProperty(property: PropertyData): PropertySpec {
        val defaultValuePropertyName = "${property.name}Default"
        val defaultValueExpression = when (property.dataType) {
            DataType.OPTION -> {
                // Store default option ID as integer for efficiency
                val defaultOptionId = property.defaultValue as Int
                CodeBlock.of("%L", defaultOptionId)
            }
            else -> createDefaultValueCodeBlock(property.dataType, property.defaultValue)
        }
        
        val propertyType = when (property.dataType) {
            DataType.OPTION -> INT // Option defaults stored as Int
            else -> property.typeName
        }
        
        return PropertySpec.builder(defaultValuePropertyName, propertyType)
            .addModifiers(KModifier.PRIVATE)
            .mutable(true)
            .initializer(defaultValueExpression)
            .build()
    }
    
    /**
     * Generates a method to update the default value of a property at runtime.
     * @param property Property metadata
     * @return FunSpec for the default value update method
     */
    private fun generateDefaultValueUpdateMethod(property: PropertyData): FunSpec {
        val defaultValuePropertyName = "${property.name}Default"
        val updateMethodName = "update${property.name.replaceFirstChar { it.titlecase() }}Default"
        
        val functionBuilder = FunSpec.builder(updateMethodName)
            .addModifiers(KModifier.PUBLIC)
            .addParameter("default", property.typeName)
        
        when (property.dataType) {
            DataType.OPTION -> {
                // Convert sealed class instance to option ID for storage
                val mappingCode = CodeBlock.builder()
                    .add("$defaultValuePropertyName = when (default) {\n")
                    .indent()
                    .apply {
                        property.optionItems.forEach { optionItem ->
                            add("is %T -> %L\n", optionItem.typeName, optionItem.optionId)
                        }
                        add("else -> %L\n", property.defaultValue as Int) // fallback to original default
                    }
                    .unindent()
                    .add("}")
                    .build()
                
                functionBuilder.addCode(mappingCode)
            }
            else -> {
                functionBuilder.addStatement("$defaultValuePropertyName = default")
            }
        }
        
        return functionBuilder.build()
    }
    
    /**
     * Generates the main property implementation with custom getter and setter.
     * 
     * For option properties:
     * - Getter reads integer ID from storage and converts to sealed class instance
     * - Setter converts sealed class instance to integer ID for storage
     * - Includes fallback handling for unknown IDs
     * 
     * For primitive properties:
     * - Getter reads value directly from storage with default fallback
     * - Setter writes value directly to storage
     * 
     * @param property Property metadata
     * @return PropertySpec for the property implementation
     */
    private fun generatePropertyImplementation(property: PropertyData): PropertySpec {
        val propertyBuilder = PropertySpec.builder(property.name, property.typeName)
            .addModifiers(KModifier.OVERRIDE)
            .mutable(true)
        
        val defaultValuePropertyName = "${property.name}Default"
        
        when (property.dataType) {
            DataType.OPTION -> {
                val defaultOptionId = property.defaultValue as Int
                
                // Getter: convert stored integer ID to sealed class instance
                val getterCode = CodeBlock.builder()
                    .add("when (store.getInt(%S, %L)) {\n", property.key, defaultValuePropertyName)
                    .indent()
                    .apply {
                        property.optionItems.forEach { optionItem ->
                            add("%L -> %T\n", optionItem.optionId, optionItem.typeName)
                        }
                        // Fallback chain for unknown IDs
                        add("else -> when ($defaultValuePropertyName) {\n")
                        indent()
                        property.optionItems.forEach { optionItem ->
                            add("%L -> %T\n", optionItem.optionId, optionItem.typeName)
                        }
                        // Final fallback to original default option
                        val originalDefaultOption = property.optionItems.find { it.isDefault }?.typeName
                        add("else -> %T\n", originalDefaultOption)
                        unindent()
                        add("}\n")
                    }
                    .unindent()
                    .add("}")
                    .build()
                
                propertyBuilder.getter(
                    FunSpec.getterBuilder()
                        .addCode("return ")
                        .addCode(getterCode)
                        .build()
                )
                
                // Setter: convert sealed class instance to integer ID
                val setterCode = CodeBlock.builder()
                    .add("val optionId = when (value) {\n")
                    .indent()
                    .apply {
                        property.optionItems.forEach { optionItem ->
                            add("is %T -> %L\n", optionItem.typeName, optionItem.optionId)
                        }
                        add("else -> %L\n", defaultOptionId)
                    }
                    .unindent()
                    .add("}\n")
                    .add("store.putInt(%S, optionId)", property.key)
                    .build()
                
                propertyBuilder.setter(
                    FunSpec.setterBuilder()
                        .addParameter("value", property.typeName)
                        .addCode(setterCode)
                        .build()
                )
            }
            else -> {
                // Primitive types: direct storage access
                val storageMethodSuffix = getStorageMethodSuffix(property.dataType)
                
                propertyBuilder.getter(
                    FunSpec.getterBuilder()
                        .addCode("return store.get%L(%S, %L)", storageMethodSuffix, property.key, defaultValuePropertyName)
                        .build()
                )
                
                propertyBuilder.setter(
                    FunSpec.setterBuilder()
                        .addParameter("value", property.typeName)
                        .addCode("store.put%L(%S, value)", storageMethodSuffix, property.key)
                        .build()
                )
            }
        }
        
        return propertyBuilder.build()
    }
    
    /**
     * Generates the getConfigItems method that provides metadata for all properties.
     * 
     * This method creates ConfigItemDescriptor instances for each property,
     * which are used by configuration management UIs to display and edit values.
     * 
     * @param configData Complete configuration metadata
     * @return FunSpec for the getConfigItems method
     */
    private fun generateConfigItemsMethod(configData: ConfigData): FunSpec {
        val configItemDescriptorClass = ClassName("io.github.mambawow.appconfig", "ConfigItemDescriptor")
        
        val methodBuilder = FunSpec.builder("getConfigItems")
            .returns(
                List::class.asClassName().parameterizedBy(
                    configItemDescriptorClass.parameterizedBy(STAR)
                )
            )

        val configItemsCode = CodeBlock.builder().add("return listOf(\n").indent()

        configData.properties.forEachIndexed { index, property ->
            val configItemCode = generateConfigItemDescriptor(property, configData.groupName)
            configItemsCode.add(configItemCode)
            if (index < configData.properties.size - 1) {
                configItemsCode.add(",\n")
            } else {
                configItemsCode.add("\n")
            }
        }
        configItemsCode.unindent().add(")")
        methodBuilder.addCode(configItemsCode.build())
        
        return methodBuilder.build()
    }
    
    /**
     * Generates a ConfigItemDescriptor for a single property.
     * @param property Property metadata
     * @param groupName Configuration group name
     * @return CodeBlock for the config item descriptor
     */
    private fun generateConfigItemDescriptor(property: PropertyData, groupName: String): CodeBlock {
        val descriptorBuilder = CodeBlock.builder()
        
        when (property.dataType) {
            DataType.OPTION -> {
                // Generate choice list for option properties
                val choiceListBlock = CodeBlock.builder().add("listOf(").apply {
                    property.optionItems.forEachIndexed { i, optionItem ->
                        add(
                            "%T(%L, %S, %T)",
                            ClassName("io.github.mambawow.appconfig", "OptionItemDescriptor"),
                            optionItem.optionId,
                            optionItem.description,
                            optionItem.typeName
                        )
                        if (i < property.optionItems.size - 1) add(", ")
                    }
                }.add(")").build()
                
                descriptorBuilder.add(
                    "%T<%T>(\n",
                    ClassName("io.github.mambawow.appconfig", "OptionConfigItem"),
                    property.typeName
                )
                descriptorBuilder.indent()
                    .add("choices = %L,\n", choiceListBlock)
            }
            else -> {
                descriptorBuilder.add(
                    "%T<%T>(\n",
                    ClassName("io.github.mambawow.appconfig", "StandardConfigItem"),
                    property.typeName
                ).indent()
            }
        }
        
        // Add common properties
        descriptorBuilder
            .add("key = %S,\n", property.key)
            .add("groupName = %S,\n", groupName)
            .add("description = %S,\n", property.description)
            .add("getCurrentValue = { this.%L },\n", property.name)
        
        // Add type-specific properties
        if (property.dataType == DataType.OPTION) {
            val defaultOptionId = property.defaultValue as Int
            
            descriptorBuilder.add("defaultOptionId = %L,\n", defaultOptionId)
                .add("updateValue = { newValue -> this.%L = newValue as %T },\n", property.name, property.typeName)
                .add("resetToDefault = { \n")
                .indent()
                .apply {
                    val defaultOption = property.optionItems.find { it.isDefault }?.typeName
                    if (defaultOption != null) {
                        add("this.%L = %T\n", property.name, defaultOption)
                    }
                }
                .unindent()
                .add("}\n")
        } else {
            val defaultValueExpression = createDefaultValueCodeBlock(property.dataType, property.defaultValue)
            descriptorBuilder.add("defaultValue = %L,\n", defaultValueExpression)
                .add("panelType = %T.%L,\n", ClassName("io.github.mambawow.appconfig", "PanelType"), property.panelType.name)
                .add("dataType = %T.%L,\n", ClassName("io.github.mambawow.appconfig", "DataType"), property.dataType.name)
                .add("updateValue = { newValue -> this.%L = newValue as %T },\n", property.name, property.typeName)
                .add("resetToDefault = { this.%L = %L }\n", property.name, defaultValueExpression)
        }
        
        descriptorBuilder.unindent().add(")")
        return descriptorBuilder.build()
    }
    
    /**
     * Generates the updateFromMap method for bulk configuration updates.
     * @param configData Complete configuration metadata
     * @return FunSpec for the updateFromMap method
     */
    private fun generateBulkUpdateMethod(configData: ConfigData): FunSpec {
        val updateMethod = FunSpec.builder("updateFromMap")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("data", Map::class.asClassName().parameterizedBy(String::class.asClassName(), ANY))
        
        if (configData.properties.isNotEmpty()) {
            updateMethod.beginControlFlow("data.forEach { (key, value) ->")
                .beginControlFlow("when (key)")
            
            configData.properties.forEach { property ->
                when (property.dataType) {
                    DataType.OPTION -> generateOptionUpdateCase(updateMethod, property)
                    else -> generatePrimitiveUpdateCase(updateMethod, property)
                }
            }
            updateMethod.endControlFlow().endControlFlow()
        } else {
            updateMethod.addComment("No properties to update from map.")
        }
        
        return updateMethod.build()
    }
    
    /**
     * Generates update case for option properties in bulk update method.
     */
    private fun generateOptionUpdateCase(methodBuilder: FunSpec.Builder, property: PropertyData) {
        methodBuilder.addCode(
            CodeBlock.builder()
                .addStatement("    %S -> when (value) {", property.key).indent()
                .addStatement("is Int -> {")
                .indent()
                .apply {
                    // Map option ID to corresponding sealed class instance
                    property.optionItems.forEach { optionItem ->
                        addStatement("if (value == %L) this.%L = %T", optionItem.optionId, property.name, optionItem.typeName)
                    }
                }
                .unindent()
                .addStatement("}")
                .unindent().addStatement("}")
                .build()
        )
    }
    
    /**
     * Generates update case for primitive properties in bulk update method.
     */
    private fun generatePrimitiveUpdateCase(methodBuilder: FunSpec.Builder, property: PropertyData) {
        methodBuilder.addCode(
            CodeBlock.builder()
                .add("    %S -> when (value) {\n", property.key).indent()
                .apply {
                    when (property.dataType) {
                        DataType.STRING -> addStatement("is String -> this.%L = value", property.name)
                        DataType.BOOLEAN -> addStatement("is Boolean -> this.%L = value", property.name)
                        DataType.INT -> addStatement(
                            "is Int -> this.%L = value\n" +
                            "                     is Number -> this.%L = value.toInt()", 
                            property.name, 
                            property.name
                        )
                        DataType.LONG -> addStatement(
                            "is Long -> this.%L = value\n" +
                            "                     is Number -> this.%L = value.toLong()", 
                            property.name, 
                            property.name
                        )
                        DataType.FLOAT -> addStatement(
                            "is Float -> this.%L = value\n" +
                            "                     is Number -> this.%L = value.toFloat()", 
                            property.name, 
                            property.name
                        )
                        DataType.DOUBLE -> addStatement(
                            "is Double -> this.%L = value\n" +
                            "                     is Number -> this.%L = value.toDouble()", 
                            property.name, 
                            property.name
                        )
                        else -> {}
                    }
                }
                .unindent().addStatement("}")
                .build()
        )
    }
    
    /**
     * Generates the resetToDefaults method for restoring all properties to their defaults.
     * 
     * @param configData Complete configuration metadata
     * @return FunSpec for the resetToDefaults method
     */
    private fun generateResetMethod(configData: ConfigData): FunSpec {
        val resetMethod = FunSpec.builder("resetToDefaults")
            .addModifiers(KModifier.SUSPEND)
        
        configData.properties.forEach { property ->
            when (property.dataType) {
                DataType.OPTION -> {
                    val defaultOption = property.optionItems.find { it.isDefault }?.typeName
                    if (defaultOption != null) {
                        resetMethod.addStatement("this.%L = %T", property.name, defaultOption)
                    }
                }
                else -> {
                    val defaultValueExpression = createDefaultValueCodeBlock(property.dataType, property.defaultValue)
                    resetMethod.addStatement("this.%L = %L", property.name, defaultValueExpression)
                }
            }
        }
        
        return resetMethod.build()
    }
    
    /**
     * Gets the ConfigStore method suffix for a given data type.
     * 
     * @param dataType The data type to get suffix for
     * @return Method suffix for ConfigStore operations
     */
    private fun getStorageMethodSuffix(dataType: DataType): String {
        return when (dataType) {
            DataType.STRING -> "String"
            DataType.BOOLEAN -> "Boolean"
            DataType.INT -> "Int"
            DataType.LONG -> "Long"
            DataType.FLOAT -> "Float"
            DataType.DOUBLE -> "Double"
            else -> ""
        }
    }
    
    /**
     * Creates a CodeBlock for a default value based on its data type.
     * 
     * @param dataType The data type of the value
     * @param defaultValue The default value to represent
     * @return CodeBlock representing the value in Kotlin code
     */
    private fun createDefaultValueCodeBlock(dataType: DataType, defaultValue: Any?): CodeBlock {
        return when (dataType) {
            DataType.STRING -> CodeBlock.of("%S", defaultValue)
            DataType.BOOLEAN -> CodeBlock.of("%L", defaultValue)
            DataType.INT -> CodeBlock.of("%L", defaultValue)
            DataType.LONG -> CodeBlock.of("%LL", defaultValue)
            DataType.FLOAT -> CodeBlock.of("%LF", defaultValue)
            DataType.DOUBLE -> CodeBlock.of("%L", defaultValue)
            else -> CodeBlock.of("null /* ERROR */")
        }
    }
} 