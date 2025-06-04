package io.github.mambawow.appconfig

import com.russhwolf.settings.Settings

/**
 * Creates a platform-specific [Settings] instance for the given configuration group.
 * 
 * This expect function provides platform-specific implementations of the Settings
 * interface from multiplatform-settings library. Each platform implements this
 * function to create appropriate storage mechanisms:
 * 
 * - **Android**: SharedPreferences-based storage
 * - **iOS**: NSUserDefaults-based storage  
 *
 * @param name The name of the configuration group, used to partition settings
 *            and prevent key collisions between different configuration groups.
 * 
 * @return A [Settings] instance configured for the current platform.
 * 
 * @see Settings
 */
expect fun createSettings(name: String): Settings