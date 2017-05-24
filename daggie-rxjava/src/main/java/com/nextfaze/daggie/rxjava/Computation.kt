package com.nextfaze.daggie.rxjava

import rx.Scheduler
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

/** Qualifies a [Scheduler] as being intended for performing computationally bound tasks. */
@Qualifier
@MustBeDocumented
@Retention(RUNTIME)
annotation class Computation