package com.nextfaze.daggie.manup

import android.app.Activity
import rx.Emitter
import rx.Observable
import rx.Scheduler
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/** Emits pause events from this [Activity]. */
internal fun Activity.pauses() = Observable.create<Unit>({ emitter ->
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

private const val EXPONENTIAL_BACKOFF_FIRST_RETRY_DELAY = 3000L

internal fun <T : Throwable> Observable<T>.exponentialBackoff(
        maxAttempts: Int = Integer.MAX_VALUE,
        maxDelay: Long = Long.MAX_VALUE,
        maxDelayUnit: TimeUnit = TimeUnit.SECONDS,
        scheduler: Scheduler = Schedulers.computation(),
        passthroughFilter: (Throwable) -> Boolean = { false }
): Observable<Long> = zipWith(Observable.range(0, maxAttempts - 1)) { e, attempt -> e to attempt }
        .flatMap {
            if (passthroughFilter.invoke(it.first)) Observable.error(it.first)
            else Observable.timer(maxDelayUnit.toMillis(maxDelay)
                    .coerceAtMost(Math.pow(2.toDouble(), it.second.toDouble()).toLong()
                            * EXPONENTIAL_BACKOFF_FIRST_RETRY_DELAY), TimeUnit.MILLISECONDS, scheduler)
        }