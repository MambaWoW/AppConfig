package io.github.mambawow.appconfig.panel.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import io.github.mambawow.appconfig.panel.ui.theme.AppTextStyles
import io.github.mambawow.appconfig.panel.ui.theme.LocalThemeType
import io.github.mambawow.appconfig.panel.ui.theme.ThemeType

/**
 * Adaptive scaffold that changes based on platform theme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val theme = LocalThemeType.current
    if (theme == ThemeType.Material) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions
        )
    } else {
        CenterAlignedTopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions
        )
    }
}