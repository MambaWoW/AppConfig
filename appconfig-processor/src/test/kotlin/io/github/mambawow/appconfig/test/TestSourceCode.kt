package io.github.mambawow.appconfig.test

/**
 * Common test source code snippets for testing the AppConfig processor.
 */
object TestSourceCode {

    /**
     * Simple valid configuration with basic properties.
     */
    const val SIMPLE_CONFIG = """
        @Config(groupName = "Test")
        interface TestConfig {
            @StringProperty(key = "testString", defaultValue = "hello", description = "Test string")
            var testString: String
            
            @IntProperty(key = "testInt", defaultValue = 42, description = "Test integer")
            var testInt: Int
            
            @BooleanProperty(key = "testBool", defaultValue = true, description = "Test boolean")
            var testBool: Boolean
        }
    """

    /**
     * Configuration using convention over configuration (empty keys).
     */
    const val CONVENTION_CONFIG = """
        @Config
        interface ConventionConfig {
            @StringProperty(defaultValue = "default")
            var myString: String
            
            @IntProperty(defaultValue = 0)
            var myInt: Int
        }
    """

    /**
     * Configuration with option property.
     */
    const val OPTION_CONFIG = """
        @Config(groupName = "Options")
        interface OptionConfig {
            @OptionProperty(key = "theme", description = "UI Theme")
            var theme: Theme
        }
        
        @Option
        sealed class Theme {
            @OptionItem(optionId = 0, description = "Light theme", isDefault = true)
            object Light : Theme()
            
            @OptionItem(optionId = 1, description = "Dark theme")
            object Dark : Theme()
        }
    """

    /**
     * Invalid configuration - class instead of interface.
     */
    const val INVALID_CLASS_CONFIG = """
        @Config(groupName = "Invalid")
        class InvalidConfig {
            @StringProperty(key = "test", defaultValue = "test")
            var test: String = ""
        }
    """

    /**
     * Invalid configuration - duplicate group names.
     */
    const val DUPLICATE_GROUP_CONFIG_1 = """
        @Config(groupName = "Duplicate")
        interface FirstConfig {
            @StringProperty(key = "first", defaultValue = "test")
            var first: String
        }
    """

    const val DUPLICATE_GROUP_CONFIG_2 = """
        @Config(groupName = "Duplicate")
        interface SecondConfig {
            @StringProperty(key = "second", defaultValue = "test")
            var second: String
        }
    """

    /**
     * Invalid option configuration - no default option.
     */
    const val NO_DEFAULT_OPTION_CONFIG = """
        @Config(groupName = "NoDefault")
        interface NoDefaultConfig {
            @OptionProperty(key = "option", description = "No default option")
            var option: NoDefaultOption
        }
        
        @Option
        sealed class NoDefaultOption {
            @OptionItem(optionId = 0, description = "Option A")
            object A : NoDefaultOption()
            
            @OptionItem(optionId = 1, description = "Option B")
            object B : NoDefaultOption()
        }
    """

    /**
     * Invalid option configuration - multiple default options.
     */
    const val MULTIPLE_DEFAULT_OPTIONS_CONFIG = """
        @Config(groupName = "MultiDefault")
        interface MultiDefaultConfig {
            @OptionProperty(key = "option", description = "Multiple defaults")
            var option: MultiDefaultOption
        }
        
        @Option
        sealed class MultiDefaultOption {
            @OptionItem(optionId = 0, description = "Option A", isDefault = true)
            object A : MultiDefaultOption()
            
            @OptionItem(optionId = 1, description = "Option B", isDefault = true)
            object B : MultiDefaultOption()
        }
    """

    /**
     * Invalid option configuration - duplicate option IDs.
     */
    const val DUPLICATE_OPTION_IDS_CONFIG = """
        @Config(groupName = "DuplicateIds")
        interface DuplicateIdsConfig {
            @OptionProperty(key = "option", description = "Duplicate IDs")
            var option: DuplicateIdsOption
        }
        
        @Option
        sealed class DuplicateIdsOption {
            @OptionItem(optionId = 0, description = "Option A", isDefault = true)
            object A : DuplicateIdsOption()
            
            @OptionItem(optionId = 0, description = "Option B")
            object B : DuplicateIdsOption()
        }
    """

    /**
     * Invalid option configuration - not a sealed class.
     */
    const val NOT_SEALED_CLASS_CONFIG = """
        @Config(groupName = "NotSealed")
        interface NotSealedConfig {
            @OptionProperty(key = "option", description = "Not sealed class")
            var option: String
        }
    """

    /**
     * Invalid configuration - duplicate keys in same group.
     */
    const val DUPLICATE_KEYS_CONFIG = """
        @Config(groupName = "DuplicateKeys")
        interface DuplicateKeysConfig {
            @StringProperty(key = "duplicate", defaultValue = "first")
            var first: String
            
            @IntProperty(key = "duplicate", defaultValue = 42)
            var second: Int
        }
    """

    /**
     * Valid configuration with all property types.
     */
    const val ALL_TYPES_CONFIG = """
        @Config(groupName = "AllTypes")
        interface AllTypesConfig {
            @StringProperty(key = "str", defaultValue = "test", description = "String property")
            var stringProp: String
            
            @IntProperty(key = "int", defaultValue = 42, description = "Int property")
            var intProp: Int
            
            @LongProperty(key = "long", defaultValue = 42L, description = "Long property")
            var longProp: Long
            
            @FloatProperty(key = "float", defaultValue = 1.5f, description = "Float property")
            var floatProp: Float
            
            @DoubleProperty(key = "double", defaultValue = 2.5, description = "Double property")
            var doubleProp: Double
            
            @BooleanProperty(key = "bool", defaultValue = false, description = "Boolean property")
            var boolProp: Boolean
            
            @OptionProperty(key = "option", description = "Option property")
            var optionProp: TestOption
        }
        
        @Option
        sealed class TestOption {
            @OptionItem(optionId = 0, description = "First option", isDefault = true)
            object First : TestOption()
            
            @OptionItem(optionId = 1, description = "Second option")
            object Second : TestOption()
        }
    """
} 