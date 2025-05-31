package io.github.mambawow.appconfig

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName

/**
 * @author Frank
 * @created 5/20/25
 */
fun KSClassDeclaration.isSealed(): Boolean {
    return Modifier.SEALED in modifiers
}

data class ConfigClass(
    val name: String,
    val packageName: String
)

val FlowClass = ConfigClass("Flow", "kotlinx.coroutines.flow")
val ConfigItemDescriptorClass = ConfigClass("ConfigItemDescriptor", "io.github.mambawow.appconfig")
val StandardConfigItemClass = ConfigClass("StandardConfigItem", "io.github.mambawow.appconfig")
val OptionConfigItemClass = ConfigClass("OptionConfigItem", "io.github.mambawow.appconfig")

fun ConfigClass.toClassName() = ClassName(packageName, name)

fun ConfigClass.canonicalName() = toClassName().canonicalName