package io.github.mambawow.appconfig

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.util.Locale.US
import kotlin.reflect.full.declaredMemberProperties

class ConfigGradlePlugin : Plugin<Project> {

    companion object {
        const val GROUP_NAME = "io.github.mambawow.appconfig"
        const val ARTIFACT_NAME = "compiler-plugin"
        const val COMPILER_PLUGIN_ID = "io.github.mambawow.AppConfigPlugin"
        const val CONFIG_KSP_PLUGIN_VERSION = "0.1.0"
        const val MIN_KSP_VERSION = "1.0.28"
        const val KSP_PLUGIN_ERROR_MESSAGE =
            "KSP plugin not applied, please add it to your build script."
        const val KSP_ID = "com.google.devtools.ksp"
        const val KSP_IS_MULTIPLATFORM = "AppConfig_isMultiplatform"
        const val CONFIG_KSP_DEPENDENCY =
            "$GROUP_NAME:app-config-processor:$CONFIG_KSP_PLUGIN_VERSION"
    }

    override fun apply(project: Project) {
        with(project) {
            val hasKspApplied = extensions.findByName("ksp") != null
            if (!hasKspApplied) {
                error(KSP_PLUGIN_ERROR_MESSAGE)
            }
            val kspPlugin = plugins.findPlugin(KSP_ID) ?: error(KSP_PLUGIN_ERROR_MESSAGE)
            val kspVersion =
                kspPlugin.javaClass.protectionDomain.codeSource.location
                    .toURI()
                    .toString()
                    .substringAfterLast("-")
                    .substringBefore(".jar")
            checkKSPVersion(kspVersion)
            val kspExtension = extensions.findByName("ksp") ?: error("KSP config not found")
            val argMethod =
                kspExtension.javaClass.getMethod("arg", String::class.java, String::class.java)
            afterEvaluate {
                if (kotlinExtension is KotlinMultiplatformExtension) {
                    val useKsp2 =
                        kspExtension.javaClass.kotlin.declaredMemberProperties.find {
                            it.name == "useKsp2"
                        }?.call(kspExtension).let {
                            (it as Property<*>?)?.get() as Boolean?
                        } ?: project.findProperty("ksp.useKSP2")?.toString()?.toBoolean() ?: false

                    if (useKsp2) {
                        tasks.named { name -> name.startsWith("ksp") }.configureEach {
                            if (name != "kspCommonMainKotlinMetadata") {
                                dependsOn("kspCommonMainKotlinMetadata")
                            }
                        }
                    } else {
                        tasks.withType(KotlinCompilationTask::class.java).configureEach {
                            if (name != "kspCommonMainKotlinMetadata") {
                                dependsOn("kspCommonMainKotlinMetadata")
                            }
                        }
                    }
                }
            }

            when (val kotlinExtension = kotlinExtension) {
                is KotlinSingleTargetExtension<*> -> {
                    argMethod.invoke(kspExtension, KSP_IS_MULTIPLATFORM, "0")
                    dependencies.add("ksp", project(":appconfig-processor"))
                }

                is KotlinMultiplatformExtension -> {
                    argMethod.invoke(kspExtension, KSP_IS_MULTIPLATFORM, "1")
                    kotlinExtension.targets.configureEach {
                        if (platformType.name == "common") {
                            dependencies.add(
                                "kspCommonMainMetadata",
                                project(":appconfig-processor")
                            )
                            return@configureEach
                        }
                        val capitalizedTargetName =
                            this.targetName.replaceFirstChar {
                                if (it.isLowerCase()) {
                                    it.titlecase(US)
                                } else {
                                    it.toString()
                                }
                            }
                        dependencies.add(
                            "ksp$capitalizedTargetName",
                            project(":appconfig-processor")
                        )

                        if (this.compilations.any { it.name == "test" }) {
                            dependencies.add(
                                "ksp${capitalizedTargetName}Test",
                                project(":appconfig-processor")
                            )
                        }
                    }
                    kotlinExtension.sourceSets
                        .named(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
                        .configure {
                            kotlin.srcDir(
                                "${layout.buildDirectory.get()}/generated/ksp/metadata/" +
                                        "${KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME}/kotlin"
                            )
                        }
                }

                else -> {}
            }
        }
    }

    private fun checkKSPVersion(kspVersion: String) {
        val kspParts = kspVersion.split(".").map { it.toInt() }
        val minParts = MIN_KSP_VERSION.split(".").map { it.toInt() }
        for (i in 0 until minOf(kspParts.size, minParts.size)) {
            if (kspParts[i] > minParts[i]) {
                return
            } else if (kspParts[i] < minParts[i]) {
                error("AppConfig: KSP version $kspVersion is not supported. You need at least version $MIN_KSP_VERSION")
            }
        }
    }
} 