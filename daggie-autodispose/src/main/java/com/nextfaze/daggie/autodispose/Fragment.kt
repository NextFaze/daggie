package com.nextfaze.daggie.autodispose

import com.nextfaze.daggie.autodispose.FragmentEvent.*
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import com.uber.autodispose.lifecycle.LifecycleScopes
import io.reactivex.CompletableSource
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

enum class FragmentEvent {
    ON_ATTACH, ON_CREATE, ON_CREATE_VIEW, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTROY_VIEW, ON_DESTROY, ON_DETACH
}

internal class FragmentLifecycleProvider(
    private val lifecycleEvents: BehaviorSubject<FragmentEvent>,
    private val untilEvent: FragmentEvent? = null
) : LifecycleScopeProvider<FragmentEvent> {

    override fun lifecycle(): Observable<FragmentEvent> = lifecycleEvents.hide()

    override fun correspondingEvents() =
        untilEvent?.let { event -> CorrespondingEventsFunction<FragmentEvent> { event } } ?: DEFAULT_CORRESPONDING_EVENTS

    override fun peekLifecycle() = lifecycleEvents.value

    override fun requestScope(): CompletableSource = LifecycleScopes.resolveScopeFromLifecycle(this)
}

private val DEFAULT_CORRESPONDING_EVENTS = CorrespondingEventsFunction<FragmentEvent> { event ->
    when (event) {
        ON_ATTACH -> ON_DETACH
        ON_CREATE -> ON_DESTROY
        ON_CREATE_VIEW -> ON_DESTROY_VIEW
        ON_START -> ON_STOP
        ON_RESUME -> ON_PAUSE
        ON_PAUSE -> ON_STOP
        ON_STOP -> ON_DESTROY_VIEW
        ON_DESTROY_VIEW -> ON_DESTROY
        ON_DESTROY -> ON_DETACH
        else -> throw LifecycleEndedException("Cannot bind to Fragment lifecycle after detach.")
    }
}
