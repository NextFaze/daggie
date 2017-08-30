package com.nextfaze.daggie

import android.app.Activity
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

/** Denotes a binding that is applicable only when at least one app [Activity] is started. */
@Qualifier
@MustBeDocumented
@Retention(BINARY)
annotation class Foreground
