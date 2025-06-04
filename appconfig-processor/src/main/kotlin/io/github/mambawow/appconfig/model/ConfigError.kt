package io.github.mambawow.appconfig.model

/**
 * Centralized error message management for configuration processing.
 * 
 * This object provides consistent error messages across the processor,
 * with both static message constants and dynamic message builders for
 * context-specific error reporting.
 * 
 * Error messages are categorized by the type of validation they represent:
 * - Class-level validation errors
 * - Property-level validation errors  
 * - Option-specific validation errors
 * - Internal processing errors
 */
object ConfigError {
    
    // ========================================
    // Class-level validation errors
    // ========================================
    
    /** Error when @Config is applied to non-interface types */
    const val ONLY_INTERFACES_CAN_BE_ANNOTATED = "Only Interfaces can be annotated with @Config"
    
    /** Error when groupName is blank or empty */
    const val BLANK_GROUP_NAME = "has a blank groupName in @Config. Skipping."
    
    /** Error when groupName is not a valid Kotlin identifier */
    const val INVALID_GROUP_NAME = "has an invalid groupName in @Config. Group name must be a valid Kotlin identifier. Skipping."
    
    /** Error when multiple classes use the same groupName */
    const val DUPLICATE_GROUP_NAME = "Duplicate groupName found. Group names must be unique. Skipping this class."
    
    // ========================================
    // Property-level validation errors
    // ========================================
    
    /** Error when property keys are not unique globally (legacy - now per-group) */
    const val DUPLICATE_PROPERTY_KEY = "Duplicate property key found. Property keys must be unique across all config classes."
    
    /** Error when property keys are not unique within the same group */
    const val DUPLICATE_PROPERTY_KEY_IN_GROUP = "Duplicate property key found within the same configuration group. Property keys must be unique within each group."
    
    /** Error when property annotation is missing required key parameter */
    const val MISSING_KEY = "is missing a non-blank 'key'. Skipping."
    
    /** Error when property annotation is missing required defaultValue */
    const val MISSING_DEFAULT_VALUE = "is missing 'defaultValue' or it is null. Skipping."
    
    /** Error for unrecognized property annotations (internal error) */
    const val UNRECOGNIZED_ANNOTATION = "Internal error: Unrecognized annotation on property"
    
    // ========================================
    // Option property validation errors  
    // ========================================
    
    /** Error when @OptionProperty type is not a sealed class */
    const val MUST_BE_SEALED_CLASS = "with @OptionProperty must be a sealed class or sealed interface"
    
    /** Error when sealed class for option property lacks @Option annotation */
    const val MISSING_OPTION_ANNOTATION = "its sealed class must be annotated with @Option"
    
    /** Error when sealed class has no subclasses for option items */
    const val NO_SUBCLASSES = "Sealed class for @OptionProperty must have at least one subclass"
    
    /** Error when option subclass lacks @OptionItem annotation */
    const val MISSING_OPTION_ITEM_ANNOTATION = "must be annotated with @OptionItem"
    
    /** Error when @OptionItem is missing required optionId */
    const val MISSING_OPTION_ID = "@OptionItem must have optionId"
    
    /** Error when multiple option items are marked as default */
    const val MULTIPLE_DEFAULT_OPTIONS = "Only one @OptionItem can have isDefault=true"
    
    /** Error when no option item is marked as default */
    const val NO_DEFAULT_OPTION = "At least one @OptionItem must have isDefault=true"
    
    /** Error when option items have duplicate optionId values */
    const val DUPLICATE_OPTION_IDS = "All @OptionItem optionId values must be unique"
    
    // ========================================
    // Internal processing errors
    // ========================================
    
    /** Error when code generation fails unexpectedly */
    const val CANNOT_CREATE_CODE_BLOCK = "Internal error: Cannot create CodeBlock for non-Option default value"
    
    /** Error when PanelType doesn't match DataType */
    const val INCOMPATIBLE_PANEL_TYPE = "PanelType is not compatible with DataType"
    
    // ========================================
    // Dynamic error message builders
    // ========================================
    
    /**
     * Creates error message for blank group name.
     * @param className The name of the class with the blank group name
     */
    fun blankGroupName(className: String) = "Class '$className' $BLANK_GROUP_NAME"
    
    /**
     * Creates error message for invalid group name.
     * @param className The name of the class with invalid group name
     * @param groupName The invalid group name
     */
    fun invalidGroupName(className: String, groupName: String) = 
        "Class '$className' $INVALID_GROUP_NAME GroupName: '$groupName'"
    
    /**
     * Creates error message for duplicate group name.
     * @param groupName The duplicated group name
     * @param className The class that attempted to use the duplicate name
     */
    fun duplicateGroupName(groupName: String, className: String) = 
        "Duplicate groupName \"$groupName\" found in class '$className'. $DUPLICATE_GROUP_NAME"
    
    /**
     * Creates error message for globally duplicate property key (legacy).
     * @param key The duplicated property key
     * @param className The class containing the duplicate key
     * @param existingClassName The class that already uses this key
     */
    fun duplicatePropertyKey(key: String, className: String, existingClassName: String) = 
        "Property key \"$key\" in class '$className' conflicts with the same key in class '$existingClassName'. $DUPLICATE_PROPERTY_KEY"
    
    /**
     * Creates error message for duplicate property key within a group.
     * @param key The duplicated property key
     * @param groupName The configuration group containing the duplicate
     * @param propertyName The property attempting to use the duplicate key
     * @param existingPropertyName The property that already uses this key
     */
    fun duplicatePropertyKeyInGroup(key: String, groupName: String, propertyName: String, existingPropertyName: String) = 
        "Property key \"$key\" in group '$groupName' is used by both property '$propertyName' and '$existingPropertyName'. $DUPLICATE_PROPERTY_KEY_IN_GROUP"
    
    /**
     * Creates error message for missing property key.
     * @param propertyName The property missing the key
     * @param className The class containing the property
     * @param annotationName The annotation type name
     */
    fun missingKey(propertyName: String, className: String, annotationName: String) = 
        "Property '$propertyName' in '$className' @$annotationName $MISSING_KEY"
    
    /**
     * Creates error message for missing default value.
     * @param propertyName The property missing the default value
     * @param annotationName The annotation type name
     */
    fun missingDefaultValue(propertyName: String, annotationName: String) = 
        "Property '$propertyName' with @$annotationName $MISSING_DEFAULT_VALUE"
    
    /**
     * Creates error message for unrecognized annotation.
     * @param annotationName The unrecognized annotation name
     * @param propertyName The property with the unrecognized annotation
     */
    fun unrecognizedAnnotation(annotationName: String, propertyName: String) = 
        "$UNRECOGNIZED_ANNOTATION '$annotationName' on property '$propertyName'."
    
    /**
     * Creates error message for non-sealed class in option property.
     * @param propertyName The property with incorrect type
     * @param actualType The actual type that was found
     */
    fun mustBeSealedClass(propertyName: String, actualType: String) = 
        "Property '$propertyName' $MUST_BE_SEALED_CLASS (actual: $actualType)."
    
    /**
     * Creates error message for missing @Option annotation.
     * @param propertyName The property with the missing annotation
     * @param className The sealed class that should have @Option
     */
    fun missingOptionAnnotation(propertyName: String, className: String) = 
        "Property '$propertyName' with @OptionProperty: $MISSING_OPTION_ANNOTATION '$className' must be annotated with @Option."
    
    /**
     * Creates error message for sealed class with no subclasses.
     * @param propertyName The property with empty sealed class
     */
    fun noSubclasses(propertyName: String) = 
        "Property '$propertyName': $NO_SUBCLASSES"
    
    /**
     * Creates error message for missing @OptionItem annotation.
     * @param propertyName The property containing the subclass
     * @param subclassName The subclass missing the annotation
     */
    fun missingOptionItemAnnotation(propertyName: String, subclassName: String) = 
        "Property '$propertyName': Subclass '$subclassName' $MISSING_OPTION_ITEM_ANNOTATION."
    
    /**
     * Creates error message for missing optionId in @OptionItem.
     * @param propertyName The property containing the option item
     * @param subclassName The subclass missing the optionId
     */
    fun missingOptionId(propertyName: String, subclassName: String) = 
        "Property '$propertyName': @OptionItem on '$subclassName' $MISSING_OPTION_ID."
    
    /**
     * Creates error message for multiple default options.
     * @param propertyName The property with multiple defaults
     */
    fun multipleDefaultOptions(propertyName: String) = 
        "Property '$propertyName': $MULTIPLE_DEFAULT_OPTIONS."
    
    /**
     * Creates error message for no default option.
     * @param propertyName The property without any default option
     */
    fun noDefaultOption(propertyName: String) = 
        "Property '$propertyName': $NO_DEFAULT_OPTION."
    
    /**
     * Creates error message for duplicate option IDs.
     * @param propertyName The property with duplicate option IDs
     */
    fun duplicateOptionIds(propertyName: String) = 
        "Property '$propertyName': $DUPLICATE_OPTION_IDS."
    
    /**
     * Creates error message for code block generation failure.
     * @param dataType The data type that failed
     * @param propertyName The property being processed
     * @param value The value that couldn't be processed
     */
    fun cannotCreateCodeBlock(dataType: String, propertyName: String, value: Any?) = 
        "$CANNOT_CREATE_CODE_BLOCK of $dataType for '$propertyName'. Value: $value. Skipping."
} 