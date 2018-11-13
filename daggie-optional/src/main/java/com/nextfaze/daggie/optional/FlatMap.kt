package com.nextfaze.daggie.optional

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

fun <T : Any, R : Any> Observable<Optional<T>>.flatMapOptional(mapper: (T) -> R?): Observable<Optional<R>> =
    map { if (it is Some) mapper(it.value).toOptional() else None }

fun <T : Any, R : Any> Observable<Optional<T>>.flatMapSingleOptional(mapper: (T) -> Single<Optional<R>>):
    Observable<Optional<R>> = flatMapSingle { if (it is Some) mapper(it.value) else Single.just(None) }

fun <T : Any, R : Any> Observable<Optional<T>>.flatMapMaybeOptional(mapper: (T) -> Maybe<Optional<R>>):
    Observable<Optional<R>> = flatMapMaybe { if (it is Some) mapper(it.value) else Maybe.just(None) }

fun <T : Any> Observable<Optional<T>>.flatMapCompletableOptional(mapper: (T) -> Completable):
    Completable = flatMapCompletable { if (it is Some) mapper(it.value) else Completable.complete() }

fun <T : Any, R : Any> Flowable<Optional<T>>.flatMapOptional(mapper: (T) -> R?): Flowable<Optional<R>> =
    map { if (it is Some) mapper(it.value).toOptional() else None }

fun <T : Any, R : Any> Flowable<Optional<T>>.flatMapSingleOptional(mapper: (T) -> Single<Optional<R>>):
    Flowable<Optional<R>> = flatMapSingle { if (it is Some) mapper(it.value) else Single.just(None) }

fun <T : Any, R : Any> Flowable<Optional<T>>.flatMapMaybeOptional(mapper: (T) -> Maybe<Optional<R>>):
    Flowable<Optional<R>> = flatMapMaybe { if (it is Some) mapper(it.value) else Maybe.just(None) }

fun <T : Any> Flowable<Optional<T>>.flatMapCompletableOptional(mapper: (T) -> Completable):
    Completable = flatMapCompletable { if (it is Some) mapper(it.value) else Completable.complete() }

fun <T : Any, R : Any> Single<Optional<T>>.flatMapOptional(mapper: (T) -> R?): Single<Optional<R>> =
    map { if (it is Some) mapper(it.value).toOptional() else None }

fun <T : Any, R : Any> Single<Optional<T>>.flatMapObservableOptional(mapper: (T) -> Observable<Optional<R>>):
    Observable<Optional<R>> = flatMapObservable { if (it is Some) mapper(it.value) else Observable.just(None) }

fun <T : Any, R : Any> Single<Optional<T>>.flatMapMaybeOptional(mapper: (T) -> Maybe<Optional<R>>):
    Maybe<Optional<R>> = flatMapMaybe { if (it is Some) mapper(it.value) else Maybe.just(None) }

fun <T : Any> Single<Optional<T>>.flatMapCompletableOptional(mapper: (T) -> Completable):
    Completable = flatMapCompletable { if (it is Some) mapper(it.value) else Completable.complete() }

fun <T : Any, R : Any> Maybe<Optional<T>>.flatMapOptional(mapper: (T) -> R?): Maybe<Optional<R>> =
    map { if (it is Some) mapper(it.value).toOptional() else None }

fun <T : Any, R : Any> Maybe<Optional<T>>.flatMapObservableOptional(mapper: (T) -> Observable<Optional<R>>):
    Observable<Optional<R>> = flatMapObservable { if (it is Some) mapper(it.value) else Observable.just(None) }

fun <T : Any, R : Any> Maybe<Optional<T>>.flatMapSingleOptional(mapper: (T) -> Single<Optional<R>>):
    Single<Optional<R>> = flatMapSingle { if (it is Some) mapper(it.value) else Single.just(None) }

fun <T : Any> Maybe<Optional<T>>.flatMapCompletableOptional(mapper: (T) -> Completable):
    Completable = flatMapCompletable { if (it is Some) mapper(it.value) else Completable.complete() }

@Deprecated("Use flatMapOptional()", ReplaceWith("flatMapOptional(mapper)"))
fun <T : Any, R : Any> Observable<Optional<T>>.mapOptional(mapper: (T) -> R?): Observable<Optional<R>> =
    map { if (it is Some) mapper(it.value).toOptional() else None }
