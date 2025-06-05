package io.github.mambawow.appconfig

import kotlinx.coroutines.runBlocking
import kotlin.test.*

/**
 * Base test class providing common utilities for configuration testing with proper isolation
 *
 * This class ensures that each test method runs in isolation by:
 * - Configuring AppConfig with isolated in-memory storage before each test
 * - Providing helper methods for consistent testing patterns
 * - Ensuring tests don't interfere with each other through proper cleanup
 */
abstract class ConfigTestBase {

    /**
     * Setup method called before each test to ensure clean state
     * Each test gets a fresh InMemoryConfigStore to prevent interference
     */
    @BeforeTest
    fun baseSetup() {
        setupCleanAppConfig()
    }

    /**
     * Cleanup method called after each test to reset state
     */
    @AfterTest
    fun baseCleanup() {
        runBlocking {
            resetAppConfig()
        }
    }

    /**
     * Initialize AppConfig with a clean, isolated storage for testing
     */
    private fun setupCleanAppConfig() {
        // Configure AppConfig with isolated in-memory storage
        // This automatically clears the existing cache
        AppConfig.configure { name ->
            IsolatedInMemoryConfigStore()
        }
    }

    /**
     * Reset AppConfig to prevent test interference
     */
    @OptIn(InternalAPI::class)
    private suspend fun resetAppConfig() {
        // Reset cache and stored data to ensure clean state for next test
        AppConfig.clear(clearStoredData = true)
    }

    /**
     * Helper function to verify default values match expected values
     *
     * @param expected The expected default value
     * @param actual The actual default value from the configuration
     * @param propertyName The name of the property being tested (for error reporting)
     */
    protected fun assertDefaultValue(expected: Any?, actual: Any?, propertyName: String) {
        assertEquals(
            expected,
            actual,
            "Default value mismatch for property '$propertyName'. Expected: $expected, Actual: $actual"
        )
    }

    /**
     * Helper function to test value assignment and retrieval with state validation
     * This method also validates that the value persists correctly
     *
     * @param setValue Lambda function to set the value
     * @param getValue Lambda function to get the value
     * @param expectedValue The value that should be set and retrieved
     * @param propertyName The name of the property being tested (for error reporting)
     */
    protected fun assertValueAssignmentAndRetrieval(
        setValue: () -> Unit,
        getValue: () -> Any?,
        expectedValue: Any?,
        propertyName: String
    ) {
        // Store original value for potential restoration
        val originalValue = getValue()

        try {
            // Set the new value
            setValue()
            val actualValue = getValue()

            // Verify the assignment worked
            assertEquals(
                expectedValue,
                actualValue,
                "Value assignment/retrieval failed for property '$propertyName'. Expected: $expectedValue, Actual: $actualValue"
            )

            // Double-check by reading the value again to ensure persistence
            val secondRead = getValue()
            assertEquals(
                expectedValue,
                secondRead,
                "Value persistence failed for property '$propertyName'. Expected: $expectedValue, Actual: $secondRead"
            )

        } catch (e: Exception) {
            // If test fails, provide more context
            throw AssertionError(
                "Failed during value assignment test for property '$propertyName'. " +
                "Original: $originalValue, Expected: $expectedValue, Error: ${e.message}",
                e
            )
        }
    }

    /**
     * Helper function to test option property selection with validation
     *
     * @param T The type of the option
     * @param setOption Lambda function to set the option value
     * @param getOption Lambda function to get the option value
     * @param testOption The option value to test
     * @param propertyName The name of the property being tested (for error reporting)
     */
    protected fun <T> assertOptionSelection(
        setOption: (T) -> Unit,
        getOption: () -> T,
        testOption: T,
        propertyName: String
    ) {
        val originalOption = getOption()

        try {
            setOption(testOption)
            val actualOption = getOption()

            assertEquals(
                testOption,
                actualOption,
                "Option selection failed for property '$propertyName'. Expected: $testOption, Actual: $actualOption"
            )

            // Verify option persists on subsequent reads
            val secondRead = getOption()
            assertEquals(
                testOption,
                secondRead,
                "Option persistence failed for property '$propertyName'. Expected: $testOption, Actual: $secondRead"
            )

        } catch (e: Exception) {
            throw AssertionError(
                "Failed during option selection test for property '$propertyName'. " +
                "Original: $originalOption, Expected: $testOption, Error: ${e.message}",
                e
            )
        }
    }

    /**
     * Helper function to test that property changes persist across multiple operations
     *
     * @param setValue Lambda function to set the value
     * @param getValue Lambda function to get the value
     * @param testValues List of values to test in sequence
     * @param propertyName The name of the property being tested (for error reporting)
     */
    protected fun assertValuePersistence(
        setValue: (Any) -> Unit,
        getValue: () -> Any?,
        testValues: List<Any>,
        propertyName: String
    ) {
        testValues.forEachIndexed { index, testValue ->
            try {
                setValue(testValue)
                val retrievedValue = getValue()
                assertEquals(
                    testValue,
                    retrievedValue,
                    "Value persistence failed for property '$propertyName' with value: $testValue (index: $index)"
                )
            } catch (e: Exception) {
                throw AssertionError(
                    "Failed during value persistence test for property '$propertyName' at index $index. " +
                    "Test value: $testValue, Error: ${e.message}",
                    e
                )
            }
        }
    }

    /**
     * Helper function to verify that property changes don't affect other properties
     *
     * @param changeProperty Lambda function to change one property
     * @param getOtherProperty Lambda function to get another property that should remain unchanged
     * @param expectedOtherValue The expected value of the other property
     * @param changedPropertyName Name of the property being changed
     * @param otherPropertyName Name of the property that should remain unchanged
     */
    protected fun assertPropertyIndependence(
        changeProperty: () -> Unit,
        getOtherProperty: () -> Any?,
        expectedOtherValue: Any?,
        changedPropertyName: String,
        otherPropertyName: String
    ) {
        val originalOtherValue = getOtherProperty()

        try {
            changeProperty()
            val actualOtherValue = getOtherProperty()

            assertEquals(
                expectedOtherValue,
                actualOtherValue,
                "Property '$otherPropertyName' should not change when '$changedPropertyName' is modified. Expected: $expectedOtherValue, Actual: $actualOtherValue"
            )

        } catch (e: Exception) {
            throw AssertionError(
                "Failed during property independence test. " +
                "Changed property: '$changedPropertyName', Other property: '$otherPropertyName'. " +
                "Original other value: $originalOtherValue, Expected: $expectedOtherValue, Error: ${e.message}",
                e
            )
        }
    }

    /**
     * Helper function to reset a configuration to its default state
     * This is useful for tests that need to verify reset functionality
     */
    protected fun resetToDefaults() {
        // Reset cache and configure with fresh isolated storage
        AppConfig.configure { name ->
            IsolatedInMemoryConfigStore()
        }
    }

} 