
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
    publishToMavenCentral()
}


