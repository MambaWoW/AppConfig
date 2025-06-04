package io.github.mambawow.appconfig.model

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.PanelType

/**
 * 配置类的数据模型
 * @param name 类名
 * @param packageName 包名
 * @param groupName 配置组名
 * @param properties 属性列表
 * @param modifiers 修饰符
 * @param ksFile 源文件
 * @param annotations 注解列表
 */
data class ConfigData(
    val name: String,
    val packageName: String,
    val groupName: String,
    val properties: List<PropertyData>,
    val modifiers: List<KModifier> = emptyList(),
    val ksFile: KSFile,
    val annotations: List<KSAnnotation> = emptyList()
) {
    val implName = "${name}Impl"
    val extensionPropertyName = groupName.lowercase()
}

/**
 * 属性数据模型
 */
data class PropertyData(
    val name: String,
    val typeName: TypeName,
    val dataType: DataType,
    val key: String,
    val description: String,
    val defaultValue: Any?,
    val optionItems: List<OptionItemData> = emptyList(),
    val panelType: PanelType
)

/**
 * 选项项数据模型
 */
data class OptionItemData(
    val className: String,
    val typeName: TypeName,
    val optionId: Int,
    val description: String,
    val isDefault: Boolean
)

