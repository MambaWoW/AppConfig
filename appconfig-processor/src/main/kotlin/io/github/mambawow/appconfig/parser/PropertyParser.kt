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
 * 属性解析器
 * 负责解析单个属性并转换为PropertyData
 */
class PropertyParser(
    private val logger: KSPLogger
) {

    /**
     * 解析属性
     */
    fun parseProperty(property: KSPropertyDeclaration, containingClass: KSClassDeclaration): PropertyData? {
        val propertyAnnotation = property.annotations.first { 
            it.annotationType.resolve().declaration.qualifiedName?.asString() in PropertyAnnotationQualifiedNames
        }
        
        val annotationName = propertyAnnotation.shortName.asString()
        val annotationArgs = propertyAnnotation.arguments.associateBy { it.name!!.asString() }
        
        // 获取key参数，如果为空字符串则使用属性名作为默认值
        val keyArg = annotationArgs["key"]
        val annotationKey = keyArg?.value as? String ?: ""
        val key = if (annotationKey.isBlank()) {
            property.simpleName.asString()
        } else {
            annotationKey
        }
        
        val description = annotationArgs["description"]?.value as? String ?: ""
        val propertyName = property.simpleName.asString()
        val propertyKSType = property.type.resolve()
        val propertyTypeName = propertyKSType.toTypeName()
        
        // 确定数据类型
        val dataType = determineDataType(annotationName, propertyName) ?: return null
        
        // 解析默认值和选项项
        val (defaultValue, optionItems) = when (dataType) {
            DataType.OPTION -> parseOptionProperty(property, propertyKSType, propertyName)
            else -> parsePrimitiveProperty(annotationArgs, annotationName, propertyName)
        } ?: return null
        
        val panelType = inferPanelTypeFromDataType(dataType)
        
        return PropertyData(
            name = propertyName,
            typeName = propertyTypeName,
            dataType = dataType,
            key = key,
            description = description,
            defaultValue = defaultValue,
            optionItems = optionItems,
            panelType = panelType
        )
    }
    
    /**
     * 确定数据类型
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
     * 解析选项属性
     */
    private fun parseOptionProperty(
        property: KSPropertyDeclaration,
        propertyKSType: KSType,
        propertyName: String
    ): Pair<Int, List<OptionItemData>>? {
        val classDecl = propertyKSType.declaration as? KSClassDeclaration
        
        // 验证是否为sealed class
        if (classDecl == null || !classDecl.isSealed()) {
            logger.error(
                ConfigError.mustBeSealedClass(propertyName, propertyKSType.toTypeName().toString()),
                property
            )
            return null
        }
        
        // 验证@Option注解
        val hasOptionAnnotation = classDecl.annotations.any { 
            it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.mambawow.appconfig.Option"
        }
        if (!hasOptionAnnotation) {
            logger.error(
                ConfigError.missingOptionAnnotation(propertyName, classDecl.simpleName.asString()),
                property
            )
            return null
        }
        
        // 解析子类
        val subclasses = classDecl.getSealedSubclasses().toList()
        if (subclasses.isEmpty()) {
            logger.error(ConfigError.noSubclasses(propertyName), property)
            return null
        }
        
        val optionItems = mutableListOf<OptionItemData>()
        var defaultOptionId: Int? = null
        var hasDefault = false
        
        for (subclass in subclasses) {
            val optionItemAnnotation = subclass.annotations.find { 
                it.annotationType.resolve().declaration.qualifiedName?.asString() == "io.github.mambawow.appconfig.OptionItem"
            }
            
            if (optionItemAnnotation != null) {
                val annotationArgs = optionItemAnnotation.arguments.associateBy { it.name?.asString() }
                val optionId = annotationArgs["optionId"]?.value as? Int
                val isDefault = annotationArgs["isDefault"]?.value as? Boolean ?: false
                val description = annotationArgs["description"]?.value as? String ?: ""

                if (optionId == null) {
                    logger.error(
                        ConfigError.missingOptionId(propertyName, subclass.simpleName.asString()),
                        property
                    )
                    return null
                }

                if (isDefault) {
                    if (hasDefault) {
                        logger.error(ConfigError.multipleDefaultOptions(propertyName), property)
                        return null
                    }
                    hasDefault = true
                    defaultOptionId = optionId
                }

                optionItems.add(
                    OptionItemData(
                        className = subclass.simpleName.asString(),
                        typeName = subclass.asType(emptyList()).toTypeName(),
                        optionId = optionId,
                        description = description,
                        isDefault = isDefault
                    )
                )
            }
        }
        
        if (!hasDefault) {
            logger.error(ConfigError.noDefaultOption(propertyName), property)
            return null
        }
        
        // 验证optionId唯一性
        val optionIds = optionItems.map { it.optionId }
        if (optionIds.size != optionIds.toSet().size) {
            logger.error(ConfigError.duplicateOptionIds(propertyName), property)
            return null
        }
        
        return defaultOptionId!! to optionItems
    }
    
    /**
     * 解析基本类型属性
     */
    private fun parsePrimitiveProperty(
        annotationArgs: Map<String, Any?>,
        annotationName: String,
        propertyName: String
    ): Pair<Any, List<OptionItemData>>? {
        val defaultValueArg = annotationArgs["defaultValue"]
        if (defaultValueArg == null) {
            logger.error(ConfigError.missingDefaultValue(propertyName, annotationName))
            return null
        }
        
        // 获取实际的默认值
        val defaultValue = (defaultValueArg as? com.google.devtools.ksp.symbol.KSValueArgument)?.value
            ?: defaultValueArg
        
        return defaultValue to emptyList()
    }

    /**
     * 根据DataType推断合适的PanelType
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