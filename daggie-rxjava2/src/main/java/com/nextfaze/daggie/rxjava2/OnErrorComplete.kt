package com.nextfaze.daggie.rxjava2

import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Returns a `Flowable` that simply completes upon an error.
 * @see io.reactivex.Flowable.onErrorResumeNext
 */
fun <T : Any> Flowable<T>.onErrorComplete(): Flowable<T> = onErrorResume { Flowable.empty<T>() }

/**
 * Returns a `Observable` that simply completes upon an error.
 * @see io.reactivex.Observable.onErrorResumeNext
 */
fun <T : Any> Observable<T>.onErrorComplete(): Observable<T> = onErrorResume { Observable.empty<T>() }
