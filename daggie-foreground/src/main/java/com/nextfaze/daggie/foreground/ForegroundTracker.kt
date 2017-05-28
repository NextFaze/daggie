package com.nextfaze.daggie.foreground

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates.observable

private const val DELAY = 1000L

internal class ForegroundTracker {

    internal val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityStarted(activity: Activity) {
            if (activityCount.incrementAndGet() == 1) {
                handler.removeCallbacks(exitRunnable)
                isForeground = true
            }
        }

        override fun onActivityStopped(activity: Activity) {
            if (activityCount.decrementAndGet() == 0) {
                exitDelayed()
            }
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
        }
    }

    private val relay: PublishSubject<Boolean> = PublishSubject.create<Boolean>()

    private val observable: Observable<Boolean> =
            Observable.defer { Observable.just(isForeground) }.concatWith(relay).distinctUntilChanged()

    private val activityCount = AtomicInteger()

    private val handler = Handler(getMainLooper())

    private val exitRunnable = Runnable { isForeground = false }

    var isForeground: Boolean by observable(false) { _, _, new -> relay.onNext(new) }
        private set

    /** Emits boolean values indicating whether the app is in the foreground or not. */
    fun foreground(): Observable<Boolean> = observable

    private fun exitDelayed() {
        handler.removeCallbacks(exitRunnable)
        handler.postDelayed(exitRunnable, DELAY)
    }
}