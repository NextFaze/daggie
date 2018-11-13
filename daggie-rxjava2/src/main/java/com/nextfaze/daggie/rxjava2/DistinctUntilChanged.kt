package com.nextfaze.daggie.rxjava2

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.Function

/**
 * Returns an `Observable` that emits all items emitted by the source `Observable` that are distinct from their
 * immediate predecessors, according to [keySelector] and based on [Object.equals] comparison of the value it returns.
 * @see io.reactivex.Observable.distinctUntilChanged(io.reactivex.functions.Function<? super T,K>)
 */
inline fun <T : Any, K : Any> Observable<T>.distinctUntilChangedBy(crossinline keySelector: (T) -> K): Observable<T> =
    distinctUntilChanged(Function<T, K> { keySelector(it) })

/**
 * Returns a `Flowable` that emits all items emitted by the source `Flowable` that are distinct from their
 * immediate predecessors, according to [keySelector] and based on [Object.equals] comparison of the value it returns.
 * @see io.reactivex.Flowable.distinctUntilChanged(io.reactivex.functions.Function<? super T,K>)
 */
inline fun <T : Any, K : Any> Flowable<T>.distinctUntilChangedBy(crossinline keySelector: (T) -> K): Flowable<T> =
    distinctUntilChanged(Function<T, K> { keySelector(it) })
