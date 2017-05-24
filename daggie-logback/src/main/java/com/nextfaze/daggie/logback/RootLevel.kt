package com.nextfaze.daggie.logback

import ch.qos.logback.classic.Level
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/** Qualifies a [Level] binding, marking it as the root log level. */
@Qualifier
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Retention(BINARY)
annotation class RootLevel