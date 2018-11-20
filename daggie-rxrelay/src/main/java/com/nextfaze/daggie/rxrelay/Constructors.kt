@file:Suppress("NOTHING_TO_INLINE")

package com.nextfaze.daggie.rxrelay

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.ReplayRelay

/** Creates a [ReplayRelay] for type [T]. */
inline fun <T : Any> replayRelay(): ReplayRelay<T> = ReplayRelay.create()

/** Creates a [PublishRelay] for type [T]. */
inline fun <T : Any> publishRelay(): PublishRelay<T> = PublishRelay.create()

/** Creates a [BehaviorRelay] for type [T]. */
inline fun <T : Any> behaviorRelay(): BehaviorRelay<T> = BehaviorRelay.create()

/** Creates a [BehaviorRelay] for type [T], with default value [defaultValue]. */
inline fun <T : Any> behaviorRelay(defaultValue: T): BehaviorRelay<T> = BehaviorRelay.createDefault(defaultValue)
