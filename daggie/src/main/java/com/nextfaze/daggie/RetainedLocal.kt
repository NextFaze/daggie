package com.nextfaze.daggie

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

@Qualifier
@MustBeDocumented
@Retention(BINARY)
annotation class RetainedLocal
