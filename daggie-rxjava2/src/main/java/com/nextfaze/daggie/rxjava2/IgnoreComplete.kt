package com.nextfaze.daggie.rxjava2

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

/** Transforms this `Observable` such that instead of completing, it never terminates. */
fun <T : Any> Observable<T>.ignoreComplete(): Observable<T> = concatWith(Observable.never())

/** Transforms this `Single` such that instead of succeeding, it never terminates. */
fun <T : Any> Single<T>.ignoreComplete(): Observable<T> = toMaybe().ignoreComplete()

/** Transforms this `Maybe` such that instead of completing, it never terminates. */
fun <T : Any> Maybe<T>.ignoreComplete(): Observable<T> = toObservable().ignoreComplete()
