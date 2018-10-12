package com.nextfaze.daggie.autodispose

import com.nextfaze.daggie.autodispose.ActivityEvent.*
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import com.uber.autodispose.lifecycle.LifecycleScopes
import io.reactivex.CompletableSource
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

enum class ActivityEvent {
    ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY
}

internal class ActivityLifecycleProvider(
    private val lifecycleEvents: BehaviorSubject<ActivityEvent>,
    private val untilEvent: ActivityEvent? = null
) : LifecycleScopeProvider<ActivityEvent> {
    override fun lifecycle(): Observable<ActivityEvent> = lifecycleEvents.hide()

    override fun correspondingEvents() =
        untilEvent?.let { event -> CorrespondingEventsFunction<ActivityEvent> { event } } ?: DEFAULT_CORRESPONDING_EVENTS

    override fun peekLifecycle() = lifecycleEvents.value

    override fun requestScope(): CompletableSource = LifecycleScopes.resolveScopeFromLifecycle(this)
}

private val DEFAULT_CORRESPONDING_EVENTS = CorrespondingEventsFunction<ActivityEvent> { activityEvent ->
    when (activityEvent) {
        ON_CREATE -> ON_DESTROY
        ON_START -> ON_STOP
        ON_RESUME -> ON_PAUSE
        ON_PAUSE -> ON_STOP
        ON_STOP -> ON_DESTROY
        else -> throw LifecycleEndedException("Cannot bind to Activity lifecycle after destroy.")
    }
}
