package com.nextfaze.daggie.rxjava2

import androidx.annotation.IntRange
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

/**
 * An operator suitable for use with [Observable.retryWhen], that uses [strategy] to determine when to retry.
 * @param strategy The strategy that dictates how often, or how many, attempts are made.
 * @param scheduler The scheduler used to schedule delayed retry attempts.
 * @param predicate An optional predicate function that is executed to determine if an error should be retried at all.
 */
fun <T : Throwable> Observable<T>.backoff(
    strategy: BackoffStrategy,
    scheduler: Scheduler = Schedulers.computation(),
    predicate: (Throwable) -> Boolean = { true }
): Observable<Any> = toFlowable(BackpressureStrategy.BUFFER).backoff(strategy, scheduler, predicate).toObservable()

/**
 * An operator suitable for use with [Flowable.retryWhen], that uses [strategy] to determine when to retry.
 * @param strategy The strategy that dictates how often, or how many, attempts are made.
 * @param scheduler The scheduler used to schedule delayed retry attempts.
 * @param predicate An optional predicate function that is executed to determine if an error should be retried at all.
 */
fun <T : Throwable> Flowable<T>.backoff(
    strategy: BackoffStrategy,
    scheduler: Scheduler = Schedulers.computation(),
    predicate: (Throwable) -> Boolean = { true }
): Flowable<Any> =
    zipWith(
        Flowable.range(0, strategy.maxAttempts.coerceAtLeast(1)),
        BiFunction<Throwable, Int, Pair<Throwable, Int>> { e, attempt -> e to attempt }
    ).flatMap { (e, attemptNumber) ->
        if (predicate(e)) strategy.retry(attemptNumber, scheduler).andThen(Flowable.just(Unit)) else Flowable.error(e)
    }

/** Specifies a strategy for use with [backoff], which determines how often or many retry attempts are made. */
interface BackoffStrategy {

    /** The maximum number of retries this strategy will make. Must be `>= 1`. */
    val maxAttempts: Int

    /**
     * Returns a [Completable] that completes when a retry attempt should be made.
     * @param attempt The attempt number in the range `[0, maxAttempts)`
     * @param scheduler The scheduler which this function can use to schedule delays.
     */
    fun retry(attempt: Int, scheduler: Scheduler): Completable

    /** Implements a backoff strategy that increases the delay between attempts exponentially, up to a maximum. */
    data class Exponential(
        @IntRange(from = 1) override val maxAttempts: Int = Int.MAX_VALUE,
        val retryDelay: Long = 3,
        val retryDelayUnit: TimeUnit = SECONDS,
        val maxDelay: Long = Long.MAX_VALUE,
        val maxDelayUnit: TimeUnit = SECONDS
    ) : BackoffStrategy {

        init {
            require(maxAttempts >= 1)
        }

        private val maxDelayMillis = maxDelayUnit.toMillis(maxDelay)
        private val retryDelayMillis = retryDelayUnit.toMillis(retryDelay)

        override fun retry(attempt: Int, scheduler: Scheduler): Completable {
            // Cap to prevent long overflow
            val factor = Math.pow(2.0, attempt.coerceAtMost(32).toDouble())
            val exponentialDelayMillis = factor.toLong() * retryDelayMillis
            return Completable.timer(maxDelayMillis.coerceAtMost(exponentialDelayMillis), MILLISECONDS, scheduler)
        }
    }
}
