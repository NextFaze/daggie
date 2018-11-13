@file:Suppress("NOTHING_TO_INLINE")

package com.nextfaze.daggie.optional

sealed class Optional<out T : Any> {

    abstract val isPresent: Boolean

    inline fun <R : Any> map(transform: (T) -> R?): Optional<R> =
        if (this is Some) transform(value).toOptional() else None

    inline fun <R : Any> flatMap(transform: (T) -> Optional<R>): Optional<R> =
        if (this is Some) transform(value) else None
}

object None : Optional<Nothing>() {
    override val isPresent: Boolean = false
    override fun toString() = "None"
}

data class Some<out T : Any>(val value: T) : Optional<T>() {
    override val isPresent: Boolean = true
    override fun toString() = "Some($value)"
}

fun <T : Any> T?.toOptional(): Optional<T> = this?.let { Some(it) } ?: None

val <T : Any> Optional<T>.value: T? get() = (this as? Some)?.value

inline val <T : Any> Optional<T>.isAbsent: Boolean get() = !isPresent
