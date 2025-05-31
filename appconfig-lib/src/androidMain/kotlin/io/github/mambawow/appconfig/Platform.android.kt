package io.github.mambawow.appconfig

import android.content.Context
import io.github.mambawow.appconfig.store.DefaultConfigStore
import java.io.File

fun AppConfig.init(context: Context) {
    initWithFactory { groupName ->
        DefaultConfigStore {
            context.preferencesDataStoreFile(groupName).absolutePath
        }
    }
}

public fun Context.preferencesDataStoreFile(name: String): File =
    File(applicationContext.filesDir, "datastore/$name.preferences_pb")
