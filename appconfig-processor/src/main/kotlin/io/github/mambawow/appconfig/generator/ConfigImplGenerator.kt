package io.github.mambawow.appconfig.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.model.*

/**
 * 配置实现类生成器
 * 负责生成ConfigImpl类
 */
class ConfigImplGenerator {
    
    private val configStoreClass = ClassName("io.github.mambawow.appconfig.store", "ConfigStore")
    private val appConfigClass = ClassName("io.github.mambawow.appconfig", "AppConfig")
    
    /**
     * 生成配置实现类
     */
    fun generateConfigImpl(configData: ConfigData): TypeSpec {
        val implBuilder = TypeSpec.classBuilder(configData.implName)
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(ClassName(configData.packageName, configData.name)) // 实现接口
        
        // 添加store属性
        implBuilder.addProperty(
            PropertySpec.builder("store", configStoreClass)
                .addModifiers(KModifier.PRIVATE)
                .initializer("AppConfig.createConfigStore(%S)", configData.groupName)
                .build()
        )
        
        // 为每个属性生成 var 属性（带 getter 和 setter）
        configData.properties.forEach { property ->
            // 生成默认值属性
            implBuilder.addProperty(generateDefaultValueProperty(property))
            // 生成更新默认值方法
            implBuilder.addFunction(generateUpdateDefaultValueMethod(property))
            // 生成主属性
            implBuilder.addProperty(generatePropertyOverride(property))
        }
        
        // 生成辅助方法
        implBuilder.addFunction(generateGetConfigItemsMethod(configData))
        implBuilder.addFunction(generateUpdateFromMapMethod(configData))
        implBuilder.addFunction(generateResetToDefaultsMethod(configData))
        
        return implBuilder.build()
    }
    
    /**
     * 生成默认值属性
     */
    private fun generateDefaultValueProperty(property: PropertyData): PropertySpec {
        val defaultValuePropertyName = "${property.name}Default"
        val defaultValueExpr = when (property.dataType) {
            DataType.OPTION -> {
                // 对于 Option 类型，存储默认选项的 ID
                val defaultOptionId = property.defaultValue as Int
                CodeBlock.of("%L", defaultOptionId)
            }
            else -> createDefaultValueCodeBlock(property.dataType, property.defaultValue)
        }
        
        val propertyType = when (property.dataType) {
            DataType.OPTION -> INT // Option 类型的默认值存储为 Int
            else -> property.typeName
        }
        
        return PropertySpec.builder(defaultValuePropertyName, propertyType)
            .addModifiers(KModifier.PRIVATE)
            .mutable(true)
            .initializer(defaultValueExpr)
            .build()
    }
    
    /**
     * 生成更新默认值方法
     */
    private fun generateUpdateDefaultValueMethod(property: PropertyData): FunSpec {
        val defaultValuePropertyName = "${property.name}Default"
        val updateMethodName = "update${property.name.replaceFirstChar { it.titlecase() }}Default"
        
        val funSpec = FunSpec.builder(updateMethodName)
            .addModifiers(KModifier.PUBLIC)
            .addParameter("default", property.typeName)
        
        when (property.dataType) {
            DataType.OPTION -> {
                // 对于 Option 类型，将枚举值转换为对应的 ID 并存储
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
                
                funSpec.addCode(mappingCode)
            }
            else -> {
                funSpec.addStatement("$defaultValuePropertyName = default")
            }
        }
        
        return funSpec.build()
    }
    
    /**
     * 生成属性重写（var 属性，带 getter 和 setter）
     */
    private fun generatePropertyOverride(property: PropertyData): PropertySpec {
        val propertyBuilder = PropertySpec.builder(property.name, property.typeName)
            .addModifiers(KModifier.OVERRIDE)
            .mutable(true) // var 属性
        
        val defaultValuePropertyName = "${property.name}Default"
        
        when (property.dataType) {
            DataType.OPTION -> {
                val defaultOptionId = property.defaultValue as Int
                
                // Getter: 从 store 读取 Int，然后转换为对应的类型
                val getterCode = CodeBlock.builder()
                    .add("when (store.getInt(%S, %L)) {\n", property.key, defaultValuePropertyName)
                    .indent()
                    .apply {
                        property.optionItems.forEach { optionItem ->
                            add("%L -> %T\n", optionItem.optionId, optionItem.typeName)
                        }
                        // 在 else 分支中，需要将存储的 ID 转换回对应的枚举值
                        add("else -> when ($defaultValuePropertyName) {\n")
                        indent()
                        property.optionItems.forEach { optionItem ->
                            add("%L -> %T\n", optionItem.optionId, optionItem.typeName)
                        }
                        // 最终 fallback 到原始默认值
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
                
                // Setter: 将类型转换为 Int 然后存储
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
                // 基础类型的处理
                val storeSuffix = getStoreMethodSuffix(property.dataType)
                
                // Getter: 使用默认值属性
                propertyBuilder.getter(
                    FunSpec.getterBuilder()
                        .addCode("return store.get%L(%S, %L)", storeSuffix, property.key, defaultValuePropertyName)
                        .build()
                )
                
                // Setter: 直接写入 store
                propertyBuilder.setter(
                    FunSpec.setterBuilder()
                        .addParameter("value", property.typeName)
                        .addCode("store.put%L(%S, value)", storeSuffix, property.key)
                        .build()
                )
            }
        }
        
        return propertyBuilder.build()
    }
    
    /**
     * 生成getConfigItems方法
     */
    private fun generateGetConfigItemsMethod(configData: ConfigData): FunSpec {
        val configItemDescriptorClass = ClassName("io.github.mambawow.appconfig", "ConfigItemDescriptor")
        
        val getConfigItemsSpec = FunSpec.builder("getConfigItems")
            .returns(
                List::class.asClassName().parameterizedBy(
                    configItemDescriptorClass.parameterizedBy(STAR)
                )
            )

        val configItemsCode = CodeBlock.builder().add("return listOf(\n").indent()

        configData.properties.forEachIndexed { index, property ->
            val configItemCode = generateConfigItemCode(property, configData.groupName)
            configItemsCode.add(configItemCode)
            if (index < configData.properties.size - 1) {
                configItemsCode.add(",\n")
            } else {
                configItemsCode.add("\n")
            }
        }
        configItemsCode.unindent().add(")")
        getConfigItemsSpec.addCode(configItemsCode.build())
        
        return getConfigItemsSpec.build()
    }
    
    /**
     * 生成ConfigItem代码
     */
    private fun generateConfigItemCode(property: PropertyData, groupName: String): CodeBlock {
        val configItemBuilder = CodeBlock.builder()
        
        when (property.dataType) {
            DataType.OPTION -> {
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
                
                configItemBuilder.add(
                    "%T<%T>(\n",
                    ClassName("io.github.mambawow.appconfig", "OptionConfigItem"),
                    property.typeName
                )
                configItemBuilder.indent()
                    .add("choices = %L,\n", choiceListBlock)
            }
            else -> {
                configItemBuilder.add(
                    "%T<%T>(\n",
                    ClassName("io.github.mambawow.appconfig", "StandardConfigItem"),
                    property.typeName
                ).indent()
            }
        }
        
        configItemBuilder
            .add("key = %S,\n", property.key)
            .add("groupName = %S,\n", groupName)
            .add("description = %S,\n", property.description)
            .add("getCurrentValue = { this.%L },\n", property.name) // 直接引用属性
        
        if (property.dataType == DataType.OPTION) {
            val defaultOptionId = property.defaultValue as Int
            
            // Build properly formatted when expression with correct indentation
            val whenExpressionBuilder = CodeBlock.builder()
            whenExpressionBuilder.add("when(newValue) {\n").indent()
            
            property.optionItems.forEach { optionItem ->
                whenExpressionBuilder.add("is %T -> %L\n", optionItem.typeName, optionItem.optionId)
            }
            whenExpressionBuilder.add("else -> %L\n", defaultOptionId)
            whenExpressionBuilder.unindent().add("}")
            
            configItemBuilder.add("defaultOptionId = %L,\n", defaultOptionId)
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
            val defaultValueExpr = createDefaultValueCodeBlock(property.dataType, property.defaultValue)
            configItemBuilder.add("defaultValue = %L,\n", defaultValueExpr)
                .add("panelType = %T.%L,\n", ClassName("io.github.mambawow.appconfig", "PanelType"), property.panelType.name)
                .add("dataType = %T.%L,\n", ClassName("io.github.mambawow.appconfig", "DataType"), property.dataType.name)
                .add("updateValue = { newValue -> this.%L = newValue as %T },\n", property.name, property.typeName)
                .add("resetToDefault = { this.%L = %L }\n", property.name, defaultValueExpr)
        }
        
        configItemBuilder.unindent().add(")")
        return configItemBuilder.build()
    }
    
    /**
     * 生成updateFromMap方法
     */
    private fun generateUpdateFromMapMethod(configData: ConfigData): FunSpec {
        val updateFromMapFunc = FunSpec.builder("updateFromMap")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("data", Map::class.asClassName().parameterizedBy(String::class.asClassName(), ANY))
        
        if (configData.properties.isNotEmpty()) {
            updateFromMapFunc.beginControlFlow("data.forEach { (key, value) ->")
                .beginControlFlow("when (key)")
            
            configData.properties.forEach { property ->
                when (property.dataType) {
                    DataType.OPTION -> updateFromMapFunc.addCode(
                        CodeBlock.builder()
                            .addStatement("    %S -> when (value) {", property.key).indent()
                            .addStatement("is Int -> {")
                            .indent()
                            .apply {
                                // 根据 optionId 设置正确的选项
                                property.optionItems.forEach { optionItem ->
                                    addStatement("if (value == %L) this.%L = %T", optionItem.optionId, property.name, optionItem.typeName)
                                }
                            }
                            .unindent()
                            .addStatement("}")
                            .unindent().addStatement("}")
                            .build()
                    )
                    else -> updateFromMapFunc.addCode(
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
            }
            updateFromMapFunc.endControlFlow().endControlFlow()
        } else {
            updateFromMapFunc.addComment("No properties to update from map.")
        }
        
        return updateFromMapFunc.build()
    }
    
    /**
     * 生成resetToDefaults方法
     */
    private fun generateResetToDefaultsMethod(configData: ConfigData): FunSpec {
        val resetDefaultsFunc = FunSpec.builder("resetToDefaults")
            .addModifiers(KModifier.SUSPEND)
        
        configData.properties.forEach { property ->
            when (property.dataType) {
                DataType.OPTION -> {
                    val defaultOption = property.optionItems.find { it.isDefault }?.typeName
                    if (defaultOption != null) {
                        resetDefaultsFunc.addStatement("this.%L = %T", property.name, defaultOption)
                    }
                }
                else -> {
                    val defaultValueExpr = createDefaultValueCodeBlock(property.dataType, property.defaultValue)
                    resetDefaultsFunc.addStatement("this.%L = %L", property.name, defaultValueExpr)
                }
            }
        }
        
        return resetDefaultsFunc.build()
    }
    
    /**
     * 获取Store方法后缀
     */
    private fun getStoreMethodSuffix(dataType: DataType): String {
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
     * 创建默认值CodeBlock
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