package com.nextfaze.daggie.foreground

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.nextfaze.daggie.Foreground
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.WeakHashMap
import javax.inject.Singleton

/**
 * Provides [Foreground] `Observable<Boolean>` and `Flowable<Boolean>` bindings indicating when the app is foregrounded.
 *
 * This implementation emits the values immediately when the app leaves the foreground, unlike early implementations
 * that did so only after a delay.
 */
@Module
class ForegroundModule {
    @Provides @Singleton
    internal fun tracker() = Tracker()

    @Provides @IntoSet
    internal fun activityLifecycleCallbacks(tracker: Tracker): ActivityLifecycleCallbacks = tracker

    @Provides @Singleton @Foreground
    internal fun observable(tracker: Tracker): Observable<Boolean> = tracker.foreground

    @Provides @Singleton @Foreground
    internal fun flowable(@Foreground foreground: Observable<Boolean>): Flowable<Boolean> =
        foreground.toFlowable(BackpressureStrategy.LATEST)

    internal class Tracker : ActivityLifecycleCallbacks {

        private val startsSubject = BehaviorSubject.createDefault(0)

        /** The count of started activities, excluding those that are in the process of changing config. */
        private var startCount: Int
            get() = startsSubject.value!!
            set(value) = startsSubject.onNext(value)

        val foreground: Observable<Boolean> = startsSubject
            .map { it > 0 }
            .distinctUntilChanged()
            .replay(1)
            .refCount()

        /** Stores whether an activity was recreated after a config change. */
        private val activityToDidChangeConfiguration = WeakHashMap<Activity, Boolean>()

        private var Activity.didChangeConfiguration
            get() = activityToDidChangeConfiguration[this]!!
            set(value) {
                activityToDidChangeConfiguration[this] = value
            }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // Weakly store the fact that this activity is being recreated after a config change.
            // We'll need to know later, and the SDK provides no way to tell after this point
            activity.didChangeConfiguration = savedInstanceState?.getBoolean(KEY_CHANGING_CONFIGURATIONS, false) == true
        }

        override fun onActivityDestroyed(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
            // Save the fact that we changed configuration, as the SDK can only tell us that during destruction
            outState?.putBoolean(KEY_CHANGING_CONFIGURATIONS, activity.isChangingConfigurations)
        }

        override fun onActivityStarted(activity: Activity) {
            if (!activity.didChangeConfiguration) startCount++
            // Clear this flag here, otherwise when the activity is backgrounded then foregrounded, it won't count
            // towards started because the flag was still on.
            activity.didChangeConfiguration = false
        }

        override fun onActivityStopped(activity: Activity) {
            if (!activity.isChangingConfigurations) startCount--
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }
    }
}

private const val KEY_CHANGING_CONFIGURATIONS = "com.nextfaze.daggie.foreground.changingConfigurations"
