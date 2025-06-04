val enableSigning = project.hasProperty("signingInMemoryKey")

plugins {
    kotlin("jvm")
    alias(libs.plugins.mavenPublish)
    signing
}

dependencies {
    implementation(projects.appconfigAnnotation)
    implementation(libs.kspApi)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoet.ksp)
}

mavenPublishing {
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


