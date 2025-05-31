val enableSigning = project.hasProperty("signingInMemoryKey")

plugins {
    kotlin("jvm")
//    alias(libs.plugins.mavenPublish)
//    signing
}

dependencies {
    implementation(projects.appconfigAnnotation)
    implementation(libs.kspApi)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoet.ksp)
}

/*mavenPublishing {
    coordinates(
        libs.versions.groupId.get(),
        "appconfig-processor",
        libs.versions.version.get(),
    )
    publishToMavenCentral()
    if (enableSigning) {
        signAllPublications()
    }
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])

            pom {
                name.set(project.name)
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/MambaWoW/AppConfig/issues")
                }
                description.set("KSP Plugin for AppConfig")
                url.set("https://github.com/MambaWoW/AppConfig")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/MambaWoW/AppConfig/blob/main/LICENSE")
                    }
                }
                scm {
                    url.set("https://github.com/MambaWoW/AppConfig")
                    connection.set("scm:git:git://github.com/MambaWoW/AppConfig.git")
                }
                developers {
                    developer {
                        name.set("Frank Shao")
                        url.set("https://github.com/MambaWoW")
                    }
                }
            }
        }
    }

    repositories {
        if (
            hasProperty("sonatypeUsername") &&
            hasProperty("sonatypePassword") &&
            hasProperty("sonatypeSnapshotUrl") &&
            hasProperty("sonatypeReleaseUrl")
        ) {
            maven {
                val url =
                    when {
                        "SNAPSHOT" in version.toString() -> property("sonatypeSnapshotUrl")
                        else -> property("sonatypeReleaseUrl")
                    } as String
                setUrl(url)
                credentials {
                    username = property("sonatypeUsername") as String
                    password = property("sonatypePassword") as String
                }
            }
        }
    }
}*/


