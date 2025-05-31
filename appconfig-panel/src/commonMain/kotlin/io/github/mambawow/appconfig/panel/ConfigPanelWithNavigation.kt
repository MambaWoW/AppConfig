package io.github.mambawow.appconfig.panel

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.mambawow.appconfig.ConfigItemDescriptor
import io.github.mambawow.appconfig.DataType
import io.github.mambawow.appconfig.OptionConfigItem
import io.github.mambawow.appconfig.panel.navigation.ConfigPanel
import io.github.mambawow.appconfig.panel.navigation.ConfigInput
import io.github.mambawow.appconfig.panel.navigation.ConfigOption
import io.github.mambawow.appconfig.panel.ui.screen.ConfigInputPage
import io.github.mambawow.appconfig.panel.ui.screen.ConfigOptionPage
import io.github.mambawow.appconfig.panel.ui.screen.ConfigPanelScreen
import io.github.mambawow.appconfig.panel.ui.screen.adapters.convertStringToType
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.Background
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.Primary
import io.github.mambawow.appconfig.panel.ui.theme.AppColors.Surface
import io.github.mambawow.appconfig.panel.ui.components.ConfigOption as ConfigOptionModel
import io.github.mambawow.appconfig.panel.viewmodel.ViewModelFactory
import io.github.mambawow.appconfig.panel.ui.theme.LocalThemeType
import io.github.mambawow.appconfig.panel.ui.theme.ThemeType
import io.github.mambawow.appconfig.panel.ui.theme.colorScheme

/**
 * navigation animations
 */
private fun createNavigationAnimations(
    slideDistance: (Int) -> Int,
    animationDuration: Int
) = object {
    val enterTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = slideDistance,
        animationSpec = tween(animationDuration)
    ) + fadeIn(animationSpec = tween(animationDuration))

    val exitTransition: ExitTransition = slideOutHorizontally(
        targetOffsetX = { -slideDistance(it) / 3 },
        animationSpec = tween(animationDuration)
    ) + fadeOut(animationSpec = tween(animationDuration))

    val popEnterTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = { -slideDistance(it) / 3 },
        animationSpec = tween(animationDuration)
    ) + fadeIn(animationSpec = tween(animationDuration))

    val popExitTransition: ExitTransition = slideOutHorizontally(
        targetOffsetX = slideDistance,
        animationSpec = tween(animationDuration)
    ) + fadeOut(animationSpec = tween(animationDuration))
}

/**
 * Configuration panel with integrated navigation support using Kotlin Serialization routes
 * Provides full-screen navigation for configuration editing with type-safe routing and iOS-style animations
 */
@Composable
fun ConfigPanelWithNavigation(
    configItems: List<ConfigItemDescriptor<*>>,
    onResetAll: (suspend () -> Unit)? = null
) {
    val navController = rememberNavController()
    val viewModel = ViewModelFactory.createConfigPanelViewModel(configItems)

    val theme = determinePlatformTheme()
    // iOS-style animation durations and specs
    val animationDuration = 350
    val slideDistance = 1000
    
    // Create reusable animations
    val defaultAnimations = createNavigationAnimations({ slideDistance }, animationDuration)
    val pageAnimations = createNavigationAnimations({ it }, animationDuration)

    CompositionLocalProvider(LocalThemeType provides theme) {
        MaterialTheme (
            colorScheme = colorScheme(theme)
        ) {
            NavHost(
                navController = navController,
                startDestination = ConfigPanel,
                enterTransition = { defaultAnimations.enterTransition },
                exitTransition = { defaultAnimations.exitTransition },
                popEnterTransition = { defaultAnimations.popEnterTransition },
                popExitTransition = { defaultAnimations.popExitTransition }
            ) {
                // Main configuration panel
                composable<ConfigPanel> {
                    ConfigPanelScreen(
                        configItems = configItems,
                        onResetAll = onResetAll,
                        onNavigateToInput = { title: String, currentValue: String, isNumeric: Boolean, dataType: DataType, key: String ->
                            navController.navigate(
                                ConfigInput(
                                    title = title,
                                    currentValue = currentValue,
                                    isNumeric = isNumeric,
                                    dataType = dataType,
                                    key = key
                                )
                            ) {
                                restoreState = true
                            }
                        },
                        onNavigateToOption = { title: String, currentOptionId: Int, key: String ->
                            navController.navigate(
                                ConfigOption(
                                    title = title,
                                    currentOptionId = currentOptionId,
                                    key = key
                                )
                            ){
                                restoreState = true
                            }
                        }
                    )
                }

                // Input configuration page
                composable<ConfigInput>(
                    enterTransition = { pageAnimations.enterTransition },
                    exitTransition = { pageAnimations.exitTransition },
                    popEnterTransition = { pageAnimations.popEnterTransition },
                    popExitTransition = { pageAnimations.popExitTransition }
                ) { backStackEntry ->
                    val configInput = backStackEntry.toRoute<ConfigInput>()

                    ConfigInputPage(
                        title = configInput.title,
                        initialValue = configInput.currentValue,
                        isNumeric = configInput.isNumeric,
                        dataType = configInput.dataType,
                        onConfirm = { newValue ->
                            // Convert string input to proper type based on dataType
                            val convertedValue = convertStringToType(newValue, configInput.dataType)
                            viewModel.updateConfigValue(configInput.key, convertedValue)
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // Option selection page
                composable<ConfigOption>(
                    enterTransition = { pageAnimations.enterTransition },
                    exitTransition = { pageAnimations.exitTransition },
                    popEnterTransition = { pageAnimations.popEnterTransition },
                    popExitTransition = { pageAnimations.popExitTransition }
                ) { backStackEntry ->
                    val configOption = backStackEntry.toRoute<ConfigOption>()

                    // Find the config item to get options
                    val configItem = configItems.find { it.key == configOption.key }
                    if (configItem is OptionConfigItem<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val options = ConfigOptionModel.fromOptionItemDescriptors(
                            configItem.choices as List<io.github.mambawow.appconfig.OptionItemDescriptor<Any?>>
                        )

                        ConfigOptionPage(
                            title = configOption.title,
                            options = options,
                            currentOptionId = configOption.currentOptionId,
                            onOptionSelected = { option ->
                                viewModel.updateConfigValue(configOption.key, option.value)
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
} 