package com.nextfaze.daggie.manup

import android.app.Activity
import rx.Emitter
import rx.Observable
import rx.Observable.create
import rx.Observable.error
import rx.Observable.range
import rx.Observable.timer
import rx.Scheduler
import rx.schedulers.Schedulers
import java.lang.Math.pow
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

/** Emits pause events from this [Activity]. */
internal fun Activity.pauses() = create<Unit>({ emitter ->
    val callbacks: SimpleActivityLifecycleCallbacks = object : SimpleActivityLifecycleCallbacks() {
        override fun onActivityPaused(activity: Activity) {
            if (activity == this@pauses) {
                emitter.onNext(Unit)
            }
        }
    }
    application.registerActivityLifecycleCallbacks(callbacks)
    emitter.setCancellation { application.unregisterActivityLifecycleCallbacks(callbacks) }
}, Emitter.BackpressureMode.BUFFER)

/** Mirror the source observable while the specified observable's latest emitted value is true. */
internal fun <T> Observable<T>.takeWhile(b: Observable<Boolean>): Observable<T> = b
        .distinctUntilChanged()
        .filter { it }
        .switchMap { takeUntil(b.distinctUntilChanged().filter { !it }) }

private const val RETRY_DELAY_MILLIS = 3000L

internal fun <T : Throwable> Observable<T>.exponentialBackoff(
        maxAttempts: Int = Integer.MAX_VALUE,
        maxDelay: Long = Long.MAX_VALUE,
        maxDelayUnit: TimeUnit = SECONDS,
        scheduler: Scheduler = Schedulers.computation(),
        passthroughFilter: (Throwable) -> Boolean = { false }
): Observable<Long> = zipWith(range(0, maxAttempts)) { e, attempt -> e to attempt }
        .flatMap { (e, attemptNumber) ->
            when {
                passthroughFilter.invoke(e) -> error(e)
                else -> {
                    // Cap to prevent long overflow
                    val factor = pow(2.0, attemptNumber.coerceAtMost(32).toDouble())
                    val exponentialDelayMillis = factor.toLong() * RETRY_DELAY_MILLIS
                    val maxDelayMillis = maxDelayUnit.toMillis(maxDelay)
                    val delayMillis = maxDelayMillis.coerceAtMost(exponentialDelayMillis)
                    timer(delayMillis, MILLISECONDS, scheduler)
                }
            }
        }