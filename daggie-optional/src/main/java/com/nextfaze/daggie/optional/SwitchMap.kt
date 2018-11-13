package com.nextfaze.daggie.optional

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

fun <T : Any, R : Any> Observable<Optional<T>>.switchMapOptional(mapper: (T) -> Observable<Optional<R>>):
    Observable<Optional<R>> = switchMap { if (it is Some) mapper(it.value) else Observable.just(None) }

fun <T : Any, R : Any> Observable<Optional<T>>.switchMapSingleOptional(mapper: (T) -> Single<Optional<R>>):
    Observable<Optional<R>> = switchMapSingle { if (it is Some) mapper(it.value) else Single.just(None) }

fun <T : Any, R : Any> Observable<Optional<T>>.switchMapMaybeOptional(mapper: (T) -> Maybe<Optional<R>>):
    Observable<Optional<R>> = switchMapMaybe { if (it is Some) mapper(it.value) else Maybe.just(None) }

fun <T : Any> Observable<Optional<T>>.switchMapCompletableOptional(mapper: (T) -> Completable):
    Completable = switchMapCompletable { if (it is Some) mapper(it.value) else Completable.complete() }

fun <T : Any, R : Any> Flowable<Optional<T>>.switchMapOptional(mapper: (T) -> Flowable<Optional<R>>):
    Flowable<Optional<R>> = switchMap { if (it is Some) mapper(it.value) else Flowable.just(None) }

fun <T : Any, R : Any> Flowable<Optional<T>>.switchMapSingleOptional(mapper: (T) -> Single<Optional<R>>):
    Flowable<Optional<R>> = switchMapSingle { if (it is Some) mapper(it.value) else Single.just(None) }

fun <T : Any, R : Any> Flowable<Optional<T>>.switchMapMaybeOptional(mapper: (T) -> Maybe<Optional<R>>):
    Flowable<Optional<R>> = switchMapMaybe { if (it is Some) mapper(it.value) else Maybe.just(None) }

fun <T : Any> Flowable<Optional<T>>.switchMapCompletableOptional(mapper: (T) -> Completable):
    Completable = switchMapCompletable { if (it is Some) mapper(it.value) else Completable.complete() }
