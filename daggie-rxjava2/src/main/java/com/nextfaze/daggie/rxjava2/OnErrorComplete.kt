package com.nextfaze.daggie.rxjava2

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Returns a `Flowable` that simply completes upon an error.
 * @see io.reactivex.Flowable.onErrorResumeNext
 */
fun <T : Any> Flowable<T>.onErrorComplete(): Flowable<T> = onErrorResume { Flowable.empty<T>() }

/**
 * Returns an `Observable` that simply completes upon an error.
 * @see io.reactivex.Observable.onErrorResumeNext
 */
fun <T : Any> Observable<T>.onErrorComplete(): Observable<T> = onErrorResume { Observable.empty<T>() }

/**
 * Returns a `Maybe` that simply completes upon an error.
 * @see io.reactivex.Observable.onErrorResumeNext
 */
fun <T : Any> Single<T>.onErrorComplete(): Maybe<T> = toMaybe().onErrorComplete()
