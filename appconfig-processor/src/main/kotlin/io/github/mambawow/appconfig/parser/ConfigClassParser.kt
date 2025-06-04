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
 * 配置类解析器
 * 负责将KSClassDeclaration转换为ConfigData
 */
class ConfigClassParser(
    private val logger: KSPLogger,
    private val propertyParser: PropertyParser
) {

    companion object {
        // Kotlin 关键字列表
        private val kotlinKeywords = setOf(
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

        /**
         * 验证字符串是否为有效的Kotlin标识符
         * @param identifier 要验证的标识符
         * @return true如果是有效的Kotlin标识符，false否则
         */
        fun isValidKotlinIdentifier(identifier: String): Boolean {
            if (identifier.isBlank()) return false
            
            // 检查是否为Kotlin关键字
            if (identifier in kotlinKeywords) return false
            
            // 检查第一个字符：必须是字母或下划线
            val firstChar = identifier.first()
            if (!firstChar.isLetter() && firstChar != '_') return false
            
            // 检查其余字符：必须是字母、数字或下划线
            return identifier.drop(1).all { char ->
                char.isLetterOrDigit() || char == '_'
            }
        }
    }

    /**
     * 解析配置类
     */
    fun parseConfigClass(configClass: KSClassDeclaration): ConfigData? {
        // 验证类的基本要求
        if (!validateClass(configClass)) {
            return null
        }

        val groupAnnotation = configClass.annotations.first { 
            it.shortName.asString() == Config::class.simpleName 
        }
        val annotationGroupName = groupAnnotation.arguments.firstOrNull { 
            it.name?.asString() == "groupName" 
        }?.value as? String ?: ""

        // 如果groupName为空字符串，使用类名作为默认值
        val groupName = if (annotationGroupName.isBlank()) {
            configClass.simpleName.asString()
        } else {
            annotationGroupName
        }

        // 验证groupName是否为合法的Kotlin标识符
        if (!isValidKotlinIdentifier(groupName)) {
            logger.error(
                ConfigError.invalidGroupName(configClass.simpleName.asString(), groupName), 
                configClass
            )
            return null
        }

        val packageName = configClass.packageName.asString()
        val className = configClass.simpleName.asString()

        // 解析属性
        val properties = parseProperties(configClass)
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
     * 验证类的基本要求
     */
    private fun validateClass(configClass: KSClassDeclaration): Boolean {
        // 检查是否为接口或类
        if (configClass.classKind != ClassKind.INTERFACE) {
            logger.error(ConfigError.ONLY_INTERFACES_CAN_BE_ANNOTATED, configClass)
            return false
        }

        // 检查是否有包名
        if (configClass.packageName.asString().isEmpty()) {
            logger.error("Interface needs to have a package", configClass)
            return false
        }

        return true
    }

    /**
     * 解析属性列表
     */
    private fun parseProperties(configClass: KSClassDeclaration): List<PropertyData> {
        val validProperties = configClass.getDeclaredProperties()
            .filter { property -> 
                property.annotations.any { annotation ->
                    annotation.annotationType.resolve().declaration.qualifiedName?.asString() in PropertyAnnotationQualifiedNames
                }
            }
            .filter { property ->
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

        return validProperties.mapNotNull { property ->
            propertyParser.parseProperty(property, configClass)
        }.toList()
    }
} 