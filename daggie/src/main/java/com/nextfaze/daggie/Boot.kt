package com.nextfaze.daggie

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

/** Signals a binding relates to the device booting up. */
@Qualifier
@MustBeDocumented
@Retention(RUNTIME)
annotation class Boot