package com.nextfaze.daggie.foreground

import com.nextfaze.daggie.Foreground
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import rx.Observable
import javax.inject.Singleton

/** Provides bindings for detecting whether the app is currently in the foreground or not. */
@Module class ForegroundModule {
    @Provides @Singleton
    internal fun foregroundTracker() = ForegroundTracker()

    @Provides @Singleton @Foreground
    internal fun observable(foregroundTracker: ForegroundTracker): Observable<Boolean> =
            foregroundTracker.foreground()

    @Provides @IntoSet
    internal fun activityLifecycleCallbacks(foregroundTracker: ForegroundTracker) =
            foregroundTracker.activityLifecycleCallbacks
}


