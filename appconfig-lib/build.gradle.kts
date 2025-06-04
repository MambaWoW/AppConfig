import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
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
            baseName = "AppConfigLib"
            isStatic = true
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.appconfigAnnotation)
                implementation(libs.multiplatform.settings)
                implementation(libs.androidx.startup.runtime)
            }
        }
    }
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "io.github.mambawow.appconfig.lib"

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
        "appconfig-lib",
        libs.versions.version.get(),
    )
    signAllPublications()
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    pom {
        name.set(project.name)
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


