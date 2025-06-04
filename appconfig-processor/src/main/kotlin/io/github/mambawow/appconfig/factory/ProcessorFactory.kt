package io.github.mambawow.appconfig.factory

import com.google.devtools.ksp.processing.KSPLogger
import io.github.mambawow.appconfig.generator.ConfigImplGenerator
import io.github.mambawow.appconfig.generator.ExtensionGenerator
import io.github.mambawow.appconfig.parser.ConfigClassParser
import io.github.mambawow.appconfig.parser.PropertyParser

/**
 * Factory for creating and managing AppConfig processor components.
 * 
 * This factory implements the Factory pattern and dependency injection principles
 * to manage the creation and wiring of all processor components.
 *
 * All components are stateless and can be safely reused across processing cycles.
 */
object ProcessorFactory {

    fun createPropertyParser(logger: KSPLogger): PropertyParser {
        return PropertyParser(logger)
    }

    fun createConfigClassParser(logger: KSPLogger): ConfigClassParser {
        val propertyParser = createPropertyParser(logger)
        return ConfigClassParser(logger, propertyParser)
    }

    fun createConfigImplGenerator(): ConfigImplGenerator {
        return ConfigImplGenerator()
    }

    fun createExtensionGenerator(): ExtensionGenerator {
        return ExtensionGenerator()
    }

    data class ProcessorComponents(
        val propertyParser: PropertyParser,
        val configClassParser: ConfigClassParser,
        val configImplGenerator: ConfigImplGenerator,
        val extensionGenerator: ExtensionGenerator
    )

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