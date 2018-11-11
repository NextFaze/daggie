@file:Suppress("NOTHING_TO_INLINE")

package com.nextfaze.daggie.optional

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

fun <T : Any> Observable<Optional<T>>.presentOr(body: () -> T): Observable<T> =
    map { it.value ?: body() }

fun <T : Any> Flowable<Optional<T>>.presentOr(body: () -> T): Flowable<T> =
    map { it.value ?: body() }

fun <T : Any> Single<Optional<T>>.presentOr(body: () -> T): Single<T> = map { it.value ?: body() }

fun <T : Any> Maybe<Optional<T>>.presentOr(body: () -> T): Maybe<T> = map { it.value ?: body() }

fun <T : Any> Observable<Optional<T>>.presentOrError(): Observable<T> = presentOrError { NoSuchElementException() }

fun <T : Any> Flowable<Optional<T>>.presentOrError(): Flowable<T> = presentOrError { NoSuchElementException() }

fun <T : Any> Single<Optional<T>>.presentOrError(): Single<T> = presentOrError { NoSuchElementException() }

fun <T : Any> Maybe<Optional<T>>.presentOrError(): Maybe<T> = presentOrError { NoSuchElementException() }

fun <T : Any> Observable<Optional<T>>.presentOrError(createError: () -> Throwable): Observable<T> =
    map { it.value ?: throw createError() }

fun <T : Any> Flowable<Optional<T>>.presentOrError(createError: () -> Throwable): Flowable<T> =
    map { it.value ?: throw createError() }

fun <T : Any> Single<Optional<T>>.presentOrError(createError: () -> Throwable): Single<T> =
    map { it.value ?: throw createError() }

fun <T : Any> Maybe<Optional<T>>.presentOrError(createError: () -> Throwable): Maybe<T> =
    map { it.value ?: throw createError() }

