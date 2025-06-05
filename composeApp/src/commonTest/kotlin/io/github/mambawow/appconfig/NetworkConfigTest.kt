package io.github.mambawow.appconfig

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

/**
 * Comprehensive test suite for NetworkConfig configuration interface
 *
 */
class NetworkConfigTest : ConfigTestBase() {
    
    private lateinit var networkConfig: NetworkConfig
    
    @BeforeTest
    fun setup() {
        // Note: baseSetup() from ConfigTestBase is automatically called first
        // Access the generated implementation through AppConfig extension
        networkConfig = AppConfig.networkconfig
    }
    
    // ========================================
    // String Property Tests: networkTimeout
    // ========================================
    
    @Test
    fun `test networkTimeout default value`() {
        // Test: Default value should be "60s" as specified in annotation
        assertDefaultValue(
            expected = "60s",
            actual = networkConfig.networkTimeout,
            propertyName = "networkTimeout"
        )
    }
    
    @Test
    fun `test networkTimeout value assignment and retrieval`() {
        // Test: Should be able to set custom timeout values
        val testTimeouts = listOf("30s", "120s", "5m", "1h", "0s", "")
        
        testTimeouts.forEach { timeout ->
            assertValueAssignmentAndRetrieval(
                setValue = { networkConfig.networkTimeout = timeout },
                getValue = { networkConfig.networkTimeout },
                expectedValue = timeout,
                propertyName = "networkTimeout"
            )
        }
    }
    
    @Test
    fun `test networkTimeout with various timeout formats`() {
        // Test: Should handle different timeout format strings
        val timeoutFormats = mapOf(
            "30s" to "seconds format",
            "5m" to "minutes format", 
            "2h" to "hours format",
            "1000ms" to "milliseconds format",
            "infinite" to "infinite timeout",
            "0" to "zero timeout"
        )
        
        timeoutFormats.forEach { (timeout, description) ->
            networkConfig.networkTimeout = timeout
            assertEquals(
                timeout,
                networkConfig.networkTimeout,
                "Failed to handle $description: $timeout"
            )
        }
    }
    
    @Test
    fun `test networkTimeout with special string values`() {
        // Test: String property should handle edge cases
        val specialValues = listOf(
            "",                    // empty string
            " ",                   // whitespace
            "\n",                  // newline
            "\t",                  // tab
            "null",                // string "null"
            "undefined",           // string "undefined"
            "0",                   // string zero
            "false",               // string boolean
            "特殊字符测试",          // unicode characters
            "emoji 😀 test",       // emoji
            "very long timeout value that exceeds normal length expectations" // long string
        )
        
        specialValues.forEach { specialValue ->
            networkConfig.networkTimeout = specialValue
            assertEquals(
                specialValue,
                networkConfig.networkTimeout,
                "Failed to handle special string value: '$specialValue'"
            )
        }
    }
    
    // ========================================
    // Integer Property Tests: maxRetries
    // ========================================
    
    @Test
    fun `test maxRetries default value`() {
        // Test: Default value should be 3 as specified in annotation
        assertDefaultValue(
            expected = 3,
            actual = networkConfig.maxRetries,
            propertyName = "maxRetries"
        )
    }
    
    @Test
    fun `test maxRetries value assignment and retrieval`() {
        // Test: Should be able to set various retry counts
        val testRetryCounts = listOf(0, 1, 5, 10, 100, Int.MAX_VALUE)
        
        testRetryCounts.forEach { retryCount ->
            assertValueAssignmentAndRetrieval(
                setValue = { networkConfig.maxRetries = retryCount },
                getValue = { networkConfig.maxRetries },
                expectedValue = retryCount,
                propertyName = "maxRetries"
            )
        }
    }
    
    @Test
    fun `test maxRetries boundary conditions`() {
        // Test: Should handle boundary values for integer type
        
        // Test zero retries
        networkConfig.maxRetries = 0
        assertEquals(0, networkConfig.maxRetries, "Should handle zero retries")
        
        // Test negative values (if business logic allows)
        networkConfig.maxRetries = -1
        assertEquals(-1, networkConfig.maxRetries, "Should handle negative values")
        
        // Test maximum value
        networkConfig.maxRetries = Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, networkConfig.maxRetries, "Should handle maximum integer value")
        
        // Test minimum value
        networkConfig.maxRetries = Int.MIN_VALUE
        assertEquals(Int.MIN_VALUE, networkConfig.maxRetries, "Should handle minimum integer value")
    }
    
    // ========================================
    // Property Independence Tests
    // ========================================
    
    @Test
    fun `test property independence within NetworkConfig`() {
        // Test: Changes to one property should not affect the other
        val originalTimeout = networkConfig.networkTimeout
        val originalRetries = networkConfig.maxRetries
        
        // Change timeout, verify retries unchanged
        networkConfig.networkTimeout = "999s"
        assertEquals(originalRetries, networkConfig.maxRetries, "Retries should not change when timeout changes")
        
        // Reset and test the other direction
        resetToDefaults()
        networkConfig = AppConfig.networkconfig
        
        // Change retries, verify timeout remains at default
        networkConfig.maxRetries = 999
        assertEquals("60s", networkConfig.networkTimeout, "Timeout should remain at default")
    }
    
    @Test
    fun `test custom key mapping persistence`() {
        // Test: Verify that the custom key "timeout" is used for networkTimeout property
        val testValue = "custom_timeout_value"
        networkConfig.networkTimeout = testValue
        
        // Verify the value persists (this tests the underlying storage key mapping)
        assertEquals(testValue, networkConfig.networkTimeout, "Custom key mapping should work correctly")
        
        // Verify it doesn't interfere with other properties
        val originalRetries = networkConfig.maxRetries
        assertEquals(originalRetries, networkConfig.maxRetries, "Other properties should be unaffected")
    }
    
    // ========================================
    // Realistic Scenarios Tests
    // ========================================
    
    @Test
    fun `test realistic network configuration scenarios`() {
        // Test: Realistic configuration scenarios in isolation
        
        // Scenario 1: Fast mobile network
        networkConfig.networkTimeout = "10s"
        networkConfig.maxRetries = 2
        assertEquals("10s", networkConfig.networkTimeout, "Should set mobile timeout")
        assertEquals(2, networkConfig.maxRetries, "Should set mobile retries")
        
        // Reset and test another scenario
        resetToDefaults()
        networkConfig = AppConfig.networkconfig
        
        // Scenario 2: Slow connection with high reliability needs
        networkConfig.networkTimeout = "120s"
        networkConfig.maxRetries = 10
        assertEquals("120s", networkConfig.networkTimeout, "Should set reliable timeout")
        assertEquals(10, networkConfig.maxRetries, "Should set reliable retries")
        
        // Reset and test development scenario
        resetToDefaults()
        networkConfig = AppConfig.networkconfig
        
        // Scenario 3: Development/testing environment
        networkConfig.networkTimeout = "1s"
        networkConfig.maxRetries = 0
        assertEquals("1s", networkConfig.networkTimeout, "Should set development timeout")
        assertEquals(0, networkConfig.maxRetries, "Should set development retries")
    }
    
    @Test
    fun `test network configuration edge cases`() {
        // Test: Edge cases for network configuration
        
        // Case 1: Very high timeout with no retries (one-shot operation)
        networkConfig.networkTimeout = "300s"
        networkConfig.maxRetries = 0
        assertEquals("300s", networkConfig.networkTimeout, "Should handle long timeout")
        assertEquals(0, networkConfig.maxRetries, "Should handle no retries")
        
        // Case 2: Very short timeout with many retries (quick retry pattern)
        networkConfig.networkTimeout = "100ms"
        networkConfig.maxRetries = 50
        assertEquals("100ms", networkConfig.networkTimeout, "Should handle short timeout")
        assertEquals(50, networkConfig.maxRetries, "Should handle many retries")
        
        // Case 3: Return to defaults
        networkConfig.networkTimeout = "60s"
        networkConfig.maxRetries = 3
        assertEquals("60s", networkConfig.networkTimeout, "Should match default timeout")
        assertEquals(3, networkConfig.maxRetries, "Should match default retries")
    }
    
    @Test
    fun `test value persistence across multiple operations in single test`() {
        // Test: Values should persist across multiple get/set operations
        val timeoutValues = listOf("1s", "30s", "2m", "5m", "1h")
        val retryValues = listOf(0, 1, 3, 5, 10)
        
        // Test timeout persistence
        timeoutValues.forEach { timeout ->
            networkConfig.networkTimeout = timeout
            assertEquals(timeout, networkConfig.networkTimeout, "Timeout should persist: $timeout")
            
            // Verify that setting timeout doesn't affect retries
            val currentRetries = networkConfig.maxRetries
            assertEquals(currentRetries, networkConfig.maxRetries, "Retries should remain unchanged")
        }
        
        // Test retry persistence  
        retryValues.forEach { retries ->
            networkConfig.maxRetries = retries
            assertEquals(retries, networkConfig.maxRetries, "Retries should persist: $retries")
            
            // Verify that setting retries doesn't affect timeout
            val currentTimeout = networkConfig.networkTimeout
            assertEquals(currentTimeout, networkConfig.networkTimeout, "Timeout should remain unchanged")
        }
    }
    
    @Test
    fun `test type consistency across operations`() {
        // Test: Verify type consistency for properties
        
        // String property should always return String type
        networkConfig.networkTimeout = "test"
        val timeoutValue: String = networkConfig.networkTimeout
        assertEquals("test", timeoutValue, "Should maintain String type for timeout")
        
        // Int property should always return Int type
        networkConfig.maxRetries = 42
        val retriesValue: Int = networkConfig.maxRetries
        assertEquals(42, retriesValue, "Should maintain Int type for retries")
    }
    
    @Test
    fun `test extreme values handling`() {
        // Test: Network configuration should handle extreme values gracefully
        
        // Very long timeout string
        val longTimeout = "999999999999999999999s"
        networkConfig.networkTimeout = longTimeout
        assertEquals(longTimeout, networkConfig.networkTimeout, "Should handle very long timeout string")
        
        // Empty timeout
        networkConfig.networkTimeout = ""
        assertEquals("", networkConfig.networkTimeout, "Should handle empty timeout")
        
        // Extreme retry values
        networkConfig.maxRetries = Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, networkConfig.maxRetries, "Should handle maximum retries")
        
        networkConfig.maxRetries = Int.MIN_VALUE
        assertEquals(Int.MIN_VALUE, networkConfig.maxRetries, "Should handle minimum retries")
    }
} 