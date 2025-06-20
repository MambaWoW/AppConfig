import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "AppConfigPanel"
            isStatic = true
        }
    }
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }

        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.appconfig.lib)
//                implementation(projects.appconfigLib)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(libs.androidx.activity.compose.navigation)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "io.github.mambawow.appconfig.panel"

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}


mavenPublishing {
    coordinates(
        libs.versions.groupId.get(),
        "appconfig-panel",
        libs.versions.version.get(),
    )
    signAllPublications()
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    pom {
        name.set(project.name)
        description.set("A powerful, type-safe configuration management library for Kotlin Multiplatform that transforms how you handle app settings with zero boilerplate code.")
        inceptionYear.set("2025")
        url.set("https://github.com/MambaWoW/AppConfig")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("MambaWoW")
                name.set("MambaWoW")
                url.set("https://github.com/MambaWoW/")
            }
        }
        scm {
            url.set("https://github.com/MambaWoW/AppConfig/")
            connection.set("scm:git:git://github.com/MambaWoW/AppConfig.git")
            developerConnection.set("scm:git:ssh://git@github.com/MambaWoW/AppConfig.git")
        }
    }
}

