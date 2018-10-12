package com.nextfaze.daggie.autodispose

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.nextfaze.daggie.Initializer
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.subjects.BehaviorSubject
import java.lang.IllegalStateException

private val lifecycleManager = AutoDisposeLifecycleManager()

@Module
class AutoDisposeModule {
    @Provides @IntoSet internal fun lifecycleInitializer(): Initializer<Application> = { lifecycleManager.registerCallbacks(it) }
}

fun Fragment.scope(untilEvent: FragmentEvent? = null): LifecycleScopeProvider<FragmentEvent> =
    FragmentLifecycleProvider(lifecycleManager.lifecycle(this), untilEvent)

fun Activity.scope(untilEvent: ActivityEvent? = null): LifecycleScopeProvider<ActivityEvent> =
    ActivityLifecycleProvider(lifecycleManager.lifecycle(this), untilEvent)

private class AutoDisposeLifecycleManager {

    private val fragments = mutableMapOf<Fragment, BehaviorSubject<FragmentEvent>>()
    private val activities = mutableMapOf<Activity, BehaviorSubject<ActivityEvent>>()

    private val fragmentCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context?) {
            fragments[f] = BehaviorSubject.createDefault(FragmentEvent.ON_ATTACH)
        }

        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) { fragments[f]?.onNext(FragmentEvent.ON_CREATE) }
        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View?, savedInstanceState: Bundle?) { fragments[f]?.onNext(FragmentEvent.ON_CREATE_VIEW) }
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) { fragments[f]?.onNext(FragmentEvent.ON_START) }
        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) { fragments[f]?.onNext(FragmentEvent.ON_RESUME) }
        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) { fragments[f]?.onNext(FragmentEvent.ON_PAUSE) }
        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) { fragments[f]?.onNext(FragmentEvent.ON_STOP) }
        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) { fragments[f]?.onNext(FragmentEvent.ON_DESTROY_VIEW) }
        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) { fragments[f]?.onNext(FragmentEvent.ON_DESTROY) }
        override fun onFragmentDetached(fm: FragmentManager, f: Fragment) { fragments.remove(f)?.onNext(FragmentEvent.ON_DETACH) }
    }

    private val activityCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(a: Activity, savedInstanceState: Bundle?) {
            activities[a] = BehaviorSubject.createDefault(ActivityEvent.ON_CREATE)
            (a as? AppCompatActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(fragmentCallbacks, true)
        }

        override fun onActivityStarted(a: Activity) { activities[a]?.onNext(ActivityEvent.ON_START) }
        override fun onActivityResumed(a: Activity) { activities[a]?.onNext(ActivityEvent.ON_RESUME) }
        override fun onActivityPaused(a: Activity) { activities[a]?.onNext(ActivityEvent.ON_PAUSE) }
        override fun onActivityStopped(a: Activity) { activities[a]?.onNext(ActivityEvent.ON_STOP) }
        override fun onActivityDestroyed(activity: Activity) { activities.remove(activity)?.onNext(ActivityEvent.ON_DESTROY) }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) { /** Ignored */ }
    }

    fun registerCallbacks(application: Application) = application.registerActivityLifecycleCallbacks(activityCallbacks)

    internal fun lifecycle(fragment: Fragment) =
        fragments[fragment] ?: throw IllegalStateException("Attempting to bind to lifecycle for $fragment when no lifecycle is available")

    internal fun lifecycle(activity: Activity) =
        activities[activity] ?: throw IllegalStateException("Attempting to bind to lifecycle for $activity when no lifecycle is available")
}
