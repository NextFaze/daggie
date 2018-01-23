package com.nextfaze.daggie.permissions

import com.nextfaze.daggie.permissions.Optional.Some
import io.reactivex.Observable

internal sealed class Optional<out T : Any> {
    abstract val isPresent: Boolean

    object None : Optional<Nothing>() {
        override val isPresent: Boolean = false
    }

    data class Some<out T : Any>(val value: T) : Optional<T>() {
        override val isPresent: Boolean = true
    }
}

internal fun <T : Any> T?.toOptional(): Optional<T> = this?.let(::Some) ?: Optional.None

internal fun <T : Any> Observable<Optional<T>>.filterPresent(): Observable<T> =
        ofType<Optional.Some<T>>().map { it.value }

private inline fun <reified R : Any> Observable<*>.ofType(): Observable<R> = ofType(R::class.java)
