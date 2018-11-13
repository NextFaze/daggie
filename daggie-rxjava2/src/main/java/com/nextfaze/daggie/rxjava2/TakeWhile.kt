package com.nextfaze.daggie.rxjava2

import io.reactivex.Flowable
import io.reactivex.Observable

/** Mirror the source `Flowable` while the specified `Flowable`'s latest emitted value is `true`. */
fun <T> Flowable<T>.takeWhile(p: Flowable<Boolean>): Flowable<T> {
    val shared = p.distinctUntilChanged().share()
    return shared.filter { it }.switchMap { takeUntil(shared.filter { !it }) }
}

/** Mirror the source `Observable` while the specified `Observable`'s latest emitted value is `true`. */
fun <T> Observable<T>.takeWhile(p: Observable<Boolean>): Observable<T> {
    val shared = p.distinctUntilChanged().share()
    return shared.filter { it }.switchMap { takeUntil(shared.filter { !it }) }
}
