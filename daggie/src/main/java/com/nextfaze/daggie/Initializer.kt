package com.nextfaze.daggie

/**
 * A function that is invoked once for the lifetime of the supplied [T] instance, typically invoked
 * after the [T] instance has initialized.
 */
typealias Initializer<T> = (T) -> Unit