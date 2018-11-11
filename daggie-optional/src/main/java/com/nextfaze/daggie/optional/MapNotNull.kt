package com.nextfaze.daggie.optional

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

inline fun <T : Any, R : Any> Observable<T>.mapNotNull(crossinline transform: (T) -> R?): Observable<R> =
    map { transform(it).toOptional() }.filterPresent()

inline fun <T : Any, R : Any> Flowable<T>.mapNotNull(crossinline transform: (T) -> R?): Flowable<R> =
    map { transform(it).toOptional() }.filterPresent()

inline fun <T : Any, R : Any> Single<T>.mapNotNull(crossinline transform: (T) -> R?): Maybe<R> =
    map { transform(it).toOptional() }.filterPresent()

inline fun <T : Any, R : Any> Maybe<T>.mapNotNull(crossinline transform: (T) -> R?): Maybe<R> =
    map { transform(it).toOptional() }.filterPresent()
