package com.nextfaze.daggie.rxjava2

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.Function

/**
 * Instructs a `Flowable` to pass control to another `Flowable` rather than invoking `onError` if it encounters an error.
 * @see io.reactivex.Flowable.onErrorResumeNext
 */
fun <T : Any> Flowable<T>.onErrorResume(next: (Throwable) -> Flowable<T>): Flowable<T> =
    onErrorResumeNext(Function { next(it) })

/**
 * Instructs an `Observable` to pass control to another `Observable` rather than invoking `onError` if it encounters an error.
 * @see io.reactivex.Observable.onErrorResumeNext
 */
fun <T : Any> Observable<T>.onErrorResume(next: (Throwable) -> Observable<T>): Observable<T> =
    onErrorResumeNext(Function { next(it) })

/**
 * Instructs an `Maybe` to pass control to another `Maybe` rather than invoking `onError` if it encounters an error.
 * @see io.reactivex.Maybe.onErrorResumeNext
 */
fun <T : Any> Maybe<T>.onErrorResume(next: (Throwable) -> Maybe<T>): Maybe<T> =
    onErrorResumeNext(Function { next(it) })

