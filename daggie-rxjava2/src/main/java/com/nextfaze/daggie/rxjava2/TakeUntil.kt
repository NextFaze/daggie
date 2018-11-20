@file:Suppress("NOTHING_TO_INLINE")

package com.nextfaze.daggie.rxjava2

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Returns an `Observable` that emits the items emitted by the source `Observable` until [completable] completes.
 * @see io.reactivex.Observable.takeUntil
 */
inline fun <T : Any> Observable<T>.takeUntil(completable: Completable): Observable<T> =
    takeUntil(completable.toObservable<T>())

/**
 * Returns an `Flowable` that emits the items emitted by the source `Flowable` until [completable] completes.
 * @see io.reactivex.Flowable.takeUntil
 */
inline fun <T : Any> Flowable<T>.takeUntil(completable: Completable): Flowable<T> =
    takeUntil(completable.toFlowable<T>())
