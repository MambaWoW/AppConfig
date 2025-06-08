import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    alias(libs.plugins.mavenPublish)
}

dependencies {
//    implementation(projects.appconfigAnnotation)
    implementation(libs.appconfig.annotation)
    implementation(libs.kspApi)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoet.ksp)
    
    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    coordinates(
        libs.versions.groupId.get(),
        "appconfig-processor",
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


