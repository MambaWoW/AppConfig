import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    kotlin("kapt")
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.mavenPublish)
    id("com.gradle.plugin-publish") version "1.3.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("stdlib"))
    implementation(libs.kotlin.gradle.plugin.api)
}


val pluginVersion = libs.versions.version.get()
group = libs.versions.groupId.get()
version = pluginVersion

gradlePlugin {
    website.set("https://github.com/MambaWoW/AppConfig")
    vcsUrl.set("https://github.com/MambaWoW/AppConfig.git")
    plugins {
        create("appConfigPlugin") {
            id = "io.github.mambawow.appconfig"
            implementationClass = "io.github.mambawow.appconfig.ConfigGradlePlugin"
            displayName = "AppConfig Gradle Plugin"
            description = "A Gradle plugin for AppConfig"
            tags.set(listOf("kotlin", "multiplatform", "configuration", "codegen", "ksp"))
        }
    }
}


mavenPublishing {
    coordinates(
        libs.versions.groupId.get(),
        "appconfig-gradle-plugin",
        pluginVersion,
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





