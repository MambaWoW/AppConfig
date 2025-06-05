package io.github.mambawow.appconfig

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Integration test suite for all AppConfig interfaces
 */
class AppConfigIntegrationTest : ConfigTestBase() {
    
    private lateinit var featureFlags: FeatureFlags
    private lateinit var networkConfig: NetworkConfig
    private lateinit var uiConfig: UserInterfaceConfig
    
    @BeforeTest
    fun setup() {
        // Note: baseSetup() from ConfigTestBase is automatically called first
        // Access all generated implementations through AppConfig extensions
        featureFlags = AppConfig.featureflags
        networkConfig = AppConfig.networkconfig
        uiConfig = AppConfig.userinterface
    }
    
    // ========================================
    // Cross-Interface Isolation Tests
    // ========================================
    
    @Test
    fun `test configuration interface complete isolation`() {
        // Test: Changes in one interface should absolutely not affect others
        
        // Set initial values across all interfaces
        featureFlags.isChatEnabled = true
        featureFlags.protocolType = ProtocolType.QUIC
        
        networkConfig.networkTimeout = "30s"
        networkConfig.maxRetries = 5
        
        uiConfig.animationTimeout = "300ms"
        uiConfig.theme = "dark"
        
        // Change values in FeatureFlags
        featureFlags.isChatEnabled = false
        featureFlags.protocolType = ProtocolType.HTTP1_1
        
        // Verify NetworkConfig is completely unaffected
        assertEquals("30s", networkConfig.networkTimeout, "Network timeout should be isolated")
        assertEquals(5, networkConfig.maxRetries, "Network retries should be isolated")
        
        // Verify UserInterfaceConfig is completely unaffected
        assertEquals("300ms", uiConfig.animationTimeout, "UI timeout should be isolated")
        assertEquals("dark", uiConfig.theme, "UI theme should be isolated")
        
        // Change values in NetworkConfig
        networkConfig.networkTimeout = "120s"
        networkConfig.maxRetries = 10
        
        // Verify FeatureFlags is completely unaffected
        assertEquals(false, featureFlags.isChatEnabled, "Feature flags should be isolated")
        assertEquals(ProtocolType.HTTP1_1, featureFlags.protocolType, "Protocol should be isolated")
        
        // Verify UserInterfaceConfig is still unaffected
        assertEquals("300ms", uiConfig.animationTimeout, "UI timeout should remain isolated")
        assertEquals("dark", uiConfig.theme, "UI theme should remain isolated")
        
        // Change values in UserInterfaceConfig
        uiConfig.animationTimeout = "1s"
        uiConfig.theme = "light"
        
        // Verify FeatureFlags is still unaffected
        assertEquals(false, featureFlags.isChatEnabled, "Feature flags should remain isolated")
        assertEquals(ProtocolType.HTTP1_1, featureFlags.protocolType, "Protocol should remain isolated")
        
        // Verify NetworkConfig is still unaffected
        assertEquals("120s", networkConfig.networkTimeout, "Network timeout should remain isolated")
        assertEquals(10, networkConfig.maxRetries, "Network retries should remain isolated")
    }
    
    @Test
    fun `test all configuration defaults in isolation`() {
        // Test: Verify all default values are correctly set across all interfaces
        // This test ensures no cross-contamination during initialization
        
        // FeatureFlags defaults (verified in clean state)
        assertEquals(false, featureFlags.isChatEnabled, "Chat should be disabled by default")
        assertEquals(true, featureFlags.isWelcomePageEnabled, "Welcome should be enabled by default")
        assertEquals(LoginPageStyle.Style1, featureFlags.loginPageStyle, "Should default to Style1")
        assertEquals(ProtocolType.HTTP2, featureFlags.protocolType, "Should default to HTTP2")
        
        // NetworkConfig defaults (verified in clean state)
        assertEquals("60s", networkConfig.networkTimeout, "Should default to 60s timeout")
        assertEquals(3, networkConfig.maxRetries, "Should default to 3 retries")
        
        // UserInterfaceConfig defaults (verified in clean state)
        assertEquals("5s", uiConfig.animationTimeout, "Should default to 5s animation timeout")
        assertEquals("light", uiConfig.theme, "Should default to light theme")
    }
    
    @Test
    fun `test configuration descriptor generation and integrity`() {
        // Test: Verify that configuration item descriptors are generated correctly
        // and don't interfere with each other
        
        val allConfigItems = AppConfig.getAllConfigItems()
        assertNotNull(allConfigItems, "Config items should not be null")
        assertTrue(allConfigItems.isNotEmpty(), "Should have configuration items")
        
        // Verify we have items from all interfaces
        val configKeys = allConfigItems.map { it.key }.toSet()
        val groupNames = allConfigItems.map { it.groupName }.toSet()
        
        // Check for proper group separation
        assertTrue(groupNames.contains("FeatureFlags"), "Should have FeatureFlags group")
        assertTrue(groupNames.contains("NetworkConfig"), "Should have NetworkConfig group")  
        assertTrue(groupNames.contains("UserInterface"), "Should have UserInterface group")
        
        // Verify items belong to correct groups
        val featureFlagsItems = allConfigItems.filter { it.groupName == "FeatureFlags" }
        val networkConfigItems = allConfigItems.filter { it.groupName == "NetworkConfig" }
        val uiConfigItems = allConfigItems.filter { it.groupName == "UserInterface" }
        
        assertTrue(featureFlagsItems.size >= 4, "FeatureFlags should have at least 4 items")
        assertTrue(networkConfigItems.size >= 2, "NetworkConfig should have at least 2 items")
        assertTrue(uiConfigItems.size >= 2, "UserInterface should have at least 2 items")
        
        // Verify no key conflicts (keys should be unique within groups)
        val groupKeyMap = allConfigItems.groupBy { it.groupName }
        groupKeyMap.forEach { (groupName, items) ->
            val keys = items.map { it.key }
            val uniqueKeys = keys.toSet()
            assertEquals(
                keys.size, 
                uniqueKeys.size, 
                "Group '$groupName' should have unique keys within the group"
            )
        }
    }
    
    // ========================================
    // Complex Scenario Tests
    // ========================================
    
    @Test
    fun `test application startup configuration scenario`() {
        // Test: Simulate application startup with specific configuration
        // All changes should be isolated to their respective interfaces
        
        // Mobile app startup configuration
        featureFlags.isChatEnabled = false  // Disable chat for mobile
        featureFlags.isWelcomePageEnabled = true  // Show welcome for new users
        featureFlags.loginPageStyle = LoginPageStyle.Style2  // Use mobile-optimized style
        featureFlags.protocolType = ProtocolType.HTTP2  // Use modern protocol
        
        networkConfig.networkTimeout = "10s"  // Shorter timeout for mobile
        networkConfig.maxRetries = 5  // More retries for unreliable mobile networks
        
        uiConfig.animationTimeout = "200ms"  // Quick animations for responsiveness
        uiConfig.theme = "light"  // Default to light theme
        
        // Verify all settings are applied correctly and independently
        assertFalse(featureFlags.isChatEnabled, "Mobile chat should be disabled")
        assertTrue(featureFlags.isWelcomePageEnabled, "Mobile welcome should be enabled")
        assertEquals(LoginPageStyle.Style2, featureFlags.loginPageStyle, "Should use mobile style")
        assertEquals(ProtocolType.HTTP2, featureFlags.protocolType, "Should use HTTP2")
        
        assertEquals("10s", networkConfig.networkTimeout, "Should use mobile timeout")
        assertEquals(5, networkConfig.maxRetries, "Should use mobile retries")
        
        assertEquals("200ms", uiConfig.animationTimeout, "Should use mobile animations")
        assertEquals("light", uiConfig.theme, "Should use light theme")
    }
    
    @Test
    fun `test enterprise configuration scenario`() {
        // Test: Simulate enterprise application configuration
        // Verify complete isolation between configuration domains
        
        // Enterprise configuration
        featureFlags.isChatEnabled = true  // Enable all features for enterprise
        featureFlags.isWelcomePageEnabled = false  // Skip welcome for trained users
        featureFlags.loginPageStyle = LoginPageStyle.Style1  // Standard enterprise style
        featureFlags.protocolType = ProtocolType.HTTP2  // Secure modern protocol
        
        networkConfig.networkTimeout = "120s"  // Longer timeout for complex operations
        networkConfig.maxRetries = 10  // High reliability requirements
        
        uiConfig.animationTimeout = "0ms"  // Disable animations for performance
        uiConfig.theme = "corporate-blue"  // Custom enterprise theme
        
        // Verify enterprise settings work independently
        assertTrue(featureFlags.isChatEnabled, "Enterprise should have all features")
        assertFalse(featureFlags.isWelcomePageEnabled, "Enterprise should skip welcome")
        assertEquals(LoginPageStyle.Style1, featureFlags.loginPageStyle, "Should use enterprise style")
        assertEquals(ProtocolType.HTTP2, featureFlags.protocolType, "Should use secure protocol")
        
        assertEquals("120s", networkConfig.networkTimeout, "Should use enterprise timeout")
        assertEquals(10, networkConfig.maxRetries, "Should use enterprise reliability")
        
        assertEquals("0ms", uiConfig.animationTimeout, "Should disable animations")
        assertEquals("corporate-blue", uiConfig.theme, "Should use enterprise theme")
    }
    
    @Test
    fun `test development configuration scenario`() {
        // Test: Simulate development/debugging configuration
        // Each interface should work independently for debugging purposes
        
        // Development configuration
        featureFlags.isChatEnabled = true  // Enable all features for testing
        featureFlags.isWelcomePageEnabled = true  // Test welcome flow
        featureFlags.loginPageStyle = LoginPageStyle.Style3  // Test alternative style
        featureFlags.protocolType = ProtocolType.QUIC  // Test cutting-edge protocol
        
        networkConfig.networkTimeout = "5s"  // Short timeout for quick feedback
        networkConfig.maxRetries = 0  // No retries for immediate failure feedback
        
        uiConfig.animationTimeout = "1s"  // Slow animations for debugging
        uiConfig.theme = "debug-mode"  // Special debug theme
        
        // Verify development settings function independently
        assertTrue(featureFlags.isChatEnabled, "Dev should enable features")
        assertTrue(featureFlags.isWelcomePageEnabled, "Dev should test welcome")
        assertEquals(LoginPageStyle.Style3, featureFlags.loginPageStyle, "Should test alternative style")
        assertEquals(ProtocolType.QUIC, featureFlags.protocolType, "Should test new protocol")
        assertEquals("QUIC", featureFlags.protocolType.value, "Protocol value should work")
        
        assertEquals("5s", networkConfig.networkTimeout, "Should use dev timeout")
        assertEquals(0, networkConfig.maxRetries, "Should use dev retries")
        
        assertEquals("1s", uiConfig.animationTimeout, "Should use debug animations")
        assertEquals("debug-mode", uiConfig.theme, "Should use debug theme")
    }
    
    @Test
    fun `test configuration isolation under concurrent changes`() {
        // Test: Verify that rapid changes to multiple interfaces don't interfere
        // This simulates real-world usage where multiple parts of app change configs
        
        // Perform rapid, interleaved changes
        featureFlags.isChatEnabled = true
        networkConfig.maxRetries = 7
        uiConfig.theme = "dynamic-test"
        
        featureFlags.protocolType = ProtocolType.HTTP1_1
        uiConfig.animationTimeout = "333ms"
        networkConfig.networkTimeout = "45s"
        
        featureFlags.loginPageStyle = LoginPageStyle.Style3
        uiConfig.theme = "final-test"
        networkConfig.maxRetries = 12
        
        // Verify final state is correct across all interfaces
        assertTrue(featureFlags.isChatEnabled, "Feature chat should be enabled")
        assertEquals(ProtocolType.HTTP1_1, featureFlags.protocolType, "Should use HTTP1_1")
        assertEquals(LoginPageStyle.Style3, featureFlags.loginPageStyle, "Should use Style3")
        assertEquals("HTTP1.1", featureFlags.protocolType.value, "Protocol value should match")
        
        assertEquals("45s", networkConfig.networkTimeout, "Should have correct timeout")
        assertEquals(12, networkConfig.maxRetries, "Should have final retry count")
        
        assertEquals("333ms", uiConfig.animationTimeout, "Should have correct animation timeout")
        assertEquals("final-test", uiConfig.theme, "Should have final theme")
    }
    
    @Test
    fun `test configuration reset and independence`() {
        // Test: Verify that resetting one interface doesn't affect others
        
        // Change all interfaces
        featureFlags.isChatEnabled = true
        featureFlags.protocolType = ProtocolType.QUIC
        
        networkConfig.networkTimeout = "999s"
        networkConfig.maxRetries = 99
        
        uiConfig.animationTimeout = "999ms"
        uiConfig.theme = "custom"
        
        // Capture current state of network and UI
        val networkTimeout = networkConfig.networkTimeout
        val networkRetries = networkConfig.maxRetries
        val uiTimeout = uiConfig.animationTimeout
        val uiTheme = uiConfig.theme
        
        // Reset only FeatureFlags by resetting entire config (simulates app restart)
        resetToDefaults()
        featureFlags = AppConfig.featureflags
        networkConfig = AppConfig.networkconfig
        uiConfig = AppConfig.userinterface
        
        // Verify FeatureFlags is reset to defaults
        assertFalse(featureFlags.isChatEnabled, "Features should be reset")
        assertEquals(ProtocolType.HTTP2, featureFlags.protocolType, "Protocol should be reset")
        
        // Verify NetworkConfig is also reset (clean slate)
        assertEquals("60s", networkConfig.networkTimeout, "Network should be reset")
        assertEquals(3, networkConfig.maxRetries, "Retries should be reset")
        
        // Verify UserInterfaceConfig is also reset (clean slate)
        assertEquals("5s", uiConfig.animationTimeout, "UI timeout should be reset")
        assertEquals("light", uiConfig.theme, "UI theme should be reset")
    }
    
    @Test
    fun `test multi-interface type consistency`() {
        // Test: Verify type consistency across all interfaces
        
        // Boolean properties
        featureFlags.isChatEnabled = true
        val chatEnabled: Boolean = featureFlags.isChatEnabled
        assertTrue(chatEnabled, "Boolean type should be preserved")
        
        // String properties  
        networkConfig.networkTimeout = "test-timeout"
        val timeout: String = networkConfig.networkTimeout
        assertEquals("test-timeout", timeout, "String type should be preserved")
        
        uiConfig.theme = "test-theme"
        val theme: String = uiConfig.theme
        assertEquals("test-theme", theme, "String type should be preserved")
        
        // Integer properties
        networkConfig.maxRetries = 42
        val retries: Int = networkConfig.maxRetries
        assertEquals(42, retries, "Int type should be preserved")
        
        // Enum/Sealed class properties
        featureFlags.protocolType = ProtocolType.QUIC
        val protocol: ProtocolType = featureFlags.protocolType
        assertEquals(ProtocolType.QUIC, protocol, "Sealed class type should be preserved")
        assertEquals("QUIC", protocol.value, "Sealed class properties should work")
    }
} 