package io.github.mambawow.appconfig.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.mambawow.appconfig.model.*

/**
 * 扩展方法生成器
 * 负责生成AppConfig的扩展属性和全局扩展方法
 */
class ExtensionGenerator {
    
    private val appConfigClass = ClassName("io.github.mambawow.appconfig", "AppConfig")
    private val configItemDescriptorClass = ClassName("io.github.mambawow.appconfig", "ConfigItemDescriptor")
    
    /**
     * 生成AppConfig扩展属性
     */
    fun generateExtensionProperty(configData: ConfigData, generatedPackageName: String): PropertySpec {
        return PropertySpec.builder(
            configData.extensionPropertyName,
            ClassName(generatedPackageName, configData.implName)
        )
            .receiver(appConfigClass)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %T()", ClassName(generatedPackageName, configData.implName))
                    .build()
            )
            .build()
    }
    
    /**
     * 生成getAllConfigItems扩展方法
     */
    fun generateGetAllConfigItemsMethod(allConfigClasses: List<Pair<String, String>>): FunSpec {
        val getAllConfigItemsSpec = FunSpec.builder("getAllConfigItems")
            .receiver(appConfigClass)
            .returns(
                List::class.asClassName().parameterizedBy(
                    configItemDescriptorClass.parameterizedBy(STAR)
                )
            )
        
        if (allConfigClasses.isEmpty()) {
            getAllConfigItemsSpec.addStatement("return emptyList()")
        } else {
            getAllConfigItemsSpec.addStatement(
                "return %L",
                allConfigClasses.map { (groupName, _) -> 
                    "${groupName.lowercase()}.getConfigItems()" 
                }.joinToString(" + ")
            )
        }
        
        return getAllConfigItemsSpec.build()
    }
    
    /**
     * 生成updateAllFromRemote扩展方法
     */
    fun generateUpdateAllFromRemoteMethod(allConfigClasses: List<Pair<String, String>>): FunSpec {
        val updateAllFromRemoteSpec = FunSpec.builder("updateAllFromRemote")
            .receiver(appConfigClass)
            .addModifiers(KModifier.SUSPEND)
            .addParameter(
                "globalConfigData",
                Map::class.asClassName().parameterizedBy(
                    String::class.asClassName(),
                    Map::class.asClassName().parameterizedBy(String::class.asClassName(), ANY)
                )
            )
        
        if (allConfigClasses.isEmpty()) {
            updateAllFromRemoteSpec.addComment("No config groups to update")
        } else {
            updateAllFromRemoteSpec.beginControlFlow("globalConfigData.forEach { (groupName, groupData) ->")
                .beginControlFlow("when (groupName)")
            
            allConfigClasses.forEach { (groupName, _) ->
                updateAllFromRemoteSpec.addStatement(
                    "%S -> %L.updateFromMap(groupData)", 
                    groupName, 
                    groupName.lowercase()
                )
            }
            
            updateAllFromRemoteSpec.endControlFlow().endControlFlow()
        }
        
        return updateAllFromRemoteSpec.build()
    }
    
    /**
     * 生成resetAllToDefaults扩展方法
     */
    fun generateResetAllToDefaultsMethod(allConfigClasses: List<Pair<String, String>>): FunSpec {
        val resetAllToDefaultsSpec = FunSpec.builder("resetAllToDefaults")
            .receiver(appConfigClass)
            .addModifiers(KModifier.SUSPEND)
        
        if (allConfigClasses.isNotEmpty()) {
            allConfigClasses.forEach { (groupName, _) ->
                resetAllToDefaultsSpec.addStatement("%L.resetToDefaults()", groupName.lowercase())
            }
        } else {
            resetAllToDefaultsSpec.addComment("No config groups to reset")
        }
        
        return resetAllToDefaultsSpec.build()
    }
} 