
plugins {
    kotlin("jvm")
    kotlin("kapt")
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.mavenPublish)
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.google.com")
        maven("https://plugins.gradle.org/m2/")
        google()
    }
}

dependencies {
    add("compileOnly", kotlin("gradle-plugin"))
    implementation(libs.kotlin.gradle.plugin.api)
}

gradlePlugin {
    /*website.set("https://github.com/Foso/Ktorfit")
    vcsUrl.set("https://github.com/Foso/Ktorfit")*/
    plugins {
        create("AppConfigPlugin") {
            id = "io.github.mambawow.appconfig.plugin"
            implementationClass = "io.github.mambawow.appconfig.ConfigGradlePlugin"
            displayName = "App Config Plugin"
            description = "App Config Plugin"
            tags.set(listOf("kotlin", "kotlin-mpp", "AppConfig"))
        }
    }
}

mavenPublishing {

    coordinates(
        libs.versions.groupId.get(),
        "appconfig-gradle-plugin",
//        libs.versions.version.get(),
        "0.0.0.3",
    )
    publishToMavenCentral()
    /*// publishToMavenCentral(SonatypeHost.S01) for publishing through s01.oss.sonatype.org
    if (enableSigning) {
        signAllPublications()
    }*/
}

group = "io.github.mambawow.appconfig.plugin"
version = "1.0.0"

