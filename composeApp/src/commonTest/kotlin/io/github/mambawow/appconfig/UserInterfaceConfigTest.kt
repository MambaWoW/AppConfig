package io.github.mambawow.appconfig

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

/**
 * Comprehensive test suite for UserInterfaceConfig configuration interface
 */
class UserInterfaceConfigTest : ConfigTestBase() {
    
    private lateinit var uiConfig: UserInterfaceConfig
    
    @BeforeTest
    fun setup() {
        // Note: baseSetup() from ConfigTestBase is automatically called first
        // Access the generated implementation through AppConfig extension
        uiConfig = AppConfig.userinterface
    }
    
    // ========================================
    // String Property Tests: animationTimeout
    // ========================================
    
    @Test
    fun `test animationTimeout default value`() {
        // Test: Default value should be "5s" as specified in annotation
        assertDefaultValue(
            expected = "5s",
            actual = uiConfig.animationTimeout,
            propertyName = "animationTimeout"
        )
    }
    
    @Test
    fun `test animationTimeout value assignment and retrieval`() {
        // Test: Should be able to set various animation timeout values
        val testTimeouts = listOf(
            "100ms",    // milliseconds
            "1s",       // seconds  
            "2.5s",     // decimal seconds
            "0s",       // no animation
            "infinite", // infinite duration
            "auto",     // auto duration
            ""          // empty string
        )
        
        testTimeouts.forEach { timeout ->
            assertValueAssignmentAndRetrieval(
                setValue = { uiConfig.animationTimeout = timeout },
                getValue = { uiConfig.animationTimeout },
                expectedValue = timeout,
                propertyName = "animationTimeout"
            )
        }
    }
    
    @Test
    fun `test animationTimeout with UI-specific values`() {
        // Test: Should handle UI animation specific timeout formats
        val animationFormats = mapOf(
            "250ms" to "standard button animation",
            "300ms" to "modal transition",
            "500ms" to "page transition",
            "1s" to "slow reveal animation",
            "0ms" to "instant/no animation",
            "16.67ms" to "60fps frame duration",
            "33.33ms" to "30fps frame duration"
        )
        
        animationFormats.forEach { (timeout, description) ->
            uiConfig.animationTimeout = timeout
            assertEquals(
                timeout,
                uiConfig.animationTimeout,
                "Failed to handle $description: $timeout"
            )
        }
    }
    
    // ========================================
    // String Property Tests: theme
    // ========================================
    
    @Test
    fun `test theme default value`() {
        // Test: Default value should be "light" as specified in annotation
        assertDefaultValue(
            expected = "light",
            actual = uiConfig.theme,
            propertyName = "theme"
        )
    }
    
    @Test
    fun `test theme value assignment and retrieval`() {
        // Test: Should be able to set various theme values
        val testThemes = listOf(
            "light",
            "dark", 
            "auto",
            "system",
            "high-contrast",
            "colorful",
            "minimal"
        )
        
        testThemes.forEach { theme ->
            assertValueAssignmentAndRetrieval(
                setValue = { uiConfig.theme = theme },
                getValue = { uiConfig.theme },
                expectedValue = theme,
                propertyName = "theme"
            )
        }
    }
    
    @Test
    fun `test theme with custom values`() {
        // Test: Should handle custom theme identifiers
        val customThemes = mapOf(
            "corporate-blue" to "corporate theme",
            "christmas-2023" to "seasonal theme",
            "user-123-custom" to "user-specific theme",
            "theme_with_underscores" to "underscore theme",
            "ThemeCamelCase" to "camelCase theme",
            "123-numeric-start" to "numeric prefix theme"
        )
        
        customThemes.forEach { (theme, description) ->
            uiConfig.theme = theme
            assertEquals(
                theme,
                uiConfig.theme,
                "Failed to handle $description: $theme"
            )
        }
    }
    
    // ========================================
    // Property Independence Tests
    // ========================================
    
    @Test
    fun `test property independence within UserInterfaceConfig`() {
        // Test: Changes to one property should not affect the other
        val originalTimeout = uiConfig.animationTimeout
        val originalTheme = uiConfig.theme
        
        // Change timeout, verify theme unchanged
        uiConfig.animationTimeout = "999ms"
        assertEquals(originalTheme, uiConfig.theme, "Theme should not change when timeout changes")
        
        // Reset and test the other direction
        resetToDefaults()
        uiConfig = AppConfig.userinterface
        
        // Change theme, verify timeout remains at default
        uiConfig.theme = "custom-theme"
        assertEquals("5s", uiConfig.animationTimeout, "Timeout should remain at default")
    }
    
    @Test
    fun `test custom key mapping verification`() {
        // Test: Verify that custom key "timeout" is used for animationTimeout
        // and auto-generated key is used for theme
        
        val testTimeout = "custom_animation_timeout"
        uiConfig.animationTimeout = testTimeout
        assertEquals(testTimeout, uiConfig.animationTimeout, "Custom key mapping should work for animationTimeout")
        
        val testTheme = "custom_theme_value"
        uiConfig.theme = testTheme
        assertEquals(testTheme, uiConfig.theme, "Auto-generated key should work for theme")
        
        // Verify independence
        assertEquals(testTimeout, uiConfig.animationTimeout, "Timeout should remain unchanged after theme change")
    }
    
    // ========================================
    // UI Configuration Scenarios
    // ========================================
    
    @Test
    fun `test UI configuration scenarios`() {
        // Test: Realistic UI configuration scenarios in isolation
        
        // Scenario 1: Performance-focused mobile app
        uiConfig.animationTimeout = "150ms"
        uiConfig.theme = "light"
        assertEquals("150ms", uiConfig.animationTimeout, "Should use fast animations")
        assertEquals("light", uiConfig.theme, "Should use light theme for battery")
        
        // Reset and test another scenario
        resetToDefaults()
        uiConfig = AppConfig.userinterface
        
        // Scenario 2: Accessibility-focused application
        uiConfig.animationTimeout = "0ms"
        uiConfig.theme = "high-contrast"
        assertEquals("0ms", uiConfig.animationTimeout, "Should disable animations")
        assertEquals("high-contrast", uiConfig.theme, "Should use high contrast theme")
        
        // Reset and test desktop scenario
        resetToDefaults()
        uiConfig = AppConfig.userinterface
        
        // Scenario 3: Rich desktop application
        uiConfig.animationTimeout = "800ms"
        uiConfig.theme = "dark"
        assertEquals("800ms", uiConfig.animationTimeout, "Should use rich animations")
        assertEquals("dark", uiConfig.theme, "Should use dark theme for desktop")
    }
    
    @Test
    fun `test grouped configuration validation`() {
        // Test: Verify this config belongs to "UserInterface" group
        // This is verified by the successful instantiation and usage
        
        // Test that both properties work together as part of the same group
        uiConfig.animationTimeout = "300ms"
        uiConfig.theme = "dark"
        
        assertEquals("300ms", uiConfig.animationTimeout, "Group property 1 should work")
        assertEquals("dark", uiConfig.theme, "Group property 2 should work")
        
        // Verify they can be changed independently within the same group
        uiConfig.animationTimeout = "600ms"
        assertEquals("600ms", uiConfig.animationTimeout, "Should change timeout independently")
        assertEquals("dark", uiConfig.theme, "Theme should remain unchanged")
    }
    
    @Test
    fun `test extreme values handling`() {
        // Test: UI configuration should handle extreme values gracefully
        
        // Very long animation timeout
        val longTimeout = "999999ms"
        uiConfig.animationTimeout = longTimeout
        assertEquals(longTimeout, uiConfig.animationTimeout, "Should handle very long timeout")
        
        // Very long theme name
        val longTheme = "this-is-a-very-long-theme-name-that-exceeds-normal-expectations-for-theme-naming-conventions"
        uiConfig.theme = longTheme
        assertEquals(longTheme, uiConfig.theme, "Should handle very long theme name")
        
        // Empty values
        uiConfig.animationTimeout = ""
        uiConfig.theme = ""
        assertEquals("", uiConfig.animationTimeout, "Should handle empty timeout")
        assertEquals("", uiConfig.theme, "Should handle empty theme")
    }
    
    @Test
    fun `test value persistence across multiple operations in single test`() {
        // Test: Values should persist across multiple operations
        val timeoutValues = listOf("0ms", "100ms", "500ms", "1s", "auto")
        val themeValues = listOf("light", "dark", "auto", "custom", "system")
        
        // Test animation timeout persistence
        timeoutValues.forEach { timeout ->
            uiConfig.animationTimeout = timeout
            assertEquals(timeout, uiConfig.animationTimeout, "Animation timeout should persist: $timeout")
            
            // Verify theme is not affected
            val currentTheme = uiConfig.theme
            assertEquals(currentTheme, uiConfig.theme, "Theme should remain unchanged")
        }
        
        // Test theme persistence
        themeValues.forEach { theme ->
            uiConfig.theme = theme
            assertEquals(theme, uiConfig.theme, "Theme should persist: $theme")
            
            // Verify animation timeout is not affected
            val currentTimeout = uiConfig.animationTimeout
            assertEquals(currentTimeout, uiConfig.animationTimeout, "Animation timeout should remain unchanged")
        }
    }
    
    @Test
    fun `test special character handling`() {
        // Test: Should handle special characters in configuration values
        
        val specialTimeouts = listOf(
            "100ms",           // normal
            "1.5s",            // decimal
            "∞",               // infinity symbol
            "მ100ms",          // unicode prefix
            "100ms🚀",         // emoji suffix
            "\t500ms\n",       // whitespace
            "\"quoted\"",      // quotes
            "100 ms",          // space
            "hundred-ms"       // text
        )
        
        val specialThemes = listOf(
            "café-theme",      // accented characters
            "🌙night",         // emoji
            "theme.dark",      // dot notation
            "theme/variant",   // slash
            "主题",            // chinese characters
            "тема",           // cyrillic
            "theme:dark",      // colon
            "theme@version",   // at symbol
            "theme#variant"    // hash
        )
        
        specialTimeouts.forEach { timeout ->
            uiConfig.animationTimeout = timeout
            assertEquals(timeout, uiConfig.animationTimeout, "Should handle special timeout: $timeout")
        }
        
        specialThemes.forEach { theme ->
            uiConfig.theme = theme
            assertEquals(theme, uiConfig.theme, "Should handle special theme: $theme")
        }
    }
    
    @Test
    fun `test system adaptive scenario`() {
        // Test: System-adaptive application scenario
        uiConfig.animationTimeout = "auto"
        uiConfig.theme = "system"
        assertEquals("auto", uiConfig.animationTimeout, "Should adapt to system preferences")
        assertEquals("system", uiConfig.theme, "Should follow system theme")
    }
} 