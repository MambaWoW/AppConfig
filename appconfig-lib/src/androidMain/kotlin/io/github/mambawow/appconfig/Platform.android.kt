package io.github.mambawow.appconfig

import android.content.Context
import androidx.startup.Initializer
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Android App Startup initializer for settings context management.
 * 
 * This initializer automatically provides the application context to the
 * settings system during app startup, eliminating the need for manual
 * context passing in Android applications.
 */
public class SettingsInitializer : Initializer<Context> {
    /**
     * Stores the application context for use by the settings system.
     * 
     * @param context The context provided during app initialization.
     * @return The application context that will be used by settings.
     */
    override fun create(context: Context): Context = context.applicationContext.also { appContext = it }

    /**
     * Returns an empty dependency list as this initializer only requires context.
     */
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

private var appContext: Context? = null

/**
 * Android-specific implementation using SharedPreferences.
 * 
 * Creates a Settings instance backed by Android's SharedPreferences system,
 * providing persistent storage that survives app restarts and follows
 * Android's standard configuration management patterns.
 * 
 * @param name The preference file name, used to isolate different configuration groups.
 * @return A Settings instance backed by SharedPreferences.
 */
actual fun createSettings(name: String): Settings = SharedPreferencesSettings.Factory(appContext!!).create(name)
