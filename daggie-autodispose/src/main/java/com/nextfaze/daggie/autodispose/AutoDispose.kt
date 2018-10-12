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
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import com.uber.autodispose.lifecycle.LifecycleScopes
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.reactivex.CompletableSource
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.lang.IllegalStateException

@Module
class AutoDisposeModule {
    @Provides @IntoSet internal fun initializer(): Initializer<Application> = { LifecycleManager.registerCallbacks(it) }
}

enum class ActivityEvent {
    ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY
}

enum class FragmentEvent {
    ON_ATTACH, ON_CREATE, ON_CREATE_VIEW, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY_VIEW, ON_DESTROY, ON_DETACH
}

fun Fragment.scope(untilEvent: FragmentEvent? = null): ScopeProvider =
    LifecycleProvider(LifecycleManager.lifecycle(this), untilEvent, DEFAULT_FRAGMENT_CORRESPONDING_EVENTS)

fun Activity.scope(untilEvent: ActivityEvent? = null): ScopeProvider =
    LifecycleProvider(LifecycleManager.lifecycle(this), untilEvent, DEFAULT_ACTIVITY_CORRESPONDING_EVENTS)

private val DEFAULT_ACTIVITY_CORRESPONDING_EVENTS = CorrespondingEventsFunction<ActivityEvent> { activityEvent ->
    when (activityEvent) {
        ActivityEvent.ON_CREATE -> ActivityEvent.ON_DESTROY
        ActivityEvent.ON_START -> ActivityEvent.ON_STOP
        ActivityEvent.ON_RESUME -> ActivityEvent.ON_PAUSE
        ActivityEvent.ON_PAUSE -> ActivityEvent.ON_STOP
        ActivityEvent.ON_STOP -> ActivityEvent.ON_DESTROY
        else -> throw LifecycleEndedException("Cannot bind to Activity lifecycle after destroy.")
    }
}

private val DEFAULT_FRAGMENT_CORRESPONDING_EVENTS = CorrespondingEventsFunction<FragmentEvent> { event ->
    when (event) {
        FragmentEvent.ON_ATTACH -> FragmentEvent.ON_DETACH
        FragmentEvent.ON_CREATE -> FragmentEvent.ON_DESTROY
        FragmentEvent.ON_CREATE_VIEW -> FragmentEvent.ON_DESTROY_VIEW
        FragmentEvent.ON_START -> FragmentEvent.ON_STOP
        FragmentEvent.ON_RESUME -> FragmentEvent.ON_PAUSE
        FragmentEvent.ON_PAUSE -> FragmentEvent.ON_STOP
        FragmentEvent.ON_STOP -> FragmentEvent.ON_DESTROY_VIEW
        FragmentEvent.ON_DESTROY_VIEW -> FragmentEvent.ON_DESTROY
        FragmentEvent.ON_DESTROY -> FragmentEvent.ON_DETACH
        else -> throw LifecycleEndedException("Cannot bind to Fragment lifecycle after detach.")
    }
}

private class LifecycleProvider<T> (
    private val lifecycleEvents: BehaviorSubject<T>,
    private val untilEvent: T? = null,
    private val defaultCorrespondingEvents: CorrespondingEventsFunction<T>
) : LifecycleScopeProvider<T> {
    override fun lifecycle(): Observable<T> = lifecycleEvents.hide()

    override fun correspondingEvents() =
        untilEvent?.let { event -> CorrespondingEventsFunction<T> { event } } ?: defaultCorrespondingEvents

    override fun peekLifecycle() = lifecycleEvents.value

    override fun requestScope(): CompletableSource = LifecycleScopes.resolveScopeFromLifecycle(this)
}

private object LifecycleManager {

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

    internal fun registerCallbacks(application: Application) = application.registerActivityLifecycleCallbacks(activityCallbacks)

    internal fun lifecycle(fragment: Fragment) =
        fragments[fragment] ?: throw IllegalStateException("Attempting to bind to lifecycle for $fragment when no lifecycle is available")

    internal fun lifecycle(activity: Activity) =
        activities[activity] ?: throw IllegalStateException("Attempting to bind to lifecycle for $activity when no lifecycle is available")
}
