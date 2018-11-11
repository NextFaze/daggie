package com.nextfaze.daggie.optional

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

fun <T : Any> Flowable<Optional<T>>.filterPresent(): Flowable<T> = ofType<Some<T>>().map { it.value }
fun <T : Any> Observable<Optional<T>>.filterPresent(): Observable<T> = ofType<Some<T>>().map { it.value }
fun <T : Any> Maybe<Optional<T>>.filterPresent(): Maybe<T> = toObservable().filterPresent().firstElement()
fun <T : Any> Single<Optional<T>>.filterPresent(): Maybe<T> = toObservable().filterPresent().firstElement()

fun <T : Any> Flowable<Optional<T>>.filterAbsent(): Flowable<None> = ofType()
fun <T : Any> Observable<Optional<T>>.filterAbsent(): Observable<None> = ofType()
fun <T : Any> Maybe<Optional<T>>.filterAbsent(): Maybe<None> = toObservable().filterAbsent().firstElement()
fun <T : Any> Single<Optional<T>>.filterAbsent(): Maybe<None> = toObservable().filterAbsent().firstElement()

internal inline fun <reified R : Any> Observable<*>.ofType(): Observable<R> = ofType(R::class.java)
internal inline fun <reified R : Any> Flowable<*>.ofType(): Flowable<R> = ofType(R::class.java)
