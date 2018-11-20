@file:Suppress("NOTHING_TO_INLINE")

package com.nextfaze.daggie.rxrelay

import com.jakewharton.rxrelay2.Relay

/** Invokes [Relay.accept] with value [value]. */
inline operator fun <T : Any> Relay<T>.invoke(value: T) = accept(value)
