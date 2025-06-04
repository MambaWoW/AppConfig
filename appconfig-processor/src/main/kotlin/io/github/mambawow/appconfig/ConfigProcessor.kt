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

/**
 * Main KSP processor for the AppConfig library.
 * 
 * This processor analyzes @Config annotated interfaces and generates:
 * - Individual implementation classes for each config interface
 * - Extension properties for convenient access to configuration instances
 * - Global helper methods for configuration management across all groups
 * 
 * The processor supports both single-platform and multiplatform projects, generating
 * separate files for each configuration class to enable efficient incremental compilation.
 * 
 * For multiplatform projects, it generates `expect` declarations in commonMain and
 * `actual` implementations in platform-specific source sets.
 */
class ConfigProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private var hasProcessed = false
    private val isMultiplatformProject = options["AppConfig_isMultiplatform"]?.toInt() == 1
    private val processorComponents = ProcessorFactory.createAllComponents(logger)
    
    companion object {
        /** Package name for all generated files */
        const val GENERATED_PACKAGE_NAME = "io.github.mambawow.appconfig"
        
        /** Prefix for generated implementation class names */
        const val GENERATED_CLASS_NAME_PREFIX = "AppConfig_"
        
        private const val COMMON_MAIN_MODULE_NAME = "commonMain"

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
        if (hasProcessed) return emptyList()

        try {
            val configurationClasses = findValidConfigurationClasses(resolver)
            if (configurationClasses.isEmpty()) {
                logger.info("No classes found with @Config annotation. Skipping processing.")
                return completeProcessing()
            }
            
            val configurationDataList = parseConfigurationClasses(configurationClasses)
            if (configurationDataList.isEmpty()) {
                logger.warn("No valid config classes found after parsing.")
                return completeProcessing()
            }
            
            validateConfigurationIntegrity(configurationDataList)
            generateAllCodeFiles(configurationDataList, resolver)
            
        } catch (e: Exception) {
            logger.error("Error during processing: ${e.message}\n${e.stackTraceToString()}")
        }
        
        return completeProcessing()
    }

    private fun completeProcessing(): List<KSAnnotated> {
        hasProcessed = true
        return emptyList()
    }
    
    /**
     * Finds and validates all classes annotated with @Config.
     * 
     * @param resolver The KSP resolver for symbol resolution
     * @return List of valid configuration class declarations
     */
    private fun findValidConfigurationClasses(resolver: Resolver): List<KSClassDeclaration> {
        val annotatedSymbols = resolver.getSymbolsWithAnnotation(Config::class.qualifiedName!!)
        
        // Validate that only interfaces are annotated with @Config
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
     * Parses configuration classes into structured data models.
     * 
     * @param configClasses List of configuration class declarations to parse
     * @return List of parsed configuration data models
     */
    private fun parseConfigurationClasses(configClasses: List<KSClassDeclaration>): List<ConfigData> {
        return configClasses.mapNotNull { configClass ->
            try {
                processorComponents.configClassParser.parseConfigClass(configClass)
            } catch (e: Exception) {
                logger.error("Error parsing config class ${configClass.simpleName.asString()}: ${e.message}", configClass)
                null
            }
        }
    }
    
    /**
     * Validates configuration integrity including unique group names and property keys.
     * 
     * @param configDataList List of configuration data to validate
     */
    private fun validateConfigurationIntegrity(configDataList: List<ConfigData>) {
        validateUniqueGroupNames(configDataList)
        validateUniquePropertyKeysWithinGroups(configDataList)
    }
    
    /**
     * Validates that all group names are unique across the project.
     * 
     * @param configDataList List of configuration data to validate
     */
    private fun validateUniqueGroupNames(configDataList: List<ConfigData>) {
        val encounteredGroupNames = mutableSetOf<String>()
        
        configDataList.forEach { configData ->
            if (configData.groupName in encounteredGroupNames) {
                logger.error(
                    ConfigError.duplicateGroupName(configData.groupName, configData.name)
                )
            } else {
                encounteredGroupNames.add(configData.groupName)
            }
        }
    }
    
    /**
     * Validates that property keys are unique within each configuration group.
     * Different groups may have duplicate keys, but within a group all keys must be unique.
     * 
     * @param configDataList List of configuration data to validate
     */
    private fun validateUniquePropertyKeysWithinGroups(configDataList: List<ConfigData>) {
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

    /**
     * Determines if the current module is the commonMain module in a multiplatform project.
     */
    private fun Resolver.isCommonMainModule(): Boolean {
        return moduleName().contains(COMMON_MAIN_MODULE_NAME)
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
     * Generates all necessary code files for the given configuration data.
     * 
     * For multiplatform projects:
     * - Generates expect declarations in commonMain
     * - Generates actual implementations in platform-specific modules
     * 
     * @param configDataList List of configuration data to generate code for
     * @param resolver The KSP resolver for module information
     */
    @OptIn(KspExperimental::class)
    private fun generateAllCodeFiles(configDataList: List<ConfigData>, resolver: Resolver) {
        val moduleName = resolver.moduleName()
        val isCommonMainModule = resolver.isCommonMainModule()
        val allConfigurationClasses = configDataList.map { it.groupName to it.name }

        // Generate individual implementation files for each configuration class
        configDataList.forEach { configData ->
            if (shouldProcessConfigurationData(configData, isCommonMainModule)) {
                generateConfigurationImplementationFile(configData, moduleName)
            }
        }
        
        // Generate global extension methods file containing helper functions
        // for all configuration groups
        if (configDataList.isNotEmpty()) {
            generateGlobalExtensionsFile(
                allConfigurationClasses, 
                isCommonMainModule, 
                moduleName, 
                configDataList
            )
        }
    }
    
    /**
     * Determines whether the given configuration data should be processed for the current module.
     * 
     * For single-platform projects, all configurations are always processed.
     * For multiplatform projects, only configurations from the appropriate source set are processed.
     * 
     * @param configData Configuration data to check
     * @param isCommonMainModule Whether the current module is commonMain
     * @return true if the configuration should be processed, false otherwise
     */
    private fun shouldProcessConfigurationData(configData: ConfigData, isCommonMainModule: Boolean): Boolean {
        if (!isMultiplatformProject) return true
        
        return if (isCommonMainModule) {
            configData.ksFile.filePath.contains(COMMON_MAIN_MODULE_NAME)
        } else {
            !configData.ksFile.filePath.contains(COMMON_MAIN_MODULE_NAME)
        }
    }
    
    /**
     * Generates implementation file for a single configuration class.
     * 
     * Each file contains:
     * - The implementation class for the configuration interface
     * - An extension property for convenient access to the configuration instance
     * 
     * @param configData Configuration data to generate implementation for
     * @param moduleName Current module name for file naming
     */
    private fun generateConfigurationImplementationFile(configData: ConfigData, moduleName: String) {
        val fileSpecBuilder = createConfigurationFileSpecBuilder(configData.implName)
        
        // Generate the configuration implementation class
        val configurationImplementation = processorComponents.configImplGenerator.generateConfigImpl(configData)
        fileSpecBuilder.addType(configurationImplementation)
        
        // Generate extension property for convenient access
        val extensionProperty = processorComponents.extensionGenerator.generateExtensionProperty(
            configData, 
            GENERATED_PACKAGE_NAME
        )
        fileSpecBuilder.addProperty(extensionProperty)
        
        // Write file with dependencies optimized for incremental compilation
        val dependencies = Dependencies(aggregating = false, sources = arrayOf(configData.ksFile))
        writeGeneratedFile(fileSpecBuilder.build(), configData.implName, moduleName, dependencies)
    }
    
    /**
     * Generates file containing global extension methods for configuration management.
     * 
     * The generated methods include:
     * - getAllConfigItems(): Retrieves all configuration items across all groups
     * - updateAllFromRemote(): Updates all configurations from remote data
     * - resetAllToDefaults(): Resets all configurations to their default values
     * 
     * @param allConfigurationClasses List of all configuration class metadata
     * @param isCommonMainModule Whether generating for commonMain module
     * @param moduleName Current module name
     * @param configDataList List of configuration data for dependency tracking
     */
    private fun generateGlobalExtensionsFile(
        allConfigurationClasses: List<Pair<String, String>>,
        isCommonMainModule: Boolean,
        moduleName: String,
        configDataList: List<ConfigData>
    ) {
        val fileSpecBuilder = createExtensionMethodsFileSpecBuilder("AppConfigExtensions")
        
        // Generate global extension methods
        val extensionGenerator = processorComponents.extensionGenerator
        fileSpecBuilder.addFunction(
            extensionGenerator.generateGetAllConfigItemsMethod(
                allConfigurationClasses, 
                isCommonMainModule, 
                isMultiplatformProject
            )
        )
        fileSpecBuilder.addFunction(
            extensionGenerator.generateUpdateAllFromRemoteMethod(
                allConfigurationClasses, 
                isCommonMainModule, 
                isMultiplatformProject
            )
        )
        fileSpecBuilder.addFunction(
            extensionGenerator.generateResetAllToDefaultsMethod(
                allConfigurationClasses, 
                isCommonMainModule, 
                isMultiplatformProject
            )
        )
        
        // Write file with dependencies on all configuration source files
        val allConfigurationSourceFiles = configDataList.map { it.ksFile }.toTypedArray()
        val dependencies = Dependencies(aggregating = true, sources = allConfigurationSourceFiles)
        writeGeneratedFile(fileSpecBuilder.build(), "AppConfigExtensions", moduleName, dependencies)
    }
    
    /**
     * Creates a FileSpec builder for configuration implementation files.
     * 
     * @param className Name of the class being generated
     * @return FileSpec builder with necessary imports
     */
    private fun createConfigurationFileSpecBuilder(className: String): FileSpec.Builder {
        return FileSpec.builder(GENERATED_PACKAGE_NAME, className).apply {
            // Import classes needed for configuration implementations
            addImport(
                "io.github.mambawow.appconfig", 
                "AppConfig", "ConfigItemDescriptor", "StandardConfigItem", 
                "OptionConfigItem", "OptionItemDescriptor", "DataType", "PanelType"
            )
            addImport("io.github.mambawow.appconfig.store", "ConfigStore")
        }
    }
    
    /**
     * Creates a FileSpec builder for extension methods files.
     * 
     * @param className Name of the class being generated
     * @return FileSpec builder with necessary imports
     */
    private fun createExtensionMethodsFileSpecBuilder(className: String): FileSpec.Builder {
        return FileSpec.builder(GENERATED_PACKAGE_NAME, className).apply {
            // Import classes needed for extension methods
            addImport("io.github.mambawow.appconfig", "AppConfig", "ConfigItemDescriptor")
        }
    }
    
    /**
     * Writes the generated file to the output directory.
     * 
     * @param fileSpec The generated file specification
     * @param className Name of the class for error reporting
     * @param moduleName Current module name for file naming
     * @param dependencies File dependencies for incremental compilation
     */
    private fun writeGeneratedFile(
        fileSpec: FileSpec, 
        className: String, 
        moduleName: String, 
        dependencies: Dependencies
    ) {
        try {
            val outputStream = codeGenerator.createNewFile(
                dependencies,
                GENERATED_PACKAGE_NAME,
                "${className}_${moduleName}"
            )
            
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                writer.write(fileSpec.toString())
            }
        } catch (e: Exception) {
            logger.error("Error generating $className: ${e.message}\n${e.stackTraceToString()}")
        }
    }
} 