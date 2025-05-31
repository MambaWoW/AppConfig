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
    
    private val flowClass = ClassName("kotlinx.coroutines.flow", "Flow")
    private val configStoreClass = ClassName("io.github.mambawow.appconfig.store", "ConfigStore")
    private val appConfigClass = ClassName("io.github.mambawow.appconfig", "AppConfig")
    
    /**
     * 生成配置实现类
     */
    fun generateConfigImpl(configData: ConfigData): TypeSpec {
        val implBuilder = TypeSpec.classBuilder(configData.implName)
            .addModifiers(KModifier.PUBLIC)
        
        // 添加store属性
        implBuilder.addProperty(
            PropertySpec.builder("store", configStoreClass)
                .addModifiers(KModifier.PRIVATE)
                .initializer("AppConfig.createConfigStore(%S)", configData.groupName)
                .build()
        )
        
        // 为每个属性生成getter和updater方法
        configData.properties.forEach { property ->
            implBuilder.addFunction(generateGetterMethod(property))
            implBuilder.addFunction(generateUpdaterMethod(property))
        }
        
        // 生成getConfigItems方法
        implBuilder.addFunction(generateGetConfigItemsMethod(configData))
        
        // 生成updateFromMap方法
        implBuilder.addFunction(generateUpdateFromMapMethod(configData))
        
        // 生成resetToDefaults方法
        implBuilder.addFunction(generateResetToDefaultsMethod(configData))
        
        return implBuilder.build()
    }
    
    /**
     * 生成getter方法
     */
    private fun generateGetterMethod(property: PropertyData): FunSpec {
        val getterSpec = FunSpec.builder(property.getterName)
            .returns(flowClass.parameterizedBy(property.typeName))
        
        when (property.dataType) {
            DataType.OPTION -> {
                val choiceListBlock = CodeBlock.builder().add("listOf(").apply {
                    property.optionItems.forEachIndexed { i, optionItem ->
                        add("%T", optionItem.typeName)
                        if (i < property.optionItems.size - 1) add(", ")
                    }
                }.add(")").build()
                
                val defaultOptionId = property.defaultValue as Int
                
                getterSpec.addCode(
                    CodeBlock.builder()
                        .addStatement("val allOptions = %L", choiceListBlock)
                        .add(
                            "return store.getInt(%S, %L).map { storedOptionId ->\n",
                            property.key,
                            defaultOptionId
                        )
                        .indent()
                        .addStatement(
                            "when (storedOptionId) {"
                        )
                        .indent()
                        .apply {
                            property.optionItems.forEach {
                                addStatement("%L -> %T", it.optionId, it.typeName)
                            }
                        }
                        .addStatement("else -> %T", property.optionItems.find { it.isDefault }?.typeName)
                        .unindent()
                        .addStatement("}")
                        .unindent()
                        .addStatement("}")
                        .build()
                )
            }
            else -> {
                val storeSuffix = getStoreMethodSuffix(property.dataType)
                val defaultValueExpr = createDefaultValueCodeBlock(property.dataType, property.defaultValue)
                getterSpec.addStatement(
                    "return store.get%L(%S, %L)",
                    storeSuffix,
                    property.key,
                    defaultValueExpr
                )
            }
        }
        
        return getterSpec.build()
    }
    
    /**
     * 生成updater方法
     */
    private fun generateUpdaterMethod(property: PropertyData): FunSpec {
        val updaterSpec = FunSpec.builder(property.updaterName)
            .addModifiers(KModifier.SUSPEND)
        
        when (property.dataType) {
            DataType.OPTION -> {
                updaterSpec.addParameter("optionId", Int::class.asClassName())
                updaterSpec.addStatement("store.putInt(%S, optionId)", property.key)
            }
            else -> {
                val storeSuffix = getStoreMethodSuffix(property.dataType)
                updaterSpec.addParameter("value", property.typeName)
                updaterSpec.addStatement("store.put%L(%S, value)", storeSuffix, property.key)
            }
        }
        
        return updaterSpec.build()
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
            .add("getCurrentValue = { %N() },\n", property.getterName)
        
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
                .add("updateValue = { newValue -> %N(%L) },\n", property.updaterName, whenExpressionBuilder.build())
                .add("resetToDefault = { %N(%L) }\n", property.updaterName, defaultOptionId)
        } else {
            val defaultValueExpr = createDefaultValueCodeBlock(property.dataType, property.defaultValue)
            configItemBuilder.add("defaultValue = %L,\n", defaultValueExpr)
                .add("panelType = %T.%L,\n", ClassName("io.github.mambawow.appconfig", "PanelType"), property.panelType.name)
                .add("dataType = %T.%L,\n", ClassName("io.github.mambawow.appconfig", "DataType"), property.dataType.name)
                .add("updateValue = { newValue -> %N(newValue as %T) },\n", property.updaterName, property.typeName)
                .add("resetToDefault = { %N(%L) }\n", property.updaterName, defaultValueExpr)
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
                            .addStatement("is Int -> %N(value)", property.updaterName)
                            .unindent().addStatement("}")
                            .build()
                    )
                    else -> updateFromMapFunc.addCode(
                        CodeBlock.builder()
                            .add("    %S -> when (value) {\n", property.key).indent()
                            .apply {
                                when (property.dataType) {
                                    DataType.STRING -> addStatement("is String -> %N(value)", property.updaterName)
                                    DataType.BOOLEAN -> addStatement("is Boolean -> %N(value)", property.updaterName)
                                    DataType.INT -> addStatement("is Int -> %N(value)\n       is Number -> %N(value.toInt())", property.updaterName, property.updaterName)
                                    DataType.LONG -> addStatement("is Long -> %N(value)\n       is Number -> %N(value.toLong())", property.updaterName, property.updaterName)
                                    DataType.FLOAT -> addStatement("is Float -> %N(value)\n       is Number -> %N(value.toFloat())", property.updaterName, property.updaterName)
                                    DataType.DOUBLE -> addStatement("is Double -> %N(value)\n       is Number -> %N(value.toDouble())", property.updaterName, property.updaterName)
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
            val resetExpr = when (property.dataType) {
                DataType.OPTION -> CodeBlock.of("%L", property.defaultValue as Int)
                else -> createDefaultValueCodeBlock(property.dataType, property.defaultValue)
            }
            resetDefaultsFunc.addStatement("%N(%L)", property.updaterName, resetExpr)
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