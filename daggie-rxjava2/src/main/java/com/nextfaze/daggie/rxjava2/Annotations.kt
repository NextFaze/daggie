package com.nextfaze.daggie.rxjava2

import io.reactivex.Scheduler
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

/** Qualifies a [Scheduler] as being intended for performing computationally bound tasks. */
@Qualifier
@MustBeDocumented
@Retention(BINARY)
annotation class Computation

/** Qualifies a [Scheduler] as being intended for performing I/O bound tasks. */
@Qualifier
@MustBeDocumented
@Retention(BINARY)
annotation class Io

/** Qualifies a [Scheduler] as the Android main thread scheduler. */
@Qualifier
@MustBeDocumented
@Retention(BINARY)
annotation class MainThread
