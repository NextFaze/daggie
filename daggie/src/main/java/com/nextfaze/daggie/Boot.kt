package com.nextfaze.daggie

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

/** Signals a binding relates to the device booting up. */
@Qualifier
@MustBeDocumented
@Retention(BINARY)
annotation class Boot
