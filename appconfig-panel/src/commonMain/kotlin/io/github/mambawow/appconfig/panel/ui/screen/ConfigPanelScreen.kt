package io.github.mambawow.appconfig.panel.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mambawow.appconfig.ConfigItemDescriptor
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.panel.ui.components.AppScaffold
import io.github.mambawow.appconfig.panel.ui.screen.adapters.ConfigItemAdapter
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.SystemBlue
import io.github.mambawow.appconfig.panel.ui.theme.LocalThemeType
import io.github.mambawow.appconfig.panel.ui.theme.ThemeType
import io.github.mambawow.appconfig.panel.viewmodel.ConfigPanelViewModel
import io.github.mambawow.appconfig.panel.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Main configuration panel screen with navigation support
 * Handles UI orchestration and navigation for configuration editing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigPanelScreen(
    configItems: List<ConfigItemDescriptor<*>>,
    onNavigateToInput: (title: String, currentValue: String, isNumeric: Boolean, dataType: DataType, key: String) -> Unit = { _, _, _, _, _ -> },
    onNavigateToOption: (title: String, currentOptionId: Int, key: String) -> Unit = { _, _, _ -> }
) {
    val groupedItems = remember(configItems) { configItems.groupBy { it.groupName } }
    val groupNames = remember(groupedItems) { groupedItems.keys.toList() }
    val pagerState = rememberPagerState { groupNames.size }
    val coroutineScope = rememberCoroutineScope()

    // Create ViewModel using factory
    val viewModel = ViewModelFactory.createConfigPanelViewModel(configItems)

    val theme = LocalThemeType.current
    if (groupNames.isEmpty()) {
        EmptyConfigScreen()
        return
    }

    AppScaffold(
        title = "ConfigPanel",
        onActionClick = {
            coroutineScope.launch {
                viewModel.resetAllConfigs()
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Tab Row for group navigation
            if (groupNames.size > 1) {
                if (groupNames.size <= 3) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        indicator = { tabPositions ->
                            if (pagerState.currentPage < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                    color = if (theme == ThemeType.Material) MaterialTheme.colorScheme.primary else SystemBlue
                                )
                            }
                        }
                    ) {
                        groupNames.forEachIndexed { index, groupName ->
                            val selected = pagerState.currentPage == index
                            Tab(
                                selected = selected,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = groupName,
                                        color = if (selected && theme == ThemeType.Cupertino) {
                                            SystemBlue
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (selected && theme == ThemeType.Material)
                                            FontWeight.SemiBold
                                        else
                                            FontWeight.W500
                                    )
                                }
                            )
                        }
                    }
                } else {
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 20.dp,
                        indicator = { tabPositions ->
                            if (pagerState.currentPage < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                    color = if (theme == ThemeType.Material) MaterialTheme.colorScheme.primary else SystemBlue
                                )
                            }
                        }
                    ) {
                        groupNames.forEachIndexed { index, groupName ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = groupName,
                                        color = if (pagerState.currentPage == index && theme == ThemeType.Cupertino) {
                                            SystemBlue
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (pagerState.currentPage == index && theme == ThemeType.Material)
                                            FontWeight.SemiBold
                                        else
                                            FontWeight.W500
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Horizontal Pager for group content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val currentGroup = groupNames[page]
                val itemsForGroup = groupedItems[currentGroup] ?: emptyList()

                ConfigGroupPage(
                    items = itemsForGroup,
                    viewModel = viewModel,
                    onNavigateToInput = onNavigateToInput,
                    onNavigateToOption = onNavigateToOption
                )
            }
        }
    }
}

/**
 * Configuration group page showing items for a specific group
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigGroupPage(
    items: List<ConfigItemDescriptor<*>>,
    viewModel: ConfigPanelViewModel,
    onNavigateToInput: (title: String, currentValue: String, isNumeric: Boolean, dataType: DataType, key: String) -> Unit,
    onNavigateToOption: (title: String, currentOptionId: Int, key: String) -> Unit
) {
    val listState = rememberLazyListState()
    val configValues by viewModel.configValues.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items, key = { it.key }) { item ->
            ConfigItemAdapter(
                item = item,
                currentValue = configValues[item.key],
                onValueChange = { newValue ->
                    viewModel.updateConfigValue(item.key, newValue)
                },
                onNavigateToInput = onNavigateToInput,
                onNavigateToOption = onNavigateToOption
            )
        }
    }
}

/**
 * Empty state when no configurations are available
 */
@Composable
private fun EmptyConfigScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No configurations found.")
    }
} 