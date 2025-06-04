package io.github.mambawow.appconfig.panel.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import io.github.mambawow.appconfig.panel.ui.theme.AppTextStyles
import io.github.mambawow.appconfig.panel.ui.theme.LocalThemeType
import io.github.mambawow.appconfig.panel.ui.theme.ThemeType

/**
 * Adaptive scaffold component that provides platform-specific layouts
 * 
 * This scaffold automatically adapts its appearance based on the current theme type:
 * - Material Design: Uses standard TopAppBar with left-aligned title
 * - Cupertino (iOS): Uses CenterAlignedTopAppBar with centered title
 * 
 * Features:
 * - Platform-adaptive navigation patterns
 * - Consistent spacing and layout structure
 * - Flexible action button configuration
 * - Responsive design for different screen sizes
 * 
 * @param title The title to display in the app bar
 * @param onNavigationClick Callback for navigation button press (e.g., back button)
 * @param onActionClick Callback for primary action button press (e.g., reset button)
 * @param actionText Text to display on the action button
 * @param content The main content of the screen wrapped in the scaffold
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    actionText: String = "Reset",
    content: @Composable (PaddingValues) -> Unit
) {
    MaterialTheme {
        Scaffold(
            topBar = {
                AdaptiveTopBar(
                    title = {
                        Text(
                            text = title,
                            style = AppTextStyles.Headline.copy(fontSize = 18.sp)
                        )
                    },
                    navigationIcon = {
                        NavigationButton(
                            onClick = onNavigationClick
                        )
                    },
                    actions = {
                        ActionButton(
                            actionText = actionText,
                            onClick = onActionClick
                        )
                    }
                )
            },
            content = content
        )
    }
}

/**
 * Platform-adaptive top app bar component
 * 
 * This component automatically switches between Material and Cupertino designs
 * based on the current theme context. The visual differences include:
 * 
 * Material Design:
 * - Left-aligned title
 * - Standard Material elevation and styling
 * - Material iconography and interactions
 * 
 * Cupertino (iOS):
 * - Center-aligned title
 * - iOS-style navigation patterns
 * - System colors and typography
 * 
 * @param title Composable content for the app bar title
 * @param navigationIcon Composable content for the navigation button (typically back button)
 * @param actions Composable content for action buttons in the app bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val theme = LocalThemeType.current
    
    when (theme) {
        ThemeType.Material -> {
            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions
            )
        }
        ThemeType.Cupertino -> {
            CenterAlignedTopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions
            )
        }
    }
}