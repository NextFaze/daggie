package com.nextfaze.daggie

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

/**
 * Used to inject initializer code that should be run very early in the application lifecycle.
 * Such initializers should have absolute minimal dependencies.
 */
@Qualifier
@Retention(BINARY)
annotation class Early