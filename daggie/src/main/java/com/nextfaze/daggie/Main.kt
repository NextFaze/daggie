package com.nextfaze.daggie

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

/**
 * Signals a binding relates to the app's primary user story(s).
 * For example, it sometimes pertains to the main activity starting.
 */
@Qualifier
@MustBeDocumented
@Retention(BINARY)
annotation class Main
