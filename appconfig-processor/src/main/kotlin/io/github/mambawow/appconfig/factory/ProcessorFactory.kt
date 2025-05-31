package io.github.mambawow.appconfig.factory

import com.google.devtools.ksp.processing.KSPLogger
import io.github.mambawow.appconfig.generator.ConfigImplGenerator
import io.github.mambawow.appconfig.generator.ExtensionGenerator
import io.github.mambawow.appconfig.parser.ConfigClassParser
import io.github.mambawow.appconfig.parser.PropertyParser

/**
 * 处理器工厂
 * 负责创建和管理各个组件的依赖关系
 * 
 * 应用工厂模式和依赖注入原则
 */
object ProcessorFactory {
    
    /**
     * 创建属性解析器
     */
    fun createPropertyParser(logger: KSPLogger): PropertyParser {
        return PropertyParser(logger)
    }
    
    /**
     * 创建配置类解析器
     */
    fun createConfigClassParser(logger: KSPLogger): ConfigClassParser {
        val propertyParser = createPropertyParser(logger)
        return ConfigClassParser(logger, propertyParser)
    }
    
    /**
     * 创建配置实现生成器
     */
    fun createConfigImplGenerator(): ConfigImplGenerator {
        return ConfigImplGenerator()
    }
    
    /**
     * 创建扩展生成器
     */
    fun createExtensionGenerator(): ExtensionGenerator {
        return ExtensionGenerator()
    }
    
    /**
     * 创建所有组件的集合
     */
    data class ProcessorComponents(
        val propertyParser: PropertyParser,
        val configClassParser: ConfigClassParser,
        val configImplGenerator: ConfigImplGenerator,
        val extensionGenerator: ExtensionGenerator
    )
    
    /**
     * 一次性创建所有组件
     */
    fun createAllComponents(logger: KSPLogger): ProcessorComponents {
        val propertyParser = createPropertyParser(logger)
        val configClassParser = ConfigClassParser(logger, propertyParser)
        val configImplGenerator = createConfigImplGenerator()
        val extensionGenerator = createExtensionGenerator()
        
        return ProcessorComponents(
            propertyParser = propertyParser,
            configClassParser = configClassParser,
            configImplGenerator = configImplGenerator,
            extensionGenerator = extensionGenerator
        )
    }
} 