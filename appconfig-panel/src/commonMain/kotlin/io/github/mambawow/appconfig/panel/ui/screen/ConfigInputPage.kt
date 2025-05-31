package io.github.mambawow.appconfig.panel.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.panel.ui.components.AppScaffold
import io.github.mambawow.appconfig.panel.ui.theme.AppColors
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.SystemBlue
import io.github.mambawow.appconfig.panel.ui.theme.LocalThemeType
import io.github.mambawow.appconfig.panel.ui.theme.ThemeType

/**
 * Configuration input page for text/numeric values
 * Replaces the previous dialog implementation with full-screen navigation
 */
@Composable
fun ConfigInputPage(
    title: String,
    initialValue: String,
    isNumeric: Boolean = false,
    dataType: DataType,
    onConfirm: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(initialValue)) }

    val theme = LocalThemeType.current
    AppScaffold(
        title = title,
        onNavigationClick = onNavigateBack,
        onActionClick = { 
            onConfirm(textFieldValue.text)
            onNavigateBack()
        },
        actionText = "Save"
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        color = if (theme == ThemeType.Material) {
                            Color(0xFFF2F2F7)
                        } else {
                            Color(0xFFE8E8ED)
                        },
                        shape = RoundedCornerShape(3.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    maxLines = 6,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    ),
                    keyboardOptions = if (isNumeric) {
                        KeyboardOptions(keyboardType = KeyboardType.Number)
                    } else {
                        KeyboardOptions.Default
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = if (LocalThemeType.current == ThemeType.Material) {
                            Color(0xFF1C2526)
                        } else {
                            SystemBlue
                        }
                    ),
                    placeholder = {
                        Text(
                            text = "Enter ${if (isNumeric) "numeric" else "text"} value...",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        )
                    }
                )
            }
            
            // Type information text
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Expected type: ${getDataTypeDisplayName(dataType)}",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (theme == ThemeType.Material) {
                        Color(0xFF6C7B7F)
                    } else {
                        Color(0xFF8E8E93)
                    },
                    fontWeight = FontWeight.W400
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Convert DataType to user-friendly display name
 */
private fun getDataTypeDisplayName(dataType: DataType): String {
    return when (dataType) {
        DataType.BOOLEAN -> "Boolean (true/false)"
        DataType.STRING -> "Text"
        DataType.INT -> "Integer number"
        DataType.LONG -> "Long number"
        DataType.FLOAT -> "Decimal number (Float)"
        DataType.DOUBLE -> "Decimal number (Double)"
        else -> dataType.name.lowercase().replaceFirstChar { it.uppercase() }
    }
} 