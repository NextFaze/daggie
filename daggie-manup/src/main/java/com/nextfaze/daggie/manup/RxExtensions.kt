package com.nextfaze.daggie.manup

import android.app.Activity
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.lang.Math.pow
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

/** Emits pause events from this [Activity]. */
internal fun Activity.pauses() = Observable.create<Unit> { emitter ->
    val callbacks: SimpleActivityLifecycleCallbacks = object : SimpleActivityLifecycleCallbacks() {
        override fun onActivityPaused(activity: Activity) {
            if (activity == this@pauses) emitter.onNext(Unit)
        }
    }
    application.registerActivityLifecycleCallbacks(callbacks)
    emitter.setCancellable { application.unregisterActivityLifecycleCallbacks(callbacks) }
}

/** Mirror the source `Flowable` while the specified `Flowable`'s latest emitted value is true. */
internal fun <T> Flowable<T>.takeWhile(o: Flowable<Boolean>): Flowable<T> {
    val shared = o.distinctUntilChanged().share()
    return shared.filter { it }.switchMap { takeUntil(shared.filter { !it }) }
}

private const val RETRY_DELAY_MILLIS = 3000L

internal fun <T : Throwable> Flowable<T>.exponentialBackoff(
        maxAttempts: Int = Integer.MAX_VALUE,
        maxDelay: Long = Long.MAX_VALUE,
        maxDelayUnit: TimeUnit = SECONDS,
        scheduler: Scheduler = Schedulers.computation(),
        passthroughFilter: (Throwable) -> Boolean = { false }
): Flowable<Long> = zipWith(Flowable.range(0, maxAttempts), BiFunction { e: Throwable, attempt: Int -> e to attempt })
        .flatMap { (e, attemptNumber) ->
            when {
                passthroughFilter.invoke(e) -> error(e)
                else -> {
                    // Cap to prevent long overflow
                    val factor = pow(2.0, attemptNumber.coerceAtMost(32).toDouble())
                    val exponentialDelayMillis = factor.toLong() * RETRY_DELAY_MILLIS
                    val maxDelayMillis = maxDelayUnit.toMillis(maxDelay)
                    val delayMillis = maxDelayMillis.coerceAtMost(exponentialDelayMillis)
                    Flowable.timer(delayMillis, MILLISECONDS, scheduler)
                }
            }
        }
