package io.github.mambawow.appconfig.model

/**
 * 配置处理错误消息集中管理
 */
object ConfigError {
    const val ONLY_INTERFACES_CAN_BE_ANNOTATED = "Only Interfaces can be annotated with @Config"
    const val BLANK_GROUP_NAME = "has a blank groupName in @Config. Skipping."
    const val INVALID_GROUP_NAME = "has an invalid groupName in @Config. Group name must be a valid Kotlin identifier. Skipping."
    const val DUPLICATE_GROUP_NAME = "Duplicate groupName found. Group names must be unique. Skipping this class."
    const val DUPLICATE_PROPERTY_KEY = "Duplicate property key found. Property keys must be unique across all config classes."
    const val DUPLICATE_PROPERTY_KEY_IN_GROUP = "Duplicate property key found within the same configuration group. Property keys must be unique within each group."
    const val MISSING_KEY = "is missing a non-blank 'key'. Skipping."
    const val MISSING_DEFAULT_VALUE = "is missing 'defaultValue' or it is null. Skipping."
    const val UNRECOGNIZED_ANNOTATION = "Internal error: Unrecognized annotation on property"
    const val MUST_BE_SEALED_CLASS = "with @OptionProperty must be a sealed class or sealed interface"
    const val MISSING_OPTION_ANNOTATION = "its sealed class must be annotated with @Option"
    const val NO_SUBCLASSES = "Sealed class for @OptionProperty must have at least one subclass"
    const val MISSING_OPTION_ITEM_ANNOTATION = "must be annotated with @OptionItem"
    const val MISSING_OPTION_ID = "@OptionItem must have optionId"
    const val MULTIPLE_DEFAULT_OPTIONS = "Only one @OptionItem can have isDefault=true"
    const val NO_DEFAULT_OPTION = "At least one @OptionItem must have isDefault=true"
    const val DUPLICATE_OPTION_IDS = "All @OptionItem optionId values must be unique"
    const val CANNOT_CREATE_CODE_BLOCK = "Internal error: Cannot create CodeBlock for non-Option default value"
    const val INCOMPATIBLE_PANEL_TYPE = "PanelType is not compatible with DataType"
    
    fun blankGroupName(className: String) = "Class '$className' $BLANK_GROUP_NAME"
    fun invalidGroupName(className: String, groupName: String) = 
        "Class '$className' $INVALID_GROUP_NAME GroupName: '$groupName'"
    fun duplicateGroupName(groupName: String, className: String) = 
        "Duplicate groupName \"$groupName\" found in class '$className'. $DUPLICATE_GROUP_NAME"
    fun duplicatePropertyKey(key: String, className: String, existingClassName: String) = 
        "Property key \"$key\" in class '$className' conflicts with the same key in class '$existingClassName'. $DUPLICATE_PROPERTY_KEY"
    fun duplicatePropertyKeyInGroup(key: String, groupName: String, propertyName: String, existingPropertyName: String) = 
        "Property key \"$key\" in group '$groupName' is used by both property '$propertyName' and '$existingPropertyName'. $DUPLICATE_PROPERTY_KEY_IN_GROUP"
    fun missingKey(propertyName: String, className: String, annotationName: String) = 
        "Property '$propertyName' in '$className' @$annotationName $MISSING_KEY"
    fun missingDefaultValue(propertyName: String, annotationName: String) = 
        "Property '$propertyName' with @$annotationName $MISSING_DEFAULT_VALUE"
    fun unrecognizedAnnotation(annotationName: String, propertyName: String) = 
        "$UNRECOGNIZED_ANNOTATION '$annotationName' on property '$propertyName'."
    fun mustBeSealedClass(propertyName: String, actualType: String) = 
        "Property '$propertyName' $MUST_BE_SEALED_CLASS (actual: $actualType)."
    fun missingOptionAnnotation(propertyName: String, className: String) = 
        "Property '$propertyName' with @OptionProperty: $MISSING_OPTION_ANNOTATION '$className' must be annotated with @Option."
    fun noSubclasses(propertyName: String) = 
        "Property '$propertyName': $NO_SUBCLASSES"
    fun missingOptionItemAnnotation(propertyName: String, subclassName: String) = 
        "Property '$propertyName': Subclass '$subclassName' $MISSING_OPTION_ITEM_ANNOTATION."
    fun missingOptionId(propertyName: String, subclassName: String) = 
        "Property '$propertyName': @OptionItem on '$subclassName' $MISSING_OPTION_ID."
    fun multipleDefaultOptions(propertyName: String) = 
        "Property '$propertyName': $MULTIPLE_DEFAULT_OPTIONS."
    fun noDefaultOption(propertyName: String) = 
        "Property '$propertyName': $NO_DEFAULT_OPTION."
    fun duplicateOptionIds(propertyName: String) = 
        "Property '$propertyName': $DUPLICATE_OPTION_IDS."
    fun cannotCreateCodeBlock(dataType: String, propertyName: String, value: Any?) = 
        "$CANNOT_CREATE_CODE_BLOCK of $dataType for '$propertyName'. Value: $value. Skipping."
} 