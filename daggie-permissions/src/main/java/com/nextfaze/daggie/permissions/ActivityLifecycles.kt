package com.nextfaze.daggie.permissions

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import io.reactivex.Observable
import java.util.*

/** Emits all `Activity` [resume events][ActivityLifecycleCallbacks.onActivityResumed]. */
internal fun Application.activityResumes() = Observable.create<Activity> { emitter ->
    val callbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            emitter.onNext(activity)
        }

        override fun onActivityPaused(activity: Activity) = emitter.onNext(activity)

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
        }
    }
    registerActivityLifecycleCallbacks(callbacks)
    emitter.setCancellable { unregisterActivityLifecycleCallbacks(callbacks) }
}

/** Emits the most recently resumed [Activity], or [Optional.None] if none are resumed. */
internal fun Application.topResumedActivity(): Observable<Optional<Activity>> = Observable.create { emitter ->
    val activityStack = LinkedList<Activity>()
    val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            activityStack.push(activity)
            emitter.onNext(activity.toOptional())
        }

        override fun onActivityPaused(activity: Activity) {
            activityStack.remove(activity)
            emitter.onNext(activityStack.peek().toOptional())
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }
    }
    emitter.onNext(Optional.None)
    registerActivityLifecycleCallbacks(callbacks)
    emitter.setCancellable { unregisterActivityLifecycleCallbacks(callbacks) }
}
