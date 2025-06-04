package io.github.mambawow.appconfig

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName

/**
 * Checks if a KSClassDeclaration is a sealed class.
 * 
 * @return true if the class declaration has the SEALED modifier, false otherwise.
 */
fun KSClassDeclaration.isSealed(): Boolean {
    return Modifier.SEALED in modifiers
}

/**
 * Represents a class reference with its name and package for code generation.
 * 
 * @property name The simple class name.
 * @property packageName The fully qualified package name.
 */
data class ConfigClass(
    val name: String,
    val packageName: String
)

// Commonly used class references for code generation
val FlowClass = ConfigClass("Flow", "kotlinx.coroutines.flow")
val ConfigItemDescriptorClass = ConfigClass("ConfigItemDescriptor", "io.github.mambawow.appconfig")
val StandardConfigItemClass = ConfigClass("StandardConfigItem", "io.github.mambawow.appconfig")
val OptionConfigItemClass = ConfigClass("OptionConfigItem", "io.github.mambawow.appconfig")

/**
 * Converts a ConfigClass to a KotlinPoet ClassName.
 * 
 * @return A ClassName instance for use in code generation.
 */
fun ConfigClass.toClassName() = ClassName(packageName, name)

/**
 * Gets the canonical name of the class.
 * 
 * @return The fully qualified class name as a string.
 */
fun ConfigClass.canonicalName() = toClassName().canonicalName