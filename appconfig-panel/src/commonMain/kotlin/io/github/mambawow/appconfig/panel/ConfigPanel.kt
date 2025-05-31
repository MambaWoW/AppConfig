package io.github.mambawow.appconfig.panel

import io.github.mambawow.appconfig.panel.ui.theme.ThemeType

/**
 * Main entry point for the configuration panel (dialog-based version)
 * For navigation-based version, use ConfigPanelWithNavigation instead
 */

expect fun determinePlatformTheme(): ThemeType
