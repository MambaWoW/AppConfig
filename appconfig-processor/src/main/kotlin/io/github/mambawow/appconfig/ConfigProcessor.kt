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

    private val isMultiplatform = options["isMultiplatform"]?.toInt() == 1
    
    // 使用工厂创建组件
    private val components = ProcessorFactory.createAllComponents(logger)
    
    // 配置常量
    companion object {
        const val GENERATED_PACKAGE_NAME = "io.github.mambawow.appconfig"
        const val GENERATED_CLASS_NAME = "AppConfigExt"

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
                logger.error(ConfigError.ONLY_CLASSES_OR_INTERFACES_CAN_BE_ANNOTATED, symbol)
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
        
        // 验证所有属性键的全局唯一性
        val allPropertyKeys = mutableMapOf<String, String>() // key -> className
        configDataList.forEach { configData ->
            configData.properties.forEach { propertyData ->
                val existingClassName = allPropertyKeys[propertyData.key]
                if (existingClassName != null) {
                    logger.error(
                        ConfigError.duplicatePropertyKey(
                            propertyData.key, 
                            configData.name, 
                            existingClassName
                        )
                    )
                } else {
                    allPropertyKeys[propertyData.key] = configData.name
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
        val fileSpecBuilder = createFileSpecBuilder()
        val allConfigClasses = configDataList.map { it.groupName to it.name }

        val moduleName = resolver.moduleName()

        val isCommonMain = resolver.isCommonMain()
        // 生成配置实现类
        configDataList.forEach { configData ->
            logger.warn("classes: $moduleName >>> ${configData.name} + ${configData.ksFile.filePath}")

            if (isMultiplatform) {
                if (isCommonMain) {
                    if (!configData.ksFile.filePath.contains(commonMainModuleName)) {
                        return@forEach
                    }
                } else {
                    if (configData.ksFile.filePath.contains(commonMainModuleName)) {
                        return@forEach
                    }
                }
            }

            val configImpl = components.configImplGenerator.generateConfigImpl(configData)
            fileSpecBuilder.addType(configImpl)
            
            // 生成扩展属性
            val extensionProperty = components.extensionGenerator.generateExtensionProperty(configData, GENERATED_PACKAGE_NAME)
            fileSpecBuilder.addProperty(extensionProperty)
        }
        
        // 生成全局扩展方法
        fileSpecBuilder.addFunction(components.extensionGenerator.generateGetAllConfigItemsMethod(allConfigClasses, isCommonMain, isMultiplatform))
        fileSpecBuilder.addFunction(components.extensionGenerator.generateUpdateAllFromRemoteMethod(allConfigClasses, isCommonMain, isMultiplatform))
        fileSpecBuilder.addFunction(components.extensionGenerator.generateResetAllToDefaultsMethod(allConfigClasses, isCommonMain, isMultiplatform))
        
        // 写入文件
        writeGeneratedFile(fileSpecBuilder.build(), configDataList, moduleName)
    }
    
    /**
     * 创建文件规格构建器
     */
    private fun createFileSpecBuilder(): FileSpec.Builder {
        val fileSpecBuilder = FileSpec.builder(GENERATED_PACKAGE_NAME, GENERATED_CLASS_NAME)
        
        // 添加必要的导入
        fileSpecBuilder.addImport("kotlinx.coroutines.flow", "Flow", "map", "first")
        fileSpecBuilder.addImport(
            "io.github.mambawow.appconfig",
            "ConfigItemDescriptor",
            "StandardConfigItem",
            "OptionConfigItem",
            "OptionItemDescriptor",
            "AppConfig",
            "DataType",
            "PanelType"
        )
        fileSpecBuilder.addImport("io.github.mambawow.appconfig.store", "ConfigStore")
        
        // 导入属性注解
        val propertyAnnotationTypes = listOf(
            IntProperty::class,
            LongProperty::class,
            FloatProperty::class,
            DoubleProperty::class,
            StringProperty::class,
            BooleanProperty::class,
            OptionProperty::class
        )
        
        propertyAnnotationTypes.firstOrNull()?.qualifiedName?.substringBeforeLast('.')?.let { pkg ->
            if (pkg.isNotBlank() && pkg != "kotlin" && pkg != "java.lang") {
                propertyAnnotationTypes.forEach { annotationClass ->
                    annotationClass.simpleName?.let { simpleName ->
                        fileSpecBuilder.addImport(pkg, simpleName)
                    }
                }
            }
        }
        
        return fileSpecBuilder
    }
    
    /**
     * 写入生成的文件
     */
    private fun writeGeneratedFile(fileSpec: FileSpec, configDataList: List<ConfigData>, moduleName: String) {
        try {
            val fileStream = codeGenerator.createNewFile(
                Dependencies.ALL_FILES,
                GENERATED_PACKAGE_NAME,
                GENERATED_CLASS_NAME + moduleName
            )
            
            OutputStreamWriter(fileStream, Charsets.UTF_8).use { writer ->
                writer.write(fileSpec.toString())
            }
            
            val groupNames = configDataList.map { it.groupName }
            logger.info("✓ Successfully generated $GENERATED_CLASS_NAME.kt in $GENERATED_PACKAGE_NAME for groups: ${groupNames.joinToString()}.")
            
        } catch (e: Exception) {
            logger.error("✗ Error generating $GENERATED_CLASS_NAME: ${e.message}\n${e.stackTraceToString()}")
        }
    }
} 