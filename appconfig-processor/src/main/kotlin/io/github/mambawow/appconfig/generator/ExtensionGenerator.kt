package io.github.mambawow.appconfig.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.mambawow.appconfig.model.*

/**
 * Generator for AppConfig extension methods and properties.
 * 
 * Creates extension methods that provide convenient access to configuration instances
 * and global operations for configuration management across all groups.
 */
class ExtensionGenerator {
    
    private val appConfigClass = ClassName("io.github.mambawow.appconfig", "AppConfig")
    private val configItemDescriptorClass = ClassName("io.github.mambawow.appconfig", "ConfigItemDescriptor")
    
    /**
     * Generates an AppConfig extension property for accessing configuration instances.
     * 
     * @param configData Configuration metadata
     * @param generatedPackageName Package name for generated classes
     * @return PropertySpec for the extension property
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
     * Generates getAllConfigItems extension method.
     * 
     * For multiplatform projects:
     * - commonMain: generates expect declaration
     * - platform targets: generates actual implementation
     * 
     * @param allConfigClasses List of all configuration class metadata
     * @param isCommonMain Whether generating for commonMain module
     * @param isMultiplatform Whether this is a multiplatform project
     * @return FunSpec for getAllConfigItems method
     */
    fun generateGetAllConfigItemsMethod(
        allConfigClasses: List<Pair<String, String>>,
        isCommonMain: Boolean,
        isMultiplatform: Boolean
    ): FunSpec {
        if (isCommonMain) {
            return FunSpec.builder("getAllConfigItems")
                .receiver(appConfigClass)
                .returns(
                    List::class.asClassName().parameterizedBy(
                        configItemDescriptorClass.parameterizedBy(STAR)
                    )
                ).addModifiers(KModifier.EXPECT)
                .build()
        } else {
            val methodBuilder = FunSpec.builder("getAllConfigItems")
                .receiver(appConfigClass)
                .returns(
                    List::class.asClassName().parameterizedBy(
                        configItemDescriptorClass.parameterizedBy(STAR)
                    )
                )

            if (isMultiplatform) {
                methodBuilder.addModifiers(KModifier.ACTUAL)
            }
            
            if (allConfigClasses.isEmpty()) {
                methodBuilder.addStatement("return emptyList()")
            } else {
                methodBuilder.addStatement(
                    "return %L",
                    allConfigClasses.map { (groupName, _) ->
                        "${groupName.lowercase()}.getConfigItems()"
                    }.joinToString(" + ")
                )
            }
            return methodBuilder.build()
        }
    }
    
    /**
     * Generates updateAllFromRemote extension method for bulk configuration updates.
     * 
     * @param allConfigClasses List of all configuration class metadata
     * @param isCommonMain Whether generating for commonMain module
     * @param isMultiplatform Whether this is a multiplatform project
     * @return FunSpec for updateAllFromRemote method
     */
    fun generateUpdateAllFromRemoteMethod(
        allConfigClasses: List<Pair<String, String>>,
        isCommonMain: Boolean,
        isMultiplatform: Boolean
    ): FunSpec {
        if (isCommonMain) {
            return FunSpec.builder("updateAllFromRemote")
                .receiver(appConfigClass)
                .addModifiers(KModifier.EXPECT, KModifier.SUSPEND)
                .addParameter(
                    "globalConfigData",
                    Map::class.asClassName().parameterizedBy(
                        String::class.asClassName(),
                        Map::class.asClassName().parameterizedBy(String::class.asClassName(), ANY)
                    )
                )
                .build()
        }
        
        val methodBuilder = FunSpec.builder("updateAllFromRemote")
            .receiver(appConfigClass)
            .addParameter(
                "globalConfigData",
                Map::class.asClassName().parameterizedBy(
                    String::class.asClassName(),
                    Map::class.asClassName().parameterizedBy(String::class.asClassName(), ANY)
                )
            )

        if (!isMultiplatform) {
            methodBuilder.addModifiers(KModifier.SUSPEND)
        } else {
            methodBuilder.addModifiers(KModifier.ACTUAL, KModifier.SUSPEND)
        }
        
        if (allConfigClasses.isEmpty()) {
            methodBuilder.addComment("No config groups to update")
        } else {
            methodBuilder.beginControlFlow("globalConfigData.forEach { (groupName, groupData) ->")
                .beginControlFlow("when (groupName)")
            
            allConfigClasses.forEach { (groupName, _) ->
                methodBuilder.addStatement(
                    "%S -> %L.updateFromMap(groupData)", 
                    groupName, 
                    groupName.lowercase()
                )
            }
            
            methodBuilder.endControlFlow().endControlFlow()
        }
        
        return methodBuilder.build()
    }
    
    /**
     * Generates resetAllToDefaults extension method for restoring all configurations.
     * 
     * @param allConfigClasses List of all configuration class metadata
     * @param isCommonMain Whether generating for commonMain module
     * @param isMultiplatform Whether this is a multiplatform project
     * @return FunSpec for resetAllToDefaults method
     */
    fun generateResetAllToDefaultsMethod(
        allConfigClasses: List<Pair<String, String>>,
        isCommonMain: Boolean,
        isMultiplatform: Boolean
    ): FunSpec {
        if (isCommonMain) {
            return FunSpec.builder("resetAllToDefaults")
                .receiver(appConfigClass)
                .addModifiers(KModifier.EXPECT, KModifier.SUSPEND)
                .build()
        }
        
        val methodBuilder = FunSpec.builder("resetAllToDefaults")
            .receiver(appConfigClass)

        if (!isMultiplatform) {
            methodBuilder.addModifiers(KModifier.SUSPEND)
        } else {
            methodBuilder.addModifiers(KModifier.ACTUAL, KModifier.SUSPEND)
        }

        if (allConfigClasses.isNotEmpty()) {
            allConfigClasses.forEach { (groupName, _) ->
                methodBuilder.addStatement("%L.resetToDefaults()", groupName.lowercase())
            }
        } else {
            methodBuilder.addComment("No config groups to reset")
        }
        
        return methodBuilder.build()
    }
} 