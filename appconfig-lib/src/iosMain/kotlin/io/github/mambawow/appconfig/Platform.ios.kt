package io.github.mambawow.appconfig

import io.github.mambawow.appconfig.store.DefaultConfigStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

fun AppConfig.initialize() {
    initWithFactory { groupName ->
        DefaultConfigStore{
            "${fileDirectory()}/datastore/$groupName.preferences_pb"
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun fileDirectory(): String {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory){ "SJY Unable to get document directory"}.path!!
}