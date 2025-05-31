package io.github.mambawow.appconfig.panel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mambawow.appconfig.panel.ui.theme.ThemeType
import io.github.mambawow.appconfig.panel.ui.theme.LocalThemeType
import io.github.mambawow.appconfig.panel.ui.theme.AppTextStyles
import io.github.mambawow.appconfig.panel.ui.theme.AppColors
import io.github.mambawow.appconfig.panel.ui.icons.AppIcons
import io.github.mambawow.appconfig.panel.ui.icons.AppIcons.ChevronBackward
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.SystemBlue
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.SystemGray
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.SystemGreen

/**
 * Common header component for configuration items
 * Following Single Responsibility Principle
 */
@Composable
fun ConfigItemHeader(
    title: String,
    description: String = ""
) {
    Column(
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = AppTextStyles.BodyBold.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal)
        )
        if (description.isNotBlank()) {
            Text(
                modifier = Modifier.padding(top = 3.dp),
                text = description,
                style = AppTextStyles.BodySmall.copy(
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            )
        }
    }
}

/**
 * Switch component for boolean configuration items
 */
@Composable
fun ConfigSwitchItem(
    title: String,
    description: String = "",
    currentValue: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier.height(48.dp)
) {
    Row(
        modifier = modifier.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            ConfigItemHeader(title, description)
        }
        
        Box(modifier = Modifier.width(60.dp).height(20.dp)) {
            val theme = LocalThemeType.current
            when (theme) {
                ThemeType.Material -> {
                    Switch(
                        modifier = Modifier.scale(0.75f),
                        checked = currentValue,
                        onCheckedChange = onValueChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SystemBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray.copy(alpha = .33f),
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
                ThemeType.Cupertino -> {
                    Switch(
                        modifier = Modifier.scale(0.7f),
                        checked = currentValue,
                        onCheckedChange = onValueChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SystemGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = SystemGray.copy(alpha = .33f),
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

/**
 * Input component for text/numeric configuration items
 * Uses navigation to display full-screen input page
 */
@Composable
fun ConfigInputItem(
    title: String,
    description: String = "",
    currentValue: String,
    isNumeric: Boolean = false,
    onNavigateToInput: (title: String, currentValue: String, isNumeric: Boolean) -> Unit,
    modifier: Modifier = Modifier.height(48.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = { onNavigateToInput(title, currentValue, isNumeric) }
        ).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConfigItemHeader(title, description)
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            modifier = Modifier.weight(1f).padding(end = 6.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            text = currentValue,
            style = AppTextStyles.BodyNormal
        )

        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = AppIcons.ChevronForward,
            contentDescription = "Forward"
        )

    }
}

/**
 * Option selection component for enum/choice configuration items
 * Uses navigation to display full-screen option selection page
 */
@Composable
fun ConfigOptionItem(
    title: String,
    description: String = "",
    currentOptionId: Int,
    currentOptionDescription: String = "",
    onNavigateToOption: (title: String, currentOptionId: Int) -> Unit,
    modifier: Modifier = Modifier.height(48.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = { onNavigateToOption(title, currentOptionId) }
        ).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConfigItemHeader(title, description)
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            modifier = Modifier.weight(1f).padding(end = 6.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            text = if (currentOptionDescription.isNotEmpty()) {
                "$currentOptionId ($currentOptionDescription)"
            } else {
                "$currentOptionId"
            },
            style = AppTextStyles.BodyNormal
        )

        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = AppIcons.ChevronForward,
            contentDescription = "Forward"
        )
    }
}

/**
 * Data class for configuration options
 * Enhanced with better type safety
 */
data class ConfigOption<T>(
    val id: Int,
    val description: String,
    val value: T
) {
    companion object {
        /**
         * Create a list of ConfigOptions from OptionItemDescriptors
         * This provides better type safety when converting from the domain model
         */
        fun <T> fromOptionItemDescriptors(
            descriptors: List<io.github.mambawow.appconfig.OptionItemDescriptor<T>>
        ): List<ConfigOption<T>> {
            return descriptors.map { descriptor ->
                ConfigOption(
                    id = descriptor.optionId,
                    description = descriptor.description,
                    value = descriptor.option
                )
            }
        }
    }
} 