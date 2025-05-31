package io.github.mambawow.appconfig.panel.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mambawow.appconfig.panel.ui.components.AppScaffold
import io.github.mambawow.appconfig.panel.ui.components.ConfigOption
import io.github.mambawow.appconfig.panel.ui.icons.AppIcons
import io.github.mambawow.appconfig.panel.ui.theme.AppColors
import io.github.mambawow.appconfig.panel.ui.theme.AppTextStyles
import io.github.mambawow.appconfig.panel.ui.theme.LocalThemeType
import io.github.mambawow.appconfig.panel.ui.theme.ThemeType

/**
 * Configuration option selection page
 * Replaces the previous dialog implementation with full-screen navigation
 */
@Composable
fun <T> ConfigOptionPage(
    title: String,
    options: List<ConfigOption<T>>,
    currentOptionId: Int,
    onOptionSelected: (ConfigOption<T>) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedOptionId by remember { mutableStateOf(currentOptionId) }
    val selectedOption = options.find { it.id == selectedOptionId }

    AppScaffold(
        title = title,
        onNavigationClick = onNavigateBack,
        onActionClick = {
            selectedOption?.let(onOptionSelected)
            onNavigateBack()
        },
        actionText = "Save"
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                ConfigOptionCard(
                    option = option,
                    isSelected = selectedOptionId == option.id,
                    onSelect = { selectedOptionId = option.id }
                )
            }
        }
    }
}

@Composable
private fun <T> ConfigOptionCard(
    option: ConfigOption<T>,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ID: ${option.id}",
                    style = AppTextStyles.BodySmall.copy(
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = option.description,
                    style = AppTextStyles.BodyNormal.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Box(
                modifier = Modifier.size(30.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    AppIcons.RadioButtonSelected(
                        modifier = Modifier.size(25.dp),
                        color = if (LocalThemeType.current == ThemeType.Cupertino)
                            AppColors.SystemGreen else
                            AppColors.SystemBlue
                    )
                } else {
                    AppIcons.RadioButtonUnselected(modifier = Modifier.size(19.dp))
                }
            }
        }
    }
} 