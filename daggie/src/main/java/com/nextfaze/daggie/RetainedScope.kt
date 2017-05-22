package com.nextfaze.daggie

import javax.inject.Scope
import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.*

@Scope
@MustBeDocumented
@Retention(BINARY)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, CLASS, FILE)
annotation class RetainedScope