package com.nextfaze.daggie.optional

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

sealed class Optional<out T : Any> {

    abstract val isPresent: Boolean

    object None : Optional<Nothing>() {
        override val isPresent: Boolean = false
        override fun toString() = "None"
    }

    data class Some<out T : Any>(val value: T) : Optional<T>() {
        override val isPresent: Boolean = true
    }

    fun <R : Any> map(transform: (T) -> R?): Optional<R> = when (this) {
        is Some -> transform(value).toOptional()
        else -> Optional.None
    }

    fun <R : Any> flatMap(transform: (T) -> Optional<R>): Optional<R> = when (this) {
        is Some -> transform(value)
        else -> Optional.None
    }
}

fun <T : Any> T?.toOptional(): Optional<T> = this?.let { Optional.Some(it) } ?: Optional.None

val <T : Any> Optional<T>.value: T? get() = (this as? Optional.Some)?.value

val <T : Any> Optional<T>.isAbsent get() = !isPresent

fun <T : Any> Flowable<Optional<T>>.filterPresent(): Flowable<T> = ofType<Optional.Some<T>>().map { it.value }
fun <T : Any> Observable<Optional<T>>.filterPresent(): Observable<T> = ofType<Optional.Some<T>>().map { it.value }
fun <T : Any> Maybe<Optional<T>>.filterPresent(): Maybe<T> = toObservable().filterPresent().firstElement()
fun <T : Any> Single<Optional<T>>.filterPresent(): Maybe<T> = toObservable().filterPresent().firstElement()

fun <T : Any> Flowable<Optional<T>>.filterAbsent(): Flowable<Optional.None> = ofType()
fun <T : Any> Observable<Optional<T>>.filterAbsent(): Observable<Optional.None> = ofType()
fun <T : Any> Maybe<Optional<T>>.filterAbsent(): Maybe<Optional.None> = toObservable().filterAbsent().firstElement()
fun <T : Any> Single<Optional<T>>.filterAbsent(): Maybe<Optional.None> = toObservable().filterAbsent().firstElement()

inline fun <T : Any, R : Any> Flowable<T>.mapNotNull(crossinline transform: (T) -> R?): Flowable<R> =
    map { transform(it).toOptional() }.filterPresent()

inline fun <T : Any, R : Any> Observable<T>.mapNotNull(crossinline transform: (T) -> R?): Observable<R> =
    map { transform(it).toOptional() }.filterPresent()

fun <T : Any> Observable<Optional<T>>.presentOr(body: () -> T): Observable<T> = map { it.value ?: body() }

fun <T : Any> Observable<Optional<T>>.presentOrResumeNext(next: () -> Observable<T>): Observable<T> =

    flatMap { Observable.just(it.value) ?: next() }

fun <T : Any> Single<Optional<T>>.presentOr(body: () -> T): Single<T> = map { it.value ?: body() }

fun <T : Any> Single<Optional<T>>.presentOrResumeNext(next: () -> Single<T>): Single<T> =
    flatMap { Single.just(it.value) ?: next() }

fun <T : Any> Observable<Optional<T>>.presentOrError(): Observable<T> = presentOrError { NoSuchElementException() }

fun <T : Any> Observable<Optional<T>>.presentOrError(createError: () -> Throwable): Observable<T> =
    map { it.value ?: throw createError() }

fun <T : Any> Single<Optional<T>>.presentOrError(): Single<T> = presentOrError { NoSuchElementException() }

fun <T : Any> Single<Optional<T>>.presentOrError(createError: () -> Throwable): Single<T> =
    map { it.value ?: throw createError() }

fun <T : Any, R : Any> Observable<Optional<T>>.flatMapOptional(mapper: (T) -> R?): Observable<Optional<R>> =
    map { if (it is Optional.Some) mapper(it.value).toOptional() else Optional.None }

fun <T : Any, R : Any> Single<Optional<T>>.flatMapOptional(mapper: (T) -> R?): Single<Optional<R>> =
    map { if (it is Optional.Some) mapper(it.value).toOptional() else Optional.None }

fun <T : Any, R : Any> Observable<Optional<T>>.flatMapSingleOptional(mapper: (T) -> Single<Optional<R>>):
        Observable<Optional<R>> = flatMapSingle { if (it is Optional.Some) mapper(it.value) else Single.just(Optional.None) }

fun <T : Any, R : Any> Observable<Optional<T>>.switchMapOptional(mapper: (T) -> Observable<Optional<R>>):
        Observable<Optional<R>> = switchMap { if (it is Optional.Some) mapper(it.value) else Observable.just(Optional.None) }

fun <T : Any, R : Any> Observable<Optional<T>>.switchMapSingleOptional(mapper: (T) -> Single<Optional<R>>):
        Observable<Optional<R>> = switchMapSingle { if (it is Optional.Some) mapper(it.value) else Single.just(Optional.None) }

fun <T : Any> Observable<Optional<T>>.switchMapCompletableOptional(mapper: (T) -> Completable):
        Completable = switchMapCompletable { if (it is Optional.Some) mapper(it.value) else Completable.complete() }

private inline fun <reified R : Any> Observable<*>.ofType(): Observable<R> = ofType(R::class.java)
private inline fun <reified R : Any> Flowable<*>.ofType(): Flowable<R> = ofType(R::class.java)
