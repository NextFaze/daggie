package com.nextfaze.daggie.rxjava

import android.app.Application
import com.nextfaze.daggie.Early
import com.nextfaze.daggie.Initializer
import com.nextfaze.daggie.slf4j.d
import com.nextfaze.daggie.slf4j.e
import com.nextfaze.daggie.slf4j.logger
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import rx.Completable
import rx.CompletableSubscriber
import rx.Observable
import rx.Scheduler
import rx.Single
import rx.SingleSubscriber
import rx.Subscriber
import rx.Subscription
import rx.android.plugins.RxAndroidPlugins
import rx.android.plugins.RxAndroidSchedulersHook
import rx.functions.Action0
import rx.functions.Action1
import rx.plugins.RxJavaHooks
import java.util.*
import java.util.Collections.newSetFromMap
import javax.inject.Singleton

/**
 * Registers RxJava exception-catching [hooks][RxJavaHooks], ensuring they're all caught and logged.
 * @see RxJavaHooks
 */
@Module class RxJavaErrorHooksModule {
    @Provides @IntoSet @Singleton @Early
    fun initializer(): Initializer<Application> = {
        RxJavaHooks.clear()
        RxJavaHooks.setOnError(RxErrorHook())
        RxJavaHooks.setOnObservableCreate(::SafeObservableOnSubscribe)
        RxJavaHooks.setOnSingleCreate(::SafeSingleOnSubscribe)
        RxJavaHooks.setOnCompletableCreate(::SafeCompletableOnSubscribe)
        RxJavaHooks.setOnScheduleAction(::SafeAction0)
        RxAndroidPlugins.getInstance().registerSchedulersHook(RxSchedulersHookAndroid())
        RxJavaHooks.lockdown()
    }
}

private val log = logger("RxJavaErrorHooks")

private class RxErrorHook : Action1<Throwable> {
    /**
     * RxJava has very weird semantics and inconsistencies for error handling.
     * Unfortunately there are circumstances where an error handler will be called with the same error multiple times,
     * hence the weak set (using map maker which does identity comparison).
     * *Frequently occurs when using a [Single] with a [Scheduler].*
     */
    private val logged = newSetFromMap(WeakHashMap<Throwable, Boolean>())!!

    override fun call(t: Throwable) {
        if (logged.add(t)) {
            log.e(t) { "RxJava Error" }
        }
    }
}


private class SafeObservableOnSubscribe(private val onSubscribe: Observable.OnSubscribe<*>) : Observable.OnSubscribe<Any> {
    override fun call(subscriber: Subscriber<in Any>) {
        try {
            onSubscribe.call(subscriber)
        } catch (t: Throwable) {
            // not .warn/.error as RxErrorHook would have already logged it
            log.d(t) { "SafeOnSubscribe call error caught: ${t.message}" }
        }
    }
}


private class SafeSingleOnSubscribe(private val onSubscribe: Single.OnSubscribe<*>) : Single.OnSubscribe<Any> {
    override fun call(singleSubscriber: SingleSubscriber<in Any>) {
        try {
            // we need to wrap it again as the exceptions thrown/caught flow can change depending on the scheduler used
            onSubscribe.call(SafeSingleSubscriber(singleSubscriber))
        } catch (t: Throwable) {
            log.d(t) { "SafeSingleOnSubscribe call error caught: ${t.message}" }
        }
    }

    private class SafeSingleSubscriber<T>(private val singleSubscriber: SingleSubscriber<in T>) : SingleSubscriber<T>() {
        override fun onSuccess(value: T?) {
            try {
                singleSubscriber.onSuccess(value)
            } catch (t: Throwable) {
                log.d(t) { "SafeSingleSubscriber onSuccess error caught: ${t.message}" }
            }
        }

        override fun onError(error: Throwable) {
            try {
                singleSubscriber.onError(error)
            } catch (t: Throwable) {
                log.d(t) { "SafeSingleSubscriber onError error caught: ${t.message}" }
            }
        }
    }
}

private class SafeCompletableOnSubscribe(private val onSubscribe: Completable.OnSubscribe) : Completable.OnSubscribe {
    override fun call(t: CompletableSubscriber) {
        try {
            onSubscribe.call(SafeCompletableSubscriber(t))
        } catch (t: Throwable) {
            log.d { "SafeCompletableOnSubscribe call error caught: ${t.message}" }
        }
    }

    private class SafeCompletableSubscriber(private val completableSubscriber: CompletableSubscriber) : CompletableSubscriber {
        override fun onSubscribe(s: Subscription) {
            try {
                completableSubscriber.onSubscribe(s)
            } catch (t: Throwable) {
                log.d { "SafeCompletableSubscriber onSubscribe error caught: ${t.message}" }
            }
        }

        override fun onError(t: Throwable) {
            try {
                completableSubscriber.onError(t)
            } catch (t: Throwable) {
                log.d { "SafeCompletableSubscriber onError error caught: ${t.message}" }
            }
        }

        override fun onCompleted() {
            try {
                completableSubscriber.onCompleted()
            } catch (t: Throwable) {
                log.d { "SafeCompletableSubscriber onCompleted error caught: ${t.message}" }
            }
        }
    }
}

private class SafeAction0(private val action0: Action0) : Action0 {
    override fun call() {
        try {
            action0.call()
        } catch (t: Throwable) {
            log.d(t) { "SafeAction0 call error caught: ${t.message}" }
        }
    }
}

private class RxSchedulersHookAndroid : RxAndroidSchedulersHook() {
    override fun onSchedule(action: Action0): Action0 = SafeAction0(action)
}