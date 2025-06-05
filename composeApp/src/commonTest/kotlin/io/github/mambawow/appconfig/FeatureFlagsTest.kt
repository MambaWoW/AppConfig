package io.github.mambawow.appconfig

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * Comprehensive test suite for FeatureFlags configuration interface
 */
class FeatureFlagsTest : ConfigTestBase() {
    
    private lateinit var featureFlags: FeatureFlags
    
    @BeforeTest
    fun setup() {
        // Note: baseSetup() from ConfigTestBase is automatically called first
        // Access the generated implementation through AppConfig extension
        featureFlags = AppConfig.featureflags
    }
    
    // ========================================
    // Boolean Property Tests: isChatEnabled
    // ========================================
    
    @Test
    fun `test isChatEnabled default value`() {
        // Test: Default value should be false as specified in annotation
        // This test is safe because each test gets a clean configuration
        assertDefaultValue(
            expected = false,
            actual = featureFlags.isChatEnabled,
            propertyName = "isChatEnabled"
        )
    }
    
    @Test
    fun `test isChatEnabled value assignment and retrieval`() {
        // Test: Should be able to set and get true value
        assertValueAssignmentAndRetrieval(
            setValue = { featureFlags.isChatEnabled = true },
            getValue = { featureFlags.isChatEnabled },
            expectedValue = true,
            propertyName = "isChatEnabled"
        )
        
        // Test: Should be able to set and get false value
        assertValueAssignmentAndRetrieval(
            setValue = { featureFlags.isChatEnabled = false },
            getValue = { featureFlags.isChatEnabled },
            expectedValue = false,
            propertyName = "isChatEnabled"
        )
    }
    
    // ========================================
    // Boolean Property Tests: isWelcomePageEnabled
    // ========================================
    
    @Test
    fun `test isWelcomePageEnabled default value`() {
        // Test: Default value should be true as specified in annotation
        assertDefaultValue(
            expected = true,
            actual = featureFlags.isWelcomePageEnabled,
            propertyName = "isWelcomePageEnabled"
        )
    }
    
    @Test
    fun `test isWelcomePageEnabled value assignment and retrieval`() {
        // Test: Should be able to toggle welcome page setting
        assertValueAssignmentAndRetrieval(
            setValue = { featureFlags.isWelcomePageEnabled = false },
            getValue = { featureFlags.isWelcomePageEnabled },
            expectedValue = false,
            propertyName = "isWelcomePageEnabled"
        )
        
        assertValueAssignmentAndRetrieval(
            setValue = { featureFlags.isWelcomePageEnabled = true },
            getValue = { featureFlags.isWelcomePageEnabled },
            expectedValue = true,
            propertyName = "isWelcomePageEnabled"
        )
    }
    
    // ========================================
    // Option Property Tests: loginPageStyle
    // ========================================
    
    @Test
    fun `test loginPageStyle default value`() {
        // Test: Default should be Style1 as marked with isDefault = true
        assertDefaultValue(
            expected = LoginPageStyle.Style1,
            actual = featureFlags.loginPageStyle,
            propertyName = "loginPageStyle"
        )
    }
    
    @Test
    fun `test loginPageStyle option selection`() {
        // Test each option selection independently
        assertOptionSelection(
            setOption = { featureFlags.loginPageStyle = it },
            getOption = { featureFlags.loginPageStyle },
            testOption = LoginPageStyle.Style2,
            propertyName = "loginPageStyle"
        )
        
        assertOptionSelection(
            setOption = { featureFlags.loginPageStyle = it },
            getOption = { featureFlags.loginPageStyle },
            testOption = LoginPageStyle.Style3,
            propertyName = "loginPageStyle"
        )
        
        assertOptionSelection(
            setOption = { featureFlags.loginPageStyle = it },
            getOption = { featureFlags.loginPageStyle },
            testOption = LoginPageStyle.Style1,
            propertyName = "loginPageStyle"
        )
    }
    
    @Test
    fun `test loginPageStyle all options availability`() {
        // Test: Verify all LoginPageStyle options are accessible
        val allStyles = listOf(
            LoginPageStyle.Style1,
            LoginPageStyle.Style2,
            LoginPageStyle.Style3
        )
        
        allStyles.forEach { style ->
            featureFlags.loginPageStyle = style
            assertEquals(
                style,
                featureFlags.loginPageStyle,
                "Failed to select login page style: $style"
            )
        }
    }
    
    // ========================================
    // Option Property Tests: protocolType
    // ========================================
    
    @Test
    fun `test protocolType default value`() {
        // Test: Default should be HTTP2 as marked with isDefault = true
        assertDefaultValue(
            expected = ProtocolType.HTTP2,
            actual = featureFlags.protocolType,
            propertyName = "protocolType"
        )
    }
    
    @Test
    fun `test protocolType option selection`() {
        // Test each protocol option independently
        assertOptionSelection(
            setOption = { featureFlags.protocolType = it },
            getOption = { featureFlags.protocolType },
            testOption = ProtocolType.HTTP1_1,
            propertyName = "protocolType"
        )
        
        assertOptionSelection(
            setOption = { featureFlags.protocolType = it },
            getOption = { featureFlags.protocolType },
            testOption = ProtocolType.QUIC,
            propertyName = "protocolType"
        )
        
        assertOptionSelection(
            setOption = { featureFlags.protocolType = it },
            getOption = { featureFlags.protocolType },
            testOption = ProtocolType.HTTP2,
            propertyName = "protocolType"
        )
    }
    
    @Test
    fun `test protocolType value property consistency`() {
        // Test: Verify that ProtocolType values have consistent string representations
        featureFlags.protocolType = ProtocolType.HTTP1_1
        assertEquals("HTTP1.1", featureFlags.protocolType.value, "HTTP1_1 value property mismatch")
        
        featureFlags.protocolType = ProtocolType.HTTP2
        assertEquals("HTTP2", featureFlags.protocolType.value, "HTTP2 value property mismatch")
        
        featureFlags.protocolType = ProtocolType.QUIC
        assertEquals("QUIC", featureFlags.protocolType.value, "QUIC value property mismatch")
    }
    
    @Test
    fun `test protocolType all options availability`() {
        // Test: Verify all ProtocolType options are accessible
        val allProtocols = listOf(
            ProtocolType.HTTP1_1,
            ProtocolType.HTTP2,
            ProtocolType.QUIC
        )
        
        allProtocols.forEach { protocol ->
            featureFlags.protocolType = protocol
            assertEquals(
                protocol,
                featureFlags.protocolType,
                "Failed to select protocol type: $protocol"
            )
            
            // Also verify the value property
            assertNotNull(
                featureFlags.protocolType.value,
                "Protocol value should not be null for: $protocol"
            )
        }
    }
    
    // ========================================
    // Integration Tests: Multiple Properties
    // ========================================
    
    @Test
    fun `test multiple property interactions within single test`() {
        // Test: Complex scenario with multiple property changes in one isolated test
        featureFlags.isChatEnabled = true
        featureFlags.isWelcomePageEnabled = false
        featureFlags.loginPageStyle = LoginPageStyle.Style3
        featureFlags.protocolType = ProtocolType.QUIC
        
        // Verify all values are preserved correctly
        assertTrue(featureFlags.isChatEnabled, "Chat should be enabled")
        assertFalse(featureFlags.isWelcomePageEnabled, "Welcome page should be disabled")
        assertEquals(LoginPageStyle.Style3, featureFlags.loginPageStyle, "Should be Style3")
        assertEquals(ProtocolType.QUIC, featureFlags.protocolType, "Should be QUIC")
        assertEquals("QUIC", featureFlags.protocolType.value, "QUIC value should be consistent")
    }
    
    @Test
    fun `test property independence within FeatureFlags`() {
        // Test: Changes to one property should not affect others within the same interface
        // Start with defaults and capture initial state
        val originalChat = featureFlags.isChatEnabled
        val originalWelcome = featureFlags.isWelcomePageEnabled
        val originalStyle = featureFlags.loginPageStyle
        val originalProtocol = featureFlags.protocolType
        
        // Change chat enabled, verify others unchanged
        featureFlags.isChatEnabled = !originalChat
        assertEquals(originalWelcome, featureFlags.isWelcomePageEnabled, "Welcome page should not change")
        assertEquals(originalStyle, featureFlags.loginPageStyle, "Login style should not change")
        assertEquals(originalProtocol, featureFlags.protocolType, "Protocol should not change")
        
        // Reset and test another property
        resetToDefaults()
        featureFlags = AppConfig.featureflags
        
        // Change login style, verify others remain at defaults
        featureFlags.loginPageStyle = LoginPageStyle.Style2
        assertEquals(false, featureFlags.isChatEnabled, "Chat should remain at default")
        assertEquals(true, featureFlags.isWelcomePageEnabled, "Welcome should remain at default")
        assertEquals(ProtocolType.HTTP2, featureFlags.protocolType, "Protocol should remain at default")
    }
    
    @Test
    fun `test feature flag realistic scenarios`() {
        // Test: Realistic feature flag usage scenarios in isolation
        
        // Scenario 1: Enable all features for power users
        featureFlags.isChatEnabled = true
        featureFlags.isWelcomePageEnabled = true
        featureFlags.loginPageStyle = LoginPageStyle.Style1
        featureFlags.protocolType = ProtocolType.HTTP2
        
        assertTrue(featureFlags.isChatEnabled, "Power user should have chat enabled")
        assertTrue(featureFlags.isWelcomePageEnabled, "Power user should see welcome page")
        assertEquals(LoginPageStyle.Style1, featureFlags.loginPageStyle, "Should use standard login style")
        assertEquals(ProtocolType.HTTP2, featureFlags.protocolType, "Should use modern protocol")
        
        // Reset and test another scenario in the same test
        resetToDefaults()
        featureFlags = AppConfig.featureflags
        
        // Scenario 2: Minimal configuration for embedded systems
        featureFlags.isChatEnabled = false
        featureFlags.isWelcomePageEnabled = false
        featureFlags.loginPageStyle = LoginPageStyle.Style3
        featureFlags.protocolType = ProtocolType.HTTP1_1
        
        assertFalse(featureFlags.isChatEnabled, "Embedded system should not have chat")
        assertFalse(featureFlags.isWelcomePageEnabled, "Embedded system should skip welcome")
        assertEquals(LoginPageStyle.Style3, featureFlags.loginPageStyle, "Should use minimal login style")
        assertEquals(ProtocolType.HTTP1_1, featureFlags.protocolType, "Should use basic protocol")
    }
    
    @Test
    fun `test option enum consistency and object identity`() {
        // Test: Verify that sealed class options maintain consistent behavior
        
        // Test LoginPageStyle sealed interface
        val loginStyles = listOf(LoginPageStyle.Style1, LoginPageStyle.Style2, LoginPageStyle.Style3)
        loginStyles.forEach { style ->
            featureFlags.loginPageStyle = style
            assertSame(style, featureFlags.loginPageStyle, "Login style object identity should be preserved")
        }
        
        // Test ProtocolType sealed class with value property
        val protocols = listOf(ProtocolType.HTTP1_1, ProtocolType.HTTP2, ProtocolType.QUIC)
        protocols.forEach { protocol ->
            featureFlags.protocolType = protocol
            assertSame(protocol, featureFlags.protocolType, "Protocol object identity should be preserved")
            
            // Verify value property is accessible and non-empty
            val value = featureFlags.protocolType.value
            assertNotNull(value, "Protocol value should not be null")
            assertTrue(value.isNotEmpty(), "Protocol value should not be empty")
        }
    }
    
    @Test
    fun `test boolean properties independence`() {
        // Test: Boolean properties should not affect each other
        
        // Test that changing isChatEnabled doesn't affect isWelcomePageEnabled
        val originalWelcome = featureFlags.isWelcomePageEnabled
        featureFlags.isChatEnabled = true
        assertEquals(originalWelcome, featureFlags.isWelcomePageEnabled, 
            "Welcome page setting should not change when chat is enabled")
        
        // Test that changing isWelcomePageEnabled doesn't affect isChatEnabled
        val currentChat = featureFlags.isChatEnabled
        featureFlags.isWelcomePageEnabled = false
        assertEquals(currentChat, featureFlags.isChatEnabled,
            "Chat setting should not change when welcome page is disabled")
    }
} 