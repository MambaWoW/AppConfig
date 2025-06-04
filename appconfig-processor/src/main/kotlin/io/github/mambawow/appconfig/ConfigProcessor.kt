package io.github.mambawow.appconfig

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import io.github.mambawow.appconfig.factory.ProcessorFactory
import io.github.mambawow.appconfig.model.ConfigData
import io.github.mambawow.appconfig.model.ConfigError
import java.io.OutputStreamWriter

class ConfigProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private var processed = false

    private val isMultiplatform = options["AppConfig_isMultiplatform"]?.toInt() == 1

    private val components = ProcessorFactory.createAllComponents(logger)
    
    companion object {
        const val GENERATED_PACKAGE_NAME = "io.github.mambawow.appconfig"
        const val GENERATED_CLASS_NAME_PREFIX = "AppConfig_"

        val PropertyAnnotationQualifiedNames = listOf(
            IntProperty::class.qualifiedName,
            LongProperty::class.qualifiedName,
            FloatProperty::class.qualifiedName,
            DoubleProperty::class.qualifiedName,
            StringProperty::class.qualifiedName,
            BooleanProperty::class.qualifiedName,
            OptionProperty::class.qualifiedName
        ).mapNotNull { it }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) return emptyList()

        logger.warn("isMultiplatform: $isMultiplatform")
        try {
            val configClasses = getValidConfigClasses(resolver)
            if (configClasses.isEmpty()) {
                logger.info("No classes found with @Config annotation. Skipping processing.")
                processed = true
                return emptyList()
            }
            
            val configDataList = parseConfigClasses(configClasses)
            if (configDataList.isEmpty()) {
                logger.warn("No valid config classes found after parsing.")
                processed = true
                return emptyList()
            }
            
            validateUniqueGroupNamesAndKeys(configDataList)
            generateCode(configDataList, resolver)
            
        } catch (e: Exception) {
            logger.error("Error during processing: ${e.message}\n${e.stackTraceToString()}")
        } finally {
            processed = true
        }
        
        return emptyList()
    }
    
    /**
     * 获取有效的配置类
     */
    private fun getValidConfigClasses(resolver: Resolver): List<KSClassDeclaration> {
        // 验证注解使用是否正确
        val annotatedSymbols = resolver.getSymbolsWithAnnotation(Config::class.qualifiedName!!)
        annotatedSymbols.forEach { symbol ->
            if (symbol !is KSClassDeclaration) {
                logger.error(ConfigError.ONLY_INTERFACES_CAN_BE_ANNOTATED, symbol)
            }
        }
        
        return annotatedSymbols
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .toList()
    }
    
    /**
     * 解析配置类
     */
    private fun parseConfigClasses(configClasses: List<KSClassDeclaration>): List<ConfigData> {
        return configClasses.mapNotNull { configClass ->
            try {
                components.configClassParser.parseConfigClass(configClass)
            } catch (e: Exception) {
                logger.error("Error parsing config class ${configClass.simpleName.asString()}: ${e.message}", configClass)
                null
            }
        }
    }
    
    /**
     * 验证组名和属性键的唯一性
     */
    private fun validateUniqueGroupNamesAndKeys(configDataList: List<ConfigData>) {
        // 验证组名唯一性
        val groupNames = mutableSetOf<String>()
        configDataList.forEach { configData ->
            if (configData.groupName in groupNames) {
                logger.error(
                    ConfigError.duplicateGroupName(configData.groupName, configData.name)
                )
            } else {
                groupNames.add(configData.groupName)
            }
        }
        
        // 验证属性键在组内的唯一性
        configDataList.forEach { configData ->
            val groupPropertyKeys = mutableMapOf<String, String>() // key -> propertyName
            configData.properties.forEach { propertyData ->
                val existingPropertyName = groupPropertyKeys[propertyData.key]
                if (existingPropertyName != null) {
                    logger.error(
                        ConfigError.duplicatePropertyKeyInGroup(
                            propertyData.key, 
                            configData.groupName,
                            propertyData.name,
                            existingPropertyName
                        )
                    )
                } else {
                    groupPropertyKeys[propertyData.key] = propertyData.name
                }
            }
        }
    }

    private val commonMainModuleName = "commonMain"

    @OptIn(KspExperimental::class)
    private fun Resolver.isCommonMain(): Boolean {
        return moduleName().contains(commonMainModuleName)
    }

    @OptIn(KspExperimental::class)
    private fun Resolver.moduleName(): String {
        return try {
            getModuleName().getShortName()
        } catch (e: Throwable) {
            ""
        }
    }

    /**
     * 生成代码
     */
    @OptIn(KspExperimental::class)
    private fun generateCode(configDataList: List<ConfigData>, resolver: Resolver) {
        val moduleName = resolver.moduleName()
        val isCommonMain = resolver.isCommonMain()
        val allConfigClasses = configDataList.map { it.groupName to it.name }

        // 为每个配置类生成单独的文件
        configDataList.forEach { configData ->
            if (shouldProcessConfigData(configData, isCommonMain)) {
                generateConfigFile(configData, moduleName)
            }
        }
        
        // 生成全局扩展方法文件 - 不是只处理当前模块的配置文件，而是全部的。
        // 只不过是在 commonmain中生成expect方法，而在具体的target比如 android ios中生成 actual 方法
        if (configDataList.isNotEmpty()) {
            generateGlobalExtensionsFile(allConfigClasses, isCommonMain, moduleName, configDataList)
        }
    }
    
    /**
     * 判断是否应该处理指定的配置数据
     */
    private fun shouldProcessConfigData(configData: ConfigData, isCommonMain: Boolean): Boolean {
        if (!isMultiplatform) return true
        
        return if (isCommonMain) {
            configData.ksFile.filePath.contains(commonMainModuleName)
        } else {
            !configData.ksFile.filePath.contains(commonMainModuleName)
        }
    }
    
    /**
     * 为单个配置类生成文件
     */
    private fun generateConfigFile(configData: ConfigData, moduleName: String) {
        val fileSpecBuilder = createConfigFileSpecBuilder(configData.implName)
        
        // 生成配置实现类
        val configImpl = components.configImplGenerator.generateConfigImpl(configData)
        fileSpecBuilder.addType(configImpl)
        
        // 生成扩展属性
        val extensionProperty = components.extensionGenerator.generateExtensionProperty(configData, GENERATED_PACKAGE_NAME)
        fileSpecBuilder.addProperty(extensionProperty)
        
        // 写入文件 - 只依赖于当前配置文件
        val dependencies = Dependencies(aggregating = false, sources = arrayOf(configData.ksFile))
        writeConfigFile(fileSpecBuilder.build(), configData.implName, moduleName, dependencies)
    }
    
    /**
     * 生成全局扩展方法文件
     */
    private fun generateGlobalExtensionsFile(
        allConfigClasses: List<Pair<String, String>>,
        isCommonMain: Boolean,
        moduleName: String,
        configDataList: List<ConfigData>
    ) {
        val fileSpecBuilder = createExtensionFileSpecBuilder("AppConfigExtensions")
        
        // 生成全局扩展方法
        fileSpecBuilder.addFunction(components.extensionGenerator.generateGetAllConfigItemsMethod(allConfigClasses, isCommonMain, isMultiplatform))
        fileSpecBuilder.addFunction(components.extensionGenerator.generateUpdateAllFromRemoteMethod(allConfigClasses, isCommonMain, isMultiplatform))
        fileSpecBuilder.addFunction(components.extensionGenerator.generateResetAllToDefaultsMethod(allConfigClasses, isCommonMain, isMultiplatform))
        
        // 写入文件 - 依赖于所有配置文件
        val allConfigFiles = configDataList.map { it.ksFile }.toTypedArray()
        val dependencies = Dependencies(aggregating = true, sources = allConfigFiles)
        writeConfigFile(fileSpecBuilder.build(), "AppConfigExtensions", moduleName, dependencies)
    }
    
    /**
     * 创建配置文件规格构建器
     */
    private fun createConfigFileSpecBuilder(className: String): FileSpec.Builder {
        val fileSpecBuilder = FileSpec.builder(GENERATED_PACKAGE_NAME, className)
        
        // 添加配置文件需要的导入
        fileSpecBuilder.addImport("io.github.mambawow.appconfig", "AppConfig", "ConfigItemDescriptor", "StandardConfigItem", "OptionConfigItem", "OptionItemDescriptor", "DataType", "PanelType")
        fileSpecBuilder.addImport("io.github.mambawow.appconfig.store", "ConfigStore")
        
        return fileSpecBuilder
    }
    
    /**
     * 创建扩展文件规格构建器
     */
    private fun createExtensionFileSpecBuilder(className: String): FileSpec.Builder {
        val fileSpecBuilder = FileSpec.builder(GENERATED_PACKAGE_NAME, className)
        
        // 添加扩展文件需要的导入
        fileSpecBuilder.addImport("io.github.mambawow.appconfig", "AppConfig", "ConfigItemDescriptor")
        
        return fileSpecBuilder
    }
    
    /**
     * 写入生成的文件
     */
    private fun writeConfigFile(fileSpec: FileSpec, className: String, moduleName: String, dependencies: Dependencies) {
        try {
            val fileStream = codeGenerator.createNewFile(
                dependencies,
                GENERATED_PACKAGE_NAME,
                className + "_" + moduleName
            )
            
            OutputStreamWriter(fileStream, Charsets.UTF_8).use { writer ->
                writer.write(fileSpec.toString())
            }
        } catch (e: Exception) {
            logger.error("✗ Error generating $className: ${e.message}\n${e.stackTraceToString()}")
        }
    }
} 