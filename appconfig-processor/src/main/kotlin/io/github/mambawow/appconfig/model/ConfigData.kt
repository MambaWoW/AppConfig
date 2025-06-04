package io.github.mambawow.appconfig.model

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.PanelType

/**
 * Data model representing a configuration class with all its metadata.
 * 
 * This class encapsulates all information needed to generate implementation
 * classes for configuration interfaces annotated with @Config.
 * 
 * @property name The simple class name of the configuration interface
 * @property packageName The fully qualified package name containing the interface  
 * @property groupName The configuration group name (used for grouping related configs)
 * @property properties List of configuration properties defined in the interface
 * @property modifiers Kotlin modifiers applied to the interface (internal, public, etc.)
 * @property ksFile The source file containing the configuration interface
 * @property annotations List of annotations applied to the configuration interface
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
    /** Generated implementation class name (e.g., "UserConfigImpl" for "UserConfig") */
    val implName = "${name}Impl"
    
    /** Extension property name for accessing this config (lowercase group name) */
    val extensionPropertyName = groupName.lowercase()
}

/**
 * Data model representing a single configuration property.
 * 
 * Contains all metadata needed to generate getters, setters, and configuration
 * descriptors for properties annotated with configuration property annotations.
 * 
 * @property name The property name as declared in the interface
 * @property typeName The Kotlin type of the property (for code generation)
 * @property dataType The internal data type classification (STRING, INT, OPTION, etc.)
 * @property key The storage key used for persisting this property value
 * @property description Human-readable description of the property's purpose
 * @property defaultValue The default value for this property
 * @property optionItems For OPTION type properties, the list of available choices
 * @property panelType The UI panel type recommended for editing this property
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
 * Data model representing a single option in an OPTION type property.
 * 
 * For sealed class-based option properties, each subclass becomes an option item
 * with a unique ID and metadata for UI presentation.
 * 
 * @property className The simple name of the sealed class subclass
 * @property typeName The full type name for code generation
 * @property optionId Unique integer ID for this option (used for storage)
 * @property description Human-readable description of this option
 * @property isDefault Whether this is the default option when no value is set
 */
data class OptionItemData(
    val className: String,
    val typeName: TypeName,
    val optionId: Int,
    val description: String,
    val isDefault: Boolean
)

