package io.github.mambawow.appconfig

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings

/**
 * iOS-specific implementation using NSUserDefaults.
 * 
 * Creates a Settings instance backed by iOS's NSUserDefaults system,
 * providing persistent storage that integrates with iOS's native
 * preference management and follows Apple's configuration guidelines.
 * 
 * @param name The preference suite name, used to isolate different configuration groups.
 * @return A Settings instance backed by NSUserDefaults.
 */
actual fun createSettings(name: String): Settings {
    return NSUserDefaultsSettings.Factory().create(name)
}


