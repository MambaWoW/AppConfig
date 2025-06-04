package io.github.mambawowo.singleandroidtarget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.mambawow.appconfig.BooleanProperty
import io.github.mambawow.appconfig.Config
import io.github.mambawow.appconfig.FloatProperty
import io.github.mambawow.appconfig.IntProperty
import io.github.mambawow.appconfig.Option
import io.github.mambawow.appconfig.OptionItem
import io.github.mambawow.appconfig.OptionProperty
import io.github.mambawow.appconfig.StringProperty
import io.github.mambawowo.singleandroidtarget.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Config(groupName = "Appearance")
interface AppearanceConfig {
    @BooleanProperty(
        key = "DarkMode",
        defaultValue = false,
        description = "Enable dark mode theme"
    )
    var isDarkModeEnabled: Boolean

    @StringProperty(
        key = "WelcomeMessage",
        defaultValue = "aaaaaa",
        description = "Welcome message for the user"
    )
    var welcomeMessage: String

    @FloatProperty(
        key = "AnimationScale",
        defaultValue = 1.0f,
        description = "Animation scale factor"
    )
    var animationScale: Float

    @IntProperty(
        key = "LogLevel",
        defaultValue = 2,
        description = "Logging level for internal diagnostics"
    )
    var logLevel: Int

    @OptionProperty(
        key = "Logoutput",
        description = "Target for debug logs"
    )
    var logOutputTarget: LogOutputTarget
}

@Option
sealed class LogOutputTarget(
    val value: String
) {
    @OptionItem(optionId = 0, description = "Option A")
    data object A : LogOutputTarget("A")

    @OptionItem(4, "Option B", isDefault = true)
    data object B : LogOutputTarget("B")

    @OptionItem(5, "Option C")
    data object C : LogOutputTarget("C")
}
