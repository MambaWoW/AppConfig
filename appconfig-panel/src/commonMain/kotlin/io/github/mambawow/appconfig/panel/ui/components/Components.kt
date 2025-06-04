package io.github.mambawow.appconfig.panel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mambawow.appconfig.panel.ui.icons.AppIcons.ChevronBackward
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.Divider
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.SystemBlue
import io.github.mambawow.appconfig.panel.ui.theme.AppTextStyles
import io.github.mambawow.appconfig.panel.ui.theme.LocalThemeType
import io.github.mambawow.appconfig.panel.ui.theme.ThemeType

/**
 * Common UI components for the configuration panel
 * 
 * This file contains reusable UI components that adapt their appearance
 * based on the current platform theme (Material vs Cupertino).
 * 
 * Components include:
 * - ActionButton: Primary action buttons with platform-specific styling
 * - NavigationButton: Back navigation with adaptive layout
 * - Dividers: Visual separators for content organization
 * 
 * @author Frank Shao
 * @created 2025/01/29
 */

/**
 * Platform-adaptive action button component
 * 
 * This button automatically adapts its appearance and interaction behavior
 * based on the current theme:
 * 
 * Material Design:
 * - Uses TextButton with Material ripple effects
 * - Standard Material button typography and spacing
 * - Material interaction states and animations
 * 
 * Cupertino (iOS):
 * - Custom implementation with iOS-style interactions
 * - Opacity-based press feedback (iOS standard)
 * - System blue color for action text
 * - No background or border (iOS button pattern)
 * 
 * @param actionText The text to display on the button
 * @param onClick Callback function when the button is pressed
 */
@Composable
fun ActionButton(
    actionText: String,
    onClick: () -> Unit
) {
    when (LocalThemeType.current) {
        ThemeType.Material -> {
            // Material Design implementation
            TextButton(onClick = onClick) {
                Text(
                    text = actionText,
                    style = AppTextStyles.BodySmall.copy(fontSize = 16.sp)
                )
            }
        }
        
        ThemeType.Cupertino -> {
            // iOS-style implementation with custom press feedback
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        // iOS-style opacity feedback on press
                        alpha = if (isPressed) 0.33f else 1f
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null, // No ripple for iOS style
                        onClick = onClick
                    )
                    .padding(end = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionText,
                    style = AppTextStyles.BodyNormal.copy(
                        fontSize = 16.sp, 
                        color = SystemBlue
                    )
                )
            }
        }
    }
}

/**
 * Platform-adaptive navigation button component
 * 
 * This component provides back navigation functionality with
 * platform-specific visual design and interaction patterns:
 * 
 * Material Design:
 * - IconButton with back arrow icon
 * - Material ripple interaction feedback
 * - Standard Material icon sizing and spacing
 * 
 * Cupertino (iOS):
 * - Custom layout with chevron and "Back" text
 * - iOS-style opacity feedback on press
 * - System blue color scheme
 * - Text + icon combination (iOS navigation pattern)
 * 
 * @param onClick Callback function when the navigation button is pressed
 */
@Composable
fun NavigationButton(
    onClick: () -> Unit
) {
    when (LocalThemeType.current) {
        ThemeType.Material -> {
            // Material Design implementation
            IconButton(onClick = onClick) {
                Icon(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(16.dp),
                    imageVector = ChevronBackward,
                    contentDescription = "Navigate back"
                )
            }
        }
        
        ThemeType.Cupertino -> {
            // iOS-style implementation with text + icon
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        // iOS-style opacity feedback on press
                        alpha = if (isPressed) 0.33f else 1f
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null, // No ripple for iOS style
                        onClick = onClick
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 4.dp)
                        .size(15.dp),
                    imageVector = ChevronBackward,
                    contentDescription = "Navigate back",
                    tint = SystemBlue
                )

                Text(
                    text = "Back",
                    style = AppTextStyles.BodyNormal.copy(
                        fontSize = 17.sp, 
                        color = SystemBlue
                    )
                )
            }
        }
    }
}

/**
 * Horizontal divider component for content separation
 * 
 * Creates a thin horizontal line used to visually separate
 * content sections, list items, or groups of related elements.
 * 
 * @param modifier Modifier for customizing the divider's layout
 * @param color Color of the divider line (defaults to theme divider color)
 */
@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = Divider
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(color = color)
    )
}

/**
 * Vertical divider component for content separation
 * 
 * Creates a thin vertical line used to visually separate
 * content sections in horizontal layouts or side-by-side elements.
 * 
 * @param modifier Modifier for customizing the divider's layout
 * @param color Color of the divider line (defaults to theme divider color)
 */
@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = Divider
) {
    Spacer(
        modifier = modifier
            .fillMaxHeight()
            .width(0.5.dp)
            .background(color = color)
    )
}