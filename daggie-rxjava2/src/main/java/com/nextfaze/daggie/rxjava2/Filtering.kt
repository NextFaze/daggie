package com.nextfaze.daggie.rxjava2

import io.reactivex.Flowable
import io.reactivex.Observable

/** Filters a `Flowable` by only emitting `true` values. */
fun Flowable<Boolean>.filterTrue(): Flowable<Boolean> = filter { it }

/** Filters an `Observable` by only emitting `true` values. */
fun Observable<Boolean>.filterTrue(): Observable<Boolean> = filter { it }

/** Filters a `Flowable` by only emitting `false` values. */
fun Flowable<Boolean>.filterFalse(): Flowable<Boolean> = filter { !it }

/** Filters an `Observable` by only emitting `false` values. */
fun Observable<Boolean>.filterFalse(): Observable<Boolean> = filter { !it }

/** Filters a `Flowable` by only emitting empty collections. */
fun <C : Collection<*>> Flowable<C>.filterEmpty(): Flowable<C> = filter { it.isEmpty() }

/** Filters an `Observable` by only emitting empty collections. */
fun <C : Collection<*>> Observable<C>.filterEmpty(): Observable<C> = filter { it.isEmpty() }

/** Filters a `Flowable` by only emitting non-empty collections. */
fun <C : Collection<*>> Flowable<C>.filterNotEmpty(): Flowable<C> = filter { it.isNotEmpty() }

/** Filters an `Observable` by only emitting non-empty collections. */
fun <C : Collection<*>> Observable<C>.filterNotEmpty(): Observable<C> = filter { it.isNotEmpty() }
