package io.github.mambawow.appconfig

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.TYPEALIAS

@RequiresOptIn(
    message = "This is an internal API and may change or be removed without notice. Use with caution.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(BINARY)
@Target(CLASS, FUNCTION, PROPERTY, TYPEALIAS)
annotation class InternalAPI