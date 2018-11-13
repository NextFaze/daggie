package com.nextfaze.daggie.foreground.testing

import androidx.annotation.CheckResult
import com.nextfaze.daggie.Foreground
import dagger.Module
import dagger.Provides
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Module
class FakeForegroundModule {
    @Provides @Singleton @Foreground
    internal fun observable(fakeForegroundManager: FakeForegroundManager): Observable<Boolean> =
        fakeForegroundManager.foreground()

    @Provides @Singleton @Foreground
    internal fun flowable(@Foreground foreground: Observable<Boolean>): Flowable<Boolean> =
        foreground.toFlowable(BackpressureStrategy.LATEST)
}

@Singleton
class FakeForegroundManager @Inject constructor() {

    private val foregroundSubject = BehaviorSubject.createDefault(true)

    var isForeground: Boolean
        get() = foregroundSubject.value!!
        set(value) = foregroundSubject.onNext(value)

    @CheckResult fun foreground(): Observable<Boolean> = foregroundSubject
}

